// AdminSidebarController.java
package com.group18.controller.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
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

    private AdminController mainController;

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
            // Get the current stage
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();

            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Scene loginScene = new Scene(loader.load());

            // Set the login scene
            currentStage.setScene(loginScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}