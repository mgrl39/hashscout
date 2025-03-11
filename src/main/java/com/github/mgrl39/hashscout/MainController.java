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
import java.util.List;
import java.lang.StringBuilder;

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
    private ProgressBar hashProgressBar;

    @FXML
    private ProgressBar searchProgressBar;

    @FXML
    private ProgressBar organizeProgressBar;

    @FXML
    private Label selectedFileLabel;

    @FXML
    private Label selectedSearchFolderLabel;

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
        
        // Inicializar barras de progreso
        hashProgressBar.setProgress(0);
        searchProgressBar.setProgress(0);
        organizeProgressBar.setProgress(0);
        
        // Inicializar etiquetas de selección
        selectedFileLabel.setText("Ninguno");
        selectedSearchFolderLabel.setText("Ninguna");
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
            // Actualizar la etiqueta con el nombre del archivo seleccionado
            selectedFileLabel.setText(file.getName());
            appendLog("Fitxer seleccionat: " + file.getName());
        }
    }

    @FXML
    protected void onGenerateHash() {
        try {
            // Resetear barra de progreso
            Platform.runLater(() -> hashProgressBar.setProgress(0));
            
            if (hashTypeComboBox.getValue() == null) {
                throw new NoFileSelectedException("No s'ha seleccionat cap tipus de hash");
            }

            if (selectedFile == null) {
                throw new NoFileSelectedException("No s'ha seleccionat cap fitxer");
            }

            appendLog("Generant hash " + hashTypeComboBox.getValue() + " per a: " + selectedFile.getName());
            
            // Mostrar progreso simulado
            final boolean[] operationCompleted = {false};
            Thread progressThread = new Thread(() -> {
                try {
                    double progress = 0.0;
                    while (!operationCompleted[0] && progress < 0.9) {
                        progress += 0.1;
                        final double currentProgress = progress;
                        Platform.runLater(() -> hashProgressBar.setProgress(currentProgress));
                        Thread.sleep(100);
                    }
                    // Asegurarse de que llegue al 100% cuando la operación termine
                    while (!operationCompleted[0]) {
                        Thread.sleep(50);
                    }
                    Platform.runLater(() -> hashProgressBar.setProgress(1.0));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            progressThread.setDaemon(true);
            progressThread.start();
            
            String hash = generateHash(selectedFile, hashTypeComboBox.getValue());
            hashResultArea.setText(hashTypeComboBox.getValue() + ": " + hash);
            appendLog("Hash generat amb èxit: " + hash);
            
            // Marcar la operación como completada para actualizar la barra
            operationCompleted[0] = true;
            // Asegurarnos de que la barra llegue al 100%
            Platform.runLater(() -> hashProgressBar.setProgress(1.0));
            
        } catch (NoFileSelectedException e) {
            // Mostrar un diálogo de alerta
            Platform.runLater(() -> hashProgressBar.setProgress(0));
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertència");
            alert.setHeaderText("Error en la generació de hash");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            appendLog("EXCEPCIÓ: " + e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            Platform.runLater(() -> hashProgressBar.setProgress(0));
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
            // Actualizar la etiqueta con la ruta de la carpeta seleccionada
            selectedSearchFolderLabel.setText(folder.getPath());
            appendLog("Carpeta seleccionada per a cerca: " + folder.getPath());
        }
    }

    @FXML
    protected void onSearchText() {
        try {
            // Resetear barra de progreso
            Platform.runLater(() -> searchProgressBar.setProgress(0));
            
            if (searchTextField.getText().isEmpty()) {
                throw new NoFileSelectedException("No s'ha introduït cap text de cerca");
            }

            if (selectedSearchFolder == null) {
                throw new NoFileSelectedException("No s'ha seleccionat cap carpeta");
            }

            String searchTerm = searchTextField.getText();
            File folder = selectedSearchFolder;
            searchResultArea.clear();
            
            appendLog("Cercant \"" + searchTerm + "\" a: " + folder.getPath());
            
            // Mostrar progreso simulado
            final boolean[] searchCompleted = {false};
            Thread progressThread = new Thread(() -> {
                try {
                    double progress = 0.0;
                    while (!searchCompleted[0] && progress < 0.9) {
                        progress += 0.05;
                        final double currentProgress = progress;
                        Platform.runLater(() -> searchProgressBar.setProgress(currentProgress));
                        Thread.sleep(100);
                    }
                    while (!searchCompleted[0]) {
                        Thread.sleep(50);
                    }
                    Platform.runLater(() -> searchProgressBar.setProgress(1.0));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            progressThread.setDaemon(true);
            progressThread.start();
            
            // Realizar la búsqueda
            final int[] totalFiles = {0};
            final int[] matchingFiles = {0};
            
            Platform.runLater(() -> {
                try {
                    Files.walk(folder.toPath())
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            totalFiles[0]++;
                            try {
                                List<String> lines = Files.readAllLines(file);
                                boolean fileHasMatch = false;
                                StringBuilder fileResults = new StringBuilder();
                                
                                for (int i = 0; i < lines.size(); i++) {
                                    String line = lines.get(i);
                                    if (line.contains(searchTerm)) {
                                        if (!fileHasMatch) {
                                            fileResults.append("📄 ").append(file.getFileName()).append(":\n");
                                            fileHasMatch = true;
                                        }
                                        // Añadir número de línea y contexto
                                        fileResults.append("   Línia ").append(i + 1).append(": ")
                                                 .append(line.trim()).append("\n");
                                    }
                                }
                                
                                if (fileHasMatch) {
                                    matchingFiles[0]++;
                                    Platform.runLater(() -> {
                                        searchResultArea.appendText(fileResults.toString() + "\n");
                                    });
                                }
                            } catch (IOException e) {
                                appendLog("Error llegint el fitxer: " + file.getFileName());
                            }
                        });
                    
                    Platform.runLater(() -> {
                        String summary = String.format("Cerca completada: %d coincidències trobades en %d de %d fitxers",
                                matchingFiles[0], matchingFiles[0], totalFiles[0]);
                        appendLog(summary);
                        statusLabel.setText(summary);
                        
                        if (matchingFiles[0] == 0) {
                            searchResultArea.setText("No s'han trobat coincidències per a \"" + searchTerm + "\"");
                        }
                    });
                    
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        appendLog("Error en la cerca: " + e.getMessage());
                        statusLabel.setText("Error en la cerca: " + e.getMessage());
                    });
                } finally {
                    searchCompleted[0] = true;
                }
            });
            
        } catch (NoFileSelectedException e) {
            Platform.runLater(() -> searchProgressBar.setProgress(0));
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertència");
            alert.setHeaderText("Error en la cerca");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            appendLog("EXCEPCIÓ: " + e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            Platform.runLater(() -> searchProgressBar.setProgress(0));
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error en la cerca");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            String errorMsg = "Error en la cerca: " + e.getMessage();
            statusLabel.setText(errorMsg);
            appendLog("EXCEPCIÓ: " + errorMsg);
        }
    }

    @FXML
    protected void onOrganizeFiles() {
        try {
            // Resetear barra de progreso
            Platform.runLater(() -> organizeProgressBar.setProgress(0));
            
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Selecciona una carpeta");
            File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

            if (folder == null || !folder.isDirectory()) {
                throw new NoFileSelectedException("No s'ha seleccionat cap carpeta per organitzar");
            }

            appendLog("🔍 Iniciant organització de fitxers a: " + folder.getPath());
            appendLog("----------------------------------------");

            // Mostrar progreso simulado
            final boolean[] organizeCompleted = {false};
            Thread progressThread = new Thread(() -> {
                try {
                    double progress = 0.0;
                    while (!organizeCompleted[0] && progress < 0.9) {
                        progress += 0.1;
                        final double currentProgress = progress;
                        Platform.runLater(() -> organizeProgressBar.setProgress(currentProgress));
                        Thread.sleep(100);
                    }
                    // Asegurarse de que llegue al 100% cuando la operación termine
                    while (!organizeCompleted[0]) {
                        Thread.sleep(50);
                    }
                    Platform.runLater(() -> organizeProgressBar.setProgress(1.0));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            progressThread.setDaemon(true);
            progressThread.start();

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
            
            // Marcar la organización como completada
            organizeCompleted[0] = true;
            // Asegurarnos de que la barra llegue al 100%
            Platform.runLater(() -> organizeProgressBar.setProgress(1.0));
            
        } catch (NoFileSelectedException e) {
            Platform.runLater(() -> organizeProgressBar.setProgress(0));
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Advertència");
            alert.setHeaderText("Error en l'organització");
            alert.setContentText("EXCEPCIÓ: " + e.getMessage());
            alert.showAndWait();
            
            appendLog("EXCEPCIÓ: " + e.getMessage());
            statusLabel.setText("Error: " + e.getMessage());
        } catch (Exception e) {
            Platform.runLater(() -> organizeProgressBar.setProgress(0));
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
