package com.example.rides.notification.grpc;

import com.example.rides.grpc.NotificationGrpcRequest;
import com.example.rides.grpc.NotificationGrpcResponse;
import com.example.rides.grpc.NotificationGrpcServiceGrpc;
import com.example.rides.notification.NotificationController;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class NotificationGrpcEndpoint extends NotificationGrpcServiceGrpc.NotificationGrpcServiceImplBase {
    private final NotificationController notificationController;

    NotificationGrpcEndpoint(NotificationController notificationController) {
        this.notificationController = notificationController;
    }

    @Override
    public void sendNotification(NotificationGrpcRequest request, StreamObserver<NotificationGrpcResponse> responseObserver) {
        NotificationController.NotificationReceipt receipt = notificationController.send(
                new NotificationController.NotificationRequest(
                        request.getTenantId(),
                        request.getRecipientId(),
                        request.getChannel(),
                        request.getMessage()));
        responseObserver.onNext(NotificationGrpcResponse.newBuilder()
                .setNotificationId(receipt.notificationId())
                .setState(receipt.state())
                .build());
        responseObserver.onCompleted();
    }
}
