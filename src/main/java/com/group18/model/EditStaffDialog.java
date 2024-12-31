package com.group18.model;

import com.group18.dao.UserDAO;
import com.group18.model.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class EditStaffDialog extends Dialog<User> {
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField usernameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final ComboBox<String> roleComboBox = new ComboBox<>();
    private final UserDAO userDAO;
    private final User user;

    public EditStaffDialog(UserDAO userDAO, User user) {
        this.userDAO = userDAO;
        this.user = user;

        setTitle("Edit Staff Member");
        setHeaderText("Edit staff member details");

        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

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

        // Populate fields with current user data
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        usernameField.setText(user.getUsername());
        passwordField.setText(user.getPassword());
        roleComboBox.setValue(user.getRole());

        getDialogPane().setContent(grid);

        // Enable/Disable save button depending on whether all data was entered
        Node saveButton = getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        // Add listeners to validate input
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
        usernameField.textProperty().addListener((observable, oldValue, newValue) -> validateInput());
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> validateInput());

        // Do initial validation
        validateInput();

        // Convert the result to a User object when the save button is clicked
        setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                User updatedUser = new User();
                updatedUser.setUserId(user.getUserId());
                updatedUser.setFirstName(firstNameField.getText().trim());
                updatedUser.setLastName(lastNameField.getText().trim());
                updatedUser.setUsername(usernameField.getText().trim());
                updatedUser.setPassword(passwordField.getText());
                updatedUser.setRole(roleComboBox.getValue());

                if (userDAO.updateUser(updatedUser)) {
                    return updatedUser;
                } else {
                    showError("Failed to update user. Username might be taken.");
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

        Node saveButton = getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
        saveButton.setDisable(!isValid);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}