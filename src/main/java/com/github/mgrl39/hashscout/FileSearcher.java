package com.github.mgrl39.hashscout;

import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSearcher {

    private final Logger logger;

    public FileSearcher(Logger logger) {
        this.logger = logger;
    }

    public void searchText(Path folder, String searchTerm, TextArea resultArea, ProgressBar progressBar) {
        AtomicBoolean searchCompleted = new AtomicBoolean(false);

        Thread progressThread = new Thread(() -> simulateProgress(progressBar, searchCompleted));
        progressThread.setDaemon(true);
        progressThread.start();

        new Thread(() -> {
            try (Stream<Path> files = Files.walk(folder).filter(Files::isRegularFile).limit(100)) {
                List<Path> fileList = files.collect(Collectors.toList());
                int matchingFiles = 0;

                for (Path file : fileList) {
                    List<String> lines = Files.readAllLines(file);
                    StringBuilder fileResults = new StringBuilder();
                    boolean foundMatch = false;

                    for (int i = 0; i < lines.size(); i++) {
                        if (lines.get(i).contains(searchTerm)) {
                            if (!foundMatch) {
                                fileResults.append("\n📄 ").append(file.getFileName()).append(":\n");
                                foundMatch = true;
                            }
                            fileResults.append("   Línia ").append(i + 1).append(": ")
                                       .append(lines.get(i).trim()).append("\n");
                        }
                    }

                    if (foundMatch) {
                        matchingFiles++;
                        Platform.runLater(() -> resultArea.appendText(fileResults.toString()));
                    }
                }

                logger.log("🔎 Cerca completa: " + matchingFiles + " coincidències trobades");
            } catch (IOException e) {
                logger.log("❌ Error durant la cerca: " + e.getMessage());
            } finally {
                searchCompleted.set(true);
            }
        }).start();
    }

    private void simulateProgress(ProgressBar progressBar, AtomicBoolean completed) {
        try {
            double progress = 0;
            while (!completed.get() && progress < 1.0) {
                progress += 0.05;
                final double currentProgress = progress;
                Platform.runLater(() -> progressBar.setProgress(currentProgress));
                Thread.sleep(100);
            }
            Platform.runLater(() -> progressBar.setProgress(1.0));
        } catch (InterruptedException ignored) {}
    }
}
