package com.group18.controller;

import com.group18.dao.UserDAO;
import com.group18.model.User;
import com.group18.util.SceneSwitcher;
import com.group18.controller.manager.ManagerSidebarController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    private Button button;

    @FXML
    private Label wrongLoginLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void initialize() {
        System.out.println("LoginController init");
    }

    @FXML
    public void handleLogin(ActionEvent event) throws IOException {
        User user = checkLoginCredentials();

        if (user != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            String role = user.getRole();

            switch (role.toLowerCase()) {
                case "admin":
                    SceneSwitcher.switchToScene("/fxml/admin/AdminView.fxml", stage);
                    break;
                case "manager":
                    // Use the new method to get the loader
                    FXMLLoader loader = SceneSwitcher.switchToSceneAndGetLoader("/fxml/manager/ManagerView.fxml", stage);

                    // Get the sidebar controller and set the current user
                    ManagerSidebarController sidebarController =
                            (ManagerSidebarController) loader.getNamespace().get("sidebarController");
                    if (sidebarController != null) {
                        sidebarController.setCurrentUser(user);
                    } else {
                        System.err.println("Warning: Could not find sidebar controller");
                    }
                    break;
                case "cashier":
                    SceneSwitcher.switchToScene("/fxml/cashier/CashierView.fxml", stage);
                    break;
                default:
                    wrongLoginLabel.setText("Role is not valid.");
                    break;
            }

            System.out.println("Logged in as: " + user.getUsername() + " with role: " + user.getRole());
        } else {
            wrongLoginLabel.setText("Username or password invalid.");
        }
    }

    private User checkLoginCredentials() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Use UserDAO to authenticate and return a User object
        return new UserDAO().authenticateUser(username, password);
    }
}