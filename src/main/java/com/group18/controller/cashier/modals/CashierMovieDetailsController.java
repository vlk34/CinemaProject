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
        posterImageView.setImage(new Image(movie.getPosterPath()));
    }
}