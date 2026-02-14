package edu.eci.arsw.dogsrace.domain;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Hybrid approach: uses both synchronized and AtomicInteger.
 * Combines monitor-based sync with atomic operations.
 */
public final class SynchronizedAtomicArrivalCounter implements ArrivalCounter {

    private final AtomicInteger count = new AtomicInteger(0);
    private final Object monitor = new Object();
    private long startTime;
    private long totalWaitTime = 0;
    private long maxWaitTime = 0;

    public SynchronizedAtomicArrivalCounter() {
        this.startTime = System.nanoTime();
    }

    @Override
    public int recordArrival(String dogName) {
        long waitStart = System.nanoTime();
        synchronized (monitor) {
            int result = count.incrementAndGet();
            long waitEnd = System.nanoTime();
            long waitTime = waitEnd - waitStart;
            totalWaitTime += waitTime;
            maxWaitTime = Math.max(maxWaitTime, waitTime);
            return result;
        }
    }

    @Override
    public int getCount() {
        return count.get();
    }

    @Override
    public synchronized void reset() {
        count.set(0);
        totalWaitTime = 0;
        maxWaitTime = 0;
        startTime = System.nanoTime();
    }

    @Override
    public String getStrategyName() {
        return "SYNCHRONIZED_ATOMIC";
    }

    @Override
    public PerformanceMetrics getMetrics() {
        synchronized (monitor) {
            return new PerformanceMetrics(
                startTime,
                System.nanoTime(),
                count.get(),
                totalWaitTime,
                maxWaitTime,
                getStrategyName()
            );
        }
    }
}
