package com.om.miniredis.commands.impl;

import com.om.miniredis.commands.Command;
import com.om.miniredis.store.DataStore;

public class TtlCommand implements Command {
    private final String key;

    public TtlCommand(String key) {
        this.key = key;
    }

    @Override
    public String execute(DataStore store) {
        if (!store.exists(key)) return "-2";
        Long ttl = store.ttl(key);
        return ttl != null ? String.valueOf(ttl) : "-1";
    }
}