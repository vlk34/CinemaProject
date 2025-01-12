package com.group18.controller.cashier;

import com.group18.controller.cashier.sharedComponents.CashierActionBarController;
import com.group18.controller.cashier.sharedComponents.CashierCartController;
import com.group18.controller.cashier.sharedComponents.CashierHeaderController;
import com.group18.controller.cashier.sharedComponents.CashierStepperController;
import com.group18.controller.cashier.stageSpecificFiles.*;
import com.group18.model.MovieSession;
import com.group18.model.Movie;
import com.group18.model.User;
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

import static com.group18.controller.cashier.stageSpecificFiles.CashierCustomerDetailsController.clearPersistentDetailsStatic;

/**
 * The {@code CashierController} class manages the multi-stage cashier workflow in the application,
 * including user-related functionality, navigation between stages, and interaction with related
 * controllers for components such as the action bar, cart, and customer details.
 *
 * This controller serves as the central coordinating interface for managing
 * transaction-related states, including selected movies, sessions, seats, and dates.
 * It also facilitates UI updates, navigation, and state resets throughout the cashier process.
 */
public class CashierController {
    /**
     * Represents the root container for the CashierController's user interface.
     * This variable is linked to the root VBox element in the corresponding FXML file.
     * It serves as the primary layout container that encapsulates all other UI components
     * within the scene managed by the CashierController.
     */
    @FXML private VBox root;
    /**
     * Represents the current stage or screen in the CashierController's workflow.
     * This variable holds a reference to the Node object corresponding to the
     * currently active stage or user interface element in the multi-stage transaction process.
     *
     * It is used in navigation and update operations within the transaction workflow.
     * The value of this variable changes dynamically as the user progresses
     * through different stages (e.g., movie selection, session selection, seat selection, etc.).
     *
     * Managed by the CashierController class to ensure proper synchronization
     * between the current stage index and the active visual representation on the interface.
     */
    @FXML private Node currentStage;
    /**
     * Controller for handling the visual representation of steps and their states
     * in the cashier workflow. The {@link CashierStepperController} manages the
     * display and updates the progress indicators based on the current workflow step.
     * It is linked to the UI elements representing the steps, and updates their
     * visual styles to indicate the current, completed, or inactive status.
     *
     * This field is used within the {@link CashierController} to coordinate
     * the stepper as part of the overall cashier transaction process.
     */
    @FXML private CashierStepperController stepperController;
    /**
     * The CashierActionBarController associated with the CashierController class.
     * This variable holds the controller responsible for managing the action bar,
     * which includes navigation buttons (e.g., cancel, back, next) and their related behavior.
     * The `actionBarController` serves as a bridge between the navigation controls
     * and the main logic of the cashier workflow.
     */
    @FXML private CashierActionBarController actionBarController;
    /**
     * Controls the header section of the cashier interface, managing
     * the display of cashier information, current role, and time.
     * Also provides logout functionality and integrates with the
     * main {@link CashierController}.
     *
     * This controller is responsible for initializing dynamic elements
     * like the clock and updating the header information based on the
     * logged-in user. It interacts with the main controller to retrieve
     * the current user details and continually updates the display.
     *
     * The header also manages user session termination by handling logout
     * actions and resetting all relevant persistent data.
     */
    @FXML private CashierHeaderController headerController;
    /**
     * A reference to an instance of the {@code CashierCartController} class.
     * This controller manages the shopping cart functionality within the cashier interface.
     * It integrates with other controllers in the {@code CashierController} workflow to handle
     * cart-related operations, such as displaying selected items, updating quantities,
     * and calculating totals.
     *
     * This field is annotated with {@code @FXML}, indicating that it is bound to an
     * element in the associated FXML file and is initialized during the FXML loading process.
     */
    @FXML private CashierCartController cashierCartController;
    /**
     * A reference to an instance of {@code CashierCustomerDetailsController}.
     * Used within the {@code CashierController} class to manage customer-related details
     * during a cashier transaction process. This field is injected using the JavaFX
     * {@code @FXML} annotation, enabling connection to the corresponding FXML layout.
     */
    @FXML private CashierCustomerDetailsController customerDetailsController;

