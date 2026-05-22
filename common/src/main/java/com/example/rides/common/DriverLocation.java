package com.example.rides.common;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record DriverLocation(
        @NotBlank String driverId,
        @NotBlank String tenantId,
        @NotBlank String region,
        String geoCell,
        double latitude,
        double longitude,
        boolean available,
        Instant updatedAt
) {
}
