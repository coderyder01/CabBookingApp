package com.example.rides.dispatch;

import com.example.rides.common.GeoUtils;
import com.example.rides.common.IdempotencyStore;
import com.example.rides.grpc.CreateTripGrpcRequest;
import com.example.rides.grpc.LocationGrpcServiceGrpc;
import com.example.rides.grpc.NearbyDriversRequest;
import com.example.rides.grpc.NotificationGrpcRequest;
import com.example.rides.grpc.NotificationGrpcServiceGrpc;
import com.example.rides.grpc.PaymentAuthorizationGrpcRequest;
import com.example.rides.grpc.PaymentAuthorizationGrpcResponse;
import com.example.rides.grpc.PaymentGrpcServiceGrpc;
import com.example.rides.grpc.StopGrpc;
import com.example.rides.grpc.SurgeDemandRequest;
import com.example.rides.grpc.SurgeGrpcServiceGrpc;
import com.example.rides.grpc.SurgeQuoteRequest;
import com.example.rides.grpc.SurgeQuoteResponse;
import com.example.rides.grpc.TripGrpcResponse;
import com.example.rides.grpc.TripGrpcServiceGrpc;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import net.devh.boot.grpc.client.inject.GrpcClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@RestController
@RequestMapping("/rides")
public class DispatchController {
    private final IdempotencyStore idempotencyStore;
    private final Map<String, DispatchDecision> decisions = new ConcurrentHashMap<>();
    private final Map<String, RideRequest> originalRequests = new ConcurrentHashMap<>();

    @GrpcClient("location-service")
    private LocationGrpcServiceGrpc.LocationGrpcServiceBlockingStub locationClient;
    @GrpcClient("surge-service")
    private SurgeGrpcServiceGrpc.SurgeGrpcServiceBlockingStub surgeClient;
    @GrpcClient("trip-service")
    private TripGrpcServiceGrpc.TripGrpcServiceBlockingStub tripClient;
    @GrpcClient("payment-service")
    private PaymentGrpcServiceGrpc.PaymentGrpcServiceBlockingStub paymentClient;
    @GrpcClient("notification-service")
    private NotificationGrpcServiceGrpc.NotificationGrpcServiceBlockingStub notificationClient;

