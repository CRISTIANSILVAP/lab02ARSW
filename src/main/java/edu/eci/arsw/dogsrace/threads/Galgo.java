package edu.eci.arsw.dogsrace.threads;

import edu.eci.arsw.dogsrace.control.RaceControl;
import edu.eci.arsw.dogsrace.domain.ArrivalRegistry;
import edu.eci.arsw.dogsrace.ui.Carril;

/**
 * A runner (greyhound) in the race.
 */
public class Galgo extends Thread {

    private final Carril carril;
    private final ArrivalRegistry registry;
    private final RaceControl control;

    private int paso = 0;

    public Galgo(Carril carril, String name, ArrivalRegistry registry, RaceControl control) {
        super(name);
        this.carril = carril;
        this.registry = registry;
        this.control = control;
    }

    private void corra() throws InterruptedException {
        while (paso < carril.size()) {
            control.awaitIfPaused();
            Thread.sleep(100);

            // If Stop was pressed during sleep, honor it before mutating UI/state.
            control.awaitIfPaused();
            carril.setPasoOn(paso++);
            carril.displayPasos(paso);

            if (paso == carril.size()) {
                carril.finish();
                registry.registerArrival(getName());
            }
        }
    }

    @Override
    public void run() {
        try {
            corra();
        } catch (InterruptedException e) {
            // Restore interruption status and exit
            Thread.currentThread().interrupt();
        }
    }
}
