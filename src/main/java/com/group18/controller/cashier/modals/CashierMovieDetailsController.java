package com.group18.controller.cashier.modals;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import com.group18.model.Movie;

import java.io.File;

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
                File posterFile = new File(movie.getPosterPath());
                if (posterFile.exists()) {
                    posterImageView.setImage(new Image(posterFile.toURI().toString()));
                } else {
                    System.err.println("File not found: " + movie.getPosterPath());
                    posterImageView.setImage(new Image(getClass().getResourceAsStream("/images/movies/dark_knight.jpg")));
                }
            } catch (Exception e) {
                e.printStackTrace();
                posterImageView.setImage(new Image(getClass().getResourceAsStream("/images/movies/dark_knight.jpg")));
            }
        } else {
            posterImageView.setImage(new Image(getClass().getResourceAsStream("/images/movies/dark_knight.jpg")));
        }

        System.out.println("Poster path used: " + movie.getPosterPath());
    }

}