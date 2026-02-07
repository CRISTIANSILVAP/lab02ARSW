package edu.eci.arsw.dogsrace.control;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class RaceControlTest {

    @Test
    void pauseAndResume_blocksAndReleasesThreads() throws Exception {
        RaceControl control = new RaceControl();
        AtomicInteger ticks = new AtomicInteger(0);

        assertFalse(control.isPaused());

        Thread worker = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    control.awaitIfPaused();
                    ticks.incrementAndGet();
                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        worker.start();
        TimeUnit.MILLISECONDS.sleep(50);
        int beforePause = ticks.get();

        control.pause();
        assertTrue(control.isPaused());
        TimeUnit.MILLISECONDS.sleep(80);
        int duringPause = ticks.get();
        // There is an unavoidable race: the worker may pass awaitIfPaused() right
        // before pause() is called and still tick once. After that, it must block.
        assertTrue(duringPause <= beforePause + 1, "Ticks must not increase while paused (allowing at most one in-flight tick)");

        control.resume();
        assertFalse(control.isPaused());
        TimeUnit.MILLISECONDS.sleep(50);
        int afterResume = ticks.get();
        assertTrue(afterResume > duringPause, "Ticks must increase after resume");

        worker.interrupt();
        worker.join(500);
        assertFalse(worker.isAlive());
    }

    @Test
    void reset_unpausesAndReleasesWaitingThreads() throws Exception {
        RaceControl control = new RaceControl();
        control.pause();

        AtomicInteger passes = new AtomicInteger(0);

        Thread worker = new Thread(() -> {
            try {
                control.awaitIfPaused();
                passes.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        worker.start();
        TimeUnit.MILLISECONDS.sleep(50);
        assertEquals(0, passes.get(), "Worker must be blocked while paused");

        control.reset();
        worker.join(500);

        assertFalse(worker.isAlive(), "Worker must be released by reset()");
        assertEquals(1, passes.get());
        assertFalse(control.isPaused());
    }
}
