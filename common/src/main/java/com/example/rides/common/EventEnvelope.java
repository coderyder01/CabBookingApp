package com.example.rides.common;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        String eventId,
        String topic,
        String tenantId,
        String region,
        Instant occurredAt,
        T payload
) {
    public static <T> EventEnvelope<T> of(String topic, String tenantId, String region, T payload) {
        return new EventEnvelope<>(UUID.randomUUID().toString(), topic, tenantId, region, Instant.now(), payload);
    }
}
