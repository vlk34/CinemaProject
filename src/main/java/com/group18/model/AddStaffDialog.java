package com.group18.model;

import com.group18.dao.UserDAO;
import com.group18.model.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class AddStaffDialog extends Dialog<User> {
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<String> roleComboBox = new ComboBox<>();
    private final UserDAO userDAO;

    public AddStaffDialog(UserDAO userDAO) {
        this.userDAO = userDAO;

        setTitle("Add New Staff Member");
        setHeaderText("Enter staff member details");

        // Set the button types
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Username:"), 0, 2);
        grid.add(usernameField, 1, 2);
        grid.add(new Label("Password:"), 0, 3);
        grid.add(passwordField, 1, 3);
        grid.add(new Label("Role:"), 0, 4);
        grid.add(roleComboBox, 1, 4);

        // Setup role combo box
        roleComboBox.getItems().addAll("cashier", "admin", "manager");
        roleComboBox.setValue("cashier");

        getDialogPane().setContent(grid);

        // Enable/Disable add button depending on whether all data was entered
        Node addButton = getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Add listeners to validate input
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> validateInput());

        // Convert the result to a User object when the add button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                User newUser = new User();
                newUser.setFirstName(firstNameField.getText().trim());
                newUser.setLastName(lastNameField.getText().trim());
                newUser.setUsername(usernameField.getText().trim());
                newUser.setPassword(passwordField.getText());
                newUser.setRole(roleComboBox.getValue());

                if (userDAO.addUser(newUser)) {
                    return newUser;
                } else {
                    showError("Failed to add user. Username might be taken.");
                    return null;
                }
            }
            return null;
        });

        // Request focus on the first field by default
        Platform.runLater(() -> firstNameField.requestFocus());
    }

    private void validateInput() {
        boolean isValid = !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty() &&
                !usernameField.getText().trim().isEmpty() &&
                !passwordField.getText().trim().isEmpty();

        Node addButton = getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
        addButton.setDisable(!isValid);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}