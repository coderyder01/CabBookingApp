package com.example.rides.location;

import com.example.rides.common.DriverLocation;
import com.example.rides.common.GeoUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LocationStore {
    private final Map<String, DriverLocation> latestLocations = new ConcurrentHashMap<>();

    public DriverLocation update(String driverId, DriverLocation request) {
        DriverLocation location = new DriverLocation(
                driverId,
                request.tenantId(),
                request.region(),
                request.geoCell() == null ? GeoUtils.geoCell(request.latitude(), request.longitude()) : request.geoCell(),
                request.latitude(),
                request.longitude(),
                request.available(),
                Instant.now());
        latestLocations.put(driverId, location);
        return location;
    }

    public List<LocationController.DriverCandidate> nearby(String tenantId, String region, double latitude, double longitude, int limit) {
        return latestLocations.values().stream()
                .filter(DriverLocation::available)
                .filter(location -> location.tenantId().equals(tenantId) && location.region().equals(region))
                .map(location -> new LocationController.DriverCandidate(
                        location.driverId(),
                        location.geoCell(),
                        location.latitude(),
                        location.longitude(),
                        GeoUtils.distanceKm(latitude, longitude, location.latitude(), location.longitude())))
                .sorted(Comparator.comparingDouble(LocationController.DriverCandidate::distanceKm))
                .limit(limit)
                .toList();
    }
}
