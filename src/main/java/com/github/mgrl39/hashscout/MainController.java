package com.github.mgrl39.hashscout;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import org.kordamp.bootstrapfx.BootstrapFX;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class MainController {

    @FXML
    private Label statusLabel;
    
    @FXML
    private TextArea logTerminal;
    
    @FXML
    private TextField searchTextField;

    @FXML
    private TextArea hashResultArea;

    @FXML
    private TextArea searchResultArea;

    @FXML
    private ComboBox<String> hashTypeComboBox;

    @FXML
    public void initialize() {
        // Configurar el estilo de la terminal
        logTerminal.setStyle(
            "-fx-font-family: 'Consolas', 'Monaco', monospace;" +
            "-fx-font-size: 12px;" +
            "-fx-background-color: #2b2b2b;" +
            "-fx-text-fill: #a9b7c6;"
        );
        logTerminal.setWrapText(true);
        logTerminal.setEditable(false);
        
        // Mensaje inicial
        appendLog("HashScout initialized. Ready for operations.");
        
        // Initialize hash types
        hashTypeComboBox.getItems().addAll(
            "MD5",
            "SHA-1",
            "SHA-256",
            "SHA-512"
        );
        hashTypeComboBox.setValue("SHA-256"); // Default selection
    }

    private void appendLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        Platform.runLater(() -> {
            logTerminal.appendText(String.format("[%s] %s%n", timestamp, message));
            logTerminal.setScrollTop(Double.MAX_VALUE); // Auto-scroll to bottom
        });
    }

    // ✅ Generador de Hash
    @FXML
    protected void onGenerateHash() {
        if (hashTypeComboBox.getValue() == null) {
            appendLog("ERROR: Please select a hash type");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        File file = fileChooser.showOpenDialog(statusLabel.getScene().getWindow());

        if (file != null) {
            try {
                appendLog("Generating " + hashTypeComboBox.getValue() + " hash for: " + file.getName());
                String hash = generateHash(file, hashTypeComboBox.getValue());
                hashResultArea.setText(hashTypeComboBox.getValue() + ": " + hash);
                appendLog("Hash generated successfully: " + hash);
            } catch (Exception e) {
                String errorMsg = "Error generating hash: " + e.getMessage();
                statusLabel.setText(errorMsg);
                appendLog("ERROR: " + errorMsg);
            }
        }
    }

    private String generateHash(File file, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] bytes = Files.readAllBytes(file.toPath());
        byte[] hash = digest.digest(bytes);
        return HexFormat.of().formatHex(hash);
    }

    // ✅ Buscador de Texto (tipo grep)
    @FXML
    protected void onSearchText() {
        if (searchTextField.getText().isEmpty()) {
            appendLog("ERROR: Please enter a search term");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona una carpeta");
        File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

        if (folder != null && folder.isDirectory()) {
            String searchTerm = searchTextField.getText();
            appendLog("Starting search for: '" + searchTerm + "' in " + folder.getPath());
            
            try (Stream<Path> files = Files.walk(folder.toPath())) {
                StringBuilder results = new StringBuilder();
                files.filter(Files::isRegularFile)
                     .forEach(path -> {
                         try {
                             if (searchInFile(path, searchTerm)) {
                                 String result = "Found in: " + path.toString();
                                 results.append(result).append("\n");
                                 appendLog(result);
                             }
                         } catch (Exception e) {
                             appendLog("ERROR: Cannot read " + path + ": " + e.getMessage());
                         }
                     });

                String finalResults = results.toString();
                statusLabel.setText(finalResults.isEmpty() ? "No matches found" : finalResults);
                appendLog("Search completed.");
            } catch (IOException e) {
                String errorMsg = "Error searching files: " + e.getMessage();
                statusLabel.setText(errorMsg);
                appendLog("ERROR: " + errorMsg);
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
            appendLog("🔍 Starting file organization in: " + folder.getPath());
            appendLog("----------------------------------------");
            
            try (Stream<Path> files = Files.list(folder.toPath())) {
                // Crear un mapa para contar archivos por categoría
                Map<String, Integer> categoryCount = new HashMap<>();
                
                files.forEach(file -> {
                    if (Files.isRegularFile(file)) {
                        try {
                            String extension = getExtension(file.getFileName().toString());
                            String category = getCategoryForExtension(extension);
                            Path targetDir = folder.toPath().resolve(category);
                            
                            // Actualizar contador
                            categoryCount.merge(category, 1, Integer::sum);
                            
                            appendLog("📄 Processing: " + file.getFileName());
                            appendLog("   └─ Category: " + category + " (" + extension + ")");
                            
                            Files.createDirectories(targetDir);
                            Files.move(file, targetDir.resolve(file.getFileName()), 
                                     StandardCopyOption.REPLACE_EXISTING);
                            
                            appendLog("   └─ ✅ Moved to: " + targetDir.getFileName());
                        } catch (IOException e) {
                            String errorMsg = "Error moving file " + file + ": " + e.getMessage();
                            statusLabel.setText(errorMsg);
                            appendLog("   └─ ❌ ERROR: " + errorMsg);
                        }
                    }
                });

                // Mostrar resumen
                appendLog("----------------------------------------");
                appendLog("📊 Organization Summary:");
                categoryCount.forEach((category, count) -> 
                    appendLog("   " + category + ": " + count + " files"));
                appendLog("----------------------------------------");
                
                String summary = categoryCount.entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));
                statusLabel.setText("Files organized successfully! " + summary);
                
            } catch (IOException e) {
                String errorMsg = "Error organizing files: " + e.getMessage();
                statusLabel.setText(errorMsg);
                appendLog("❌ ERROR: " + errorMsg);
            }
        }
    }

    private String getCategoryForExtension(String extension) {
        extension = extension.toLowerCase();
        if (extension.matches("zip|rar|7z|tar|gz|bz2")) return "Comprimidos";
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp|svg")) return "Imágenes";
        if (extension.matches("mp3|wav|ogg|m4a|flac")) return "Audio";
        if (extension.matches("mp4|avi|mkv|mov|wmv")) return "Video";
        if (extension.matches("doc|docx|pdf|txt|rtf|odt")) return "Documentos";
        if (extension.matches("exe|msi|deb|rpm|appimage")) return "Ejecutables";
        if (extension.matches("js|ts|java|py|cpp|cs|php|html|css")) return "Código";
        if (extension.matches("json|xml|yaml|yml|ini|conf")) return "Configuración";
        return "Otros";
    }

    private String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf(".");
        return lastIndex == -1 ? "No_Extension" : fileName.substring(lastIndex + 1);
    }

    @FXML
    protected void handleExit() {
        Platform.exit();
    }

    @FXML
    protected void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About HashScout");
        alert.setHeaderText("HashScout v1.0");
        alert.setContentText("A file management utility tool.\nDeveloped by MGRL39");
        alert.showAndWait();
    }
}
