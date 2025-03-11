package com.github.mgrl39.hashscout;

import javafx.application.Platform;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class HashGenerator {

    public String generateHash(File file, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] bytes = Files.readAllBytes(file.toPath());
        byte[] hash = digest.digest(bytes);
        return HexFormat.of().formatHex(hash);
    }
    
    // Método con barra de progreso
    public void generateHashWithProgress(File file, String algorithm, Consumer<String> resultConsumer, Consumer<Double> progressConsumer) {
        AtomicBoolean completed = new AtomicBoolean(false);
        
        // Hilo para simular progreso
        Thread progressThread = new Thread(() -> simulateProgress(progressConsumer, completed));
        progressThread.setDaemon(true);
        progressThread.start();
        
        // Hilo para el cálculo real
        new Thread(() -> {
            try {
                String hash = generateHash(file, algorithm);
                final String result = algorithm + ": " + hash;
                Platform.runLater(() -> resultConsumer.accept(result));
            } catch (Exception e) {
                Platform.runLater(() -> resultConsumer.accept("Error: " + e.getMessage()));
            } finally {
                completed.set(true);
            }
        }).start();
    }
    
    private void simulateProgress(Consumer<Double> progressConsumer, AtomicBoolean completed) {
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
