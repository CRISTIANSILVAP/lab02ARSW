package edu.eci.arsw.dogsrace.domain;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Atomic approach: uses AtomicInteger for lock-free synchronization.
 */
public final class AtomicArrivalCounter implements ArrivalCounter {

    private final AtomicInteger count = new AtomicInteger(0);
    private long startTime;
    private final AtomicInteger totalWaitTime = new AtomicInteger(0);
    private final AtomicInteger maxWaitTime = new AtomicInteger(0);

    public AtomicArrivalCounter() {
        this.startTime = System.nanoTime();
    }

    @Override
    public int recordArrival(String dogName) {
        long waitStart = System.nanoTime();
        int result = count.incrementAndGet();
        long waitEnd = System.nanoTime();
        long waitTime = waitEnd - waitStart;
        
        totalWaitTime.addAndGet((int) waitTime);
        if (waitTime > maxWaitTime.get()) {
            maxWaitTime.set((int) waitTime);
        }
        
        return result;
    }

    @Override
    public int getCount() {
        return count.get();
    }

    @Override
    public void reset() {
        count.set(0);
        totalWaitTime.set(0);
        maxWaitTime.set(0);
        startTime = System.nanoTime();
    }

    @Override
    public String getStrategyName() {
        return "ATOMIC";
    }

    @Override
    public PerformanceMetrics getMetrics() {
        return new PerformanceMetrics(
            startTime,
            System.nanoTime(),
            count.get(),
            totalWaitTime.get(),
            maxWaitTime.get(),
            getStrategyName()
        );
    }
}
