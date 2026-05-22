package com.example.rides.notification;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final List<NotificationReceipt> receipts = new CopyOnWriteArrayList<>();

    @PostMapping
    public NotificationReceipt send(@Valid @RequestBody NotificationRequest request) {
        NotificationReceipt receipt = new NotificationReceipt(UUID.randomUUID().toString(), request.tenantId(),
                request.recipientId(), request.channel(), "QUEUED", Instant.now(), request.message());
        receipts.add(receipt);
        return receipt;
    }

    @GetMapping
    List<NotificationReceipt> list() {
        return receipts;
    }

    public record NotificationRequest(@NotBlank String tenantId, @NotBlank String recipientId,
                                      @NotBlank String channel, @NotBlank String message) {
    }

    public record NotificationReceipt(String notificationId, String tenantId, String recipientId,
                                      String channel, String state, Instant queuedAt, String message) {
    }
}
