package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.controller.cashier.sharedComponents.CashierCartController;
import com.group18.dao.ScheduleDAO;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.geometry.Pos;
import com.group18.model.Movie;
import com.group18.model.Schedule;
import com.group18.model.MovieSession;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The CashierSessionSelectController class manages the user interface for selecting movie sessions.
 * It handles interactions such as selecting a movie, choosing a date, and displaying available
 * sessions. The controller integrates with the cashier functionality and utilizes a data access
 * object (DAO) to interact with session scheduling data.
 *
 * Core functionalities include:
 * - Displaying movie details (poster, title, genres).
 * - Configuring and validating date selection for session scheduling.
 * - Loading and displaying session information based on the selected movie and date.
 * - Providing hover animations and interactive UI elements for better user experience.
 * - Retaining state such as previously selected session and date for seamless navigation.
 */
public class CashierSessionSelectController {
    /**
     * An ImageView component associated with displaying the poster of a movie.
     * This field is part of the user interface controlled by the CashierSessionSelectController.
     * It is dynamically updated to reflect the currently selected movie's poster.
     */
    @FXML private ImageView moviePosterView;
    /**
     * Represents the label used to display the title of the currently selected movie
     * within the cashier session selection interface.
     * This label is dynamically updated based on the movie selection and is linked
     * to the FXML file for the graphical user interface.
     */
    @FXML private Label movieTitleLabel;
    /**
     * A FlowPane container that is used to display the genres associated with the currently selected movie.
     * This UI component dynamically updates to reflect the genres of the movie set in the controller.
     * Each genre is visually represented, typically as a label or styled node within the pane.
     */
    @FXML private FlowPane genresContainer;
    /**
     * The DatePicker control allows users to select a date.
     * It is used within the CashierSessionSelectController class to let users choose a date for viewing available movie sessions.
     * The selected date determines which movie sessions are displayed on that particular day.
     */
    @FXML private DatePicker datePicker;
    /**
     * Represents the layout container for displaying available movie sessions
     * as a grid in the CashierSessionSelectController.
     *
     * The sessionsGrid is utilized to dynamically arrange and display individual
     * session details, such as time and selection buttons. It organizes the
     * sessions into rows and columns, allowing clear and concise presentation
     * of session options for a selected movie.
     *
     * This field is annotated with @FXML to enable interaction and manipulation
     * through FXML-defined UI components.
     */
    @FXML private GridPane sessionsGrid;
    /**
     * Represents a button in the UI that triggers the action to display available sessions
     * for the selected movie on the selected date. The button is linked to the `handleShowSessions`
     * method, which executes the logic to populate and display the sessions grid based on user input.
     */
    @FXML private Button showSessionsButton;

    /**
     * Represents the movie currently selected by the user in the CashierSessionSelectController.
     * This field holds the details of a specific movie object, which includes properties such as
     * title, genres, summary, duration, and poster data.
     * It is used throughout the application context for managing and displaying information
     * and sessions related to the selected movie.
     */
    private Movie selectedMovie;
    /**
     * Represents the currently selected schedule in the context of a cashier session selection process.
     * This variable holds a reference to the {@code Schedule} object that the user has chosen for further actions,
     * such as finalizing a booking or displaying session details.
     */
    private Schedule selectedSchedule;
    /**
     * A list that contains all schedules currently available across multiple dates.
     * This variable is intended to store instances of the {@link Schedule} class,
     * representing the scheduling details for various movie sessions.
     *
     * It is primarily maintained and updated by the {@code CashierSessionSelectController} class
     * to manage and display schedules for selection.
     */
    private List<Schedule> allAvailableSchedules = new ArrayList<>();  // For all dates
    /**
     * Holds a list of schedule entries for the current date.
     * This variable is intended to store and manage a collection of schedules
     * that are specific to the date currently being viewed or processed.
     */
    private List<Schedule> currentDateSchedules = new ArrayList<>();   // For current date
    /**
     * The ScheduleDAO instance used for interacting with the database to perform
     * operations related to movie schedules. It provides various methods for
     * retrieving, creating, updating, and deleting schedule records.
     *
     * This variable is likely utilized to manage schedule-related tasks such as
     * retrieving available schedules, checking schedule availability, and
     * maintaining session integrity within the application.
     */
    private ScheduleDAO scheduleDAO;
    /**
     * Represents a reference to the CashierController used to manage
     * interactions between the cashier's interface and the current session selection workflow.
     * Allows communication and coordination of actions or data between
     * the cashier-related functionalities and the session selection process.
     */
    private CashierController cashierController;
    /**
     * Stores a reference to the previously selected movie session.
     * This variable is used to track and manage the session that was most recently chosen by the user.
     * It allows for actions such as highlighting the selected session or enabling session-specific functionality.
     */
    private MovieSession previouslySelectedSession;
    /**
     * Represents the previously selected date in the context of the cashier session selection.
     * This variable helps in tracking and restoring the last selected date in the date picker,
     * ensuring a consistent user experience when navigating through different sessions or views.
     */
    private LocalDate previouslySelectedDate;

