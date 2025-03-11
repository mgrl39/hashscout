package com.github.mgrl39.hashscout;

import javafx.application.Platform;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @class HashGenerator
 * @brief Generador de hashes que implementa múltiples algorismes de manera segura i eficient
 *
 * Aquesta classe demostra:
 * - Ús de MessageDigest per generació segura de hashes
 * - Programació concurrent amb threads
 * - Gestió d'excepcions robusta
 * - Patró Observer per actualitzacions de progrés
 * - Interfície fluida amb Consumers
 *
 * @author mgrl39
 */
public class HashGenerator {

    /**
     * @brief Genera el hash d'un fitxer utilitzant l'algorisme especificat
     *
     * Demostra:
     * - Ús segur de MessageDigest
     * - Gestió eficient de fitxers amb Files
     * - Conversió hexadecimal optimitzada
     *
     * @param file Fitxer a processar
     * @param algorithm Algorisme de hash (MD5, SHA-1, SHA-256)
     * @return String Hash en format hexadecimal
     * @throws Exception Si hi ha errors en la lectura o el hash
     */
    public String generateHash(File file, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] bytes = Files.readAllBytes(file.toPath());
        byte[] hash = digest.digest(bytes);
        return HexFormat.of().formatHex(hash);
    }
    
    /**
     * @brief Genera hash de manera asíncrona amb indicador de progrés
     *
     * Implementa:
     * - Threads per operacions asíncrones
     * - Lambdes per callbacks
     * - Platform.runLater per actualitzacions UI
     * - Gestió d'errors amb try-catch
     *
     * @param file Fitxer a processar
     * @param algorithm Algorisme seleccionat
     * @param resultConsumer Consumer pel resultat
     * @param progressConsumer Consumer pel progrés
     */
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
