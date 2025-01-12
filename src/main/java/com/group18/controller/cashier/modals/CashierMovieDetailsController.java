package com.group18.controller.cashier.modals;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import com.group18.model.Movie;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Controller class for the movie details modal in the cashier section.
 * This class handles the display of movie details including title, genres, summary,
 * and poster image.
 */
public class CashierMovieDetailsController {
    @FXML private ImageView posterImageView;
    @FXML private Label titleLabel;
    @FXML private Label genresLabel;
    @FXML private TextArea summaryArea;

    /**
     * Sets the movie details in the modal.
     *
     * @param movie The movie whose details will be displayed.
     */
    public void setMovie(Movie movie) {
        titleLabel.setText(movie.getTitle());
        genresLabel.setText(movie.getGenresAsString());
        summaryArea.setText(movie.getSummary());

        // Handle poster image using byte array data
        if (movie.getPosterData() != null && movie.getPosterData().length > 0) {
            try {
                Image image = new Image(new ByteArrayInputStream(movie.getPosterData()));
                if (!image.isError()) {
                    posterImageView.setImage(image);
                } else {
                    setDefaultPoster();
                }
            } catch (Exception e) {
                e.printStackTrace();
                setDefaultPoster();
            }
        } else {
            setDefaultPoster();
        }
    }

    /**
     * Sets a default poster image in case the movie does not have a poster.
     * The default image is read from the resources.
     */
    private void setDefaultPoster() {
        try {
            byte[] defaultImageData = getClass().getResourceAsStream("/images/movies/dark_knight.jpg").readAllBytes();
            Image defaultImage = new Image(new ByteArrayInputStream(defaultImageData));
            posterImageView.setImage(defaultImage);
        } catch (IOException e) {
            e.printStackTrace();
            posterImageView.setImage(null);
        }
    }
}