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

    // Definir una clase de excepción personalizada
    private class NoFileSelectedException extends Exception {
        public NoFileSelectedException(String message) {
            super(message);
        }
    }

    private File selectedFile = null;

    @FXML
    protected void onSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un fitxer");
        File file = fileChooser.showOpenDialog(statusLabel.getScene().getWindow());

        if (file != null) {
            selectedFile = file;
            appendLog("Fitxer seleccionat: " + file.getName());
        }
    }

    @FXML
    protected void onGenerateHash() {
        try {
            if (hashTypeComboBox.getValue() == null) {
                throw new NoFileSelectedException("No s'ha seleccionat cap tipus de hash");
            }

            if (selectedFile == null) {
                throw new NoFileSelectedException("No s'ha seleccionat cap fitxer");
            }

            appendLog("Generant hash " + hashTypeComboBox.getValue() + " per a: " + selectedFile.getName());
            String hash = generateHash(selectedFile, hashTypeComboBox.getValue());
            hashResultArea.setText(hashTypeComboBox.getValue() + ": " + hash);
            appendLog("Hash generat amb èxit: " + hash);
        } catch (NoFileSelectedException e) {
            // Mostrar un diálogo de alerta
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertència");
            alert.setHeaderText("Error en la generació de hash");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            appendLog("EXCEPCIÓ: " + e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error en la generació de hash");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            String errorMsg = "Error generant hash: " + e.getMessage();
            statusLabel.setText(errorMsg);
            appendLog("EXCEPCIÓ: " + errorMsg);
        }
    }

    private String generateHash(File file, String algorithm) throws Exception {
        MessageDigest digest = MessageDigest.getInstance(algorithm);
        byte[] bytes = Files.readAllBytes(file.toPath());
        byte[] hash = digest.digest(bytes);
        return HexFormat.of().formatHex(hash);
    }

    // Variables para almacenar selecciones
    private File selectedSearchFolder = null;

    @FXML
    protected void onSelectFolderForSearch() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona una carpeta");
        File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

        if (folder != null && folder.isDirectory()) {
            selectedSearchFolder = folder;
            appendLog("Carpeta seleccionada per a cerca: " + folder.getPath());
        }
    }

    @FXML
    protected void onSearchText() {
        try {
            if (searchTextField.getText().isEmpty()) {
                throw new NoFileSelectedException("No s'ha introduït cap terme de cerca");
            }

            if (selectedSearchFolder == null) {
                throw new NoFileSelectedException("No s'ha seleccionat cap carpeta per a la cerca");
            }

            String searchTerm = searchTextField.getText();
            appendLog("Iniciant cerca per: '" + searchTerm + "' a " + selectedSearchFolder.getPath());

            try (Stream<Path> files = Files.walk(selectedSearchFolder.toPath())) {
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
                searchResultArea.setText(finalResults);
                statusLabel.setText(finalResults.isEmpty() ? "No s'han trobat coincidències" : "S'han trobat coincidències");
                appendLog("Cerca completada.");
            }
        } catch (NoFileSelectedException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertència");
            alert.setHeaderText("Error en la cerca");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            appendLog("EXCEPCIÓ: " + e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error en la cerca");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            String errorMsg = "Error cercant fitxers: " + e.getMessage();
            statusLabel.setText(errorMsg);
            appendLog("EXCEPCIÓ: " + errorMsg);
        }
    }

    private boolean searchInFile(Path path, String searchTerm) {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.anyMatch(line -> line.contains(searchTerm));
        } catch (IOException e) {
            return false;
        }
    }

    @FXML
    protected void onOrganizeFiles() {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Selecciona una carpeta");
            File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

            if (folder == null || !folder.isDirectory()) {
                throw new NoFileSelectedException("No s'ha seleccionat cap carpeta per organitzar");
            }

            appendLog("🔍 Iniciant organització de fitxers a: " + folder.getPath());
            appendLog("----------------------------------------");

            try (Stream<Path> files = Files.list(folder.toPath())) {
                Map<String, Integer> categoryCount = new HashMap<>();

                files.forEach(file -> {
                    if (Files.isRegularFile(file)) {
                        try {
                            String extension = getExtension(file.getFileName().toString());
                            String category = getCategoryForExtension(extension);
                            Path targetDir = folder.toPath().resolve(category);

                            categoryCount.merge(category, 1, Integer::sum);

                            appendLog("📄 Processant: " + file.getFileName());
                            appendLog("   └─ Categoria: " + category + " (" + extension + ")");

                            Files.createDirectories(targetDir);
                            Files.move(file, targetDir.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);

                            appendLog("   └─ ✅ Mogut a: " + targetDir.getFileName());
                        } catch (IOException e) {
                            String errorMsg = "Error movent fitxer " + file + ": " + e.getMessage();
                            statusLabel.setText(errorMsg);
                            appendLog("   └─ ❌ ERROR: " + errorMsg);
                        }
                    }
                });

                appendLog("----------------------------------------");
                appendLog("📊 Resum de l'organització:");
                categoryCount.forEach((category, count) ->
                    appendLog("   " + category + ": " + count + " fitxers"));
                appendLog("----------------------------------------");

                String summary = categoryCount.entrySet().stream()
                    .map(e -> e.getKey() + ": " + e.getValue())
                    .collect(Collectors.joining(", "));
                statusLabel.setText("Fitxers organitzats amb èxit! " + summary);
            }
        } catch (NoFileSelectedException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertència");
            alert.setHeaderText("Error en l'organització");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            appendLog("EXCEPCIÓ: " + e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error en l'organització");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            String errorMsg = "Error organitzant fitxers: " + e.getMessage();
            statusLabel.setText(errorMsg);
            appendLog("EXCEPCIÓ: " + errorMsg);
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
