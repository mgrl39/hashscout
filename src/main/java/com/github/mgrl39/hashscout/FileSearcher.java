package com.github.mgrl39.hashscout;

import javafx.application.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @class FileSearcher
 * @brief Cercador de text que implementa cerca concurrent i eficient
 *
 * Demostra:
 * - Ús avançat de Streams amb Files.walk
 * - Programació concurrent segura
 * - Gestió eficient de recursos
 * - Patró Observer per resultats
 * - StringBuilder per rendiment
 */
public class FileSearcher {

    private final Logger logger;

    public FileSearcher(Logger logger) {
        this.logger = logger;
    }

    /**
     * @brief Cerca text en fitxers de manera concurrent
     *
     * Implementa:
     * - Streams per recórrer fitxers
     * - Lambdes per processar resultats
     * - Platform.runLater per actualitzacions UI
     * - Try-with-resources per gestió de recursos
     *
     * @param folder Carpeta on cercar
     * @param searchTerm Text a cercar
     * @param resultConsumer Consumer pels resultats
     * @param progressConsumer Consumer pel progrés
     */
    public void searchText(Path folder, String searchTerm, Consumer<String> resultConsumer, Consumer<Double> progressConsumer) {
        AtomicBoolean searchCompleted = new AtomicBoolean(false);

        Thread progressThread = new Thread(() -> ProgressUtils.simulateProgress(progressConsumer, searchCompleted));
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
                        final String results = fileResults.toString();
                        Platform.runLater(() -> resultConsumer.accept(results));
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
}
