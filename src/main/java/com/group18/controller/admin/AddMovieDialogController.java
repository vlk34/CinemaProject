package com.group18.controller.admin;

import com.group18.dao.MovieDAO;
import com.group18.model.Movie;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class AddMovieDialogController {
    @FXML private TextField titleField;
    @FXML private TextArea summaryField;
    @FXML private TextField durationField;
    @FXML private ImageView posterImageView;
    @FXML private Button selectPosterButton;
    @FXML private Button addMovieButton;
    @FXML private Button cancelButton;
    @FXML private ComboBox<String> genreComboBox;

    private MovieDAO movieDAO;
    private String currentPosterPath;
    private Stage dialogStage;

    @FXML
    private void initialize() {
        movieDAO = new MovieDAO();

        // Setup genre combo box
        genreComboBox.getItems().addAll("Action", "Comedy", "Drama", "Horror", "Science Fiction");

        // Disable add movie button initially
        addMovieButton.setDisable(true);

        // Setup validation listeners
        setupValidation();

        selectPosterButton.setOnAction(event -> handleSelectPoster());
        addMovieButton.setOnAction(event -> handleAddMovie());
        cancelButton.setOnAction(event -> dialogStage.close());
    }

    private void setupValidation() {
        // Title validation
        titleField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInputs();
        });

        // Genre validation
        genreComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            validateInputs();
        });

        // Summary validation
        summaryField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInputs();
        });

        durationField.setText("120");
        durationField.setEditable(false);
    }

    private void validateInputs() {
        // Check all input fields
        boolean isTitleValid = !titleField.getText().trim().isEmpty();
        boolean isGenreValid = genreComboBox.getValue() != null;
        boolean isSummaryValid = !summaryField.getText().trim().isEmpty();

        // Validate duration
        boolean isDurationValid = false;
        try {
            int duration = Integer.parseInt(durationField.getText().trim());
            isDurationValid = duration >= 60 && duration <= 120;
        } catch (NumberFormatException e) {
            isDurationValid = false;
        }

        // Validate poster (optional)
        boolean isPosterValid = currentPosterPath != null && !currentPosterPath.isEmpty();

        // Enable/disable add movie button
        addMovieButton.setDisable(!(isTitleValid && isGenreValid &&
                isSummaryValid && isDurationValid && isPosterValid));
    }

    private void handleAddMovie() {
        try {
            String title = titleField.getText().trim();
            String genre = genreComboBox.getValue();
            String summary = summaryField.getText().trim();
            int duration = 120;

            // Create new movie
            Movie newMovie = new Movie(title, genre, summary, currentPosterPath, duration);

            if (movieDAO.addMovie(newMovie)) {
                showAlert(Alert.AlertType.INFORMATION, "Success", "Movie added successfully.");
                dialogStage.close();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add movie.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    private void handleSelectPoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.setInitialDirectory(new File("src/main/resources/images/movies"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectPosterButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                posterImageView.setImage(new Image(selectedFile.toURI().toString()));

                // Store the relative path for database
                currentPosterPath = "/images/movies/" + selectedFile.getName();

                // Validate inputs after poster selection
                validateInputs();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to select poster: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}