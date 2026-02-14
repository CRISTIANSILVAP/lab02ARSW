package edu.eci.arsw.dogsrace.domain;

/**
 * Synchronized approach: uses monitor-based synchronization.
 */
public final class SynchronizedArrivalCounter implements ArrivalCounter {

    private int count = 0;
    private final Object monitor = new Object();
    private long startTime;
    private long totalWaitTime = 0;
    private long maxWaitTime = 0;

    public SynchronizedArrivalCounter() {
        this.startTime = System.nanoTime();
    }

    @Override
    public int recordArrival(String dogName) {
        long waitStart = System.nanoTime();
        synchronized (monitor) {
            long waitEnd = System.nanoTime();
            long waitTime = waitEnd - waitStart;
            totalWaitTime += waitTime;
            maxWaitTime = Math.max(maxWaitTime, waitTime);
            
            count++;
            return count;
        }
    }

    @Override
    public synchronized int getCount() {
        return count;
    }

    @Override
    public synchronized void reset() {
        count = 0;
        totalWaitTime = 0;
        maxWaitTime = 0;
        startTime = System.nanoTime();
    }

    @Override
    public String getStrategyName() {
        return "SYNCHRONIZED";
    }

    @Override
    public PerformanceMetrics getMetrics() {
        synchronized (monitor) {
            return new PerformanceMetrics(
                startTime,
                System.nanoTime(),
                count,
                totalWaitTime,
                maxWaitTime,
                getStrategyName()
            );
        }
    }
}
