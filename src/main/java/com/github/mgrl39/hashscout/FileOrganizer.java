package com.github.mgrl39.hashscout;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;

public class FileOrganizer {

    private final Logger logger;

    public FileOrganizer(Logger logger) {
        this.logger = logger;
    }

    public void organizeFiles(Path folder) {
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
        }
    }

    private String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        return lastIndex == -1 ? "No_Extension" : fileName.substring(lastIndex + 1);
    }

    private String getCategoryForExtension(String extension) {
        if (extension.matches("jpg|jpeg|png")) return "images";
        if (extension.matches("mp4|avi|mkv")) return "videos";
        if (extension.matches("txt|pdf|doc")) return "documents";
        return "others";
    }
}
