package com.group18.controller.cashier.sharedComponents;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import java.util.List;
import java.util.Arrays;

public class CashierStepperController {
    @FXML private Circle circle1;
    @FXML private Circle circle2;
    @FXML private Circle circle3;
    @FXML private Circle circle4;
    @FXML private Circle circle5;

    private List<Circle> circles;
    private List<VBox> steps;

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