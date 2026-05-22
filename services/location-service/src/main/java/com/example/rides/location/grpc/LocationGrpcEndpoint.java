package com.example.rides.location.grpc;

import com.example.rides.grpc.DriverCandidateGrpc;
import com.example.rides.grpc.LocationGrpcServiceGrpc;
import com.example.rides.grpc.NearbyDriversRequest;
import com.example.rides.grpc.NearbyDriversResponse;
import com.example.rides.location.LocationController;
import com.example.rides.location.LocationStore;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class LocationGrpcEndpoint extends LocationGrpcServiceGrpc.LocationGrpcServiceImplBase {
    private final LocationStore locationStore;

    LocationGrpcEndpoint(LocationStore locationStore) {
        this.locationStore = locationStore;
    }

    @Override
    public void findNearbyDrivers(NearbyDriversRequest request, StreamObserver<NearbyDriversResponse> responseObserver) {
        NearbyDriversResponse.Builder response = NearbyDriversResponse.newBuilder();
        for (LocationController.DriverCandidate candidate : locationStore.nearby(
                request.getTenantId(), request.getRegion(), request.getLatitude(), request.getLongitude(), request.getLimit())) {
            response.addDrivers(DriverCandidateGrpc.newBuilder()
                    .setDriverId(candidate.driverId())
                    .setGeoCell(candidate.geoCell())
                    .setLatitude(candidate.latitude())
                    .setLongitude(candidate.longitude())
                    .setDistanceKm(candidate.distanceKm())
                    .build());
        }
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
