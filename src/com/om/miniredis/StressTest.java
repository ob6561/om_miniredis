package com.om.miniredis;

import com.om.miniredis.store.DataStore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class StressTest {
    public static void main(String[] args) throws Exception {
        DataStore store = new DataStore(100);
        int threads = 8;
        int opsPerThread = 200_000;
        AtomicInteger crashed = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.execute(() -> {
                ThreadLocalRandom rnd = ThreadLocalRandom.current();
                try {
                    for (int i = 0; i < opsPerThread; i++) {
                        String key = "k" + rnd.nextInt(500);
                        switch (rnd.nextInt(4)) {
                            case 0 -> store.set(key, "v");
                            case 1 -> store.get(key);
                            case 2 -> store.delete(key);
                            default -> store.expire(key, 1);
                        }
                    }
                } catch (Throwable e) {
                    crashed.incrementAndGet();
                    System.out.println("Thread crashed: " + e);
                } finally {
                    done.countDown();
                }
            });
        }

        boolean finished = done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();
        System.out.println(finished ? "Finished." : "TIMED OUT (possible hang or deadlock)");
        System.out.println("Threads that crashed: " + crashed.get());
    }
}