package com.group18.controller.manager;

import com.group18.dao.UserDAO;
import com.group18.model.AddStaffDialog;
import com.group18.model.EditStaffDialog;
import com.group18.model.User;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * The ManagerStaffController class implements the management interface for handling staff-related operations
 * within an application. It allows the viewing, addition, deletion, and editing of staff details and supports
 * functions such as filtering, searching, and displaying user statistics.
 *
 * This class provides an interactive GUI interface to manage staff records stored in a database using
 * JavaFX components. It is designed to handle only non-current user accounts for specific operations
 * and ensures intuitive and efficient data management.
 *
 * Primary UI elements include staff metrics, an interactive table for user details, search functionality,
 * role filters, and action buttons.
 */
public class ManagerStaffController implements Initializable {
    /**
     * A button in the UI that triggers the functionality to add a new staff member to the system.
     *
     * This button is managed within the ManagerStaffController and is linked to actions such as opening
     * a dialog or form for entering new staff details. It is styled and behaviorally managed using FXML
     * and controller methods.
     */
    @FXML
    private Button addStaffButton;
    /**
     * Represents a label in the ManagerStaffController responsible for displaying
     * the total count of staff members.
     *
     * This label is updated dynamically based on the current staff data
     * in the application and is part of the user interface.
     */
    @FXML
    private Label totalStaffLabel;
    /**
     * Represents a label within the user interface that displays information related to new staff members.
     *
     * This label is part of the ManagerStaffController class and is typically used to present
     * details or messages regarding the addition or display of new staff in the application.
     *
     * It is annotated with @FXML to indicate that it is linked to an element in the corresponding FXML file.
     */
    @FXML
    private Label newStaffLabel;
    /**
     * Represents a label in the UI that displays information related to cashiers.
     *
     * The `cashiersLabel` is part of the ManagerStaffController and is used to present
     * details or statistics specifically associated with cashiers.
     */
    @FXML
    private Label cashiersLabel;
    /**
     * Label displayed in the UI to represent or indicate the total count or list
     * of administrators within the system.
     *
     * Typically used in conjunction with other UI components to provide
     * staff-related statistics or details in the ManagerStaffController.
     */
    @FXML
    private Label adminsLabel;
    /**
     * Represents the label used to display the count or information related to managers
     * within the staff management system.
     *
     * This label is a part of the `ManagerStaffController` UI and is dynamically updated
     * to reflect the current data or status associated with managers.
     *
     * It is managed via the FXML framework and linked to the controller for interaction.
     */
    @FXML
    private Label managersLabel;

    /**
     * A TableView component designed to display a list of staff members represented
     * by User objects. The table manages and visualizes data related to staff,
     * such as their first name, last name, username, password, and role.
     *
     * This table interacts with other components of the ManagerStaffController class
     * to allow operations like adding, editing, filtering, and deleting staff entries.
     * It is also used for applying role-based filters or searching through the staff list.
     *
     * The table's columns are dynamically set up to bind to the properties of the User
     * class, facilitating the display of user-specific attributes.
     */
    @FXML
    private TableView<User> staffTable;
    /**
     * Represents the column in the staff table that displays the first name of a user.
     *
     * This column is linked to the "firstName" property of the User class and
     * allows displaying and sorting user first names within the staff table in the user interface.
     *
     * It is an FXML-linked element that is initialized when the corresponding FXML file is loaded.
     */
    @FXML
    private TableColumn<User, String> firstNameColumn;  // New
    /**
     * A TableColumn used within the staff management table to display the last name
     * of a user. The column is associated with the `lastName` property of the User class.
     *
     * This column is part of the `staffTable` and contributes to presenting user details,
     * enabling the management of staff information within the system. It is populated
     * with the last names of users and supports data-binding features for dynamic updates.
     */
    @FXML
    private TableColumn<User, String> lastNameColumn;   // New
    /**
     * Represents a column in the staff table that displays the role of a user.
     *
     * The roleColumn is bound to the "role" property of the User object and is used
     * to show the role of each staff member in the table view. Typical roles can include
     * "admin," "manager," or "cashier."
     *
     * This column is part of the staff management table in the ManagerStaffController
     * and allows users to view and manage staff roles effectively.
     */
    @FXML
    private TableColumn<User, String> roleColumn;
    /**
     * Represents a column in a TableView for displaying the username property of a User object.
     *
     * This TableColumn binds to the username attribute of User instances and is used
     * to display or manage user-related data within the staff management table.
     *
     * The column is initialized and configured in the setupTableColumns() method of
     * the ManagerStaffController class and is part of the overall TableView
     * structure for managing staff data.
     */
    @FXML
    private TableColumn<User, String> usernameColumn;
    /**
     * Represents a column in the staff management table for displaying
     * the password of a user. The column is bound to the "password" property
     * of the {@code User} class.
     *
     * This column is part of the {@code ManagerStaffController} and is used
     * to display or manage sensitive user information. Depending on the use case,
     * the actual display of the password may be masked or hidden to ensure security.
     *
     * Associated data type: {@code String}.
     */
    @FXML
    private TableColumn<User, String> passwordColumn;
    /**
     * Represents a column in the staff management table dedicated to user actions.
     *
     * This column is associated with operations or buttons that allow the user
     * to perform specific actions, such as editing or deleting a staff member.
     * It utilizes a parameterized type to indicate that it displays action
     * controls related to a User object.
     *
     * The contents of this column are not text-based but are instead interactive
     * elements (e.g., buttons) for performing actions on staff records.
     */
    @FXML
    private TableColumn<User, Void> actionsColumn;

