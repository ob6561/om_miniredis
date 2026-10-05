package com.om.miniredis.commands;
import com.om.miniredis.store.DataStore;
public interface Command {
    String execute(DataStore store);
}
