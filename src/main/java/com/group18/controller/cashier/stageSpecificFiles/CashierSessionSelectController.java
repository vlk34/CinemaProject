package com.group18.controller.cashier.stageSpecificFiles;

import com.group18.controller.cashier.CashierController;
import com.group18.controller.cashier.sharedComponents.CashierCartController;
import com.group18.dao.ScheduleDAO;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
import java.util.stream.Collectors;

public class CashierSessionSelectController {
    @FXML private ImageView moviePosterView;
    @FXML private Label movieTitleLabel;
    @FXML private FlowPane genresContainer;
    @FXML private DatePicker datePicker;
    @FXML private GridPane sessionsGrid;

    private Movie selectedMovie;
    private Schedule selectedSchedule;
    private List<Schedule> allAvailableSchedules = new ArrayList<>();  // For all dates
    private List<Schedule> currentDateSchedules = new ArrayList<>();   // For current date
    private ScheduleDAO scheduleDAO;
    private CashierController cashierController;
    private MovieSession previouslySelectedSession;
    private LocalDate previouslySelectedDate;

    @FXML
    private void initialize() {
        scheduleDAO = new ScheduleDAO();
        setupDatePicker();
    }

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

    public void setCashierController(CashierController controller) {
        this.cashierController = controller;
    }

    public void setMovie(Movie movie) {
        this.selectedMovie = movie;
        updateMovieDetails();
        updateAvailableDates();
    }

    public void restorePreviousSelection(MovieSession session, LocalDate date) {
        this.previouslySelectedSession = session;
        this.previouslySelectedDate = date;

        if (date != null) {
            datePicker.setValue(date);
        }

        if (selectedMovie != null && date != null) {
            handleShowSessions();
        }
    }

    private void updateMovieDetails() {
        if (selectedMovie != null) {
            loadMoviePoster();
            movieTitleLabel.setText(selectedMovie.getTitle());
            updateGenresDisplay();
        }
    }

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

    private void loadMoviePoster() {
        if (selectedMovie == null || selectedMovie.getPosterPath() == null) {
            moviePosterView.setImage(new Image(getClass().getResourceAsStream("/images/movies/default_poster.jpg")));
            return;
        }

        try {
            Image resourceImage = new Image(getClass().getResourceAsStream(selectedMovie.getPosterPath()));

            if (resourceImage.isError()) {
                File posterFile = new File(selectedMovie.getPosterPath());
                if (posterFile.exists()) {
                    resourceImage = new Image(posterFile.toURI().toString());
                }
            }

            moviePosterView.setImage(resourceImage.isError()
                    ? new Image(getClass().getResourceAsStream("/images/movies/default_poster.jpg"))
                    : resourceImage);

        } catch (Exception e) {
            moviePosterView.setImage(new Image(getClass().getResourceAsStream("/images/movies/default_poster.jpg")));
            System.err.println("Error loading poster: " + e.getMessage());
        }
    }

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