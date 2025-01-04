package com.group18.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    private static Stage stg;

    @Override
    public void start(Stage primaryStage) throws IOException {
        stg = primaryStage;
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        // Create the scene with desired dimensions
        Scene scene = new Scene(root);

        // Set stage properties
        primaryStage.setTitle("Cinema Management System");
        primaryStage.setScene(scene);

        // Set window size
        primaryStage.setWidth(1800);
        primaryStage.setHeight(1000);

        // Set minimum size
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(720);

        // Center the window
        primaryStage.centerOnScreen();

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}