package com.group18.model;

import java.math.BigDecimal;

public class Movie {
    private int movieId;
    private String title;
    private String genre;
    private String summary;
    private String posterPath;
    private int duration;

    public Movie() {}

    public Movie(String title, String genre, String summary, String posterPath, int duration) {
        this.title = title;
        this.genre = genre;
        this.summary = summary;
        this.posterPath = posterPath;
        this.duration = duration;
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}