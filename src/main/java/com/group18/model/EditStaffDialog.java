package com.group18.model;

import com.group18.dao.UserDAO;
import com.group18.model.User;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

/**
 * The EditStaffDialog class provides a dialog interface to edit the details of a staff member.
 * It extends the Dialog class and supports editing attributes such as first name, last name,
 * username, password, and role of the user.
 *
 * This class interacts with the UserDAO for updating the user's details in the underlying data
 * store. It validates user input, ensuring correctness before saving the changes.
 */
public class EditStaffDialog extends Dialog<User> {
    /**
     * Represents the text field used for inputting or displaying the first name
     * of a user in the context of staff member editing.
     *
     * This field is initialized as a part of the EditStaffDialog class and typically
     * binds to or retrieves data pertaining to the first name property of a User object.
     *
     * Used within user-editing dialogs to validate, display, or collect the user's
     * first name data.
     */
    private final TextField firstNameField = new TextField();
    /**
     * TextField for entering or displaying the last name of the user being edited.
     *
     * This field is used within the EditStaffDialog to manage and validate the last name
     * input as part of the user's personal details during staff information updates.
     */
    private final TextField lastNameField = new TextField();
    /**
     * TextField used to input or display the username of the staff member.
     *
     * This field is part of the form in the EditStaffDialog and is initialized as
     * an empty text field. It allows setting or editing the username for a staff
     * member, which is typically a unique identifier within the system.
     */
    private final TextField usernameField = new TextField();
    /**
     * Represents a password input field within the EditStaffDialog class.
     *
     * This field is used to capture or modify the password associated with a staff member.
     * It is a private, final instance of PasswordField, ensuring secure input for sensitive
     * information like passwords.
     *
     * The passwordField is typically displayed within the user interface of the
     * EditStaffDialog and interacts with other components such as
     * input validation and data persistence for user updates.
     */
    private final PasswordField passwordField = new PasswordField();
    /**
     * A ComboBox used to select the role of a user within the system.
     *
     * The roleComboBox presents the available role options, such as "admin", "manager",
     * or "cashier", to be assigned to a user. It facilitates role selection during
     * operations like editing or creating a new user.
     *
     * This ComboBox is initialized as private and final, indicating it is immutable
     * and accessible only within the EditStaffDialog class.
     */
    private final ComboBox<String> roleComboBox = new ComboBox<>();
    /**
     * Instance of UserDAO used to manage user-related database operations.
     * This field provides data access functionality for interacting with the
     * "users" table, including user authentication, retrieval, addition,
     * updating, and deletion of user records.
     */
    private final UserDAO userDAO;
    /**
     * Represents the {@link User} instance currently being edited in the dialog.
     *
     * This object contains the details of a user such as their username, password,
     * role, first name, and last name. It is used within the dialog to prefill the
     * input fields for editing or to store the updated details after validation.
     *
     * The {@code user} field is immutable and is passed during the creation of the
     * {@code EditStaffDialog} instance. This ensures consistency and prevents any
     * unexpected modifications to the user data outside the dialog's scope.
     */
    private final User user;

    /**
     * Constructs an EditStaffDialog instance used for creating and managing a dialog
     * to edit the details of a staff member, including their first name, last name,
     * username, password, and role.
     *
     * @param userDAO The data access object for user-related database operations.
     * @param user    The user object containing the initial details of the staff member to be edited.
     */
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
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                Platform.runLater(() -> {
                    // Allow empty strings, single characters, or multiple Unicode letters with optional spaces
                    if (newValue.trim().isEmpty() ||
                            newValue.trim().matches("^[\\p{L}]+(\\s+[\\p{L}]+)*$")) {
                        validateInput();
                    } else {
                        firstNameField.setText(oldValue);
                    }
                });
            }
        });

        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                Platform.runLater(() -> {
                    // Allow empty strings, single characters, or multiple Unicode letters with optional spaces
                    if (newValue.trim().isEmpty() ||
                            newValue.trim().matches("^[\\p{L}]+(\\s+[\\p{L}]+)*$")) {
                        validateInput();
                    } else {
                        lastNameField.setText(oldValue);
                    }
                });
            }
        });
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

    /**
     * Validates the input fields for the EditStaffDialog and updates the state of the save button.
     *
     * This method ensures that:
     * - The first name and last name contain only Unicode letters.
     * - All required fields (first name, last name, username, password) are not empty.
     * - The save button is enabled only when all input fields meet the validation criteria.
     *
     * The validation logic:
     * - Uses regular expressions to check that the first name and last name consist of Unicode letters.
     * - Trims whitespace from input fields before checking if they are empty.
     * - Disables the save button if any validation fails.
     *
     * Side effects:
     * - The save button, located within the dialog pane, is enabled or disabled based on the validity of the input.
     */
    private void validateInput() {
        // Validate that first and last names contain only Unicode letters and proper spaces
        boolean isFirstNameValid = firstNameField.getText().trim().matches("[\\p{L}]+(\\s[\\p{L}]+)*");
        boolean isLastNameValid = lastNameField.getText().trim().matches("[\\p{L}]+(\\s[\\p{L}]+)*");

        // Ensure input is not empty and matches the pattern
        boolean isValid = isFirstNameValid &&
                isLastNameValid &&
                !firstNameField.getText().trim().isEmpty() &&
                !lastNameField.getText().trim().isEmpty() &&
                !usernameField.getText().trim().isEmpty() &&
                !passwordField.getText().trim().isEmpty();

        Node saveButton = getDialogPane().lookupButton(getDialogPane().getButtonTypes().get(0));
        saveButton.setDisable(!isValid);
    }

    /**
     * Displays an error message in a dialog box to inform the user of an error.
     *
     * @param message the error message to be shown to the user
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}