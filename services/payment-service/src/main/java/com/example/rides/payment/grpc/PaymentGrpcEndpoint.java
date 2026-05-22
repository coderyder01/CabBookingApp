package com.example.rides.payment.grpc;

import com.example.rides.grpc.PaymentAuthorizationGrpcRequest;
import com.example.rides.grpc.PaymentAuthorizationGrpcResponse;
import com.example.rides.grpc.PaymentGrpcServiceGrpc;
import com.example.rides.payment.PaymentController;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.math.BigDecimal;

@GrpcService
public class PaymentGrpcEndpoint extends PaymentGrpcServiceGrpc.PaymentGrpcServiceImplBase {
    private final PaymentController paymentController;

    PaymentGrpcEndpoint(PaymentController paymentController) {
        this.paymentController = paymentController;
    }

    @Override
    public void authorizePayment(PaymentAuthorizationGrpcRequest request, StreamObserver<PaymentAuthorizationGrpcResponse> responseObserver) {
        PaymentController.PaymentAuthorization authorization = paymentController.authorize(
                request.getIdempotencyKey(),
                new PaymentController.AuthorizationRequest(
                        request.getTenantId(),
                        request.getRiderId(),
                        request.getPaymentMethodId(),
                        new BigDecimal(request.getEstimatedAmount())));
        responseObserver.onNext(PaymentAuthorizationGrpcResponse.newBuilder()
                .setAuthorizationId(authorization.authorizationId())
                .setTenantId(authorization.tenantId())
                .setRiderId(authorization.riderId())
                .setPaymentMethodId(authorization.paymentMethodId())
                .setState(authorization.state().name())
                .setAmount(authorization.amount().toPlainString())
                .setPsp(authorization.psp())
                .build());
        responseObserver.onCompleted();
    }
}
