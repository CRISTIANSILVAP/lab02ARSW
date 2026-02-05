package edu.eci.arsw.dogsrace.app;

import edu.eci.arsw.dogsrace.control.RaceControl;
import edu.eci.arsw.dogsrace.domain.ArrivalRegistry;
import edu.eci.arsw.dogsrace.threads.Galgo;
import edu.eci.arsw.dogsrace.ui.Canodromo;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Entry point (UI + orchestration).
 *
 * NOTE: the start action runs in a separate thread so the Swing UI thread is not blocked.
 */
public final class MainCanodromo {

    private static Galgo[] galgos;
    private static Canodromo can;

    private static final ArrivalRegistry registry = new ArrivalRegistry();
    private static final RaceControl control = new RaceControl();

    public static void main(String[] args) {
        can = new Canodromo(10, 20);
        galgos = new Galgo[can.getNumCarriles()];
        can.setVisible(true);

        can.setStartAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ((JButton) e.getSource()).setEnabled(false);

                new Thread(() -> {
                    // 1) create and start all runners
                    for (int i = 0; i < can.getNumCarriles(); i++) {
                        galgos[i] = new Galgo(can.getCarril(i), String.valueOf(i), registry, control);
                        galgos[i].start();
                    }

                    // 2) wait for all threads (join)
                    for (Galgo g : galgos) {
                        try {
                            g.join();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    // 3) show results ONLY after all threads finished
                    String winner = registry.getWinner();
                    int total = registry.getNextPosition() - 1;

                    can.winnerDialog(winner, total);
                    System.out.println("El ganador fue: " + winner);
                }, "race-orchestrator").start();
            }
        });

        can.setStopAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                control.pause();
                System.out.println("Carrera pausada!");
            }
        });

        can.setContinueAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                control.resume();
                System.out.println("Carrera reanudada!");
            }
        });

        can.SetRestartAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                // 1. Pausar carrera actual
                control.pause();

                // 2. Resetear lógica
                registry.reset();
                control.reset();

                // 3. Limpiar UI
                can.restart();

                // 4. Habilitar Start nuevamente
                can.enableStart(true);

                System.out.println("Carrera reiniciada correctamente");
            }
        });



    }
}
