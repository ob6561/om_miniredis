package com.om.miniredis.server;

import com.om.miniredis.store.DataStore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port;
    private final DataStore store;
    private final ExecutorService pool = Executors.newFixedThreadPool(50);

    public Server(int port, DataStore store) {
        this.port = port;
        this.store = store;
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Mini-Redis listening on port " + port);
            while (true) {
                Socket client = serverSocket.accept(); // blocks until someone connects
                pool.execute(new ClientHandler(client, store));
            }
        }
    }
}