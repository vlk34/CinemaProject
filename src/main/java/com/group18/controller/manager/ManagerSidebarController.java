package com.group18.controller.manager;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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

    private ManagerController mainController;

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
        mainController.switchContent("/fxml/manager/ManagerStaff.fxml");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
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