// HashScout.java
package com.github.mgrl39.hashscout;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class HashScout extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HashScout.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);

        // Comentar o eliminar esta línea para evitar el error si no tienes el archivo icon.png
        // stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        
        // Alternativa: verificar que el recurso existe antes de usarlo
        /*
        var iconStream = getClass().getResourceAsStream("/icon.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }
        */

        stage.setTitle("HashScout");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}