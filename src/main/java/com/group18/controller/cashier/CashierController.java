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
import java.util.Map;
import java.util.Set;

public class CashierController {
    @FXML private VBox root;
    @FXML private Node currentStage;
    @FXML private CashierStepperController stepperController;
    @FXML private CashierActionBarController actionBarController;
    @FXML private CashierCartController cashierCartController;

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
    private Set<String> selectedSeats;
    private LocalDate selectedDate;
    // In CashierController.java
    @FXML
    private void initialize() {
        try {
            // Set the main controller reference for the action bar
            actionBarController.setMainController(this);

            cashierCartController.setMainController(this);
            System.out.println(cashierCartController);

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
        // Store data based on current stage
        System.out.println(currentStageIndex);
        switch (currentStageIndex) {
            case 0: // Movie Search
                selectedMovie = (Movie) data;
                break;
            case 1: // Session Select
                if (data instanceof Map) {
                    Map<String, Object> sessionData = (Map<String, Object>) data;
                    selectedSession = (MovieSession) sessionData.get("session");
                    selectedDate = (LocalDate) sessionData.get("date");
                }
                break;
            case 2: // Seat Select
                selectedSeats = (Set<String>) data;
                break;
        }

        // Move to next stage
        nextStage();

        if (actionBarController != null) {
            actionBarController.updateButtonStates(currentStageIndex);
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
            seatController.setSessionInfo(selectedMovie, selectedSession, selectedDate);
        }
        else if (controller instanceof CashierCustomerDetailsController) {
            CashierCustomerDetailsController customerController = (CashierCustomerDetailsController) controller;
            customerController.setCashierController(this);
            customerController.setSelectedSeats(selectedSeats);
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
    }

    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    public CashierCartController getCartController() {
        System.out.println("Getting cart controller - Current value: " + (cashierCartController == null ? "NULL" : "NOT NULL"));
        return cashierCartController;
    }

    // Getters for stored data
    public Movie getSelectedMovie() { return selectedMovie; }
    public MovieSession getSelectedSession() { return selectedSession; }
    public Set<String> getSelectedSeats() { return selectedSeats; }
}