    /**
     * Represents the current stage index in the transaction workflow within the
     * CashierController.
     *
     * This variable keeps track of the user's progression through a predefined
     * series of stages, such as movie selection, session selection, seat selection,
     * customer details, and checkout. The value of this variable corresponds to
     * the step currently being displayed and is used to control navigation
     * between stages as well as UI updates.
     *
     * The initial value of this variable is 0, indicating the first stage in the
     * workflow. It is updated throughout the transaction process as the user
     * advances or goes back between stages.
     */
    private int currentStageIndex = 0;
    /**
     * Represents the file paths for different stages of the cashier workflow.
     * Each element in this array corresponds to an FXML file that defines the UI layout
     * and structure for a specific step in the cashier's transaction process.
     *
     * The stages included are:
     * 1. Movie Search (CashierMovieSearch.fxml)
     * 2. Session Selection (CashierSessionSelect.fxml)
     * 3. Seat Selection (CashierSeatSelect.fxml)
     * 4. Customer Details (CashierCustomerDetails.fxml)
     * 5. Payment Processing (CashierPayment.fxml)
     *
     * This array is used to load specific FXML files as the user navigates through
     * the different steps of the transaction within the cashier interface.
     */
    private final String[] stages = {
            "/fxml/cashier/stageSpecificFiles/CashierMovieSearch.fxml",
            "/fxml/cashier/stageSpecificFiles/CashierSessionSelect.fxml",
            "/fxml/cashier/stageSpecificFiles/CashierSeatSelect.fxml",
            "/fxml/cashier/stageSpecificFiles/CashierCustomerDetails.fxml",
            "/fxml/cashier/stageSpecificFiles/CashierPayment.fxml"
    };

    /**
     * Represents the currently selected movie in the cashier flow.
     * This field stores an instance of the Movie class, which contains details about the selected movie
     * such as its title, genres, summary, poster image data, and duration.
     * It serves as the data passed between different stages of the transaction process in the cashier system.
     */
    // Data to pass between stages
    private Movie selectedMovie;
    /**
     * Represents the currently selected movie session in the CashierController workflow.
     * This variable stores the `MovieSession` object chosen by the user during the session selection stage.
     * It is used throughout the transaction process to retrieve details about the selected session,
     * including its schedule, time, hall, and availability.
     *
     * This variable is crucial for validating the session selection stage,
     * as well as for proceeding to seat selection, customer details entry, and final transaction confirmation.
     */
    private MovieSession selectedSession;
    /**
     * Stores the set of seat identifiers selected by the user during
     * the seat selection stage in the cashier process. Each entry in the set
     * represents a seat that has been chosen for the selected session.
     *
     * This field supports the seat selection functionality by holding unique
     * seat identifiers, preventing duplicate selections.
     *
     * The set of selected seats is used in the subsequent stages of the transaction
     * process, including customer details verification and payment processing.
     */
    private Set<String> selectedSeats = new HashSet<>();
    /**
     * Represents the selected date in the transaction process within the
     * CashierController. This variable is typically used to store a date
     * chosen by the cashier for scheduling, bookings, or other date-specific
     * operations, such as selecting a movie session.
     *
     * It is managed as part of the stage navigation logic in the controller
     * and validated in relevant stages to ensure the accuracy and completeness
     * of the transaction.
     */
    private LocalDate selectedDate;
    /**
     * Holds the reference to the currently logged-in user in the system.
     *
     * This variable is used across the CashierController to manage and retrieve
     * details of the user operating the system. It supports functions such as
     * user authentication, role-based authorization, and personalization of
     * the application experience.
     */
    private User currentUser;

