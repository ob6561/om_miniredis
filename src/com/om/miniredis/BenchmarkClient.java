package com.om.miniredis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

public class BenchmarkClient {
    private static final String HOST = "localhost";
    private static final int PORT = 6379;
    private static final int KEYSPACE = 50; // below store capacity (100), so no eviction noise

    public static void main(String[] args) throws Exception {
        System.out.println("Warming up...");
        run(1, 5_000, false);

        System.out.println("clients | total ops | ops/sec  | p50 (us) | p99 (us)");
        for (int clients : new int[]{1, 4, 16}) {
            run(clients, 20_000, true);
        }
    }

    private static void run(int clients, int opsPerClient, boolean print) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(clients);
        CountDownLatch ready = new CountDownLatch(clients);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<long[]>> futures = new ArrayList<>();

        for (int c = 0; c < clients; c++) {
            futures.add(pool.submit(() -> worker(opsPerClient, ready, go)));
        }

        if (!ready.await(10, TimeUnit.SECONDS)) {
            pool.shutdownNow();
            throw new IllegalStateException("Clients couldn't connect. Is the server running?");
        }
        long start = System.nanoTime();
        go.countDown();

        long[] all = new long[clients * opsPerClient];
        int idx = 0;
        for (Future<long[]> f : futures) {
            long[] lat = f.get();
            System.arraycopy(lat, 0, all, idx, lat.length);
            idx += lat.length;
        }
        long elapsed = System.nanoTime() - start;
        pool.shutdown();

        if (print) {
            Arrays.sort(all);
            double opsPerSec = all.length / (elapsed / 1e9);
            System.out.printf("%7d | %9d | %8.0f | %8.1f | %8.1f%n",
                    clients, all.length, opsPerSec,
                    all[all.length / 2] / 1000.0,
                    all[(int) (all.length * 0.99)] / 1000.0);
        }
    }

    private static long[] worker(int ops, CountDownLatch ready, CountDownLatch go) throws Exception {
        long[] latencies = new long[ops];
        try (Socket socket = new Socket(HOST, PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            ready.countDown();
            go.await();

            ThreadLocalRandom rnd = ThreadLocalRandom.current();
            for (int i = 0; i < ops; i++) {
                String key = "bench" + rnd.nextInt(KEYSPACE);
                String cmd = (i % 2 == 0) ? "SET " + key + " value" : "GET " + key;
                long t0 = System.nanoTime();
                out.println(cmd);
                in.readLine(); // wait for the reply before sending the next command
                latencies[i] = System.nanoTime() - t0;
            }
        }
        return latencies;
    }
}