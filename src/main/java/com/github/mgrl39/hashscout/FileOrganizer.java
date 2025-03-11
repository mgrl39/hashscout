package com.github.mgrl39.hashscout;

import java.io.IOException;
import java.nio.file.Files;  // Per operacions amb fitxers del sistema
import java.nio.file.Path;   // Representa rutes al sistema de fitxers
import java.util.Map;
import java.util.HashMap;    // Per emmagatzemar comptadors de categories
import java.util.concurrent.atomic.AtomicBoolean;  // Variables atòmiques thread-safe
import java.util.function.Consumer;  // Interfície funcional per callbacks
import java.util.stream.Stream;  // API d'Streams per processar col·leccions

/**
 * @class FileOrganizer
 * @brief Organitzador de fitxers que demostra l'ús avançat de característiques Java modernes
 *
 * Aquesta classe exemplifica:
 * - Ús de Streams per processar fitxers de manera eficient
 * - Expressions regulars per classificar extensions
 * - Lambdes per operacions funcionals
 * - Gestió d'excepcions amb try-catch
 * - Programació concurrent amb threads
 * - Patró Observer amb Consumer<Double>
 *
 * @author mgrl39
 */
public class FileOrganizer {

    /** Logger per registrar events i errors */
    private final Logger logger;

    /**
     * @brief Constructor que injecta dependències
     * 
     * @param logger Sistema de logging per registrar missatges
     */
    public FileOrganizer(Logger logger) {
        this.logger = logger;
    }

    /**
     * @brief Organitza els fitxers d'una carpeta en categories
     *
     * Demostra:
     * - Ús de try-with-resources amb Streams (tanca recursos automàticament)
     * - Lambdes amb files.forEach (processament funcional)
     * - HashMap per comptabilitzar categories (estructura de dades)
     * - Gestió concurrent amb AtomicBoolean (sincronització segura)
     * - Expressions regulars per classificar fitxers (pattern matching)
     *
     * @param folder Carpeta a organitzar
     * @param progressConsumer Consumer per actualitzar el progrés (patró Observer)
     */
    public void organizeFiles(Path folder, Consumer<Double> progressConsumer) {
        /** Flag atòmic per indicar finalització als threads */
        AtomicBoolean completed = new AtomicBoolean(false);
        
        /** Thread per simular progrés de manera visual */
        Thread progressThread = new Thread(() -> ProgressUtils.simulateProgress(progressConsumer, completed));
        progressThread.setDaemon(true);  // Thread daemon (no bloqueja tancament de l'aplicació)
        progressThread.start();
        
        /** Thread principal per organitzar fitxers de manera asíncrona */
        new Thread(() -> {
            /** try-with-resources garanteix tancament correcte de l'Stream */
            try (Stream<Path> files = Files.list(folder)) {  // Llista tots els fitxers de la carpeta
                /** Mapa per comptabilitzar fitxers per categoria */
                Map<String, Integer> categoryCount = new HashMap<>();

                /** Processa cada fitxer amb lambdes (programació funcional) */
                files.forEach(file -> {
                    if (Files.isRegularFile(file)) {  // Comprova que sigui un fitxer regular
                        try {
                            /** Obté l'extensió i determina la categoria */
                            String extension = getExtension(file.getFileName().toString());
                            String category = getCategoryForExtension(extension);
                            Path targetDir = folder.resolve(category);  // Crea la ruta destí

                            /** Crea directoris si no existeixen */
                            Files.createDirectories(targetDir);
                            /** Mou el fitxer a la categoria corresponent */
                            Files.move(file, targetDir.resolve(file.getFileName()));

                            /** Incrementa comptador de categoria (merge és thread-safe) */
                            categoryCount.merge(category, 1, Integer::sum);
                        } catch (IOException e) {
                            /** Gestió d'excepcions específica */
                            logger.log("❌ Error movent fitxer: " + e.getMessage());
                        }
                    }
                });

                /** Registra finalització i estadístiques */
                logger.log("✔️ Organització completa");
                /** Utilitza lambdes per iterar el mapa i mostrar resultats */
                categoryCount.forEach((category, count) ->
                        logger.log("   " + category + ": " + count + " fitxers"));
            } catch (IOException e) {
                /** Gestió d'excepcions global del procés */
                logger.log("❌ Error organitzant: " + e.getMessage());
            } finally {
                /** Assegura que el flag de finalització es marca */
                completed.set(true);
            }
        }).start();  // Inicia el thread d'organització
    }

    /**
     * @brief Obté l'extensió d'un fitxer
     * 
     * Utilitza String.lastIndexOf de manera eficient
     * amb operador ternari per gestionar casos sense extensió
     * 
     * @param fileName Nom del fitxer a analitzar
     * @return String amb l'extensió o "No_Extension" si no en té
     */
    private String getExtension(String fileName) {
        /** Busca l'última ocurrència del punt */
        int lastIndex = fileName.lastIndexOf(".");
        /** Operador ternari per gestionar casos sense extensió */
        return lastIndex == -1 ? "No_Extension" : fileName.substring(lastIndex + 1);
    }

    /**
     * @brief Classifica els fitxers per extensió
     * 
     * Demostra l'ús efectiu d'expressions regulars
     * per categoritzar fitxers de manera flexible i mantenible
     * 
     * @param extension Extensió del fitxer a classificar
     * @return String amb la categoria corresponent
     */
    private String getCategoryForExtension(String extension) {
        /** Expressions regulars per match d'extensió (pattern matching) */
        if (extension.matches("zip|rar|7z|tar|gz|bz2")) return "comprimits";
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp|svg")) return "imatges";
        if (extension.matches("mp3|wav|ogg|m4a|flac")) return "audio";
        if (extension.matches("mp4|avi|mkv|mov|wmv")) return "video";
        if (extension.matches("doc|docx|pdf|txt|rtf|odt")) return "documents";
        if (extension.matches("exe|msi|deb|rpm|appimage")) return "executables";
        if (extension.matches("js|ts|java|py|cpp|cs|php|html|css")) return "codi";
        if (extension.matches("json|xml|yaml|yml|ini|conf")) return "config";
        return "others";  // Categoria per defecte
    }
}
