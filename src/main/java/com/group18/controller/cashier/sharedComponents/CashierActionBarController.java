package com.group18.controller.cashier.sharedComponents;

import com.group18.controller.cashier.stageSpecificFiles.CashierCustomerDetailsController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.group18.controller.cashier.CashierController;
import java.util.Optional;

/**
 * Controller class for the action bar in the cashier section of the application.
 * This class handles the navigation between stages of the cashier process, such as movie selection,
 * session selection, seat selection, and customer details.
 */
public class CashierActionBarController {
    @FXML private Button cancelButton;
    @FXML private Button backButton;
    @FXML private Button nextButton;

    private CashierController mainController;

    /**
     * Validates if the current stage has valid data to proceed.
     * The validation logic varies depending on the stage of the cashier process.
     *
     * @return true if the current stage is valid, false otherwise.
     */
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
                        customerDetailsController != null && hasPersistentDetails;

            default:
                return true;
        }
    }

    /**
     * Updates the states of the action buttons (Back and Next) based on the current stage.
     * The back button is disabled on the first stage, and the next button is disabled
     * if the current stage is not valid.
     *
     * @param currentStage The index of the current stage in the cashier process.
     */
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
    }

    /**
     * Sets the main controller for the action bar.
     *
     * @param controller The main controller of the cashier process.
     */
    public void setMainController(CashierController controller) {
        this.mainController = controller;
    }

    /**
     * Initializes the action bar by setting the button states.
     */
    @FXML
    private void initialize() {
        // Disable back button on first stage
        updateButtonStates(0);
    }

    /**
     * Handles the cancel action by displaying a confirmation dialog.
     * If confirmed, the transaction is reset and all progress is lost.
     */
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

    /**
     * Handles the back action by navigating to the previous stage.
     * The button state is updated after the stage change.
     */
    @FXML
    private void handleBack() {
        if (mainController != null) {
            mainController.previousStage();
            updateButtonStates(mainController.getCurrentStageIndex());
        }
    }

    /**
     * Handles the next action by navigating to the next stage.
     * The current stage is validated before proceeding to the next stage.
     */
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