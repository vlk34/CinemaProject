package com.group18.controller.cashier.sharedComponents;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import java.util.List;
import java.util.Arrays;

/**
 * Controller class responsible for managing the stepper UI component in the cashier interface.
 * This component visually represents the steps in a process with circles that indicate the current step,
 * completed steps, and inactive steps.
 */
public class CashierStepperController {
    @FXML private Circle circle1;
    @FXML private Circle circle2;
    @FXML private Circle circle3;
    @FXML private Circle circle4;
    @FXML private Circle circle5;

    private List<Circle> circles;
    private List<VBox> steps;

    /**
     * Initializes the controller by setting up the list of circles and steps,
     * and sets the initial state of the stepper to the first step.
     */
    @FXML
    private void initialize() {
        circles = Arrays.asList(circle1, circle2, circle3, circle4, circle5);
        // Get parent VBox for each circle
        steps = circles.stream()
                .map(circle -> (VBox) circle.getParent().getParent())
                .toList();

        // Set initial state
        updateSteps(0);
    }

    /**
     * Updates the stepper UI based on the current step.
     * Circles are styled with "completed", "active", or "inactive" based on their position relative to the current step.
     *
     * @param currentStep The index of the current step (0-based).
     */
    public void updateSteps(int currentStep) {
        for (int i = 0; i < steps.size(); i++) {
            VBox step = steps.get(i);
            step.getStyleClass().removeAll("active", "completed", "inactive");

            if (i < currentStep) {
                step.getStyleClass().add("completed");
            } else if (i == currentStep) {
                step.getStyleClass().add("active");
            } else {
                step.getStyleClass().add("inactive");
            }
        }
    }
}