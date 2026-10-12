package com.om.miniredis.store;

public class DataStore {
    private static final int DEFAULT_CAPACITY = 100;
    private final LRUCache cache;
    private final ExpiryManager expiryManager = new ExpiryManager();

    public DataStore() {
        this(DEFAULT_CAPACITY);
    }

    public DataStore(int capacity) {
        this.cache = new LRUCache(capacity);
    }

    public synchronized void set(String key, String value) {
        cache.put(key, value);
        expiryManager.clearExpiry(key);
    }

    public synchronized String get(String key) {
        purgeIfExpired(key);
        return cache.get(key);
    }

    public synchronized boolean delete(String key) {
        expiryManager.clearExpiry(key);
        return cache.remove(key);
    }

    public synchronized boolean exists(String key) {
        purgeIfExpired(key);
        return cache.containsKey(key);
    }

    public synchronized boolean expire(String key, long ttlSeconds) {
        if (!cache.containsKey(key)) return false;
        expiryManager.setExpiry(key, ttlSeconds);
        return true;
    }

    public synchronized Long ttl(String key) {
        if (!cache.containsKey(key)) return null;
        return expiryManager.getTtlSeconds(key);
    }
    public synchronized int purgeExpiredKeys() {
        java.util.List<String> expired = expiryManager.pollExpiredKeys();
        for (String key : expired) {
            cache.remove(key);
        }
        return expired.size();
    }

    public synchronized java.util.List<SnapshotEntry> snapshot() {
        java.util.List<SnapshotEntry> result = new java.util.ArrayList<>();
        for (var e : cache.entriesLeastToMostRecent()) {
            Long expiry = expiryManager.getExpiryMillis(e.getKey());
            result.add(new SnapshotEntry(e.getKey(), e.getValue(), expiry != null ? expiry : -1));
        }
        return result;
    }

    public synchronized void restore(java.util.List<SnapshotEntry> entries) {
        long now = System.currentTimeMillis();
        for (SnapshotEntry e : entries) {
            if (e.expiryMillis() != -1 && e.expiryMillis() <= now) continue; // expired while server was down
            cache.put(e.key(), e.value());
            if (e.expiryMillis() != -1) {
                expiryManager.setExpiryAt(e.key(), e.expiryMillis());
            }
        }
    }
    public synchronized java.util.List<String> keys() {
        purgeExpiredKeys();
        java.util.List<String> result = new java.util.ArrayList<>();
        for (var e : cache.entriesLeastToMostRecent()) {
            result.add(e.getKey());
        }
        return result;
    }

    private void purgeIfExpired(String key) {
        if (expiryManager.isExpired(key)) {
            cache.remove(key);
            expiryManager.clearExpiry(key);
        }
    }
}