package com.group18.controller.manager;

import com.group18.dao.UserDAO;
import com.group18.model.AddStaffDialog;
import com.group18.model.EditStaffDialog;
import com.group18.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ManagerStaffController implements Initializable {
    @FXML
    private Button addStaffButton;
    @FXML
    private Label totalStaffLabel;
    @FXML
    private Label newStaffLabel;
    @FXML
    private Label cashiersLabel;
    @FXML
    private Label adminsLabel;
    @FXML
    private Label managersLabel;

    @FXML
    private TableView<User> staffTable;
    @FXML
    private TableColumn<User, String> firstNameColumn;  // New
    @FXML
    private TableColumn<User, String> lastNameColumn;   // New
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> passwordColumn;
    @FXML
    private TableColumn<User, Void> actionsColumn;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilterComboBox;

    private UserDAO userDAO;
    private ObservableList<User> masterData;
    private User currentUser; // To track the logged-in user

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        userDAO = new UserDAO();

        // Setup table columns
        setupTableColumns();
        setupFilters();
        setupSearch();
        setupAddButton();

        // Load initial data
        loadStaffData();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadStaffData(); // Reload data with current user context
    }

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

    private void loadStaffData() {
        // Get all users except the current user
        masterData = FXCollections.observableArrayList(
                userDAO.getAllUsers().stream()
                        .filter(user -> currentUser == null || user.getUserId() != currentUser.getUserId())
                        .collect(Collectors.toList())
        );

        staffTable.setItems(masterData);
        updateStaffStats();
    }

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

    private void showAddStaffDialog() {
        AddStaffDialog dialog = new AddStaffDialog(userDAO);
        dialog.showAndWait().ifPresent(newUser -> {
            loadStaffData();
        });
    }

    private void showEditStaffDialog(User user) {
        EditStaffDialog dialog = new EditStaffDialog(userDAO, user);
        dialog.showAndWait().ifPresent(updatedUser -> {
            int index = masterData.indexOf(user);
            if (index != -1) {
                masterData.set(index, updatedUser);
                updateStaffStats();
                staffTable.refresh();
            }
        });
    }

    private void handleDeleteStaff(User user) {
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

    private void setupFilters() {
        roleFilterComboBox.getItems().addAll("All Roles", "Cashier", "Admin", "Manager");
        roleFilterComboBox.setValue("All Roles");
        roleFilterComboBox.setOnAction(e -> filterStaff());
    }

    private void setupSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterStaff());
    }

    private void setupAddButton() {
        addStaffButton.setOnAction(e -> showAddStaffDialog());
    }

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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}