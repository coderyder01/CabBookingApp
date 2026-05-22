package com.example.rides.payment;

import com.example.rides.common.IdempotencyStore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final IdempotencyStore idempotencyStore;

    PaymentController(IdempotencyStore idempotencyStore) {
        this.idempotencyStore = idempotencyStore;
    }

    @PostMapping("/authorize")
    public PaymentAuthorization authorize(@RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                   @Valid @RequestBody AuthorizationRequest request) {
        String key = idempotencyKey == null ? UUID.randomUUID().toString() : idempotencyKey;
        return idempotencyStore.getOrCreate(key, () -> new PaymentAuthorization(
                UUID.randomUUID().toString(),
                request.tenantId(),
                request.riderId(),
                request.paymentMethodId(),
                PaymentState.AUTHORIZED,
                request.estimatedAmount(),
                "mock-psp",
                Instant.now()));
    }

    public record AuthorizationRequest(@NotBlank String tenantId, @NotBlank String riderId,
                                       @NotBlank String paymentMethodId, BigDecimal estimatedAmount) {
    }

    public enum PaymentState {
        AUTHORIZED, CAPTURED, FAILED
    }

    public record PaymentAuthorization(String authorizationId, String tenantId, String riderId,
                                       String paymentMethodId, PaymentState state, BigDecimal amount,
                                       String psp, Instant authorizedAt) {
    }
}
