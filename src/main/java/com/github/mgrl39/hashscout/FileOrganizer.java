package com.github.mgrl39.hashscout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class FileOrganizer {

    private final Logger logger;

    public FileOrganizer(Logger logger) {
        this.logger = logger;
    }

    public void organizeFiles(Path folder, Consumer<Double> progressConsumer) {
        AtomicBoolean completed = new AtomicBoolean(false);
        
        Thread progressThread = new Thread(() -> ProgressUtils.simulateProgress(progressConsumer, completed));
        progressThread.setDaemon(true);
        progressThread.start();
        
        new Thread(() -> {
            try (Stream<Path> files = Files.list(folder)) {
                Map<String, Integer> categoryCount = new HashMap<>();

                files.forEach(file -> {
                    if (Files.isRegularFile(file)) {
                        try {
                            String extension = getExtension(file.getFileName().toString());
                            String category = getCategoryForExtension(extension);
                            Path targetDir = folder.resolve(category);

                            Files.createDirectories(targetDir);
                            Files.move(file, targetDir.resolve(file.getFileName()));

                            categoryCount.merge(category, 1, Integer::sum);
                        } catch (IOException e) {
                            logger.log("❌ Error movent fitxer: " + e.getMessage());
                        }
                    }
                });

                logger.log("✔️ Organització completa");
                categoryCount.forEach((category, count) ->
                        logger.log("   " + category + ": " + count + " fitxers"));
            } catch (IOException e) {
                logger.log("❌ Error organitzant: " + e.getMessage());
            } finally {
                completed.set(true);
            }
        }).start();
    }

    private String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        return lastIndex == -1 ? "No_Extension" : fileName.substring(lastIndex + 1);
    }

    private String getCategoryForExtension(String extension) {
        if (extension.matches("zip|rar|7z|tar|gz|bz2")) return "comprimits";
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp|svg")) return "imatges";
        if (extension.matches("mp3|wav|ogg|m4a|flac")) return "audio";
        if (extension.matches("mp4|avi|mkv|mov|wmv")) return "video";
        if (extension.matches("doc|docx|pdf|txt|rtf|odt")) return "documents";
        if (extension.matches("exe|msi|deb|rpm|appimage")) return "executables";
        if (extension.matches("js|ts|java|py|cpp|cs|php|html|css")) return "codi";
        if (extension.matches("json|xml|yaml|yml|ini|conf")) return "config";
        return "others";
    }
}
