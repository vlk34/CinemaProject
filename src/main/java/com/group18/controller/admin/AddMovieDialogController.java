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
    private String currentPosterPath;
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
            checkBox.setPadding(new Insets(0, 10, 0, 0));  // Add padding for spacing
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
        // Check all input fields
        boolean isTitleValid = !titleField.getText().trim().isEmpty();
        boolean isGenreValid = !selectedGenres.isEmpty();  // Must select at least one genre
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
            String summary = summaryField.getText().trim();
            int duration = 120;

            // Create new movie with multiple genres
            Movie newMovie = new Movie();
            newMovie.setTitle(title);
            newMovie.setGenres(selectedGenres);
            newMovie.setSummary(summary);
            newMovie.setPosterPath(currentPosterPath);
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
                // Create movies directory if it doesn't exist
                Path moviesDir = Paths.get("src/main/resources/images/movies");
                Files.createDirectories(moviesDir);

                // Use the original filename
                String fileName = selectedFile.getName();
                Path targetPath = moviesDir.resolve(fileName);

                // Check if file already exists
                if (Files.exists(targetPath)) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("File Already Exists");
                    confirmAlert.setHeaderText("A file with the name '" + fileName + "' already exists.");
                    confirmAlert.setContentText("Do you want to replace the existing file?");

                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isEmpty() || result.get() != ButtonType.OK) {
                        // User chose not to replace, so open file chooser again
                        handleSelectPoster();
                        return;
                    }
                }

                // Copy file
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                // Set the relative path for database storage
                currentPosterPath = "/images/movies/" + fileName;

                // Debug information
                System.out.println("Selected file path: " + selectedFile.getAbsolutePath());
                System.out.println("Target path: " + targetPath.toString());
                System.out.println("Current poster path: " + currentPosterPath);

                // Verify file exists after copying
                File copiedFile = targetPath.toFile();
                if (copiedFile.exists()) {
                    System.out.println("File copied successfully. Size: " + copiedFile.length() + " bytes");
                } else {
                    System.err.println("File copy failed");
                }

                // Load and display the copied image
                Image image = new Image(targetPath.toUri().toString());
                posterImageView.setImage(image);

                // Validate inputs after poster selection
                validateInputs();

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to copy image file: " + e.getMessage());
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