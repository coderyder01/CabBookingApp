package com.example.rides.trip;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/trips")
public class TripController {
    private final Map<String, Trip> trips = new ConcurrentHashMap<>();
    private final Map<String, SafetyIncident> safetyIncidents = new ConcurrentHashMap<>();

    @PostMapping
    public Trip create(@Valid @RequestBody CreateTrip request) {
        String tripId = UUID.randomUUID().toString();
        Trip trip = new Trip(tripId, request.tenantId(), request.region(), request.riderId(), request.driverId(),
                request.pickup(), request.destination(), request.tier(), TripState.ASSIGNED,
                request.surgeMultiplier(), null, Instant.now(), null, null);
        trips.put(tripId, trip);
        return trip;
    }

    @PostMapping("/{tripId}/start")
    Trip start(@PathVariable String tripId) {
        return transition(tripId, TripState.IN_PROGRESS, null);
    }

    @PostMapping("/{tripId}/pause")
    Trip pause(@PathVariable String tripId) {
        return transition(tripId, TripState.PAUSED, null);
    }

    @PostMapping("/{tripId}/end")
    Trip end(@PathVariable String tripId, @RequestBody EndTrip request) {
        BigDecimal fare = BigDecimal.valueOf(40 + request.distanceKm() * 18)
                .multiply(BigDecimal.valueOf(find(tripId).surgeMultiplier()))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        return transition(tripId, TripState.COMPLETED, fare);
    }

    @GetMapping("/{tripId}")
    Trip get(@PathVariable String tripId) {
        return find(tripId);
    }

    @PostMapping("/{tripId}/sos")
    SafetyIncident sos(@PathVariable String tripId, @RequestBody SosRequest request) {
        Trip trip = find(tripId);
        SafetyIncident incident = new SafetyIncident(UUID.randomUUID().toString(), tripId, trip.tenantId(),
                request.reportedBy(), request.reason(), "OPEN", Instant.now());
        safetyIncidents.put(incident.incidentId(), incident);
        return incident;
    }

    private Trip transition(String tripId, TripState state, BigDecimal fare) {
        Trip current = find(tripId);
        Trip updated = new Trip(current.tripId(), current.tenantId(), current.region(), current.riderId(),
                current.driverId(), current.pickup(), current.destination(), current.tier(), state,
                current.surgeMultiplier(), fare == null ? current.finalFare() : fare,
                current.createdAt(), state == TripState.IN_PROGRESS ? Instant.now() : current.startedAt(),
                state == TripState.COMPLETED ? Instant.now() : current.endedAt());
        trips.put(tripId, updated);
        return updated;
    }

    private Trip find(String tripId) {
        Trip trip = trips.get(tripId);
        if (trip == null) {
            throw new ResponseStatusException(NOT_FOUND, "Trip not found");
        }
        return trip;
    }

    public record CreateTrip(@NotBlank String tenantId, @NotBlank String region, @NotBlank String riderId,
                             @NotBlank String driverId, Stop pickup, Stop destination, String tier,
                             double surgeMultiplier) {
    }

    public record EndTrip(double distanceKm) {
    }

    public record SosRequest(String reportedBy, String reason) {
    }

    public record Stop(double latitude, double longitude, String label) {
    }

    public enum TripState {
        ASSIGNED, IN_PROGRESS, PAUSED, COMPLETED
    }

    public record Trip(String tripId, String tenantId, String region, String riderId, String driverId,
                       Stop pickup, Stop destination, String tier, TripState state, double surgeMultiplier,
                       BigDecimal finalFare, Instant createdAt, Instant startedAt, Instant endedAt) {
    }

    public record SafetyIncident(String incidentId, String tripId, String tenantId, String reportedBy,
                                 String reason, String state, Instant createdAt) {
    }
}
