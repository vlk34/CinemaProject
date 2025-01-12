package com.group18.controller;

import com.group18.controller.admin.AdminSidebarController;
import com.group18.controller.cashier.CashierController;
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

/**
 * Controller class responsible for handling the login functionality of the application.
 * This class interacts with the user interface to validate login credentials,
 * manage error messages, and navigate to appropriate views based on the user's role.
 */
public class LoginController {
    /**
     * Represents a Button element in the LoginController JavaFX class.
     *
     * This button is defined in the associated FXML file and serves as a control
     * that may handle user interactions, such as triggering a login action when clicked.
     * It is linked to specific functionality, such as submitting login credentials
     * via an event handler defined in the LoginController.
     *
     * The button's behavior and visual properties are defined in the FXML file as well as
     * programmatically in the LoginController class.
     */
    @FXML
    private Button button;

    /**
     * A label used in the login form to display messages related to incorrect login attempts.
     * This label is updated dynamically to provide feedback to the user, such as displaying error messages
     * when the entered credentials are invalid.
     *
     * It is part of the LoginController class and is connected to the FXML file through the @FXML annotation.
     */
    @FXML
    private Label wrongLoginLabel;

    /**
     * A JavaFX TextField component used in the login interface for capturing the username
     * entered by the user. This field is pivotal in gathering user credentials as part
     * of the authentication process.
     *
     * This variable is linked to the FXML file corresponding to the login scene
     * and is injected by the JavaFX framework during runtime.
     */
    @FXML
    private TextField usernameField;

    /**
     * Represents a GUI component for entering the user's password in a login form.
     * This field is intended for securely capturing password input and
     * is paired with functionalities to validate user credentials.
     *
     * This PasswordField is part of the LoginController and is linked to
     * handling user authentication, such as retrieving text input
     * for verifying login credentials.
     */
    @FXML
    private PasswordField passwordField;

    /**
     * Initializes the LoginController by setting up the necessary configurations
     * for the user interface. This method is automatically called after the FXML
     * file has been loaded and is primarily responsible for preparing the login
     * form for use.
     *
     * The method performs the following actions:
     * 1. Hides the wrong login label initially.
     * 2. Adds listeners to the username and password fields to clear
     *    the error message whenever the user starts typing in these fields.
     *
     * This ensures that the displayed error messages are dynamically cleared as the
     * user interacts with the input fields, providing a more responsive and intuitive
     * user experience.
     */
    @FXML
    public void initialize() {
        System.out.println("LoginController init");
        wrongLoginLabel.setVisible(false);

        // Add listeners to clear error message when user starts typing
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
            wrongLoginLabel.setVisible(false);
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            wrongLoginLabel.setVisible(false);
        });
    }

    /**
     * Handles the login process when the login button is clicked. Validates user credentials
     * and navigates to the appropriate view based on the user's role. If credentials are invalid,
     * displays an error message.
     *
     * @param event the action event triggered by the login button click
     * @throws IOException if an error occurs during scene loading or navigation
     */
    @FXML
    public void handleLogin(ActionEvent event) throws IOException {
        User user = checkLoginCredentials();

        if (user != null) {
            wrongLoginLabel.setVisible(false);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            String role = user.getRole();
            FXMLLoader loader;
            switch (role.toLowerCase()) {
                case "admin":
                    loader = SceneSwitcher.switchToSceneAndGetLoader("/fxml/admin/AdminView.fxml", stage);

                    AdminSidebarController adminSidebarController = (AdminSidebarController) loader.getNamespace().get("sidebarController");
                    if (adminSidebarController != null) {
                        adminSidebarController.setCurrentUser(user);
                    }
                    break;
                case "manager":
                    // Use the new method to get the loader
                    loader = SceneSwitcher.switchToSceneAndGetLoader("/fxml/manager/ManagerView.fxml", stage);

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
                    loader = SceneSwitcher.switchToSceneAndGetLoader("/fxml/cashier/CashierView.fxml", stage);

                    // Directly get the controller from the loader
                    CashierController cashierController = loader.getController();
                    if (cashierController != null) {
                        cashierController.setCurrentUser(user);
                    } else {
                        System.err.println("Warning: Could not find cashier controller");
                    }
                    break;
                default:
                    wrongLoginLabel.setText("Role is not valid.");
                    break;
            }

            System.out.println("Logged in as: " + user.getUsername() + " with role: " + user.getRole());
        } else {
            wrongLoginLabel.setText("Invalid username or password");
            wrongLoginLabel.setVisible(true);
        }
    }

    /**
     * Validates the login credentials provided by the user and returns a User object
     * if the authentication is successful.
     *
     * This method retrieves the username and password entered by the user through
     * the corresponding UI fields, and uses the UserDAO class to authenticate the
     * user against the stored data.
     *
     * @return a User object representing the authenticated user if the credentials are valid;
     *         null if the authentication fails.
     */
    private User checkLoginCredentials() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Use UserDAO to authenticate and return a User object
        return new UserDAO().authenticateUser(username, password);
    }
}