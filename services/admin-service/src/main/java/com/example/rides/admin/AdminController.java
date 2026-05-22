package com.example.rides.admin;

import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final Map<String, Boolean> flags = new ConcurrentHashMap<>(Map.of(
            "dispatch.enabled", true,
            "payments.enabled", true,
            "surge.enabled", true));

    @GetMapping("/flags")
    Map<String, Boolean> flags() {
        return flags;
    }

    @PutMapping("/flags/{name}")
    Map<String, Boolean> update(@PathVariable String name, @RequestBody FlagUpdate update) {
        flags.put(name, update.enabled());
        return flags;
    }

    @GetMapping("/flags/{name}")
    Flag flag(@PathVariable String name) {
        return new Flag(name, flags.getOrDefault(name, false));
    }

    public record FlagUpdate(boolean enabled) {
    }

    public record Flag(String name, boolean enabled) {
    }
}