    DispatchController(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    @PostMapping
    DispatchDecision requestRide(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                 @Valid @RequestBody RideRequest request) {
        String key = idempotencyKey == null ? request.riderId() + ":" + request.clientRequestId() : idempotencyKey;
        return idempotencyStore.getOrCreate(key, () -> dispatch(request, Set.of()));
    }

    @PostMapping("/{rideId}/decline")
    DispatchDecision decline(@PathVariable String rideId, @RequestBody DriverDecline decline) {
        DispatchDecision previous = decisions.get(rideId);
        RideRequest request = originalRequests.get(rideId);
        if (previous == null || request == null) {
            throw new ResponseStatusException(NOT_FOUND, "Ride not found");
        }
        if (!previous.driverId().equals(decline.driverId())) {
            throw new ResponseStatusException(CONFLICT, "Only the assigned driver can decline this offer");
        }
        return dispatch(request, Set.of(decline.driverId()));
    }

    @GetMapping("/{rideId}")
    DispatchDecision get(@PathVariable String rideId) {
        DispatchDecision decision = decisions.get(rideId);
        if (decision == null) {
            throw new ResponseStatusException(NOT_FOUND, "Ride not found");
        }
        return decision;
    }

    private DispatchDecision dispatch(RideRequest request, Set<String> excludedDrivers) {
        Instant start = Instant.now();
        String geoCell = GeoUtils.geoCell(request.pickup().latitude(), request.pickup().longitude());
        postDemand(request, geoCell);
        List<DriverCandidate> candidates = nearbyDrivers(request);
        DriverCandidate driver = candidates.stream()
                .filter(candidate -> !excludedDrivers.contains(candidate.driverId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(SERVICE_UNAVAILABLE, "No available drivers nearby"));
        SurgeQuote quote = surgeQuote(request, geoCell);
        PaymentAuthorization authorization = authorizePayment(request, quote);
        Trip trip = createTrip(request, driver, quote);
        sendNotification(request.tenantId(), request.riderId(), "PUSH", "Driver " + driver.driverId() + " is on the way");
        sendNotification(request.tenantId(), driver.driverId(), "PUSH", "New ride request for " + request.riderId());
        DispatchDecision decision = new DispatchDecision(
                request.clientRequestId(),
                trip.tripId(),
                request.tenantId(),
                request.region(),
                request.riderId(),
                driver.driverId(),
                driver.distanceKm(),
                quote.multiplier(),
                authorization.authorizationId(),
                "ASSIGNED",
                Duration.between(start, Instant.now()).toMillis());
        decisions.put(decision.rideId(), decision);
        originalRequests.put(decision.rideId(), request);
        return decision;
    }

    private List<DriverCandidate> nearbyDrivers(RideRequest request) {
        return locationClient.findNearbyDrivers(NearbyDriversRequest.newBuilder()
                        .setTenantId(request.tenantId())
                        .setRegion(request.region())
                        .setLatitude(request.pickup().latitude())
                        .setLongitude(request.pickup().longitude())
                        .setLimit(5)
                        .build())
                .getDriversList()
                .stream()
                .map(candidate -> new DriverCandidate(
                        candidate.getDriverId(),
                        candidate.getGeoCell(),
                        candidate.getLatitude(),
                        candidate.getLongitude(),
                        candidate.getDistanceKm()))
                .toList();
    }

    private void postDemand(RideRequest request, String geoCell) {
        surgeClient.recordDemand(SurgeDemandRequest.newBuilder()
                .setTenantId(request.tenantId())
                .setRegion(request.region())
                .setGeoCell(geoCell)
                .build());
    }

    private SurgeQuote surgeQuote(RideRequest request, String geoCell) {
        SurgeQuoteResponse quote = surgeClient.getSurgeQuote(SurgeQuoteRequest.newBuilder()
                .setTenantId(request.tenantId())
                .setRegion(request.region())
                .setGeoCell(geoCell)
                .build());
        return new SurgeQuote(quote.getTenantId(), quote.getRegion(), quote.getGeoCell(),
                quote.getDemand(), quote.getSupply(), quote.getMultiplier());
    }

    private PaymentAuthorization authorizePayment(RideRequest request, SurgeQuote quote) {
        BigDecimal estimate = BigDecimal.valueOf(180).multiply(BigDecimal.valueOf(quote.multiplier()));
        PaymentAuthorizationGrpcResponse authorization = paymentClient.authorizePayment(PaymentAuthorizationGrpcRequest.newBuilder()
                .setIdempotencyKey("pay:" + request.clientRequestId())
                .setTenantId(request.tenantId())
                .setRiderId(request.riderId())
                .setPaymentMethodId(request.paymentMethodId())
                .setEstimatedAmount(estimate.toPlainString())
                .build());
        return new PaymentAuthorization(authorization.getAuthorizationId(), authorization.getTenantId(),
                authorization.getRiderId(), authorization.getPaymentMethodId(), authorization.getState(),
                new BigDecimal(authorization.getAmount()), authorization.getPsp());
    }

    private Trip createTrip(RideRequest request, DriverCandidate driver, SurgeQuote quote) {
        TripGrpcResponse trip = tripClient.createTrip(CreateTripGrpcRequest.newBuilder()
                .setTenantId(request.tenantId())
                .setRegion(request.region())
                .setRiderId(request.riderId())
                .setDriverId(driver.driverId())
                .setPickup(toGrpcStop(request.pickup()))
                .setDestination(toGrpcStop(request.destination()))
                .setTier(request.tier())
                .setSurgeMultiplier(quote.multiplier())
                .build());
        return new Trip(trip.getTripId(), trip.getTenantId(), trip.getRegion(), trip.getRiderId(), trip.getDriverId());
    }

    private void sendNotification(String tenantId, String recipientId, String channel, String message) {
        notificationClient.sendNotification(NotificationGrpcRequest.newBuilder()
                .setTenantId(tenantId)
                .setRecipientId(recipientId)
                .setChannel(channel)
                .setMessage(message)
                .build());
    }

    private StopGrpc toGrpcStop(Stop stop) {
        return StopGrpc.newBuilder()
                .setLatitude(stop.latitude())
                .setLongitude(stop.longitude())
                .setLabel(stop.label() == null ? "" : stop.label())
                .build();
    }

    public record RideRequest(@NotBlank String clientRequestId, @NotBlank String tenantId, @NotBlank String region,
                              @NotBlank String riderId, Stop pickup, Stop destination, String tier,
                              @NotBlank String paymentMethodId) {
    }

    public record Stop(double latitude, double longitude, String label) {
    }

    public record DriverDecline(@NotBlank String driverId) {
    }

    public record DriverCandidate(String driverId, String geoCell, double latitude, double longitude, double distanceKm) {
    }

    public record SurgeQuote(String tenantId, String region, String geoCell, int demand, int supply, double multiplier) {
    }

    public record PaymentAuthorization(String authorizationId, String tenantId, String riderId, String paymentMethodId,
                                       String state, BigDecimal amount, String psp) {
    }

    public record Trip(String tripId, String tenantId, String region, String riderId, String driverId) {
    }

    public record DispatchDecision(String rideId, String tripId, String tenantId, String region, String riderId,
                                   String driverId, double driverDistanceKm, double surgeMultiplier,
                                   String authorizationId, String state, long decisionLatencyMs) {
    }
}