    /**
     * TextField component used for entering a search query to filter or locate specific staff members.
     *
     * This field is designed to allow users to input textual data, such as a name or username,
     * and its content is processed to perform real-time or triggered filtering of staff records
     * displayed in the staff table.
     *
     * Typically, the search functionality interacts with other parts of the UI and the backend
     * to dynamically update the visible staff data based on the search criteria entered to this field.
     */
    @FXML
    private TextField searchField;
    /**
     * Represents a ComboBox UI element used for filtering staff members by their role.
     *
     * This ComboBox allows users to select a specific role (e.g., "Admin", "Manager", "Cashier")
     * to filter the staff list displayed in the table. It interacts with the filtering functionality
     * of the application to dynamically update the displayed staff members based on the selected role.
     *
     * Field is annotated with @FXML, indicating it is injected by the JavaFX framework and
     * is associated with a corresponding ComboBox UI component defined in the FXML file.
     */
    @FXML
    private ComboBox<String> roleFilterComboBox;

    /**
     * An instance of the UserDAO class used for handling interactions with
     * the "users" table in the database. This variable provides access to methods
     * for performing CRUD operations and user-related queries such as authentication,
     * retrieving user information, and managing user accounts. It acts as the data
     * access layer between the application and the database.
     */
    private UserDAO userDAO;
    /**
     * Represents the main data storage for the application, containing an observable list of users.
     *
     * This variable is used to manage and display the staff information in the UI, such as their
     * roles, usernames, and other personal details. It serves as the centralized collection of
     * all user objects, providing functionalities like filtering, searching, and updating the
     * staff data dynamically.
     */
    private ObservableList<User> masterData;
    /**
     * Represents the currently logged-in user within the system.
     *
     * This variable is used to track the active user session and store the
     * user's information such as their credentials and role. It plays a key role
     * in managing user-specific functionalities, including permissions and personalized
     * operations, throughout the application.
     */
    private User currentUser; // To track the logged-in user

    private ManagerSidebarController sidebarController;

