package com.om.miniredis.commands.impl;

import com.om.miniredis.commands.Command;
import com.om.miniredis.store.DataStore;

public class GetCommand implements Command {
    private final String key;

    public GetCommand(String key) {
        this.key = key;
    }

    @Override
    public String execute(DataStore store) {
        String value = store.get(key);
        return value != null ? value : "(nil)";
    }
}