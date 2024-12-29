package com.group18.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.group18.model.Movie;

public class MovieDAO {
    private Connection connection;

    public MovieDAO() {
        this.connection = DBConnection.getConnection();
    }

    public Movie findById(int movieId) {
        String query = "SELECT * FROM movies WHERE movie_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMovieFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Movie> searchByGenre(String genre) {
        String query = "SELECT * FROM movies WHERE genre = ?";
        List<Movie> movies = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, genre);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                movies.add(extractMovieFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public List<Movie> searchByPartialTitle(String partialTitle) {
        String query = "SELECT * FROM movies WHERE title LIKE ?";
        List<Movie> movies = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + partialTitle + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                movies.add(extractMovieFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public List<Movie> searchByFullTitle(String fullTitle) {
        String query = "SELECT * FROM movies WHERE title = ?";
        List<Movie> movies = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, fullTitle);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                movies.add(extractMovieFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    public boolean addMovie(Movie movie) {
        String query = "INSERT INTO movies (title, genre, summary, poster_path) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setString(3, movie.getSummary());
            stmt.setString(4, movie.getPosterPath());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    movie.setMovieId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMovie(Movie movie) {
        String query = "UPDATE movies SET title = ?, genre = ?, summary = ?, poster_path = ? WHERE movie_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenre());
            stmt.setString(3, movie.getSummary());
            stmt.setString(4, movie.getPosterPath());
            stmt.setInt(5, movie.getMovieId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Movie extractMovieFromResultSet(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setMovieId(rs.getInt("movie_id"));
        movie.setTitle(rs.getString("title"));
        movie.setGenre(rs.getString("genre"));
        movie.setSummary(rs.getString("summary"));
        movie.setPosterPath(rs.getString("poster_path"));
        return movie;
    }
}