package com.github.mgrl39.hashscout.utils;

import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ProgressUtils {
    
    /**
     * Simulates progress for long-running operations by incrementing progress value at regular intervals.
     * @param progressConsumer Consumer to receive progress updates
     * @param completed AtomicBoolean indicating if the operation has completed
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