    /**
     * Initializes the controller and sets up the necessary components.
     *
     * This method is automatically invoked when the associated FXML file is loaded.
     * It performs the following actions:
     *
     * 1. Instantiates the ScheduleDAO object to handle data access operations related to schedules.
     * 2. Configures the date picker widget for schedule selection.
     * 3. Adds a hover animation effect to the "Show Available Sessions" button to enhance the user experience.
     */
    @FXML
    private void initialize() {
        scheduleDAO = new ScheduleDAO();
        setupDatePicker();

        // Add hover effect to "Show Available Sessions" button
        addHoverAnimationToButton(showSessionsButton);
    }

    /**
     * Configures the date picker by initializing its value, defining selectable dates,
     * and setting up a custom day cell factory for date validation and styling.
     *
     * The method initializes the date picker value to a previously selected date if available,
     * or to the current date. It defines a 30-day range of selectable dates starting from today.
     * Only dates with associated schedules are selectable, and past dates or those outside the
     * defined range are disabled. Additionally, disabled dates are styled with a distinct background color.
     */
    private void setupDatePicker() {
        LocalDate initialDate = previouslySelectedDate != null ? previouslySelectedDate : LocalDate.now();
        datePicker.setValue(initialDate);
        updateAvailableDates();

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                boolean hasSchedules = allAvailableSchedules.stream()
                        .anyMatch(s -> s.getSessionDate().equals(date));

                setDisable(empty ||
                        date.compareTo(today) < 0 ||
                        date.compareTo(today.plusDays(30)) > 0 ||
                        !hasSchedules);

                if (isDisabled()) {
                    setStyle("-fx-background-color: #f0f0f0;");
                }
            }
        });
    }

    /**
     * Updates the available dates for scheduling based on the selected movie and current date.
     *
     * This method retrieves a list of schedules for the selected movie within a date range
     * spanning from the current date to 30 days into the future. If a movie is selected,
     * it fetches schedules using the scheduleDAO and updates the day cell factory for the
     * date picker. If a date is already selected in the date picker, the method invokes
     * the handling of session display for the selected date.
     *
     * Preconditions:
     * - A movie must be selected (selectedMovie is not null).
     *
     * Postconditions:
     * - allAvailableSchedules is populated with schedules from the database for the selected movie.
     * - The date picker's day cell factory is updated.
     * - If a date is selected in the date picker, session details for that date are displayed.
     */
    private void updateAvailableDates() {
        if (selectedMovie != null) {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(30);

            // Load all available schedules
            allAvailableSchedules = scheduleDAO.getSchedulesBetweenDates(
                    selectedMovie.getMovieId(),
                    today,
                    endDate
            );

            datePicker.setDayCellFactory(datePicker.getDayCellFactory());

            if (datePicker.getValue() != null) {
                handleShowSessions();
            }
        }
    }

    /**
     * Sets the CashierController instance for this controller, enabling interaction between
     * this controller and the cashier functionalities. This method also restores previously
     * selected movie session and date, if available, and updates the session display accordingly.
     *
     * @param controller the CashierController instance to set for this controller
     */
    public void setCashierController(CashierController controller) {
        this.cashierController = controller;

        // Get previous session and date from cashier controller
        MovieSession prevSession = controller.getSelectedSession();
        LocalDate prevDate = controller.getSelectedDate();

        if (prevSession != null && prevDate != null) {
            // Set these as our previously selected values
            this.previouslySelectedSession = prevSession;
            this.previouslySelectedDate = prevDate;

            // If we already have a movie set, we can restore the session display
            if (selectedMovie != null) {
                datePicker.setValue(prevDate);
                updateAvailableDates();
                handleShowSessions();
            }
        }
    }

    /**
     * Sets the selected movie for the session selection view and updates the relevant UI components.
     * This method updates the displayed movie details based on the selected movie, resets the date picker
     * if a previously selected date exists, and refreshes the available dates for session selection.
     *
     * @param movie The movie to be selected. Provides details to update the UI components, such as the movie title and poster.
     */
    public void setMovie(Movie movie) {
        this.selectedMovie = movie;
        updateMovieDetails();

        // If we have a previous selection, use it
        if (previouslySelectedDate != null) {
            datePicker.setValue(previouslySelectedDate);
        }

        updateAvailableDates();
    }

    /**
     * Updates the details of the currently selected movie on the user interface.
     * This involves loading the movie's poster image, updating the movie title label,
     * and displaying the genres associated with the movie.
     *
     * The method performs the following steps:
     * 1. Checks if a movie is selected using the `selectedMovie` field.
     * 2. Calls {@link #loadMoviePoster()} to load or set the movie's poster.
     * 3. Sets the movie title by updating the text of `movieTitleLabel`.
     * 4. Calls {@link #updateGenresDisplay()} to reflect the genres of the selected movie.
     *
     * If no movie is currently selected, no updates are performed.
     */
    private void updateMovieDetails() {
        if (selectedMovie != null) {
            loadMoviePoster();
            movieTitleLabel.setText(selectedMovie.getTitle());
            updateGenresDisplay();
        }
    }

    /**
     * Updates the display of movie genres in the genresContainer.
     *
     * This method clears the current content of the genresContainer and dynamically
     * generates a styled label for each genre associated with the selected movie.
     * Each label is styled with a light background, rounded corners, padding, and
     * custom text color. Margins are added between labels for better visual layout.
     * The resulting labels are added to the genresContainer for display.
     *
     * This method assumes the selectedMovie object is not null and its associated
     * genres can be retrieved through the getGenres method, which returns a set
     * of strings.
     */
    private void updateGenresDisplay() {
        genresContainer.getChildren().clear();

        // Create a label for each genre
        for (String genre : selectedMovie.getGenres()) {
            Label genreLabel = new Label(genre);
            genreLabel.setStyle("-fx-background-color: #f0f0f0; " +
                    "-fx-padding: 5 10; " +
                    "-fx-background-radius: 15; " +
                    "-fx-text-fill: #2a1b35;");
            // Add margin between genre labels
            FlowPane.setMargin(genreLabel, new Insets(0, 5, 5, 0));
            genresContainer.getChildren().add(genreLabel);
        }
    }

    /**
     * Loads and displays the movie poster image for the currently selected movie.
     * If the selected movie's poster data is available and valid, it is displayed in the
     * movie poster view. Otherwise, a default poster image is displayed.
     *
     * The method retrieves the poster data as a byte array from the selected movie.
     * If the byte array is not null and contains data, it attempts to create an image
     * object from the data. If the image creation fails or if the byte array is invalid,
     * the default poster image is loaded instead. It also handles any exceptions
     * that occur during the image loading process.
     */
    private void loadMoviePoster() {
        // Handle poster image using byte array data
        if (selectedMovie != null && selectedMovie.getPosterData() != null &&
                selectedMovie.getPosterData().length > 0) {
            try {
                Image image = new Image(new ByteArrayInputStream(selectedMovie.getPosterData()));
                if (!image.isError()) {
                    moviePosterView.setImage(image);
                } else {
                    setDefaultPoster();
                }
            } catch (Exception e) {
                e.printStackTrace();
                setDefaultPoster();
            }
        } else {
            setDefaultPoster();
        }
    }

    /**
     * Sets a default poster image to the moviePosterView.
     *
     * This method is used as a fallback when a custom movie poster cannot be loaded.
     * It retrieves the default poster image from the local resource directory and displays it.
     * If an error occurs during the loading process, the moviePosterView image will be set to null.
     */
    private void setDefaultPoster() {
        try {
            byte[] defaultImageData = getClass().getResourceAsStream("/images/movies/dark_knight.jpg").readAllBytes();
            Image defaultImage = new Image(new ByteArrayInputStream(defaultImageData));
            moviePosterView.setImage(defaultImage);
        } catch (Exception e) {
            e.printStackTrace();
            moviePosterView.setImage(null);
        }
    }

    /**
     * Handles the action for displaying available sessions based on the selected movie
     * and date chosen in the date picker. This method filters and processes session
     * data to show relevant schedules.
     *
     * If a valid date is selected in the date picker and a movie is currently selected,
     * the method retrieves all schedules corresponding to the selected date from the
     * complete list of available schedules. If schedules are found for the selected
     * date, they are displayed using helper methods. If no schedules are found, a
     * message indicating the unavailability of sessions is displayed in the UI.
     *
     * The method performs the following key steps:
     * - Retrieves the currently selected date from the date picker.
     * - Filters all available schedules to match the selected date.
     * - Clears the sessions UI grid if no schedules are found and displays a notification.
     * - Calls helper methods to display the filtered sessions if schedules are available.
     */
    @FXML
    private void handleShowSessions() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null && selectedMovie != null) {
            // Filter schedules for the selected date from all available schedules
            currentDateSchedules = allAvailableSchedules.stream()
                    .filter(s -> s.getSessionDate().equals(selectedDate))
                    .collect(Collectors.toList());

            if (currentDateSchedules.isEmpty()) {
                clearSessionsGrid();
                Label noSessionsLabel = new Label("No sessions available for the selected date.");
                noSessionsLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-padding: 10;");
                sessionsGrid.addRow(1, noSessionsLabel);
            } else {
                displaySessions();
            }
        }
    }

    /**
     * Adds a hover animation to the given button, creating a scale effect
     * when the mouse pointer enters and exits the button area.
     *
     * @param button the button to which the hover animation will be applied
     */
    private void addHoverAnimationToButton(Button button) {
        // Add hover animation for scale effect
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
            scaleUp.setToX(1.03);
            scaleUp.setToY(1.03);
            scaleUp.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
            scaleDown.setToX(1.0);
            scaleDown.setToY(1.0);
            scaleDown.play();
        });
    }

    /**
     * Updates the sessions grid by clearing any existing session entries and
     * populating it with session data for the currently selected date.
     * This method combines the functionality of clearing previous session data
     * from the grid and adding updated session information based on the current
     * availability.
     *
     * The method is typically invoked when the sessions need to be refreshed,
     * such as when a new date is selected or session data is updated.
     *
     * It relies on auxiliary methods:
     * - `clearSessionsGrid()`: Removes existing session entries from the grid,
     *   keeping only non-session related content intact.
     * - `populateSessionsGrid()`: Adds new session entries to the grid based on
     *   the filtered schedules for the selected date.
     */
    private void displaySessions() {
        clearSessionsGrid();
        populateSessionsGrid();
    }

    /**
     * Clears all the rows in the sessions grid except for the header row.
     *
     * This method removes all child nodes from the sessionsGrid where their
     * row index is greater than zero. It is typically used to reset the grid
     * before populating it with new or updated session data.
     */
    private void clearSessionsGrid() {
        sessionsGrid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0
        );
    }

    /**
     * Populates the sessions grid with movie session data based on the current date's schedules.
     * For each schedule in the `currentDateSchedules` list, this method:
     * - Retrieves the number of available seats corresponding to the schedule's ID
     *   by using the `scheduleDAO.getAvailableSeatsCount` method.
     * - Determines the hall name based on the `hallId` associated with the schedule.
     * - Creates a `MovieSession` object encapsulating the relevant details such as
     *   schedule ID, hall name, session time, and available seats.
     * - Adds the created `MovieSession` object to the sessions grid at the specified row
     *   by calling the `addSessionToGrid` method.
     */
    private void populateSessionsGrid() {
        int row = 1;
        for (Schedule schedule : currentDateSchedules) {
            int availableSeats = scheduleDAO.getAvailableSeatsCount(schedule.getScheduleId());
            String hallName = schedule.getHallId() == 1 ? "Hall_A" : "Hall_B";

            MovieSession session = new MovieSession(
                    schedule.getScheduleId(),
                    hallName,
                    schedule.getSessionTime(),
                    availableSeats
            );

            addSessionToGrid(session, row++);
        }
    }

    /**
     * Adds a movie session to the grid at a specific row, displaying its details
     * such as time, hall, available seats, and a selection button. If the session
     * matches the previously selected schedule, the button is visually highlighted.
     *
     * @param session the {@code MovieSession} object containing details such as time, hall,
     *                available seats, and schedule ID
     * @param row     the row index in the grid where the session information should be added
     */
    private void addSessionToGrid(MovieSession session, int row) {
        Label timeLabel = new Label(session.getTime().toString());
        timeLabel.setStyle("-fx-padding: 8 15; -fx-alignment: CENTER;");

        Label hallLabel = new Label(session.getHall());
        hallLabel.setStyle("-fx-padding: 8 15; -fx-alignment: CENTER;");

        Label seatsLabel = new Label(session.getAvailableSeats() + " seats");
        seatsLabel.setStyle("-fx-padding: 8 15; -fx-alignment: CENTER;");

        Button selectButton = createSelectButton(session);

        if (previouslySelectedSession != null &&
                previouslySelectedSession.getScheduleId() == session.getScheduleId()) {
            selectButton.setStyle(selectButton.getStyle() + "; -fx-background-color: #4a3b55;");
        }

        HBox buttonBox = new HBox(selectButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 8 15;");

        sessionsGrid.addRow(row, timeLabel, hallLabel, seatsLabel, buttonBox);
    }

    /**
     * Creates a button for selecting a specific movie session. The button appearance
     * and behavior are configured, including styles, hover animations, and click actions.
     * The button is disabled if the session does not have available seats.
     *
     * @param session the {@code MovieSession} instance that this button corresponds to
     * @return a {@code Button} configured with the appropriate styles, animations, and actions
     */
    private Button createSelectButton(MovieSession session) {
        Button button = new Button("Select");
        button.setStyle("-fx-background-color: #2a1b35; -fx-text-fill: white; -fx-padding: 5 15;");
        button.setDisable(session.getAvailableSeats() == 0);

        // Add hover animation
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleUp = new ScaleTransition(Duration.millis(100), button);
            scaleUp.setToX(1.03); // Slightly increase the button size
            scaleUp.setToY(1.03);
            scaleUp.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scaleDown = new ScaleTransition(Duration.millis(100), button);
            scaleDown.setToX(1.0); // Reset to the original size
            scaleDown.setToY(1.0);
            scaleDown.play();
        });

        button.setOnAction(e -> handleSessionSelection(session));
        return button;
    }

    /**
     * Handles the user's selection of a movie session, confirming the selection and performing
     * necessary updates such as clearing the cart and refreshing the user interface.
     * Upon confirmation, it navigates to the next screen with the selected session data.
     *
     * @param session the {@code MovieSession} object representing the selected movie session
     */
    private void handleSessionSelection(MovieSession session) {
        Alert confirm = createConfirmationDialog(session);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && cashierController != null) {
                // Clear cart when selecting a new session and force UI update
                CashierCartController cartController = cashierController.getCartController();
                if (cartController != null) {
                    cartController.clearCart();
                    // Explicitly update the summary
                    cartController.updateSummary();
                }

                CashierSeatSelectController.clearSelectedSeatsStatic();


                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("session", session);
                sessionData.put("date", datePicker.getValue());
                cashierController.navigateWithData(sessionData);
            }
        });
    }

    /**
     * Creates a confirmation dialog for a selected movie session. The dialog displays
     * the session details such as the movie title, session date, time, hall, and
     * the number of available seats.
     *
     * @param session the {@code MovieSession} object containing details of the
     *                selected movie session
     * @return an {@code Alert} object configured as a confirmation dialog displaying
     *         the session details
     */
    private Alert createConfirmationDialog(MovieSession session) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Session");
        confirm.setHeaderText("Selected Session Details");
        confirm.setContentText(String.format(
                "Movie: %s%nDate: %s%nTime: %s%nHall: %s%nAvailable Seats: %d",
                selectedMovie.getTitle(),
                datePicker.getValue(),
                session.getTime(),
                session.getHall(),
                session.getAvailableSeats()
        ));
        return confirm;
    }
}