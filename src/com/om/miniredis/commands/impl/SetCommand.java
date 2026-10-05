package com.om.miniredis.commands.impl;

import com.om.miniredis.commands.Command;
import com.om.miniredis.store.DataStore;

public class SetCommand implements Command {
    private final String key;
    private final String value;

    public SetCommand(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String execute(DataStore store) {
        store.set(key, value);
        return "OK";
    }
}