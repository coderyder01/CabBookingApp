package com.example.rides.surge;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/surge")
public class SurgeController {
    private final SurgeEngine surgeEngine;

    SurgeController(SurgeEngine surgeEngine) {
        this.surgeEngine = surgeEngine;
    }

    @PostMapping("/cells/{geoCell}/demand")
    CellStats recordDemand(@PathVariable String geoCell, @RequestParam String tenantId, @RequestParam String region) {
        return surgeEngine.recordDemand(tenantId, region, geoCell);
    }

    @PutMapping("/cells/{geoCell}/supply")
    CellStats updateSupply(@PathVariable String geoCell, @RequestBody SupplyUpdate request) {
        return surgeEngine.updateSupply(request.tenantId(), request.region(), geoCell, request.availableDrivers());
    }

    @GetMapping("/cells/{geoCell}")
    CellStats quote(@PathVariable String geoCell, @RequestParam String tenantId, @RequestParam String region) {
        return surgeEngine.quote(tenantId, region, geoCell);
    }

    public record SupplyUpdate(@NotBlank String tenantId, @NotBlank String region, int availableDrivers) {
    }

    public record CellStats(String tenantId, String region, String geoCell, int demand, int supply, double multiplier) {
        public CellStats withDemand(int nextDemand) {
            return new CellStats(tenantId, region, geoCell, nextDemand, supply, computeMultiplier(nextDemand, supply));
        }

        public CellStats withSupply(int nextSupply) {
            return new CellStats(tenantId, region, geoCell, demand, nextSupply, computeMultiplier(demand, nextSupply));
        }

        private static double computeMultiplier(int demand, int supply) {
            if (supply <= 0) {
                return 2.5;
            }
            double ratio = (double) demand / supply;
            return Math.max(1.0, Math.min(2.5, Math.round((1.0 + ratio * 0.35) * 100.0) / 100.0));
        }
    }
}
