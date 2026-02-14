package edu.eci.arsw.dogsrace.domain;

/**
 * Interface for different synchronization strategies to handle concurrent arrivals.
 */
public interface ArrivalCounter {

    /**
     * Record an arrival and return the position assigned to this galgo.
     * @param dogName the name of the galgo
     * @return the arrival position (1 = first, 2 = second, etc.)
     */
    int recordArrival(String dogName);

    /**
     * Get the current count of arrivals.
     */
    int getCount();

    /**
     * Reset the counter.
     */
    void reset();

    /**
     * Get the name of the synchronization strategy.
     */
    String getStrategyName();

    /**
     * Get performance metrics (for analysis).
     */
    PerformanceMetrics getMetrics();

    /**
     * Holder for performance metrics.
     */
    record PerformanceMetrics(
        long startTime,
        long endTime,
        int totalArrivals,
        long totalWaitTime,
        long maxWaitTime,
        String strategyName
    ) { }
}
