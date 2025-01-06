package com.group18.controller.cashier.sharedComponents;

import com.group18.controller.cashier.stageSpecificFiles.CashierCustomerDetailsController;
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

    private boolean validateCurrentStage() {
        if (mainController == null) return false;

        int currentStage = mainController.getCurrentStageIndex();

        switch (currentStage) {
            case 0: // Movie Search
                return mainController.getSelectedMovie() != null;

            case 1: // Session Select
                return mainController.getSelectedSession() != null &&
                        mainController.getSelectedDate() != null;

            case 2: // Seat Select
                return mainController.getSelectedSeats() != null &&
                        !mainController.getSelectedSeats().isEmpty();

            case 3: // Customer Details
                CashierCustomerDetailsController customerDetailsController =
                        mainController.getCustomerDetailsController();

                // Check if we have persistent details or current details are validated
                boolean hasPersistentDetails = customerDetailsController != null &&
                        customerDetailsController.hasValidDetailsAndVerification();

                return mainController.getCartController() != null &&
                        customerDetailsController != null &&
                        (customerDetailsController.hasItems() ||
                                hasPersistentDetails);

            default:
                return true;
        }
    }

    public void updateButtonStates(int currentStage) {
        // Disable back button on first stage
        backButton.setDisable(currentStage == 0);

        // Update next button text and state
        if (currentStage == 4) { // Final stage
            nextButton.setText("Next");
            nextButton.setDisable(validateCurrentStage());
        } else {
            nextButton.setText("Next");
            nextButton.setDisable(!validateCurrentStage());
        }

        // Disable next button if current stage isn't valid

    }

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
}