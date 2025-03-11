package com.github.mgrl39.hashscout;

import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @class ProgressUtils
 * @brief Utilitat per simular i gestionar barres de progrés de manera concurrent
 *
 * Aquesta classe demostra:
 * - Ús de AtomicBoolean per sincronització
 * - Lambdes amb Consumer
 * - Platform.runLater per UI thread-safe
 * - Gestió de threads daemon
 *
 * @author mgrl39
 */
public class ProgressUtils {
    
    /**
     * @brief Simula el progrés d'una operació de manera suau
     *
     * Implementa:
     * - Control concurrent amb AtomicBoolean
     * - Actualitzacions UI thread-safe
     * - Gestió d'interrupcions
     * - Increment gradual del progrés
     *
     * @param progressConsumer Consumer per actualitzar la UI
     * @param completed Flag de finalització
     */
    public static void simulateProgress(Consumer<Double> progressConsumer, AtomicBoolean completed) {
        try {
            double progress = 0;
            while (!completed.get() && progress < 1.0) {
                progress += 0.05;
                final double currentProgress = progress;
                Platform.runLater(() -> progressConsumer.accept(currentProgress));
                Thread.sleep(100);
            }
            Platform.runLater(() -> progressConsumer.accept(1.0));
        } catch (InterruptedException ignored) {}
    }
} 