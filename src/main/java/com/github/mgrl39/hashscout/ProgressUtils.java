package com.github.mgrl39.hashscout;

import javafx.application.Platform;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ProgressUtils {
    
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