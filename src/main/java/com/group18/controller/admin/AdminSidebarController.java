// AdminSidebarController.java
package com.group18.controller.admin;

import com.group18.dao.UserDAO;
import com.group18.model.User;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller for managing the admin sidebar.
 * Handles navigation to different admin pages.
 */
public class AdminSidebarController {
    @FXML
    private Button moviesButton;

    @FXML
    private Button scheduleButton;

    @FXML
    private Button cancellationsButton;

    @FXML
    private Button logoutButton;

    @FXML
    private Label userNameLabel;
    @FXML
    private Label roleLabel;

    private UserDAO userDAO;
    private User currentUser;

    private AdminController mainController;

    /**
     * Initializes the controller by setting up user information and adding hover animations to the sidebar buttons.
     */
    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        initializeUserInfo();
        setupSidebarHoverAnimation(moviesButton);
        setupSidebarHoverAnimation(scheduleButton);
        setupSidebarHoverAnimation(cancellationsButton);
        setupSidebarHoverAnimation(logoutButton);
    }

    /**
     * Sets up hover animations for the given sidebar button. The button will have a scaling and translation effect,
     * along with a subtle shadow and background color change when hovered over.
     *
     * @param button The button to apply the hover animation to.
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
     * Sets the current user for the sidebar and updates the user information displayed.
     *
     * @param user The user to be set as the current user.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeUserInfo();
    }

    /**
     * Initializes and updates the user information (full name and role) displayed on the sidebar.
     */
    private void initializeUserInfo() {
        if (currentUser != null && "admin".equals(currentUser.getRole())) {
            // Set the full name
            String fullName = String.format("%s %s", currentUser.getFirstName(), currentUser.getLastName());
            userNameLabel.setText(fullName);

            // Set the role
            String formattedRole = currentUser.getRole().substring(0, 1).toUpperCase() +
                    currentUser.getRole().substring(1).toLowerCase();
            roleLabel.setText(formattedRole);
        }
    }

    /**
     * Sets the main controller for the admin sidebar, allowing the sidebar to switch content.
     *
     * @param controller The main controller of the admin dashboard.
     */
    public void setMainController(AdminController controller) {
        this.mainController = controller;
    }

    /**
     * Handles the "Movies" button click by switching to the admin movies view.
     */
    @FXML
    private void handleMovies() {
        mainController.switchContent("/fxml/admin/AdminMovies.fxml");
    }

    /**
     * Handles the "Schedule" button click by switching to the admin schedule view.
     */
    @FXML
    private void handleSchedule() {
        mainController.switchContent("/fxml/admin/AdminSchedules.fxml");
    }

    /**
     * Handles the "Cancellations" button click by switching to the admin cancellations view.
     */
    @FXML
    private void handleCancellations() {
        mainController.switchContent("/fxml/admin/AdminCancellations.fxml");
    }

    /**
     * Handles the "Logout" button click by loading the login view and logging the user out.
     */
    @FXML
    private void handleLogout() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent loginView = loader.load();

            // Get the current stage from the logout button's scene
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Set the login scene
            Scene loginScene = new Scene(loginView);
            stage.setScene(loginScene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}