package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.dao.ScheduleDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.*;
import javafx.geometry.Pos;
import com.group18.model.Movie;
import com.group18.model.Schedule;
import com.group18.model.MovieSession;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class CashierSessionSelectController {
    @FXML private ImageView moviePosterView;
    @FXML private Label movieTitleLabel;
    @FXML private Label movieGenresLabel;
    @FXML private DatePicker datePicker;
    @FXML private GridPane sessionsGrid;

    private Movie selectedMovie;
    private Schedule selectedSchedule;
    private List<Schedule> availableSchedules = new ArrayList<>();
    private ScheduleDAO scheduleDAO;
    private CashierController cashierController;

    @FXML
    private void initialize() {
        scheduleDAO = new ScheduleDAO();
        setupDatePicker();
    }

    private void setupDatePicker() {
        datePicker.setValue(LocalDate.now());
        updateAvailableDates(); // Call this whenever movie changes

        datePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();

                // Disable if: empty, past date, more than 30 days ahead, or no schedules
                boolean hasSchedules = availableSchedules.stream()
                        .anyMatch(s -> s.getSessionDate().equals(date));

                setDisable(empty ||
                        date.compareTo(today) < 0 ||
                        date.compareTo(today.plusDays(30)) > 0 ||
                        !hasSchedules);

                // Optional: Style disabled dates differently
                if (isDisabled()) {
                    setStyle("-fx-background-color: #f0f0f0;");
                }
            }
        });
    }

    // Add this method to update available dates when movie changes
    private void updateAvailableDates() {
        if (selectedMovie != null) {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(30);
            availableSchedules = scheduleDAO.getSchedulesBetweenDates(
                    selectedMovie.getMovieId(),
                    today,
                    endDate
            );

            // Optional: Trigger a refresh of the date picker to reflect available dates
            datePicker.setDayCellFactory(datePicker.getDayCellFactory());
        }
    }

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    // Update setMovie to also refresh available dates
    public void setMovie(Movie movie) {
        this.selectedMovie = movie;
        updateMovieDetails();
        updateAvailableDates(); // Add this line
        datePicker.setValue(LocalDate.now()); // Reset to today's date
    }

    private void updateMovieDetails() {
        if (selectedMovie != null) {
            loadMoviePoster();
            movieTitleLabel.setText(selectedMovie.getTitle());
            movieGenresLabel.setText(selectedMovie.getGenre());
        }
    }

    private void loadMoviePoster() {
        if (selectedMovie == null || selectedMovie.getPosterPath() == null) {
            // Set default image if no movie or poster path
            moviePosterView.setImage(new Image(getClass().getResourceAsStream("/images/movies/default_poster.jpg")));
            return;
        }

        try {
            // Try loading from resource path first
            Image resourceImage = new Image(getClass().getResourceAsStream(selectedMovie.getPosterPath()));

            // If resource image is valid, use it
            if (resourceImage.isError()) {
                // If resource loading fails, try file path
                File posterFile = new File(selectedMovie.getPosterPath());
                if (posterFile.exists()) {
                    resourceImage = new Image(posterFile.toURI().toString());
                }
            }

            // Set image, fallback to default if still invalid
            moviePosterView.setImage(resourceImage.isError()
                    ? new Image(getClass().getResourceAsStream("/images/movies/default_poster.jpg"))
                    : resourceImage);

        } catch (Exception e) {
            // Fallback to default image if any exception occurs
            moviePosterView.setImage(new Image(getClass().getResourceAsStream("/images/movies/default_poster.jpg")));
            System.err.println("Error loading poster: " + e.getMessage());
        }
    }

    private Image getDefaultImage() {
        return new Image(getClass().getResourceAsStream("/images/movies/default_poster.jpg"));
    }

    @FXML
    private void handleShowSessions() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null && selectedMovie != null) {
            List<Schedule> sessionsForDate = scheduleDAO.getAvailableSchedules(selectedMovie.getMovieId(), selectedDate);

            if (sessionsForDate.isEmpty()) {
                // Clear the grid and show an informative message
                clearSessionsGrid();

                // Add a label to inform the user no sessions are available
                Label noSessionsLabel = new Label("No sessions available for the selected date.");
                noSessionsLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-padding: 10;");
                sessionsGrid.addRow(1, noSessionsLabel);
            } else {
                // Update available schedules and display them
                availableSchedules = sessionsForDate;
                displaySessions();
            }
        }
    }

    private void loadAvailableSessions(LocalDate date) {
        availableSchedules = scheduleDAO.getAvailableSchedules(selectedMovie.getMovieId(), date);
    }

    private void displaySessions() {
        clearSessionsGrid();
        populateSessionsGrid();
    }

    private void clearSessionsGrid() {
        sessionsGrid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0
        );
    }

    private void populateSessionsGrid() {
        int row = 1;
        for (Schedule schedule : availableSchedules) {
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

    private void addSessionToGrid(MovieSession session, int row) {
        Label timeLabel = new Label(session.getTime().toString());
        timeLabel.setStyle("-fx-padding: 8 15; -fx-alignment: CENTER;");

        Label hallLabel = new Label(session.getHall());
        hallLabel.setStyle("-fx-padding: 8 15; -fx-alignment: CENTER;");

        Label seatsLabel = new Label(session.getAvailableSeats() + " seats");
        seatsLabel.setStyle("-fx-padding: 8 15; -fx-alignment: CENTER;");

        Button selectButton = createSelectButton(session);
        HBox buttonBox = new HBox(selectButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 8 15;");

        sessionsGrid.addRow(row, timeLabel, hallLabel, seatsLabel, buttonBox);
    }

    private Button createSelectButton(MovieSession session) {
        Button button = new Button("Select");
        button.setStyle("-fx-background-color: #2a1b35; -fx-text-fill: white; -fx-padding: 5 15;");
        button.setDisable(session.getAvailableSeats() == 0);
        button.setOnAction(e -> handleSessionSelection(session));
        return button;
    }

    private void handleSessionSelection(MovieSession session) {
        Alert confirm = createConfirmationDialog(session);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK && cashierController != null) {
                Map<String, Object> sessionData = new HashMap<>();
                sessionData.put("session", session);
                sessionData.put("date", datePicker.getValue());
                cashierController.navigateWithData(sessionData);
            }
        });
    }

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

    public LocalDate getSelectedDate() {
        return datePicker.getValue();
    }
}