package com.example.rides.trip.grpc;

import com.example.rides.grpc.CreateTripGrpcRequest;
import com.example.rides.grpc.TripGrpcResponse;
import com.example.rides.grpc.TripGrpcServiceGrpc;
import com.example.rides.trip.TripController;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class TripGrpcEndpoint extends TripGrpcServiceGrpc.TripGrpcServiceImplBase {
    private final TripController tripController;

    TripGrpcEndpoint(TripController tripController) {
        this.tripController = tripController;
    }

    @Override
    public void createTrip(CreateTripGrpcRequest request, StreamObserver<TripGrpcResponse> responseObserver) {
        TripController.Trip trip = tripController.create(new TripController.CreateTrip(
                request.getTenantId(),
                request.getRegion(),
                request.getRiderId(),
                request.getDriverId(),
                new TripController.Stop(request.getPickup().getLatitude(), request.getPickup().getLongitude(), request.getPickup().getLabel()),
                new TripController.Stop(request.getDestination().getLatitude(), request.getDestination().getLongitude(), request.getDestination().getLabel()),
                request.getTier(),
                request.getSurgeMultiplier()));
        responseObserver.onNext(TripGrpcResponse.newBuilder()
                .setTripId(trip.tripId())
                .setTenantId(trip.tenantId())
                .setRegion(trip.region())
                .setRiderId(trip.riderId())
                .setDriverId(trip.driverId())
                .setState(trip.state().name())
                .build());
        responseObserver.onCompleted();
    }
}