    public void setSidebarController(ManagerSidebarController sidebarController) {
        System.out.println("Setting sidebar controller from: " +
                Arrays.stream(Thread.currentThread().getStackTrace())
                        .map(StackTraceElement::getMethodName)
                        .limit(5)
                        .collect(Collectors.joining(" <- "))
        );
        this.sidebarController = sidebarController;
        System.out.println("Sidebar controller set: " + sidebarController);
    }
    /**
     * Initializes the ManagerStaffController by setting up table columns, filters, search functionality,
     * and other UI components. It also loads the initial staff data.
     *
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if no resources are specified.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();

        // Setup table columns
        setupTableColumns();
        setupFilters();
        setupSearch();
        setupAddButton();

        setupButtonHoverAnimation(addStaffButton);
        // Load initial data
        loadStaffData();
    }

    /**
     * Updates the controller with the specified current user and reloads the staff data
     * to reflect the context of the provided user.
     *
     * @param user The user to be set as the current user of this controller.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadStaffData(); // Reload data with current user context
    }

    /**
     * Configures the columns of the staff table, setting up the appropriate data bindings,
     * styles, and functionalities for each column, including the action buttons for edit
     * and delete operations.
     *
     * The method initializes the following columns:
     * - First Name Column: Binds to the "firstName" property of the User object and centers the content.
     * - Last Name Column: Binds to the "lastName" property of the User object and centers the content.
     * - Role Column: Binds to the "role" property of the User object and centers the content.
     * - Username Column: Binds to the "username" property of the User object and centers the content.
     * - Password Column: Binds to the "password" property of the User object and centers the content.
     *
     * A custom Actions Column is created with buttons for "Edit" and "Delete" functionalities:
     * - The Edit button opens a dialog to make modifications to the selected user's data.
     * - The Delete button removes the selected user from the system.
     * - The Delete button is hidden for the current user to prevent self-deletion.
     *
     * Additional configuration includes:
     * - Styling the buttons with hover effects for better user interaction.
     * - Dynamically creating an HBox to align the buttons and exclude the Delete button when necessary.
     * - Ensuring the table view accurately reflects changes after user actions.
     */
    private void setupTableColumns() {
        // Replace the name column setup with separate first name and last name columns
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameColumn.setStyle("-fx-alignment: CENTER;");

        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameColumn.setStyle("-fx-alignment: CENTER;");

        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setStyle("-fx-alignment: CENTER;");

        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameColumn.setStyle("-fx-alignment: CENTER;");

        passwordColumn.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordColumn.setStyle("-fx-alignment: CENTER;");

        // Actions column with edit and delete buttons
        actionsColumn.setCellFactory(column -> new TableCell<User, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                // Base styles matching inventory view (clean, no borders)
                editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2a1b35;");
                deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2a1b35;");

                // Hover effects - only change background color
                editButton.setOnMouseEntered(e ->
                        editButton.setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: #2a1b35;")
                );
                editButton.setOnMouseExited(e ->
                        editButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2a1b35;")
                );

                deleteButton.setOnMouseEntered(e ->
                        deleteButton.setStyle("-fx-background-color: #ffebee; -fx-text-fill: #2a1b35;")
                );
                deleteButton.setOnMouseExited(e ->
                        deleteButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #2a1b35;")
                );

                editButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    showEditStaffDialog(user);
                });

                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteStaff(user);
                });

                // Center the HBox containing the buttons
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    User user = getTableView().getItems().get(getIndex());
                    HBox buttonsBox;
                    // Don't show delete button for the current user
                    if (currentUser != null && user.getUserId() == currentUser.getUserId()) {
                        buttonsBox = new HBox(8, editButton);
                    } else {
                        buttonsBox = new HBox(8, editButton, deleteButton);
                    }
                    buttonsBox.setAlignment(Pos.CENTER);
                    setGraphic(buttonsBox);
                }
            }
        });
        actionsColumn.setStyle("-fx-alignment: CENTER;");
    }

    /**
     * Loads the staff data into the staff table while excluding the current user from the list.
     *
     * The method retrieves all users from the data source, filters out the current user (if any),
     * and populates the table with the resulting list of users. Additionally, it updates staff
     * statistics after reloading the data.
     *
     * The user data is fetched using the `userDAO.getAllUsers()` method, and is set to an observable
     * list to support dynamic updates in the UI. This observable list is then assigned to the staff
     * table for display.
     *
     * After updating the staff table, the method calls `updateStaffStats()` to refresh role-specific
     * counts and other staff-related metrics in the UI.
     */
    private void loadStaffData() {
        // Get all users (including current user)
        masterData = FXCollections.observableArrayList(userDAO.getAllUsers());
        staffTable.setItems(masterData);
        updateStaffStats();
    }

    /**
     * Updates staff statistics displayed in the user interface.
     *
     * This method calculates various metrics related to staff members,
     * including the total number of staff, the number of new staff members for the
     * current month, and the count of staff members grouped by specific roles.
     *
     * The calculated data is extracted from the underlying staff data and database,
     * and the statistics are updated asynchronously on the JavaFX application thread
     * to ensure proper UI rendering.
     *
     * The following statistics are displayed:
     * - Total number of staff members.
     * - Number of new staff members for the current month.
     * - Count of staff members for roles such as "cashier", "admin", and "manager".
     */
    private void updateStaffStats() {
        Map<String, Long> roleCounts = masterData.stream()
                .collect(Collectors.groupingBy(User::getRole, Collectors.counting()));

        // Get new users count for this month
        long newUsersThisMonth = userDAO.getNewUsersThisMonth();

        Platform.runLater(() -> {
            totalStaffLabel.setText(String.valueOf(masterData.size()));
            newStaffLabel.setText(newUsersThisMonth + " new this month");
            cashiersLabel.setText(roleCounts.getOrDefault("cashier", 0L) + " Cashiers");
            adminsLabel.setText(roleCounts.getOrDefault("admin", 0L) + " Admins");
            managersLabel.setText(roleCounts.getOrDefault("manager", 0L) + " Managers");
        });
    }

    /**
     * Displays the "Add Staff" dialog, allowing the user to input details for a new staff member.
     *
     * This method creates an instance of the AddStaffDialog, leveraging the UserDAO for database interaction.
     * The dialog presents fields for entering the staff member's first name, last name, username, password,
     * and role. Upon successful input and validation, the dialog returns a new User object, which gets added
     * to the data model and displayed in the staff table.
     *
     * If a new staff member is added, the staff data is refreshed via the loadStaffData() method to include
     * the newly added user.
     */
    private void showAddStaffDialog() {
        AddStaffDialog dialog = new AddStaffDialog(userDAO);
        dialog.showAndWait().ifPresent(newUser -> {
            loadStaffData();
        });
    }

    /**
     * Opens a dialog window to edit the details of an existing staff member.
     * If the user's details are successfully updated, the changes are reflected
     * in the staff list and relevant statistics are updated.
     *
     * @param user the User object representing the staff member to be edited
     */
    private void showEditStaffDialog(User user) {
        System.out.println("showEditStaffDialog called from: " +
                Arrays.stream(Thread.currentThread().getStackTrace())
                        .map(StackTraceElement::getMethodName)
                        .limit(5)
                        .collect(Collectors.joining(" <- "))
        );

        EditStaffDialog dialog = new EditStaffDialog(userDAO, user);
        dialog.showAndWait().ifPresent(updatedUser -> {
            int index = masterData.indexOf(user);
            if (index != -1) {
                masterData.set(index, updatedUser);
                updateStaffStats();
                staffTable.refresh();

                // Check if the updated user is the current user
                if (currentUser != null && updatedUser.getUserId() == currentUser.getUserId()) {
                    // Update the current user reference
                    currentUser = updatedUser;

                    System.out.println("Sidebar Controller (before check): " + sidebarController);
                    System.out.println("Current User ID: " + currentUser.getUserId());
                    System.out.println("Updated User ID: " + updatedUser.getUserId());

                    if (sidebarController != null) {
                        System.out.println("Updating sidebar user info");
                        sidebarController.setCurrentUser(updatedUser);
                    } else {
                        System.out.println("WARNING: Sidebar controller is NULL!");
                        System.out.println("Current stack trace: " +
                                Arrays.stream(Thread.currentThread().getStackTrace())
                                        .map(StackTraceElement::getMethodName)
                                        .limit(10)
                                        .collect(Collectors.joining(" <- "))
                        );
                    }
                }
            }
        });
    }

    /**
     * Handles deleting a staff member from the system based on the provided user.
     * This method ensures the current user cannot delete their own account and prompts
     * a confirmation dialog before proceeding with the deletion.
     *
     * @param user The User object representing the staff member to be deleted.
     *             Must contain valid user information, including a unique user ID
     *             and personal details such as first and last names.
     */
    private void handleDeleteStaff(User user) {
        System.out.println(currentUser.getUserId());
        System.out.println(user.getUserId());
        if (currentUser != null && user.getUserId() == currentUser.getUserId()) {
            showAlert("Error", "You cannot delete your own account.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Staff Member");
        confirm.setHeaderText("Are you sure you want to delete " + user.getFirstName() + " " + user.getLastName() + "?");
        confirm.setContentText("This action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (userDAO.deleteUser(user.getUserId())) {
                    masterData.remove(user);
                    updateStaffStats();
                    showAlert("Success", "Staff member has been deleted successfully.");
                } else {
                    showAlert("Error", "Failed to delete staff member.");
                }
            }
        });
    }

    /**
     * Configures the role filter dropdown in the user interface.
     *
     * This method initializes the role filter ComboBox by populating it with predefined role options
     * and setting its default value to "All Roles". It also defines the behavior for when the
     * selected value of the ComboBox changes, triggering the filtering of the staff list accordingly.
     *
     * The available role options include:
     * - "All Roles"
     * - "Cashier"
     * - "Admin"
     * - "Manager"
     *
     * Selecting a specific role filters the displayed staff based on the selected role.
     * The "All Roles" option shows all staff members without applying a role-specific filter.
     *
     * The filtering logic is handled by the {@code filterStaff} method.
     */
    private void setupFilters() {
        roleFilterComboBox.getItems().addAll("All Roles", "Cashier", "Admin", "Manager");
        roleFilterComboBox.setValue("All Roles");
        roleFilterComboBox.setOnAction(e -> filterStaff());
    }

    /**
     * Configures a search mechanism for filtering staff data within the application.
     *
     * The method adds a listener to the text property of the search field. When the text in the
     * search field changes (e.g., when the user types or deletes content), the `filterStaff`
     * method is triggered to update the displayed staff list based on the entered search query.
     *
     * This functionality helps in dynamically filtering staff information in real-time,
     * enhancing the user experience for finding staff by names or usernames.
     */
    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterStaff());
    }

    /**
     * Configures the action for the "Add Staff" button.
     *
     * This method assigns a click event handler to the addStaffButton that
     * triggers the display of a dialog for adding a new staff member. The
     * dialog allows the user to input details for the new staff. Once the dialog
     * completes successfully, the staff data is reloaded to include the newly
     * added staff.
     */
    private void setupAddButton() {
        addStaffButton.setOnAction(e -> showAddStaffDialog());
    }

    /**
     * Filters the list of staff displayed in the staff table based on search text and role selection.
     *
     * This method performs two main filtering operations:
     * 1. A search filter that matches the user's first name, last name, or username with the text entered
     *    to the search field. The matching is case-insensitive.
     * 2. A role filter that only displays users with the role selected in the role filter combo box,
     *    unless "All Roles" is selected, which displays users of all roles.
     *
     * If both filters are applied, the resulting table view will only show users
     * that meet both filtering criteria. The method also refreshes the staff
     * table to reflect the updated filtered data.
     *
     * Preconditions: The master data list must be initialized before invoking this method.
     *
     * Post conditions: Updates the staff table to display filtered user data.
     */
    private void filterStaff() {
        if (masterData == null) return;

        ObservableList<User> filteredData = FXCollections.observableArrayList(masterData);

        // Apply search filter
        String searchText = searchField.getText().toLowerCase();
        if (!searchText.isEmpty()) {
            filteredData = filteredData.filtered(user ->
                    user.getFirstName().toLowerCase().contains(searchText) ||
                            user.getLastName().toLowerCase().contains(searchText) ||
                            user.getUsername().toLowerCase().contains(searchText)
            );
        }

        // Apply role filter
        String roleFilter = roleFilterComboBox.getValue();
        if (!"All Roles".equals(roleFilter)) {
            filteredData = filteredData.filtered(user ->
                    user.getRole().equalsIgnoreCase(roleFilter)
            );
        }

        staffTable.setItems(filteredData);
        staffTable.refresh(); // Force refresh of the table view
    }

    /**
     * Displays an alert dialog with the specified title and content.
     *
     * @param title the title of the alert dialog
     * @param content the message content to be displayed in the alert dialog
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Sets up a hover animation for the specified button, creating a scaling effect
     * when the button is pressed, released, or the mouse exits during press.
     *
     * @param button the Button instance to which the hover animation will be applied
     */
    public void setupButtonHoverAnimation(Button button) {
        // Create scale transition
        ScaleTransition pressTransition = new ScaleTransition(Duration.millis(100), button);
        ScaleTransition releaseTransition = new ScaleTransition(Duration.millis(100), button);

        // Add pressed state animation
        button.setOnMousePressed(e -> {
            pressTransition.setToX(0.95);
            pressTransition.setToY(0.95);
            pressTransition.play();
        });

        button.setOnMouseReleased(e -> {
            releaseTransition.setToX(1.0);
            releaseTransition.setToY(1.0);
            releaseTransition.play();
        });

        // Reset button state when mouse exits during press
        button.setOnMouseExited(e -> {
            releaseTransition.setToX(1.0);
            releaseTransition.setToY(1.0);
            releaseTransition.play();
        });
    }
}