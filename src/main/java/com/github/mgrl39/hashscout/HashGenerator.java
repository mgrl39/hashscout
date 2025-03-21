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
    
    public void generateHashWithProgress(File file, String algorithm, Consumer<String> resultConsumer, Consumer<Double> progressConsumer) {
        AtomicBoolean completed = new AtomicBoolean(false);
        
        Thread progressThread = new Thread(() -> ProgressUtils.simulateProgress(progressConsumer, completed));
        progressThread.setDaemon(true);
        progressThread.start();
        
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
}
