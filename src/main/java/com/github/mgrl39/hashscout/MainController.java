package com.github.mgrl39.hashscout;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MainController {

    @FXML
    private Label statusLabel;

    // ✅ Generador de Hash
    @FXML
    protected void onGenerateHash() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un fitxer");
        File file = fileChooser.showOpenDialog(statusLabel.getScene().getWindow());

        if (file != null) {
            try {
                String hash = generateSHA256(file);
                statusLabel.setText("SHA-256: " + hash);
            } catch (Exception e) {
                statusLabel.setText("Error en generar el hash: " + e.getMessage());
            }
        }
    }

    private String generateSHA256(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = Files.readAllBytes(file.toPath());
        byte[] hash = digest.digest(bytes);

        // ✅ Corregido: Convertir bytes a hex correctamente
        return HexFormat.of().formatHex(hash);
    }

    // ✅ Buscador de Texto (tipo grep)
    @FXML
    protected void onSearchText() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona una carpeta");
        File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

        if (folder != null && folder.isDirectory()) {
            String searchTerm = "TODO"; // Para hacerlo dinámico, añade un campo de texto
            try (Stream<Path> files = Files.walk(folder.toPath())) {
                String result = files
                        .filter(Files::isRegularFile)
                        .filter(path -> searchInFile(path, searchTerm))
                        .map(Path::toString)
                        .collect(Collectors.joining("\n"));

                statusLabel.setText(result.isEmpty() ? "No s'han trobat coincidències" : result);
            } catch (IOException e) {
                statusLabel.setText("Error en buscar el text: " + e.getMessage());
            }
        }
    }

    private boolean searchInFile(Path path, String searchTerm) {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.anyMatch(line -> line.contains(searchTerm));
        } catch (IOException e) {
            return false;
        }
    }

    // ✅ Organizador de Archivos
    @FXML
    protected void onOrganizeFiles() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona una carpeta");
        File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

        if (folder != null && folder.isDirectory()) {
            try (Stream<Path> files = Files.list(folder.toPath())) {
                files.forEach(file -> {
                    if (Files.isRegularFile(file)) {
                        try {
                            String extension = getExtension(file.getFileName().toString());
                            Path targetDir = folder.toPath().resolve(extension);

                            // ✅ Crear directorio si no existe
                            Files.createDirectories(targetDir);

                            // ✅ Mover archivo
                            Files.move(file, targetDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            statusLabel.setText("Error en moure fitxers: " + e.getMessage());
                        }
                    }
                });

                statusLabel.setText("Arxius organitzats correctament!");
            } catch (IOException e) {
                statusLabel.setText("Error en organitzar els arxius: " + e.getMessage());
            }
        }
    }

    private String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        return lastIndex == -1 ? "Sense_Extensió" : fileName.substring(lastIndex + 1);
    }
}
