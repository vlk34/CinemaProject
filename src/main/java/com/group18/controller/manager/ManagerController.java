// ManagerController.java
package com.group18.controller.manager;

import com.group18.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.Node;
import java.io.IOException;

/**
 * Controller for managing the primary manager interface.
 *
 * This class acts as the main container for the manager's application interface, integrating the
 * sidebar navigation and main content area. It handles content switching between different manager views
 * based on user interactions and sets up appropriate controller dependencies for the loaded views.
 */
public class ManagerController {
    /**
     * The root layout container for the manager interface.
     *
     * This BorderPane serves as the main layout for the application's manager view,
     * containing a sidebar for navigation and a content area for the primary views.
     * It acts as the parent node for dynamically loaded content and manages
     * the relationships between different UI elements of the manager's interface.
     */
    @FXML
    private BorderPane root;

    /**
     * Represents the main content node within the interface managed by the controller.
     *
     * This node serves as a placeholder within the view where different content can be dynamically loaded
     * and displayed depending on user actions or application state changes. It is typically updated via
     * content-switching logic implemented in the controller.
     */
    @FXML
    private Node content;

    /**
     * Controller for the sidebar section of the manager interface.
     *
     * This variable represents an instance of the ManagerSidebarController
     * that handles user interactions and functionality within the sidebar
     * component of the primary manager interface. It is responsible for navigating
     * between different sections of the application by communicating with the
     * main controller.
     *
     * This controller is injected via the FXML file and is initialized during
     * the loading phase of the parent ManagerController. The sidebar controller
     * relies on the main controller for managing content switching and providing
     * context for the application.
     */
    @FXML
    private ManagerSidebarController sidebarController;

    /**
     * Initializes the controller by setting up the required dependencies.
     *
     * This method establishes the communication link between the sidebar controller
     * and the main controller by assigning the current controller instance to the
     * sidebar's main controller.
     */

    @FXML
    private void initialize() {
        sidebarController.setMainController(this);
    }

    /**
     * Switches the main content area to a new FXML view and sets up the associated controller.
     * This method dynamically loads the specified FXML file, updates the main content area,
     * and ensures the relevant controller has access to the current user.
     *
     * @param fxmlPath the path to the FXML file to be loaded
     * @param currentUser the currently logged-in user whose information may be passed to the new controller
     */
    public void switchContent(String fxmlPath, User currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node newContent = loader.load();

            Object controller = loader.getController();
            if (controller instanceof ManagerPricingController) {
                ((ManagerPricingController) controller).setCurrentUser(currentUser);
            }

            if (loader.getController() instanceof ManagerStaffController) {
                ManagerStaffController staffController = loader.getController();
                staffController.setCurrentUser(currentUser);

                staffController.setSidebarController(sidebarController);
            }

            root.setCenter(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateSidebarUserInfo(User user) {
        if (sidebarController != null) {
            sidebarController.setCurrentUser(user);
        }
    }
}