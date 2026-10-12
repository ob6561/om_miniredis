package com.om.miniredis.store;

public record SnapshotEntry(String key, String value, long expiryMillis) {
    // expiryMillis == -1 means "no TTL"
}