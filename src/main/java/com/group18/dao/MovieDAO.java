package com.group18.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.group18.model.Movie;

/**
 * This class provides CRUD operations (Create, Read, Update, Delete) for managing movies in the database.
 * It interacts with the 'movies' table in the database to perform operations like adding, updating, removing,
 * and fetching movie details.
 */
public class MovieDAO {
    private Connection connection;

    /**
     * Constructor that initializes the connection to the database by calling the DBConnection's getConnection method.
     */
    public MovieDAO() {
        this.connection = DBConnection.getConnection();
    }

    /**
     * Finds a movie by its unique identifier (movieId).
     *
     * @param movieId The unique identifier of the movie.
     * @return The Movie object corresponding to the provided movieId, or null if no such movie exists.
     */
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

    /**
     * Adds a new movie to the database.
     *
     * @param movie The Movie object containing the details of the movie to be added.
     * @return True if the movie was successfully added; false otherwise.
     */
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

    /**
     * Updates an existing movie in the database.
     *
     * @param movie The Movie object containing the updated details of the movie.
     * @return True if the movie was successfully updated; false otherwise.
     */
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

    /**
     * Retrieves all movies from the database.
     *
     * @return A list of all movies in the database.
     */
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

    /**
     * Extracts a Movie object from the result set.
     *
     * @param rs The ResultSet object containing the movie data.
     * @return A Movie object populated with data from the result set.
     * @throws SQLException If an error occurs while extracting data from the result set.
     */
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

    /**
     * Removes a movie from the database by its unique identifier (movieId),
     * only if the movie is not associated with any schedules.
     *
     * @param movieId The unique identifier of the movie to be removed.
     * @return True if the movie was successfully removed; false otherwise.
     */
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

    /**
     * Finds a movie based on its associated schedule ID.
     *
     * @param scheduleId The unique identifier of the schedule.
     * @return The Movie object associated with the given schedule ID, or null if no such movie exists.
     */
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