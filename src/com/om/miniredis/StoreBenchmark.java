package com.om.miniredis;

import com.om.miniredis.store.DataStore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class StoreBenchmark {
    private static final String[] KEYS = new String[50];
    static {
        for (int i = 0; i < KEYS.length; i++) KEYS[i] = "bench" + i;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Warming up...");
        run(1, 1_000_000, false);

        System.out.println("threads | total ops | ops/sec");
        for (int threads : new int[]{1, 4, 16}) {
            run(threads, 1_000_000, true);
        }
    }

    private static void run(int threads, int opsPerThread, boolean print) throws Exception {
        DataStore store = new DataStore(100);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.execute(() -> {
                try {
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    ready.countDown();
                    go.await();
                    for (int i = 0; i < opsPerThread; i++) {
                        String key = KEYS[rnd.nextInt(KEYS.length)];
                        if (i % 2 == 0) store.set(key, "value");
                        else store.get(key);
                    }
                } catch (InterruptedException ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        long start = System.nanoTime();
        go.countDown();
        done.await();
        long elapsed = System.nanoTime() - start;
        pool.shutdown();

        if (print) {
            long total = (long) threads * opsPerThread;
            System.out.printf("%7d | %9d | %,.0f%n", threads, total, total / (elapsed / 1e9));
        }
    }
}