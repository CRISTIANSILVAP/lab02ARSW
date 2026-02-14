package edu.eci.arsw.dogsrace.app;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import edu.eci.arsw.dogsrace.control.RaceControl;
import edu.eci.arsw.dogsrace.domain.ArrivalCounter;
import edu.eci.arsw.dogsrace.domain.ArrivalRegistry;
import edu.eci.arsw.dogsrace.domain.AtomicArrivalCounter;
import edu.eci.arsw.dogsrace.domain.SynchronizedArrivalCounter;
import edu.eci.arsw.dogsrace.domain.SynchronizedAtomicArrivalCounter;
import edu.eci.arsw.dogsrace.threads.Galgo;
import edu.eci.arsw.dogsrace.ui.Canodromo;

/**
 * Entry point (UI + orchestration).
 * 
 * This orchestrator supports 3 synchronization strategies:
 * 1. SYNCHRONIZED - classical monitor-based synchronization
 * 2. ATOMIC - lock-free using AtomicInteger
 * 3. SYNCHRONIZED_ATOMIC - hybrid approach
 *
 * NOTE: the start action runs in a separate thread so the Swing UI thread is not blocked.
 */
public final class MainCanodromo {

    /**
     * Enum for selecting synchronization strategy
     */
    private enum SynchronizationStrategy {
        SYNCHRONIZED("Synchronized (Monitor-based)", 
            () -> new SynchronizedArrivalCounter()),
        ATOMIC("Atomic (Lock-free)", 
            () -> new AtomicArrivalCounter()),
        SYNCHRONIZED_ATOMIC("Synchronized + Atomic (Hybrid)", 
            () -> new SynchronizedAtomicArrivalCounter());

        private final String description;
        private final CounterSupplier supplier;

        SynchronizationStrategy(String description, CounterSupplier supplier) {
            this.description = description;
            this.supplier = supplier;
        }

        public ArrivalCounter createCounter() {
            return supplier.create();
        }

        @FunctionalInterface
        interface CounterSupplier {
            ArrivalCounter create();
        }
    }

    // ===== CHANGE THIS TO SELECT STRATEGY =====
    private static final SynchronizationStrategy SELECTED_STRATEGY = SynchronizationStrategy.SYNCHRONIZED_ATOMIC;
    // ==========================================

    private static Galgo[] galgos;
    private static Canodromo can;
    private static ArrivalRegistry registry;
    private static final RaceControl control = new RaceControl();

    public static void main(String[] args) {
        // Initialize registry with selected strategy
        ArrivalCounter counter = SELECTED_STRATEGY.createCounter();
        registry = new ArrivalRegistry(counter);

        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║     CARRERA DE GALGOS - SINCRONIZACIÓN COMPARATIVA     ║");
        System.out.println("╠════════════════════════════════════════════════════════╣");
        System.out.println("║ Estrategia: " + SELECTED_STRATEGY.description);
        System.out.println("╚════════════════════════════════════════════════════════╝\n");

        // Configuración: puedes cambiar el número de galgos aquí
        int numGalgos = 10;  // Cambia este valor para probar con más o menos galgos
        int longitudPista = 20;
        
        System.out.println("Configuración:");
        System.out.println("  - Número de galgos: " + numGalgos);
        System.out.println("  - Longitud de pista: " + longitudPista);
        System.out.println();

        can = new Canodromo(numGalgos, longitudPista);
        galgos = new Galgo[can.getNumCarriles()];
        can.setVisible(true);

        can.setStartAction(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ((JButton) e.getSource()).setEnabled(false);

                new Thread(() -> {
                    long raceStartTime = System.currentTimeMillis();
                    
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

                    long raceEndTime = System.currentTimeMillis();
                    long totalRaceTime = raceEndTime - raceStartTime;

                    // 3) show results ONLY after all threads finished
                    var ranking = registry.getRanking();
                    String winner = registry.getWinner();

                    can.winnerDialog(winner, ranking.size());

                    System.out.println("\n╔════════════════════════════════════════════════════════╗");
                    System.out.println("║                    RESULTADOS FINALES                   ║");
                    System.out.println("╠════════════════════════════════════════════════════════╣");
                    System.out.println("║ Estrategia: " + SELECTED_STRATEGY.description);
                    System.out.println("║ Tiempo total de carrera: " + totalRaceTime + " ms");
                    System.out.println("║ Total de participantes: " + ranking.size());
                    System.out.println("║ Ganador: " + winner);
                    System.out.println("╠════════════════════════════════════════════════════════╣");

                    System.out.println("║                   RANKING FINAL                        ║");
                    System.out.println("╠════════════════════════════════════════════════════════╣");
                    for (int pos = 0; pos < ranking.size(); pos++) {
                        System.out.printf("║ %2d) Galgo %s %-40s ║%n", pos + 1, ranking.get(pos), "");
                    }
                    System.out.println("╚════════════════════════════════════════════════════════╝");

                    // Print performance metrics
                    var metrics = registry.getMetrics();
                    long elapsedNanos = metrics.endTime() - metrics.startTime();
                    double elapsedMillis = elapsedNanos / 1_000_000.0;

                    System.out.println("\n╔════════════════════════════════════════════════════════╗");
                    System.out.println("║              MÉTRICAS DE SINCRONIZACIÓN                ║");
                    System.out.println("╠════════════════════════════════════════════════════════╣");
                    System.out.println("║ Estrategia: " + metrics.strategyName());
                    System.out.println("║ Tiempo de contador: " + String.format("%.3f ms", elapsedMillis));
                    System.out.println("║ Total de llegadas: " + metrics.totalArrivals());
                    System.out.println("║ Tiempo espera total: " + String.format("%.3f µs", metrics.totalWaitTime() / 1000.0));
                    System.out.println("║ Max espera en lock: " + String.format("%.3f µs", metrics.maxWaitTime() / 1000.0));
                    System.out.println("║ Promedio espera: " + 
                        String.format("%.3f µs", (metrics.totalWaitTime() / (double) Math.max(1, metrics.totalArrivals())) / 1000.0));
                    System.out.println("╚════════════════════════════════════════════════════════╝\n");
                }, "race-orchestrator").start();
            }
        });

        can.setStopAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                control.pause();
                System.out.println("⏸️  Carrera pausada!");
            }
        });

        can.setContinueAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                control.resume();
                System.out.println("▶️  Carrera reanudada!");
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

                System.out.println("🔄 Carrera reiniciada correctamente\n");
            }
        });



    }
}
