package com.github.mgrl39.hashscout;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @class Logger
 * @brief Sistema de logging que implementa registre concurrent i formatat
 *
 * Aquesta classe demostra:
 * - Ús de DateTimeFormatter per timestamps
 * - Actualitzacions UI thread-safe
 * - Gestió eficient de TextArea
 * - Format consistent de missatges
 *
 * @author mgrl39
 */
public class Logger {

    private final TextArea logTerminal;

    public Logger(TextArea logTerminal) {
        this.logTerminal = logTerminal;
    }

    /**
     * @brief Registra un missatge amb timestamp al terminal
     *
     * Implementa:
     * - Format consistent de data/hora
     * - Platform.runLater per thread safety
     * - Auto-scroll del terminal
     * - Format visual amb emojis
     *
     * @param message Missatge a registrar
     */
    public void log(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            logTerminal.appendText(String.format("[%s] %s%n", timestamp, message));
            logTerminal.setScrollTop(Double.MAX_VALUE);
        });
    }
}
