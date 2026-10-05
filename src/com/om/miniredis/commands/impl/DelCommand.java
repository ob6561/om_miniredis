package com.om.miniredis.commands.impl;

import com.om.miniredis.commands.Command;
import com.om.miniredis.store.DataStore;

public class DelCommand implements Command {
    private final String key;

    public DelCommand(String key) {
        this.key = key;
    }

    @Override
    public String execute(DataStore store) {
        return store.delete(key) ? "1" : "0";
    }
}