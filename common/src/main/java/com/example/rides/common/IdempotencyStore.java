package com.example.rides.common;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

@Component
public class IdempotencyStore {
    private final ConcurrentMap<String, Object> responses = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> T getOrCreate(String key, Supplier<T> supplier) {
        return (T) responses.computeIfAbsent(key, ignored -> supplier.get());
    }

    public Optional<Object> get(String key) {
        return Optional.ofNullable(responses.get(key));
    }
}
