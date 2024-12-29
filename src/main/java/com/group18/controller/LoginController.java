package com.group18.controller;

import com.group18.dao.UserDAO;
import com.group18.model.User;
import com.group18.util.SceneSwitcher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {
    @FXML
    public void initialize() {
        System.out.println("LoginController init");
    }

    // This method is called when the user clicks the login button
    @FXML
    public void handleLogin(ActionEvent event) throws IOException {
        User user = checkLoginCredentials();

        if (user != null) {
            // If user is found, switch scenes based on the user's role
            Stage stage = (Stage) usernameField.getScene().getWindow();
            String role = user.getRole();

            // Switch to the correct view based on the user's role
            switch (role.toLowerCase()) {
                case "admin":
                    SceneSwitcher.switchToScene("/fxml/admin/AdminView.fxml", stage); // Admin view
                    break;
                case "manager":
                    SceneSwitcher.switchToScene("/fxml/manager/ManagerView.fxml", stage); // Manager view
                    break;
                case "cashier":
                    SceneSwitcher.switchToScene("/fxml/CashierView.fxml", stage); // Cashier view
                    break;
                default:
                    wrongLoginLabel.setText("Role is not valid.");
                    break;
            }

            // Optionally, you can store or use the user's information here if needed
            System.out.println("Logged in as: " + user.getUsername() + " with role: " + user.getRole());

        } else {
            // If credentials are invalid, show an error message
            wrongLoginLabel.setText("Username or password invalid.");
        }
    }

    // This method checks the login credentials and returns a User object or null
    private User checkLoginCredentials() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Use UserDAO to authenticate and return a User object
        return new UserDAO().authenticateUser(username, password);
    }

    @FXML
    private Button button;

    @FXML
    private Label wrongLoginLabel;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;


}




