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
        // Configurar l'estil del terminal
        logTerminal.setStyle(
            "-fx-font-family: 'Consolas', 'Monaco', monospace;" +
            "-fx-font-size: 12px;" +
            "-fx-background-color: #2b2b2b;" +
            "-fx-text-fill: #a9b7c6;"
        );
        logTerminal.setWrapText(true);
        logTerminal.setEditable(false);
        
        // Missatge inicial
        appendLog("HashScout inicialitzat. Preparat per a operacions.");
        
        // Inicialitzar tipus de hash
        hashTypeComboBox.getItems().addAll(
            "MD5",
            "SHA-1",
            "SHA-256",
            "SHA-512"
        );
        hashTypeComboBox.setValue("SHA-256"); // Selecció per defecte
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
            appendLog("ERROR: Si us plau, selecciona un tipus de hash");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un fitxer");
        File file = fileChooser.showOpenDialog(statusLabel.getScene().getWindow());

        if (file != null) {
            try {
                appendLog("Generant hash " + hashTypeComboBox.getValue() + " per a: " + file.getName());
                String hash = generateHash(file, hashTypeComboBox.getValue());
                hashResultArea.setText(hashTypeComboBox.getValue() + ": " + hash);
                appendLog("Hash generat amb èxit: " + hash);
            } catch (Exception e) {
                String errorMsg = "Error generant hash: " + e.getMessage();
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

    // ✅ Cercador de Text (tipus grep)
    @FXML
    protected void onSearchText() {
        if (searchTextField.getText().isEmpty()) {
            appendLog("ERROR: Si us plau, introdueix un terme de cerca");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona una carpeta");
        File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

        if (folder != null && folder.isDirectory()) {
            String searchTerm = searchTextField.getText();
            appendLog("Iniciant cerca per: '" + searchTerm + "' a " + folder.getPath());
            
            try (Stream<Path> files = Files.walk(folder.toPath())) {
                StringBuilder results = new StringBuilder();
                files.filter(Files::isRegularFile)
                     .forEach(path -> {
                         try {
                             if (searchInFile(path, searchTerm)) {
                                 String result = "Trobat a: " + path.toString();
                                 results.append(result).append("\n");
                                 appendLog(result);
                             }
                         } catch (Exception e) {
                             appendLog("ERROR: No es pot llegir " + path + ": " + e.getMessage());
                         }
                     });

                String finalResults = results.toString();
                statusLabel.setText(finalResults.isEmpty() ? "No s'han trobat coincidències" : finalResults);
                appendLog("Cerca completada.");
            } catch (IOException e) {
                String errorMsg = "Error cercant fitxers: " + e.getMessage();
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

    // ✅ Organitzador de Fitxers
    @FXML
    protected void onOrganizeFiles() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona una carpeta");
        File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

        if (folder != null && folder.isDirectory()) {
            appendLog("🔍 Iniciant organització de fitxers a: " + folder.getPath());
            appendLog("----------------------------------------");
            
            try (Stream<Path> files = Files.list(folder.toPath())) {
                // Crear un mapa per comptar fitxers per categoria
                Map<String, Integer> categoryCount = new HashMap<>();
                
                files.forEach(file -> {
                    if (Files.isRegularFile(file)) {
                        try {
                            String extension = getExtension(file.getFileName().toString());
                            String category = getCategoryForExtension(extension);
                            Path targetDir = folder.toPath().resolve(category);
                            
                            // Actualitzar comptador
                            categoryCount.merge(category, 1, Integer::sum);
                            
                            appendLog("📄 Processant: " + file.getFileName());
                            appendLog("   └─ Categoria: " + category + " (" + extension + ")");
                            
                            Files.createDirectories(targetDir);
                            Files.move(file, targetDir.resolve(file.getFileName()), 
                                     StandardCopyOption.REPLACE_EXISTING);
                            
                            appendLog("   └─ ✅ Mogut a: " + targetDir.getFileName());
                        } catch (IOException e) {
                            String errorMsg = "Error movent fitxer " + file + ": " + e.getMessage();
                            statusLabel.setText(errorMsg);
                            appendLog("   └─ ❌ ERROR: " + errorMsg);
                        }
                    }
                });

                // Mostrar resum
                appendLog("----------------------------------------");
                appendLog("📊 Resum de l'organització:");
                categoryCount.forEach((category, count) -> 
                    appendLog("   " + category + ": " + count + " fitxers"));
                appendLog("----------------------------------------");
                
                String summary = categoryCount.entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));
                statusLabel.setText("Fitxers organitzats amb èxit! " + summary);
                
            } catch (IOException e) {
                String errorMsg = "Error organitzant fitxers: " + e.getMessage();
                statusLabel.setText(errorMsg);
                appendLog("❌ ERROR: " + errorMsg);
            }
        }
    }

    private String getCategoryForExtension(String extension) {
        extension = extension.toLowerCase();
        if (extension.matches("zip|rar|7z|tar|gz|bz2")) return "comprimits";
        if (extension.matches("jpg|jpeg|png|gif|bmp|webp|svg")) return "imatges";
        if (extension.matches("mp3|wav|ogg|m4a|flac")) return "audio";
        if (extension.matches("mp4|avi|mkv|mov|wmv")) return "video";
        if (extension.matches("doc|docx|pdf|txt|rtf|odt")) return "documents";
        if (extension.matches("exe|msi|deb|rpm|appimage")) return "executables";
        if (extension.matches("js|ts|java|py|cpp|cs|php|html|css")) return "codi";
        if (extension.matches("json|xml|yaml|yml|ini|conf")) return "config";
        return "altres";
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
        alert.setTitle("Sobre HashScout");
        alert.setHeaderText("HashScout v1.0");
        alert.setContentText("Una eina de gestió de fitxers.\nDesenvolupat per mgrl39 amb JavaFX");
        alert.showAndWait();
    }
}
