package com.group18.controller.manager;

import com.group18.model.User;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller responsible for managing the sidebar functionality for a manager's interface.
 * It handles the navigation between different views such as Dashboard, Inventory, Staff,
 * Pricing, and Revenue, as well as user information display and logout functionality.
 */
public class ManagerSidebarController {
    /**
     * The Button representing a dashboard navigation option in the manager's sidebar.
     *
     * This button*/
    @FXML
    private Button dashboardButton;
    /**
     * Represents the button in the sidebar that allows the user to access the*/
    @FXML
    private Button inventoryButton;
    /**
     *
     */
    @FXML
    private Button staffButton;
    /**
     * Represents the button in the sidebar responsible for navigating to or managing
     * actions related to the pricing section of the application.
     *
     * This*/
    @FXML
    private Button pricingButton;
    /**
     * Represents the "Revenue" button in the manager sidebar.
     *
     */
    @FXML
    private Button revenueButton;
    /**
     * Represents the button for logging out the current user.
     *
     */
    @FXML
    private Button logoutButton;

    /**
     * A JavaFX Label element used to display the username of the currently logged-in*/
    @FXML private Label userNameLabel;
    /**
     * Represents the label in the sidebar that displays the role of the currently logged-in*/
    @FXML private Label roleLabel;

    /**
     * Represents the main controller of the application that manages*/
    private ManagerController mainController;
    /**
     * Represents the currently logged-in user in the system.
     *
     * This variable holds the current user's details, which may include
     * their*/
    private User currentUser;


    /**
     * Initializes the sidebar component of the Manager interface.
     *
     * This method configures hover animations for all navigation buttons
     * within the sidebar, enhancing the user experience by adding visual
     * feedback when the user interacts with the buttons. The animations
     * include scaling, translation, and shadow effects.
     *
     * Buttons included:
     * - Inventory
     * - Staff
     * - Pricing
     * - Revenue
     * - Logout
     */
    @FXML
    private void initialize() {
        // Add hover animations to all navigation buttons
        setupSidebarHoverAnimation(inventoryButton);
        setupSidebarHoverAnimation(staffButton);
        setupSidebarHoverAnimation(pricingButton);
        setupSidebarHoverAnimation(revenueButton);
        setupSidebarHoverAnimation(logoutButton);
    }

    /**
     * Initializes user-related information for the sidebar. Displays the user's full name and role
     * on the interface if the currently*/
    private void initializeUserInfo() {
        if (currentUser != null && "manager".equals(currentUser.getRole())) {
            // Set the full name
            String fullName = String.format("%s %s",
                    currentUser.getFirstName(),
                    currentUser.getLastName());
            userNameLabel.setText(fullName);

            // Set the role with first letter capitalized
            String formattedRole = currentUser.getRole().substring(0, 1).toUpperCase() +
                    currentUser.getRole().substring(1).toLowerCase();
            roleLabel.setText(formattedRole);
        } else {
            handleLogout();
        }
    }

    /**
     * Configures hover and click animations for a sidebar button.
     *
     * This method enhances the interactivity of the button by applying
     * visual effects such as scaling, translation, and shadowing when
     * the user hovers over, clicks, or releases the button.
     *
     * @param button the Button for which the hover and click animations
     *               need to be applied
     */
    public static void setupSidebarHoverAnimation(Button button) {
        // Create a subtle shadow effect
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        shadow.setRadius(10);
        shadow.setSpread(0.2);

        // Mouse enter effect
        button.setOnMouseEntered(e -> {
            button.setEffect(shadow);

            // Create new transitions for each animation
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), button);

            // Scale up slightly
            scaleTransition.setToX(1.02);
            scaleTransition.setToY(1.02);

            // Move slightly right
            translateTransition.setToX(5);

            // Play both animations together
            ParallelTransition parallelTransition = new ParallelTransition(
                    button,
                    scaleTransition,
                    translateTransition
            );
            parallelTransition.play();

            // Change background opacity
            button.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 8; -fx-cursor: hand;");
        });

        // Mouse exit effect
        button.setOnMouseExited(e -> {
            button.setEffect(null);

            // Create new transitions for exit animation
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            TranslateTransition translateTransition = new TranslateTransition(Duration.millis(200), button);

            // Scale back to original
            scaleTransition.setToX(1.0);
            scaleTransition.setToY(1.0);

            // Move back to original position
            translateTransition.setToX(0);

            // Play both animations together
            ParallelTransition parallelTransition = new ParallelTransition(
                    button,
                    scaleTransition,
                    translateTransition
            );
            parallelTransition.play();

            // Reset background
            button.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 8; -fx-cursor: hand;");
        });

        // Add pressed state animation
        button.setOnMousePressed(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(0.98);
            scaleTransition.setToY(0.98);
            scaleTransition.play();
        });

        button.setOnMouseReleased(e -> {
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), button);
            scaleTransition.setToX(1.02);
            scaleTransition.setToY(1.02);
            scaleTransition.play();
        });
    }

    /**
     * Sets the current user for the ManagerSidebarController*/
    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeUserInfo();
    }

    /**
     * Gets the current user from the ManagerSidebarController*/
    public User getCurrentUser() {
        return currentUser;
    }
    /**
     * Sets the main controller for this sidebar controller.
     *
     * @param controller the main controller to be assigned*/
    // Method to set the main controller
    public void setMainController(ManagerController controller) {
        this.mainController = controller;
    }

    /**
     * Handles the action for navigating to the inventory section of the application.
     **/
    @FXML
    private void handleInventory() {
        mainController.switchContent("/fxml/manager/ManagerInventory.fxml", currentUser);
    }

    /**
     * Handles the action for navigating to the staff management view.
     *
     * This method is triggered when the associated*/
    @FXML
    private void handleStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manager/ManagerStaff.fxml"));
            Node staffView = loader.load();

            // Get the controller and set the current user
            ManagerStaffController controller = loader.getController();

            controller.setSidebarController(this);

            controller.setCurrentUser(currentUser);

            // Switch the content
            mainController.switchContent("/fxml/manager/ManagerStaff.fxml", currentUser);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Pricing" button action within the manager's sidebar.
     * When invoked, it updates the main content area to display the pricing management view.
     *
     * The*/
    @FXML
    private void handlePricing() {
        mainController.switchContent("/fxml/manager/ManagerPricing.fxml", currentUser);
    }

    /**
     *
     */
    @FXML
    private void handleRevenue() {
        mainController.switchContent("/fxml/manager/ManagerRevenue.fxml", currentUser);
    }

    /**
     * Handles the logout action for the manager by redirecting the application
     */
    @FXML
    private void handleLogout() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent loginView = loader.load();

            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Set the login scene
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}