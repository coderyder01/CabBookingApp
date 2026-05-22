package com.example.rides.surge;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SurgeEngine {
    private final Map<String, SurgeController.CellStats> stats = new ConcurrentHashMap<>();

    public SurgeController.CellStats recordDemand(String tenantId, String region, String geoCell) {
        return stats.compute(key(tenantId, region, geoCell), (ignored, current) -> {
            SurgeController.CellStats value = current == null ? new SurgeController.CellStats(tenantId, region, geoCell, 0, 0, 1.0) : current;
            return value.withDemand(value.demand() + 1);
        });
    }

    public SurgeController.CellStats updateSupply(String tenantId, String region, String geoCell, int availableDrivers) {
        return stats.compute(key(tenantId, region, geoCell), (ignored, current) -> {
            SurgeController.CellStats value = current == null ? new SurgeController.CellStats(tenantId, region, geoCell, 0, 0, 1.0) : current;
            return value.withSupply(availableDrivers);
        });
    }

    public SurgeController.CellStats quote(String tenantId, String region, String geoCell) {
        return stats.computeIfAbsent(key(tenantId, region, geoCell), ignored -> new SurgeController.CellStats(tenantId, region, geoCell, 0, 1, 1.0));
    }

    private String key(String tenantId, String region, String geoCell) {
        return tenantId + ":" + region + ":" + geoCell;
    }
}
