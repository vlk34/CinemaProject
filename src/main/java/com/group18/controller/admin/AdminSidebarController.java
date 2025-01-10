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

    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        initializeUserInfo();
        setupSidebarHoverAnimation(moviesButton);
        setupSidebarHoverAnimation(scheduleButton);
        setupSidebarHoverAnimation(cancellationsButton);
        setupSidebarHoverAnimation(logoutButton);
    }

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

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeUserInfo();
    }

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


    public void setMainController(AdminController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleMovies() {
        mainController.switchContent("/fxml/admin/AdminMovies.fxml");
    }

    @FXML
    private void handleSchedule() {
        mainController.switchContent("/fxml/admin/AdminSchedules.fxml");
    }

    @FXML
    private void handleCancellations() {
        mainController.switchContent("/fxml/admin/AdminCancellations.fxml");
    }

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