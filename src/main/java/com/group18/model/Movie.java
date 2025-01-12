package com.group18.model;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a movie with properties such as title, genres, summary, poster image data, and duration.
 * This class provides functionality to manage movie details, including adding or removing genres
 * and handling image data for the movie's poster.
 */
public class Movie {
    /**
     * Represents the unique identifier for a movie.
     * This identifier is used to differentiate each movie within the system.
     */
    private int movieId;
    /**
     * Represents the title of the movie.
     * This field stores the name of the movie as a string.
     */
    private String title;
    /**
     * Represents the set of genres associated with a movie.
     * Each genre is stored as a unique string in the set.
     * This field allows categorization and filtering of movies based on their genres.
     */
    private Set<String> genres;
    /**
     * A brief textual description of the movie's plot or key points.
     * This provides an overview of the movie content to users.
     */
    private String summary;
    /**
     * Represents the binary data of the poster image associated with the movie.
     * Typically used to store and retrieve the movie's visual representation.
     */
    private byte[] posterData;
    /**
     * Represents the duration of the movie in minutes.
     */
    private int duration;

    /**
     * Default constructor for the Movie class.
     * This constructor initializes a new instance of the Movie object with default values.
     */
    public Movie() {}

    /**
     * Constructs a new Movie object with the specified details.
     *
     * @param title       The title of the movie.
     * @param genres      A set of genres associated with the movie. If null, an empty set is assigned.
     * @param summary     A brief summary or description of the movie.
     * @param posterData  A byte array containing the data for the movie's poster image.
     * @param duration    The duration of the movie in minutes.
     */
    public Movie(String title, Set<String> genres, String summary, byte[] posterData, int duration) {
        this.title = title;
        this.genres = genres != null ? genres : new HashSet<>();
        this.summary = summary;
        this.posterData = posterData;
        this.duration = duration;
    }

    /**
     * Retrieves the binary data of the movie's poster image.
     *
     * @return A byte array representing the movie's poster image data,
     *         or null if no poster data is available.
     */
    public byte[] getPosterData() {
        return posterData;
    }

    /**
     * Sets the poster data for the movie.
     *
     * @param posterData An array of bytes representing the poster image data.
     */
    public void setPosterData(byte[] posterData) {
        this.posterData = posterData;
    }

    /**
     * Retrieves the poster image for the movie. Converts the byte array representing
     * the poster data to an Image object.
     *
     * @return the poster image as an Image object if the poster data is available
     *         and valid, otherwise returns null.
     */
    public Image getPosterImage() {
        if (posterData != null) {
            try {
                return new Image(new ByteArrayInputStream(posterData));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Retrieves the unique identifier of the movie.
     *
     * @return The unique identifier of the movie as an integer.
     */
    public int getMovieId() {
        return movieId;
    }

    /**
     * Sets the unique identifier for the movie.
     *
     * @param movieId The unique identifier of the movie.
     */
    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    /**
     * Retrieves the title of the movie.
     *
     * @return the title of the movie as a String
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the movie.
     *
     * @param title The title of the movie to be set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Retrieves the set of genres associated with the movie.
     *
     * @return a set of strings representing the genres of the movie.
     */
    public Set<String> getGenres() {
        return genres;
    }

    /**
     * Sets the genres for the movie.
     *
     * @param genres A set of strings representing the genres of the movie.
     */
    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    /**
     * Adds a genre to the set of genres associated with the movie.
     * If the genres set has not been initialized, it initializes it before adding the genre.
     *
     * @param genre the genre to be added to the movie's genre collection
     */
    public void addGenre(String genre) {
        if (this.genres == null) {
            this.genres = new HashSet<>();
        }
        this.genres.add(genre);
    }

    /**
     * Removes the specified genre from the set of genres associated with the Movie.
     * If the genres set is null, no action is performed.
     *
     * @param genre the genre to be removed from the Movie's genres set
     */
    public void removeGenre(String genre) {
        if (this.genres != null) {
            this.genres.remove(genre);
        }
    }

    /**
     * Converts the set of genres associated with a movie into a single string,
     * with each genre separated by a comma and a space.
     *
     * @return a string representation of the movie's genres, separated by commas.
     */
    // Helper methods for database storage
    public String getGenresAsString() {
        return String.join(", ", genres);
    }

    /**
     * Sets the genres of the movie from a comma-separated string.
     * Each genre in the string is trimmed of leading and trailing whitespace
     * and added to the set of genres. If the input string is null or empty,
     * the genres set is cleared.
     *
     * @param genresStr A comma-separated string containing genres, or null/empty to clear the genres set.
     */
    public void setGenresFromString(String genresStr) {
        if (genresStr != null && !genresStr.trim().isEmpty()) {
            this.genres = Arrays.stream(genresStr.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        } else {
            this.genres = new HashSet<>();
        }
    }

    /**
     * Retrieves the summary of the movie.
     *
     * @return The summary of the movie as a string.
     */
    public String getSummary() {
        return summary;
    }

    /**
     * Sets the summary of the movie.
     *
     * @param summary The summary of the movie to set.
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Retrieves the duration of the movie.
     *
     * @return The duration of the movie in minutes.
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration of the movie in minutes.
     *
     * @param duration The duration of the movie in minutes.
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
}