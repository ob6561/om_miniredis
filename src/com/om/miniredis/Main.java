package com.om.miniredis;

import com.om.miniredis.persistence.SnapshotManager;
import com.om.miniredis.server.Server;
import com.om.miniredis.store.DataStore;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        DataStore store = new DataStore();
        SnapshotManager snapshots = new SnapshotManager(Path.of("dump.mrdb"));

        int loaded = snapshots.load(store);
        System.out.println("Loaded " + loaded + " key(s) from snapshot");

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        // expiry sweeper
        scheduler.scheduleAtFixedRate(() -> {
            int n = store.purgeExpiredKeys();
            if (n > 0) System.out.println("Sweeper purged " + n + " expired key(s)");
        }, 1, 1, TimeUnit.SECONDS);

        // periodic snapshot
        scheduler.scheduleAtFixedRate(() -> {
            try {
                snapshots.save(store);
                //System.out.println("Snapshot saved");
            } catch (IOException e) {
                System.out.println("Snapshot failed: " + e.getMessage());
            }
        }, 10, 10, TimeUnit.SECONDS);

        // best-effort final snapshot on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                snapshots.save(store);
            } catch (IOException e) {
                System.out.println("Final snapshot failed: " + e.getMessage());
            }
        }));

        new Server(6379, store).start();
    }
}