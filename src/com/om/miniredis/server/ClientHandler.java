package com.om.miniredis.server;

import com.om.miniredis.commands.Command;
import com.om.miniredis.commands.CommandParser;
import com.om.miniredis.store.DataStore;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final DataStore store;

    public ClientHandler(Socket socket, DataStore store) {
        this.socket = socket;
        this.store = store;
    }

    @Override
    public void run() {
        try (socket;
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.equalsIgnoreCase("EXIT") || line.equalsIgnoreCase("QUIT")) {
                    out.println("Bye.");
                    break;
                }

                try {
                    Command command = CommandParser.parse(line);
                    out.println(command.execute(store));
                } catch (IllegalArgumentException e) {
                    out.println(e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Client connection error: " + e.getMessage());
        }
    }
}