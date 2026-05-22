package com.example.rides.surge.grpc;

import com.example.rides.grpc.SurgeDemandRequest;
import com.example.rides.grpc.SurgeGrpcServiceGrpc;
import com.example.rides.grpc.SurgeQuoteRequest;
import com.example.rides.grpc.SurgeQuoteResponse;
import com.example.rides.surge.SurgeController;
import com.example.rides.surge.SurgeEngine;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class SurgeGrpcEndpoint extends SurgeGrpcServiceGrpc.SurgeGrpcServiceImplBase {
    private final SurgeEngine surgeEngine;

    SurgeGrpcEndpoint(SurgeEngine surgeEngine) {
        this.surgeEngine = surgeEngine;
    }

    @Override
    public void recordDemand(SurgeDemandRequest request, StreamObserver<SurgeQuoteResponse> responseObserver) {
        responseObserver.onNext(toResponse(surgeEngine.recordDemand(request.getTenantId(), request.getRegion(), request.getGeoCell())));
        responseObserver.onCompleted();
    }

    @Override
    public void getSurgeQuote(SurgeQuoteRequest request, StreamObserver<SurgeQuoteResponse> responseObserver) {
        responseObserver.onNext(toResponse(surgeEngine.quote(request.getTenantId(), request.getRegion(), request.getGeoCell())));
        responseObserver.onCompleted();
    }

    private SurgeQuoteResponse toResponse(SurgeController.CellStats stats) {
        return SurgeQuoteResponse.newBuilder()
                .setTenantId(stats.tenantId())
                .setRegion(stats.region())
                .setGeoCell(stats.geoCell())
                .setDemand(stats.demand())
                .setSupply(stats.supply())
                .setMultiplier(stats.multiplier())
                .build();
    }
}
