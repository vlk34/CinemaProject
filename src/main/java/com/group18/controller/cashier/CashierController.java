package com.group18.controller.cashier;

import com.group18.controller.cashier.sharedComponents.CashierActionBarController;
import com.group18.controller.cashier.sharedComponents.CashierCartController;
import com.group18.controller.cashier.sharedComponents.CashierStepperController;
import com.group18.controller.cashier.stageSpecificFiles.*;
import com.group18.model.MovieSession;
import com.group18.model.Movie;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CashierController {
    @FXML private VBox root;
    @FXML private Node currentStage;
    @FXML private CashierStepperController stepperController;
    @FXML private CashierActionBarController actionBarController;
    @FXML private CashierCartController cashierCartController;
    @FXML private CashierCustomerDetailsController customerDetailsController;

    private int currentStageIndex = 0;
    private final String[] stages = {
            "/fxml/cashier/stageSpecificFiles/CashierMovieSearch.fxml",
            "/fxml/cashier/stageSpecificFiles/CashierSessionSelect.fxml",
            "/fxml/cashier/stageSpecificFiles/CashierSeatSelect.fxml",
            "/fxml/cashier/stageSpecificFiles/CashierCustomerDetails.fxml",
            "/fxml/cashier/stageSpecificFiles/CashierPayment.fxml"
    };

    // Data to pass between stages
    private Movie selectedMovie;
    private MovieSession selectedSession;
    private Set<String> selectedSeats = new HashSet<>();
    private LocalDate selectedDate;

    @FXML
    private void initialize() {
        try {
            // Set the main controller reference for the action bar
            actionBarController.setMainController(this);

            cashierCartController.setMainController(this);

            // Load initial stage
            FXMLLoader loader = new FXMLLoader(getClass().getResource(stages[0]));
            Node initialStage = loader.load();

            CashierMovieSearchController controller = loader.getController();
            controller.setCashierController(this);

            StackPane contentArea = (StackPane) currentStage.getParent();
            contentArea.getChildren().setAll(initialStage);
            currentStage = initialStage;

            if (stepperController != null) {
                stepperController.updateSteps(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void navigateWithData(Object data) {
        boolean dataChanged = false;

        switch (currentStageIndex) {
            case 0: // Movie Search
                Movie newMovie = (Movie) data;
                if (newMovie != selectedMovie) {
                    selectedMovie = newMovie;
                    dataChanged = true;
                }
                break;

            case 1: // Session Select
                if (data instanceof Map) {
                    Map<String, Object> sessionData = (Map<String, Object>) data;
                    MovieSession newSession = (MovieSession) sessionData.get("session");
                    LocalDate newDate = (LocalDate) sessionData.get("date");

                    if (newSession != selectedSession || newDate != selectedDate) {
                        selectedSession = newSession;
                        selectedDate = newDate;
                        dataChanged = true;
                    }
                }
                break;

            case 2: // Seat Select
                Set<String> newSeats = (Set<String>) data;
                // Preserve previous seats if the new selection is not empty
                if (newSeats != null && !newSeats.isEmpty()) {
                    selectedSeats = new HashSet<>(newSeats);
                    dataChanged = true;
                }
                break;
        }

        // Update button states if data changed
        if (dataChanged && actionBarController != null) {
            actionBarController.updateButtonStates(currentStageIndex);
        }

        // Only proceed to next stage if we have valid data
        if (dataChanged) {
            nextStage();
        }
    }

    public void nextStage() {
        if (currentStageIndex < stages.length - 1) {
            loadStage(++currentStageIndex);
        }
    }

    public void previousStage() {
        if (currentStageIndex > 0) {
            loadStage(--currentStageIndex);
        }
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    private void loadStage(int index) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(stages[index]));
            Node newStage = loader.load();

            // Get controller and pass necessary data
            Object controller = loader.getController();
            setupStageController(controller);

            StackPane contentArea = (StackPane) currentStage.getParent();
            contentArea.getChildren().setAll(newStage);
            currentStage = newStage;

            // Update stepper
            if (stepperController != null) {
                stepperController.updateSteps(index);
            }

            if (actionBarController != null) {
                actionBarController.updateButtonStates(currentStageIndex);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupStageController(Object controller) {
        if (controller instanceof CashierMovieSearchController) {
            ((CashierMovieSearchController) controller).setCashierController(this);
        }
        else if (controller instanceof CashierSessionSelectController) {
            CashierSessionSelectController sessionController = (CashierSessionSelectController) controller;
            sessionController.setCashierController(this);
            sessionController.setMovie(selectedMovie);
        }
        else if (controller instanceof CashierSeatSelectController) {
            CashierSeatSelectController seatController = (CashierSeatSelectController) controller;
            seatController.setCashierController(this);

            // Restore previous state if available
            if (selectedMovie != null && selectedSession != null && selectedDate != null) {
                seatController.setSessionInfo(selectedMovie, selectedSession, selectedDate);
                if (selectedSeats != null && !selectedSeats.isEmpty()) {
                    seatController.restorePreviousSeats(selectedSeats);
                }
            }
        }
        else if (controller instanceof CashierCustomerDetailsController) {
            customerDetailsController = (CashierCustomerDetailsController) controller;
            customerDetailsController.setCashierController(this);

            // Restore previous seat selection
            if (selectedSeats != null && !selectedSeats.isEmpty()) {
                customerDetailsController.setSelectedSeats(selectedSeats);
            }

            if (actionBarController != null) {
                actionBarController.updateButtonStates(currentStageIndex);
            }
        }
        else if (controller instanceof CashierPaymentController) {
            ((CashierPaymentController) controller).setCashierController(this);
        }
    }

    public void resetTransaction() {
        // Reset all stored data
        selectedMovie = null;
        selectedSession = null;
        selectedSeats = null;

        // Reset to first stage
        currentStageIndex = 0;
        loadStage(currentStageIndex);

        // Clear the cart using the injected controller
        if (cashierCartController != null) {
            cashierCartController.clearCart();
        }

        // Clear persistent customer details
        if (customerDetailsController != null) {
            customerDetailsController.clearPersistentDetails();
        }
    }

    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    public CashierCartController getCartController() {
        return cashierCartController;
    }

    public CashierActionBarController getActionBarController() {
        return actionBarController;
    }

    public CashierCustomerDetailsController getCustomerDetailsController() {
        return customerDetailsController;
    }

    // Getters for stored data
    public Movie getSelectedMovie() { return selectedMovie; }
    public MovieSession getSelectedSession() { return selectedSession; }
    public Set<String> getSelectedSeats() { return selectedSeats; }
}