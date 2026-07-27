package com.gymnetwork.auth.service.impl;

import com.gymnetwork.auth.service.AuthTokenStore;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
@Profile({"dev", "test"})
public class InMemoryAuthTokenStore implements AuthTokenStore {

    private final ConcurrentMap<String, StoreEntry> entries = new ConcurrentHashMap<>();

    @Override
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        entries.put(key, new StoreEntry(value, Instant.now().plusMillis(unit.toMillis(timeout)).toEpochMilli()));
    }

    @Override
    public Object get(String key) {
        StoreEntry entry = entries.get(key);
        if (entry == null) {
            return null;
        }
        if (entry.expiresAtMillis() <= Instant.now().toEpochMilli()) {
            entries.remove(key);
            return null;
        }
        return entry.value();
    }

    @Override
    public void delete(String key) {
        entries.remove(key);
    }

    private record StoreEntry(Object value, long expiresAtMillis) {
    }
}
