package edu.eci.arsw.dogsrace.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Thread-safe arrival registry.
 * Critical section is limited to the position assignment and winner selection.
 */
public final class ArrivalRegistry {

    private int nextPosition = 1;
    private String winner = null;
    private final List<String> ranking = new ArrayList<>();

    public synchronized ArrivalSnapshot registerArrival(String dogName) {
        Objects.requireNonNull(dogName, "dogName");
        final int position = nextPosition++;
        if (position == 1) {
            winner = dogName;
        }
        // This list is written only inside this critical section, so it stays consistent.
        ranking.add(dogName);
        return new ArrivalSnapshot(position, winner);
    }

    public synchronized int getNextPosition() {
        return nextPosition;
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
        nextPosition = 1;
        ranking.clear();
    }

}
