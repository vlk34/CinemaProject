package com.group18.controller.cashier.sharedComponents;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.group18.controller.cashier.CashierController;
import java.util.Optional;

public class CashierActionBarController {
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button nextButton;

    private CashierController mainController;

    public void setMainController(CashierController controller) {
        this.mainController = controller;
    }

    @FXML
    private void initialize() {
        // Disable back button on first stage
        updateButtonStates(0);
    }

    @FXML
    private void handleCancel() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Transaction");
        alert.setHeaderText("Cancel Current Transaction");
        alert.setContentText("Are you sure you want to cancel this transaction? All progress will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Reset to first stage
            mainController.resetTransaction();
        }
    }

    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.previousStage();
            updateButtonStates(mainController.getCurrentStageIndex());
        }
    }

    @FXML
    private void handleNext() {
        if (mainController != null) {
            // Validate current stage before proceeding
            if (validateCurrentStage()) {
                mainController.nextStage();
                updateButtonStates(mainController.getCurrentStageIndex());
            }
        }
    }

    private boolean validateCurrentStage() {
        // This will be implemented with specific validation for each stage
        return true;
    }

    public void updateButtonStates(int currentStage) {
        // Disable back button on first stage
        backButton.setDisable(currentStage == 0);

        // Update next button text on final stage
        if (currentStage == 4) { // Final stage
            nextButton.setText("Confirm Payment");
        } else {
            nextButton.setText("Next");
        }
    }
}