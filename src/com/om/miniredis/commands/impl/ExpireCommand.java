package com.om.miniredis.commands.impl;

import com.om.miniredis.commands.Command;
import com.om.miniredis.store.DataStore;

public class ExpireCommand implements Command {
    private final String key;
    private final long ttlSeconds;

    public ExpireCommand(String key, long ttlSeconds) {
        this.key = key;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public String execute(DataStore store) {
        return store.expire(key, ttlSeconds) ? "1" : "0";
    }
}