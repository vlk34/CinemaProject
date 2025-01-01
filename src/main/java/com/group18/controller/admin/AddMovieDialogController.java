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

    private MovieDAO movieDAO;
    private String currentPosterPath;
    private Stage dialogStage;

    @FXML
    private void initialize() {
        movieDAO = new MovieDAO();
        selectPosterButton.setOnAction(event -> handleSelectPoster());
        addMovieButton.setOnAction(event -> handleAddMovie());
        cancelButton.setOnAction(event -> dialogStage.close());
    }

    private void handleAddMovie() {
        try {
            String title = titleField.getText().trim();
            String genre = genreField.getText().trim();
            String summary = summaryField.getText().trim();
            int duration = Integer.parseInt(durationField.getText().trim());

            if (title.isEmpty() || genre.isEmpty() || summary.isEmpty()) {
                showAlert("Validation Error", "Please fill in all fields.");
                return;
            }

            Movie newMovie = new Movie(title, genre, summary, currentPosterPath, duration);

            if (movieDAO.addMovie(newMovie)) {
                showAlert("Success", "Movie added successfully.");
                dialogStage.close();
            } else {
                showAlert("Error", "Failed to add movie.");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid duration format.");
        }
    }

    private void handleSelectPoster() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Movie Poster");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(selectPosterButton.getScene().getWindow());

        if (selectedFile != null) {
            try {
                Path posterDir = Paths.get("src/main/resources/posters");
                Files.createDirectories(posterDir);
                String fileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                Path targetPath = posterDir.resolve(fileName);
                Files.copy(selectedFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                posterImageView.setImage(new Image(targetPath.toUri().toString()));
                currentPosterPath = targetPath.toString();
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