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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class AddMovieDialogController {
    @FXML private TextField titleField;
    @FXML private TextField genreField;
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
        genreComboBox.getItems().addAll("Action", "Comedy", "Drama", "Horror", "Science Fiction");
        selectPosterButton.setOnAction(event -> handleSelectPoster());
        addMovieButton.setOnAction(event -> handleAddMovie());
        cancelButton.setOnAction(event -> dialogStage.close());
    }

    private void handleAddMovie() {
        try {
            String title = titleField.getText().trim();
            String genre = genreComboBox.getValue();
            String summary = summaryField.getText().trim();
            String durationStr = durationField.getText().trim();

            // Validate all fields are filled
            if (title.isEmpty() || genre == null || summary.isEmpty() || durationStr.isEmpty()) {
                showAlert("Validation Error", "Please fill in all fields.");
                return;
            }

            // Parse and validate duration
            int duration;
            try {
                duration = Integer.parseInt(durationStr);
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid duration format. Please enter a valid number.");
                return;
            }

            // Validate duration range
            if (duration < 60 || duration > 120) {
                showAlert("Invalid Duration",
                        "Movie duration must be between 60 and 120 minutes.\n" +
                                "Please enter a duration between 1 and 2 hours.");
                return;
            }

            // Create new movie
            Movie newMovie = new Movie(title, genre, summary, currentPosterPath, duration);

            if (movieDAO.addMovie(newMovie)) {
                showAlert("Success", "Movie added successfully.");
                dialogStage.close();
            } else {
                showAlert("Error", "Failed to add movie.");
            }
        } catch (Exception e) {
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
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
            } catch (Exception e) {
                showAlert("Error", "Failed to select poster: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
}