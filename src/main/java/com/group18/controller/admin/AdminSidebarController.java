// AdminSidebarController.java
package com.group18.controller.admin;

import com.group18.dao.UserDAO;
import com.group18.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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