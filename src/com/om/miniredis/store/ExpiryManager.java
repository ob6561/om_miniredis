package com.om.miniredis.store;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class ExpiryManager {

    private static class ExpiryEntry {
        String key;
        long expiryTimeMillis;

        ExpiryEntry(String key, long expiryTimeMillis) {
            this.key = key;
            this.expiryTimeMillis = expiryTimeMillis;
        }
    }

    private final PriorityQueue<ExpiryEntry> heap =
            new PriorityQueue<>((a, b) -> Long.compare(a.expiryTimeMillis, b.expiryTimeMillis));

    // tracks the CURRENT valid expiry time for each key, so we can detect stale heap entries
    private final Map<String, Long> currentExpiry = new HashMap<>();

    public void setExpiry(String key, long ttlSeconds) {
        long expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
        currentExpiry.put(key, expiryTime);
        heap.offer(new ExpiryEntry(key, expiryTime));
    }

    public void clearExpiry(String key) {
        currentExpiry.remove(key);
        // stale heap entry (if any) will be skipped lazily when popped
    }

    public Long getTtlSeconds(String key) {
        Long expiry = currentExpiry.get(key);
        if (expiry == null) return null; // no TTL set
        long remainingMillis = expiry - System.currentTimeMillis();
        return remainingMillis > 0 ? remainingMillis / 1000 : 0L;
    }

    public boolean isExpired(String key) {
        Long expiry = currentExpiry.get(key);
        return expiry != null && System.currentTimeMillis() >= expiry;
    }

    // Call periodically to purge expired keys; returns list of keys that just expired
    public java.util.List<String> pollExpiredKeys() {
        java.util.List<String> expired = new java.util.ArrayList<>();
        long now = System.currentTimeMillis();

        while (!heap.isEmpty() && heap.peek().expiryTimeMillis <= now) {
            ExpiryEntry entry = heap.poll();
            Long stillValid = currentExpiry.get(entry.key);
            // skip if this entry is stale (key was re-expired, deleted, or TTL cleared since)
            if (stillValid != null && stillValid.equals(entry.expiryTimeMillis)) {
                expired.add(entry.key);
                currentExpiry.remove(entry.key);
            }
        }
        return expired;
    }
}