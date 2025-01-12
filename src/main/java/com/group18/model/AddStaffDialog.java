package com.group18.model;

import com.group18.dao.UserDAO;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * Represents a dialog interface for adding a new staff member to the system.
 *
 * This class extends the JavaFX `Dialog` class, allowing users to input details
 * for creating a new user, including first name, last name, username, password,
 * and role. The dialog validates user input and ensures proper formatting before
 * storing the data.
 *
 * The result of the dialog is a `User` object, which is created and returned if
 * the input is valid and the user data is successfully added to the system via
 * the `UserDAO` instance.
 */
public class AddStaffDialog extends Dialog<User> {
    /**
     * A text field component for entering the first name of a staff member.
     *
     * This field allows input of textual data representing the first name,
     **/
    private final TextField firstNameField = new TextField();
    /**
     * A TextField component for capturing or displaying the last name of a staff member.
     *
     * This field is part of the AddStaffDialog and is used to input the last name
     * when adding or editing a staff member's details.
     */
    private final TextField lastNameField = new TextField();
    /**
     * A TextField component used to capture the username input during staff addition.
     *
     * This field is part of the user input*/
    private final TextField usernameField = new TextField();
    /**
     * Represents a password input field for entering the staff member's password in the dialog.
     * This field is used to securely capture the password when adding a new staff member.
     */
    private final PasswordField passwordField = new PasswordField();
    /**
     * A ComboBox component used to allow the selection of a user's role in the system.
     *
     * This field is part of the AddStaffDialog class,*/
    private final ComboBox<String> roleComboBox = new ComboBox<>();
    /**
     * An instance of the UserDAO class used for interacting with the "users" table in the database.
     * Provides methods for performing CRUD operations and handling user-related queries.
     */
    private final UserDAO userDAO;

    /**
     * Constructs an AddStaffDialog window for adding a new staff member.
     * This dialog allows the user to enter details like first name, last*/
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
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allow only Unicode letters
            if (!newValue.matches("\\p{L}*")) {
                firstNameField.setText(oldValue);
            }
            validateInput();
        });

        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Allow only Unicode letters
            if (!newValue.matches("\\p{L}*")) {
                lastNameField.setText(oldValue);
            }
            validateInput();
        });
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

    /**
     * Validates user input in the form fields and controls the state of the "Add" button.
     *
     * This method performs the following validations:
     * - Checks that the first name*/
    private void validateInput() {
        // Validate that first and last names only contain Unicode letters
        boolean isFirstNameValid = firstNameField.getText().trim().matches("\\p{L}*");
        boolean isLastNameValid = lastNameField.getText().trim().matches("\\p{L}*");

        // Ensure input is not empty and contains only letters
        boolean isValid = isFirstNameValid &&
                isLastNameValid &&
                !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty() &&
                !usernameField.getText().trim().isEmpty() &&
                !passwordField.getText().trim().isEmpty();

        Node addButton = getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
        addButton.setDisable(!isValid);
    }

    /**
     * Displays an error message to the user*/
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}