package edu.eci.arsw.dogsrace.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Thread-safe arrival registry.
 * Uses pluggable ArrivalCounter for evaluating different synchronization strategies.
 */
public final class ArrivalRegistry {

    private String winner = null;
    private final List<String> ranking = new ArrayList<>();
    private final ArrivalCounter counter;

    public ArrivalRegistry(ArrivalCounter counter) {
        this.counter = Objects.requireNonNull(counter, "counter cannot be null");
    }

    public synchronized ArrivalSnapshot registerArrival(String dogName) {
        Objects.requireNonNull(dogName, "dogName");
        final int position = counter.recordArrival(dogName);
        if (position == 1) {
            winner = dogName;
        }
        // This list is written only inside this critical section, so it stays consistent.
        ranking.add(dogName);
        return new ArrivalSnapshot(position, winner);
    }

    public synchronized int getNextPosition() {
        return counter.getCount() + 1;
    }

    public synchronized String getWinner() {
        return winner;
    }

    /**
     * Returns a snapshot of the arrival order (index 0 = position 1).
     */
    public synchronized List<String> getRanking() {
        return List.copyOf(ranking);
    }

    public record ArrivalSnapshot(int position, String winner) { }
    
    public synchronized void reset() {
        winner = null;
        ranking.clear();
        counter.reset();
    }

    public ArrivalCounter.PerformanceMetrics getMetrics() {
        return counter.getMetrics();
    }

    public String getStrategyName() {
        return counter.getStrategyName();
    }

}
