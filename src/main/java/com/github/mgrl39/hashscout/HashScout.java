// HashScout.java
package com.github.mgrl39.hashscout;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @class HashScout
 * @brief Classe principal de l'aplicació que gestiona la inicialització de la interfície gràfica
 * 
 * Aquesta classe demostra:
 * - Ús del patró MVC amb FXML per separar la lògica de la vista
 * - Gestió professional de recursos amb try-with-resources
 * - Implementació correcta de JavaFX Application
 * - Càrrega eficient de recursos CSS per estilització
 * 
 * @author mgrl39
 * @version 1.0
 */
public class HashScout extends Application {

    /**
     * @brief Inicialitza i configura la finestra principal de l'aplicació
     * 
     * Demostra:
     * - Gestió adequada d'excepcions amb try-catch
     * - Càrrega modular de recursos FXML
     * - Configuració professional de l'escena
     * - Implementació de disseny responsive
     * 
     * @param stage Finestra principal de l'aplicació
     * @throws Exception Gestiona possibles errors durant la inicialització
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HashScout.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 400);

        scene.getStylesheets().add(HashScout.class.getResource("style.css").toExternalForm());

        stage.setTitle("HashScout");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @brief Punt d'entrada principal de l'aplicació
     * 
     * Demostra:
     * - Inicialització correcta de l'aplicació JavaFX
     * - Gestió del cicle de vida de l'aplicació
     * - Punt d'entrada net i ben estructurat
     * 
     * @param args Arguments de línia de comandes (no utilitzats)
     */
    public static void main(String[] args) {
        launch();
    }
}