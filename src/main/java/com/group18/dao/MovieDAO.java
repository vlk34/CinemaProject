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
        String query = "SELECT * FROM movies WHERE genre LIKE ?";
        List<Movie> movies = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + genre + "%");
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
        String query = "INSERT INTO movies (title, genre, summary, poster_data, duration) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenresAsString());
            stmt.setString(3, movie.getSummary());

            // Handle poster image BLOB
            if (movie.getPosterData() != null) {
                stmt.setBytes(4, movie.getPosterData());
            } else {
                stmt.setNull(4, Types.BLOB);
            }

            stmt.setInt(5, movie.getDuration());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating movie failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    movie.setMovieId(generatedKeys.getInt(1));
                    return true;
                } else {
                    throw new SQLException("Creating movie failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateMovie(Movie movie) {
        String query = "UPDATE movies SET title = ?, genre = ?, summary = ?, poster_data = ?, duration = ? WHERE movie_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, movie.getTitle());
            stmt.setString(2, movie.getGenresAsString());
            stmt.setString(3, movie.getSummary());

            // Handle poster image BLOB
            if (movie.getPosterData() != null) {
                stmt.setBytes(4, movie.getPosterData());
            } else {
                stmt.setNull(4, Types.BLOB);
            }

            stmt.setInt(5, movie.getDuration());
            stmt.setInt(6, movie.getMovieId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Movie> getAllMovies() {
        String query = "SELECT * FROM movies";
        List<Movie> movies = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                movies.add(extractMovieFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    private Movie extractMovieFromResultSet(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setMovieId(rs.getInt("movie_id"));
        movie.setTitle(rs.getString("title"));
        movie.setGenresFromString(rs.getString("genre"));
        movie.setSummary(rs.getString("summary"));
        movie.setPosterData(rs.getBytes("poster_data"));
        movie.setDuration(rs.getInt("duration"));
        return movie;
    }

    public boolean removeMovie(int movieId) {
        String query = "DELETE FROM movies WHERE movie_id = ? AND NOT EXISTS " +
                "(SELECT 1 FROM schedules WHERE movie_id = ?)";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, movieId);
            stmt.setInt(2, movieId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Movie findMovieByScheduleId(int scheduleId) {
        String query = "SELECT m.* FROM movies m " +
                "JOIN schedules s ON m.movie_id = s.movie_id " +
                "WHERE s.schedule_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, scheduleId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractMovieFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}