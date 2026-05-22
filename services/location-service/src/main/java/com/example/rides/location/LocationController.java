package com.example.rides.location;

import com.example.rides.common.DriverLocation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {
    private final LocationStore locationStore;

    LocationController(LocationStore locationStore) {
        this.locationStore = locationStore;
    }

    @PutMapping("/drivers/{driverId}")
    DriverLocation update(@PathVariable String driverId, @Valid @RequestBody DriverLocation request) {
        return locationStore.update(driverId, request);
    }

    @GetMapping("/drivers/nearby")
    List<DriverCandidate> nearby(@RequestParam String tenantId,
                                 @RequestParam String region,
                                 @RequestParam double latitude,
                                 @RequestParam double longitude,
                                 @RequestParam(defaultValue = "5") int limit) {
        return locationStore.nearby(tenantId, region, latitude, longitude, limit);
    }

    public record DriverCandidate(String driverId, String geoCell, double latitude, double longitude, double distanceKm) {
    }
}
