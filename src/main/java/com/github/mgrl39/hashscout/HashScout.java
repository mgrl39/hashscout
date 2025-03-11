// HashScout.java
package com.github.mgrl39.hashscout;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HashScout extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(HashScout.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 400);

        scene.getStylesheets().add(HashScout.class.getResource("style.css").toExternalForm());

        stage.setTitle("HashScout");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}