    /**
     * Sets the current user for the CashierController and performs necessary initializations
     * that depend on the user's context. This method updates the shared state of the controller
     * with the provided user and triggers any dependent operations or configurations.
     *
     * @param user the user to set as the current user in the CashierController. This user object
     *             contains details such as username, role, and other user-specific credentials.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        headerController.setMainController(this);

        // Perform actions that depend on the user being set
        if (headerController != null) {
            headerController.getValidUser(currentUser);
        }
    }

    /**
     * Retrieves the current user associated with the system.
     *
     * @return The currently authenticated User object, or null if no user is authenticated.
     */
    // Method to get the current user
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Initializes the controller after the root element has been loaded.
     *
     * This method sets up initial configurations and relationships between the
     * components and controllers in the application. It performs the following
     * operations:
     *
     * 1. Sets the main controller reference for associated controllers such as
     *    the action bar and cashier cart controllers.
     * 2. Loads the initial stage of the application from the defined resource file.
     * 3. Assigns the main cashier controller to the newly loaded stage's controller.
     * 4. Replaces the current content area with the loaded initial stage.
     * 5. Updates the stepper stage controller if it exists to reflect the initial step.
     *
     * Any IOException encountered during the loading process is caught and
     * printed to the standard error stream.
     */
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

    /**
     * Navigates to a new stage in the application by processing the provided data
     * and updating the state accordingly. The behavior of this method varies
     * depending on the current stage index and the type of data passed.
     *
     * @param data the input data required to perform the navigation or update
     *             state. The expected type of data depends on the current stage:
     *             - For stage 0 (Movie Search): an instance of {@code Movie}
     *             - For stage 1 (Session Select): a {@code Map<String, Object>}
     *               containing a {@code MovieSession} and a {@code LocalDate}
     *             - For stage 2 (Seat Select): a {@code Set<String>} representing
     *               selected seat identifiers
     */
    public void navigateWithData(Object data) {
        boolean dataChanged = false;

        switch (currentStageIndex) {
            case 0: // Movie Search
                Movie newMovie = (Movie) data;
                if (newMovie != selectedMovie) {
                    System.out.println("selected new movie resetting ui and data");
                    clearPersistentDetailsStatic();
                    CashierSeatSelectController.clearSelectedSeatsStatic();
                    selectedMovie = newMovie;
                    selectedSession = null;
                    selectedDate = null;
                    selectedSeats = new HashSet<>();
                    cashierCartController.clearCart();
                    dataChanged = true;
                }
                break;

            case 1: // Session Select
                if (data instanceof Map) {
                    Map<String, Object> sessionData = (Map<String, Object>) data;
                    MovieSession newSession = (MovieSession) sessionData.get("session");
                    LocalDate newDate = (LocalDate) sessionData.get("date");

                    if (newSession != selectedSession || newDate != selectedDate) {
                        selectedSeats = new HashSet<>();
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

    /**
     * Advances the application to the next stage of the cashier workflow.
     *
     * This method increments the current stage index if the current stage is not
     * the last one and loads the corresponding stage. It updates the UI and internal
     * state by leveraging the private `loadStage` method.
     *
     * Preconditions:
     * - The `stages` array must be correctly initialized with stage information.
     * - `currentStageIndex` must reflect the current stage's index within the `stages` array.
     *
     * Postconditions:
     * - If not already on the last stage, the `currentStageIndex` is incremented by 1,
     *   and the corresponding stage is loaded.
     * - The UI is updated to reflect the new stage.
     * - Controllers such as a stepper or action bar may also be updated to match the new stage.
     */
    public void nextStage() {
        if (currentStageIndex < stages.length - 1) {
            loadStage(++currentStageIndex);
        }
    }

    /**
     * Navigates to the previous stage in the workflow if the current stage index
     * is greater than zero. Decrements the current stage index and loads the corresponding stage.
     *
     * The navigation updates the UI to reflect the previous stage in the sequence
     * and ensures proper management of stages within the application.
     *
     * Preconditions:
     * - The `currentStageIndex` must be greater than zero to navigate to a previous stage.
     *
     * Postconditions:
     * - The `currentStageIndex` is decremented by 1.
     * - The corresponding stage is loaded and displayed.
     *
     * Relies on:
     * - The `loadStage(int index)` method to load and display the appropriate stage.
     */
    public void previousStage() {
        if (currentStageIndex > 0) {
            loadStage(--currentStageIndex);
        }
    }

    /**
     * Retrieves the currently selected date.
     *
     * @return the selected date as a LocalDate object, or null if no date is selected
     */
    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * Loads a specific stage in the application based on the provided index.
     * Updates the current stage and related UI elements such as the stage stack, stepper, and action bar.
     *
     * @param index The index of the stage to load. Must correspond to a valid entry in the stages array.
     */
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

    /**
     * Configures the given controller based on its type and associates
     * it with the {@code CashierController}, restoring state where applicable.
     * This method is used to initialize specific controllers to ensure they
     * have the necessary references and data to function properly within
     * the cashier workflow.
     *
     * @param controller The controller instance to set up, which must be
     *                    one of the following types:
     *                    {@code CashierMovieSearchController},
     *                    {@code CashierSessionSelectController},
     *                    {@code CashierSeatSelectController},
     *                    {@code CashierCustomerDetailsController}, or
     *                    {@code CashierPaymentController}.
     */
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

    /**
     * Resets the current transaction state by clearing all stored selections,
     * resetting the current stage to the initial state, and resetting associated controllers.
     *
     * This method:
     * - Clears the selected movie, session, seats, and other transaction-related data.
     * - Resets the stage navigation to the initial index (typically the first stage).
     * - Resets the shopping cart via the cashier cart controller, if available.
     * - Resets customer details via the customer details controller, if available.
     *
     * Use this method to start a new transaction or to clear the current transaction
     * data for reinitialization purposes.
     */
    public void resetTransaction() {
        // Reset all stored data
        selectedMovie = null;
        selectedSession = null;
        selectedSeats = new HashSet<>(); // Reset to empty set rather than null
        CashierSeatSelectController.clearSelectedSeatsStatic();

        // Reset to first stage
        currentStageIndex = 0;
        loadStage(currentStageIndex);

        // Clear the cart using the injected controller
        if (cashierCartController != null) {
            cashierCartController.clearCart();
        }

        // Clear persistent customer details
        if (customerDetailsController != null) {
            customerDetailsController.resetUI();
        }
    }

    /**
     * Retrieves the index of the current stage in the multi-stage workflow process.
     *
     * @return the current stage index as an integer. This value represents the position
     *         in the workflow where the process is currently active.
     */
    public int getCurrentStageIndex() {
        return currentStageIndex;
    }

    /**
     * Retrieves the instance of CashierCartController associated with the CashierController.
     *
     * @return the CashierCartController instance that is responsible for managing cart-related functionalities.
     */
    public CashierCartController getCartController() {
        return cashierCartController;
    }

    /**
     * Retrieves the {@link CashierActionBarController} instance associated with this controller.
     *
     * @return the {@link CashierActionBarController} instance responsible for handling the
     *         action bar's state and behavior within the cashier workflow.
     */
    public CashierActionBarController getActionBarController() {
        return actionBarController;
    }

    /**
     * Retrieves the controller responsible for handling customer details in the cashier system.
     *
     * @return the instance of CashierCustomerDetailsController managing customer details.
     */
    public CashierCustomerDetailsController getCustomerDetailsController() {
        return customerDetailsController;
    }

    /**
     * Retrieves the currently selected movie in the system.
     *
     * @return The currently selected Movie object, or null if no movie is selected.
     */
    // Getters for stored data
    public Movie getSelectedMovie() { return selectedMovie; }
    /**
     * Retrieves the currently selected movie session.
     * This method provides access to the {@code MovieSession} object
     * associated with the user's current selection in the application.
     *
     * @return the {@code MovieSession} object that is currently selected,
     *         or {@code null} if no session has been selected.
     */
    public MovieSession getSelectedSession() { return selectedSession; }
    /**
     * Retrieves the set of seat identifiers that have been selected.
     *
     * @return a set of strings representing the selected seat IDs.
     */
    public Set<String> getSelectedSeats() { return selectedSeats; }
}