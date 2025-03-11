package com.github.mgrl39.hashscout;

import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.*;

import java.io.File;

public class MainController {

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea logTerminal;

    @FXML
    private TextArea hashResultArea;

    @FXML
    private TextArea searchResultArea;

    @FXML
    private TextField searchTextField;

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
    private Label selectedOrganizeFolderLabel;

    private Logger logger;
    private HashGenerator hashGenerator;
    private FileSearcher fileSearcher;
    private FileOrganizer fileOrganizer;
    private File selectedFile;
    private File selectedSearchFolder;
    private File selectedOrganizeFolder;

    @FXML
    public void initialize() {
        // Inicializar Logger
        logger = new Logger(logTerminal);
        hashGenerator = new HashGenerator();
        fileSearcher = new FileSearcher(logger);
        fileOrganizer = new FileOrganizer(logger);

        logger.log("🔵 HashScout inicialitzat");

        hashTypeComboBox.getItems().addAll("MD5", "SHA-1", "SHA-256");
        hashTypeComboBox.setValue("SHA-256");

        hashProgressBar.setProgress(0);
        searchProgressBar.setProgress(0);
        organizeProgressBar.setProgress(0);
    }

    @FXML
    protected void onSelectFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecciona un fitxer");
        File file = fileChooser.showOpenDialog(statusLabel.getScene().getWindow());

        if (file != null) {
            selectedFile = file;
            selectedFileLabel.setText(file.getName());
            logger.log("📂 Fitxer seleccionat: " + file.getName());
        }
    }

    @FXML
    protected void onGenerateHash() {
        if (selectedFile == null) {
            showWarning("Selecciona un fitxer abans de generar el hash.");
            return;
        }

        String algorithm = hashTypeComboBox.getValue();
        try {
            String hash = hashGenerator.generateHash(selectedFile, algorithm);
            hashResultArea.setText(algorithm + ": " + hash);
            logger.log("✔️ Hash generat amb èxit: " + hash);
        } catch (Exception e) {
            logger.log("❌ Error generant hash: " + e.getMessage());
            showError("Error generant hash", e.getMessage());
        }
    }

    @FXML
    protected void onSelectFolderForSearch() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona una carpeta");
        File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

        if (folder != null) {
            selectedSearchFolder = folder;
            selectedSearchFolderLabel.setText(folder.getPath());
            logger.log("📁 Carpeta seleccionada per a cerca: " + folder.getPath());
        }
    }

    @FXML
    protected void onSearchText() {
        if (selectedSearchFolder == null) {
            showWarning("Selecciona una carpeta abans de cercar.");
            return;
        }

        String searchTerm = searchTextField.getText();
        if (searchTerm.isEmpty()) {
            showWarning("Introdueix un text de cerca.");
            return;
        }

        searchResultArea.clear();
        fileSearcher.searchText(
                selectedSearchFolder.toPath(),
                searchTerm,
                searchResultArea,
                searchProgressBar
        );
    }

    @FXML
    protected void onSelectFolderForOrganization() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Selecciona una carpeta per organitzar");
        File folder = directoryChooser.showDialog(statusLabel.getScene().getWindow());

        if (folder != null) {
            selectedOrganizeFolder = folder;
            selectedOrganizeFolderLabel.setText(folder.getPath());
            logger.log("📁 Carpeta seleccionada per a organització: " + folder.getPath());
        }
    }

    @FXML
    protected void onOrganizeFiles() {
        if (selectedOrganizeFolder == null) {
            showWarning("Selecciona una carpeta abans d'organitzar.");
            return;
        }

        fileOrganizer.organizeFiles(selectedOrganizeFolder.toPath());
    }

    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertència");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    protected void handleExit() {
        System.exit(0);
    }

    @FXML
    protected void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sobre HashScout");
        alert.setHeaderText("HashScout v1.0");
        alert.setContentText("Una eina de gestió de fitxers desenvolupada per mgrl39 amb JavaFX.");
        alert.showAndWait();
    }
}
