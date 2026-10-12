package com.om.miniredis;

import com.om.miniredis.server.Server;
import com.om.miniredis.store.DataStore;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        DataStore store = new DataStore();

        ScheduledExecutorService sweeper = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "expiry-sweeper");
            t.setDaemon(true); // won't keep the JVM alive on shutdown
            return t;
        });
        sweeper.scheduleAtFixedRate(() -> {
            int n = store.purgeExpiredKeys();
            if (n > 0) System.out.println("Sweeper purged " + n + " expired key(s)");
        }, 1, 1, TimeUnit.SECONDS);

        new Server(6379, store).start();
    }
}