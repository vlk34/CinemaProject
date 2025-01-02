package com.group18.controller.cashier.modals;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import com.group18.model.Movie;

public class CashierMovieDetailsController {
    @FXML private ImageView posterImageView;
    @FXML private Label titleLabel;
    @FXML private Label genresLabel;
    @FXML private TextArea summaryArea;

    public void setMovie(Movie movie) {
        titleLabel.setText(movie.getTitle());
        genresLabel.setText(String.join(", ", movie.getGenre()));
        summaryArea.setText(movie.getSummary());

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            try {
                // Use getResourceAsStream to load the image
                Image image = new Image(getClass().getResourceAsStream(movie.getPosterPath()));
                posterImageView.setImage(image);
            } catch (Exception e) {
                e.printStackTrace();
                // Set default image if loading fails
                posterImageView.setImage(new Image(getClass().getResourceAsStream("/images/movies/dark_knight.jpg")));
            }
        } else {
            // Set default image if no poster path
            posterImageView.setImage(new Image(getClass().getResourceAsStream("/images/movies/dark_knight.jpg")));
        }

        System.out.println("Poster path used: " + movie.getPosterPath());
    }
}