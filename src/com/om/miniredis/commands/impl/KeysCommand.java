package com.om.miniredis.commands.impl;

import com.om.miniredis.commands.Command;
import com.om.miniredis.store.DataStore;

import java.util.List;

public class KeysCommand implements Command {
    @Override
    public String execute(DataStore store) {
        List<String> keys = store.keys();
        return keys.isEmpty() ? "(empty)" : String.join(", ", keys);
    }
}