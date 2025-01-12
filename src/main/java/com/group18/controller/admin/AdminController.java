// AdminController.java
package com.group18.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import java.io.IOException;

/**
 * The AdminController class serves as the main controller for the admin dashboard
 * in the application. It manages the primary layout and switches the content in the
 * central area of the screen depending on user interactions.
 */
public class AdminController {
    /**
     * Represents the root layout of the admin dashboard, specifically a {@code BorderPane}
     * that serves as the main container for different content areas within the admin view.
     * The {@code root} pane organizes the overall UI, with sections for a menu, central content,
     * and other components. The central content area is dynamically switched based on user
     * interactions.
     */
    @FXML
    private BorderPane root;

    /**
     * Represents the dynamically loadable content to be displayed in the central area of the admin dashboard.
     * This field is used together with the {@link BorderPane} layout to switch views in the application
     * based on user interactions.
     */
    @FXML
    private Node content;

    /**
     * References the AdminSidebarController instance associated with this controller.
     * The sidebarController is responsible for managing the admin sidebar's behavior,
     * such as handling user interactions with sidebar buttons, updating user-related
     * information, and communicating with the main AdminController.
     *
     * This variable is injected using the @FXML annotation and is initialized during
     * the loading of the FXML file. The `setMainController` method is called to
     * establish a link between this controller and the AdminSidebarController, ensuring
     * proper interaction between the sidebar and main admin control logic.
     */
    @FXML
    private AdminSidebarController sidebarController;

    /**
     * Initializes the main controller by setting itself as the main controller
     * for the associated sidebar controller. This method is automatically
     * called by the JavaFX framework during the view's initialization phase.
     */
    @FXML
    private void initialize() {
        sidebarController.setMainController(this);
    }

    /**
     * Switches the content displayed in the central area of the admin dashboard.
     * Loads the FXML file specified by the given path and updates the UI accordingly.
     *
     * @param fxmlPath The path to the FXML file to load and display in the central area.
     */
    public void switchContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node newContent = loader.load();
            root.setCenter(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}