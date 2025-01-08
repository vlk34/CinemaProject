package com.group18.model;

import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Movie {
    private int movieId;
    private String title;
    private Set<String> genres;
    private String summary;
    private byte[] posterData;
    private int duration;

    public Movie() {}

    public Movie(String title, Set<String> genres, String summary, byte[] posterData, int duration) {
        this.title = title;
        this.genres = genres != null ? genres : new HashSet<>();
        this.summary = summary;
        this.posterData = posterData;
        this.duration = duration;
    }

    public byte[] getPosterData() {
        return posterData;
    }

    public void setPosterData(byte[] posterData) {
        this.posterData = posterData;
    }

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

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<String> getGenres() {
        return genres;
    }

    public void setGenres(Set<String> genres) {
        this.genres = genres;
    }

    public void addGenre(String genre) {
        if (this.genres == null) {
            this.genres = new HashSet<>();
        }
        this.genres.add(genre);
    }

    public void removeGenre(String genre) {
        if (this.genres != null) {
            this.genres.remove(genre);
        }
    }

    // Helper methods for database storage
    public String getGenresAsString() {
        return String.join(", ", genres);
    }

    public void setGenresFromString(String genresStr) {
        if (genresStr != null && !genresStr.trim().isEmpty()) {
            this.genres = Arrays.stream(genresStr.split(","))
                    .map(String::trim)
                    .collect(Collectors.toSet());
        } else {
            this.genres = new HashSet<>();
        }
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}