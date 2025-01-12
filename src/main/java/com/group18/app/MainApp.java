package com.group18.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * MainApp is the entry point of the JavaFX application. It initializes and displays
 * the main stage of the application, setting up the scene, window properties, and
 * application icon.
 *
 * This class extends the JavaFX Application class and overrides the start method to
 * configure and display the primary stage with a default LoginView.
 */
public class MainApp extends Application {

    /**
     * Represents the primary stage of the JavaFX application.
     * This variable holds a reference to the main stage of the application, allowing
     * other components to access and modify its properties (e.g., scene changes, resizing).
     * It is initialized in the start method of the MainApp class.
     */
    private static Stage stg;

    /**
     * Initializes and displays the primary stage of the application. This method sets up
     * the scene, window properties, and application icon, and ensures proper layout and
     * appearance of the main application window.
     *
     * @param primaryStage the primary stage for this application, provided by the JavaFX runtime
     * @throws IOException if loading the FXML resource or application resources fails
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        stg = primaryStage;
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
        Parent root = loader.load();

        // Create the scene with desired dimensions
        Scene scene = new Scene(root);

        // Set stage properties
        primaryStage.setTitle("Group18 CinemaCenter");

        // Set the application icon
        try {
            Image icon = new Image(getClass().getResourceAsStream("/images/cinema-icon.png"));
            primaryStage.getIcons().add(icon);
        } catch (Exception e) {
            System.err.println("Failed to load application icon: " + e.getMessage());
        }

        primaryStage.setScene(scene);

        // Set window size
        primaryStage.setWidth(1800);
        primaryStage.setHeight(1000);

        // Set minimum size
        primaryStage.setMinWidth(1280);
        primaryStage.setMinHeight(720);

        primaryStage.setMaximized(true);

        // Center the window
        primaryStage.centerOnScreen();

        primaryStage.show();
    }

    /**
     * The main entry point for the JavaFX application. This method is invoked
     * when the application is launched. It delegates to the JavaFX Application
     * launch method to initialize and start the application lifecycle.
     *
     * @param args the command-line arguments passed to the application
     */
    public static void main(String[] args) {
        launch(args);
    }
}