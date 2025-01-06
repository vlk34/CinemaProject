package com.group18.controller.cashier.sharedComponents;

import com.group18.controller.cashier.stageSpecificFiles.CashierCustomerDetailsController;
import com.group18.controller.cashier.stageSpecificFiles.CashierSeatSelectController;
import com.group18.model.ShoppingCart;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
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
    @FXML
    private Label timeLabel;

    @FXML
    private Label cashierNameLabel;

    @FXML
    private Button logoutButton;

    private Timeline clock;

    @FXML
    private void initialize() {
        // Set cashier name from login session
        cashierNameLabel.setText("Cashier1"); // This should come from session

        // Initialize clock
        initializeClock();
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
}