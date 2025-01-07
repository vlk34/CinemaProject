package com.group18.controller.manager;

import com.group18.dao.UserDAO;
import com.group18.model.User;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class ManagerSidebarController {
    @FXML
    private Button dashboardButton;
    @FXML
    private Button inventoryButton;
    @FXML
    private Button staffButton;
    @FXML
    private Button pricingButton;
    @FXML
    private Button revenueButton;
    @FXML
    private Button logoutButton;

    @FXML private Label userNameLabel;
    @FXML private Label roleLabel;

    private ManagerController mainController;
    private UserDAO userDAO;
    private User currentUser;

//    @FXML
//    private void initialize() {
//        userDAO = new UserDAO();
//        initializeUserInfo();
//    }

    private void initializeUserInfo() {
        if (currentUser != null && "manager".equals(currentUser.getRole())) {
            // Set the full name
            String fullName = String.format("%s %s",
                    currentUser.getFirstName(),
                    currentUser.getLastName());
            userNameLabel.setText(fullName);

            // Set the role with first letter capitalized
            String formattedRole = currentUser.getRole().substring(0, 1).toUpperCase() +
                    currentUser.getRole().substring(1).toLowerCase();
            roleLabel.setText(formattedRole);
        } else {
            handleLogout();
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        initializeUserInfo();
    }

    // Method to set the main controller
    public void setMainController(ManagerController controller) {
        this.mainController = controller;
    }

    @FXML
    private void handleInventory() {
        mainController.switchContent("/fxml/manager/ManagerInventory.fxml");
    }

    @FXML
    private void handleStaff() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/manager/ManagerStaff.fxml"));
            Node staffView = loader.load();

            // Get the controller and set the current user
            ManagerStaffController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            // Switch the content
            mainController.switchContent("/fxml/manager/ManagerStaff.fxml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handlePricing() {
        mainController.switchContent("/fxml/manager/ManagerPricing.fxml");
    }

    @FXML
    private void handleRevenue() {
        mainController.switchContent("/fxml/manager/ManagerRevenue.fxml");
    }

    @FXML
    private void handleLogout() {
        try {
            // Load the login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent loginView = loader.load();

            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Set the login scene
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error appropriately
        }
    }
}