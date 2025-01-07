package com.group18.controller.admin;

import com.group18.dao.MovieDAO;
import com.group18.model.Movie;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class AddMovieDialogController {
    @FXML private TextField titleField;
    @FXML private TextArea summaryField;
    @FXML private TextField durationField;
    @FXML private ImageView posterImageView;
    @FXML private Button selectPosterButton;
    @FXML private Button addMovieButton;
    @FXML private Button cancelButton;
    @FXML private FlowPane genreContainer;

    private MovieDAO movieDAO;
    private byte[] currentPosterData;
    private Stage dialogStage;
    private Set<String> selectedGenres;

    @FXML
    private void initialize() {
        movieDAO = new MovieDAO();
        selectedGenres = new HashSet<>();

        // Create checkboxes for genres
        String[] genres = {"Action", "Comedy", "Drama", "Horror", "Science Fiction", "Fantasy"};
        for (String genre : genres) {
            CheckBox checkBox = new CheckBox(genre);
            checkBox.setPadding(new Insets(0, 10, 0, 0));
            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    selectedGenres.add(genre);
                } else {
                    selectedGenres.remove(genre);
                }
                validateInputs();
            });
            genreContainer.getChildren().add(checkBox);
        }

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

        // Summary validation
        summaryField.textProperty().addListener((observable, oldValue, newValue) -> {
            validateInputs();
        });

        durationField.setText("120");
        durationField.setEditable(false);
    }

    private void validateInputs() {
        boolean isTitleValid = !titleField.getText().trim().isEmpty();
        boolean isGenreValid = !selectedGenres.isEmpty();
        boolean isSummaryValid = !summaryField.getText().trim().isEmpty();
        boolean isDurationValid = true; // Always true since it's fixed at 120

        // Validate poster (optional)
        boolean isPosterValid = currentPosterData != null && currentPosterData.length > 0;

        addMovieButton.setDisable(!(isTitleValid && isGenreValid &&
                isSummaryValid && isDurationValid && isPosterValid));
    }

    private void handleAddMovie() {
        try {
            String title = titleField.getText().trim();
            String summary = summaryField.getText().trim();
            int duration = 120;

            Movie newMovie = new Movie();
            newMovie.setTitle(title);
            newMovie.setGenres(selectedGenres);
            newMovie.setSummary(summary);
            newMovie.setPosterData(currentPosterData);  // Using new poster data field
            newMovie.setDuration(duration);

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

    @FXML
    private void handleSelectPoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectPosterButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                // Read file into byte array
                byte[] imageData = Files.readAllBytes(selectedFile.toPath());

                // Validate image data
                try (ByteArrayInputStream bis = new ByteArrayInputStream(imageData)) {
                    Image testImage = new Image(bis);
                    if (testImage.isError()) {
                        throw new IOException("Invalid image data");
                    }
                }

                // If valid, update UI and store the data
                Image image = new Image(new ByteArrayInputStream(imageData));
                posterImageView.setImage(image);
                currentPosterData = imageData;

                // Validate inputs after successful poster selection
                validateInputs();

            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to load image file: " + e.getMessage());
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