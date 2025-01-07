package com.group18.controller.cashier.sharedComponents;

import com.group18.controller.cashier.CashierController;
import com.group18.controller.cashier.stageSpecificFiles.CashierCustomerDetailsController;
import com.group18.controller.cashier.stageSpecificFiles.CashierSeatSelectController;
import com.group18.dao.UserDAO;
import com.group18.model.ShoppingCart;
import com.group18.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.group18.controller.cashier.stageSpecificFiles.CashierCustomerDetailsController.clearPersistentDetailsStatic;

public class CashierHeaderController {
    @FXML private Label timeLabel;
    @FXML private Label cashierNameLabel;
    @FXML private Label roleLabel;
    @FXML private Button logoutButton;

    private Timeline clock;
    private UserDAO userDAO;
    private User currentCashier;
    private CashierController mainController;

    @FXML
    private void initialize() {
        userDAO = new UserDAO();
        // Initialize clock, but no need to call initializeUserInfo here
        initializeClock();
    }

    public void setMainController(CashierController controller) {
        this.mainController = controller;
        if (mainController != null) {
            System.out.println("main controller in header controller is not null ");
            // Now that mainController is set, initialize the user info
            initializeUserInfoAfterControllerSet();
        }
    }

    public void getValidUser(User user) {
        this.currentCashier = user;
    }

    private void initializeUserInfoAfterControllerSet() {
        if (mainController != null) {
            // returns null
            currentCashier = mainController.getCurrentUser();
            initializeUserInfo();
        } else {
            System.out.println("Main controller is not set yet.");
        }
    }

    private void initializeUserInfo() {
        try {

            // Get the currently logged-in cashier
            currentCashier = userDAO.authenticateUser(currentCashier.getUsername(), currentCashier.getPassword());

            if (currentCashier != null && "cashier".equals(currentCashier.getRole())) {
                // Set the full name of the cashier
                String fullName = String.format("%s %s",
                        currentCashier.getFirstName(),
                        currentCashier.getLastName());
                cashierNameLabel.setText(fullName);

                // Set the role with first letter capitalized
                String formattedRole = currentCashier.getRole().substring(0, 1).toUpperCase() +
                        currentCashier.getRole().substring(1).toLowerCase();
                roleLabel.setText(formattedRole);

                // Style role label
                roleLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");

            } else {
                showError("Authentication Error",
                        "Unable to verify cashier credentials. Please log in again.");
                handleLogout();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Database Error",
                    "Unable to connect to the database. Please try again later.");
        }
    }

    private void initializeClock() {
        clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
            timeLabel.setText(now.format(formatter));
        }), new KeyFrame(Duration.seconds(1)));

        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    @FXML
    private void handleLogout() {
        try {
            // Stop the clock timeline
            if (clock != null) {
                clock.stop();
            }

            // Reset persistent customer info
            clearPersistentDetailsStatic();
            CashierSeatSelectController.clearSelectedSeatsStatic();
            ShoppingCart.getInstance().clear();

            // Get current stage
            Stage currentStage = (Stage) logoutButton.getScene().getWindow();

            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Scene loginScene = new Scene(loader.load());

            // Set the login scene
            currentStage.setScene(loginScene);
            currentStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Getter for current cashier (might be needed by other components)
    public User getCurrentCashier() {
        return currentCashier;
    }

}