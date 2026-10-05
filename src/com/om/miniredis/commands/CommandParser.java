package com.om.miniredis.commands;

import com.om.miniredis.commands.impl.DelCommand;
import com.om.miniredis.commands.impl.GetCommand;
import com.om.miniredis.commands.impl.SetCommand;

public class CommandParser {

    public static Command parse(String line) {
        String[] parts = line.trim().split("\\s+", 3);
        if (parts.length == 0 || parts[0].isEmpty()) {
            return null;
        }

        String cmd = parts[0].toUpperCase();

        switch (cmd) {
            case "SET":
                if (parts.length < 3) {
                    throw new IllegalArgumentException("ERR usage: SET key value");
                }
                return new SetCommand(parts[1], parts[2]);

            case "GET":
                if (parts.length < 2) {
                    throw new IllegalArgumentException("ERR usage: GET key");
                }
                return new GetCommand(parts[1]);

            case "DEL":
                if (parts.length < 2) {
                    throw new IllegalArgumentException("ERR usage: DEL key");
                }
                return new DelCommand(parts[1]);

            default:
                throw new IllegalArgumentException("ERR unknown command '" + cmd + "'");
        }
    }
}