package com.group18.dao;

import com.group18.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for interacting with the "users" table in the database.
 * Provides methods for CRUD operations and user-related queries.
 */
public class UserDAO {
    private Connection connection;

    /**
     * Constructs a new UserDAO object with a database connection.
     */
    public UserDAO() {
        this.connection = DBConnection.getConnection();
    }

    /**
     * Authenticates a user based on the provided username and password.
     *
     * @param username The username of the user to authenticate.
     * @param password The password of the user to authenticate.
     * @return The authenticated User object, or null if authentication fails.
     */
    public User authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param userId The ID of the user to retrieve.
     * @return The User object corresponding to the given ID, or null if not found.
     */
    public User findById(int userId) {
        String query = "SELECT * FROM users WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Retrieves all users from the "users" table.
     *
     * @return A list of all users in the database.
     */
    public List<User> getAllUsers() {
        String query = "SELECT * FROM users";
        List<User> users = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /**
     * Adds a new user to the "users" table.
     *
     * @param user The User object to be added.
     * @return True if the user was successfully added, false otherwise.
     */
    public boolean addUser(User user) {
        // First, check if the username already exists
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, user.getUsername());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Username is already taken
                    return false;
                }
            }

            // If username is unique, proceed with insert
            String query = "INSERT INTO users (username, password, role, first_name, last_name) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getRole());
                stmt.setString(4, user.getFirstName());
                stmt.setString(5, user.getLastName());

                int affectedRows = stmt.executeUpdate();

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            user.setUserId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Updates the details of an existing user in the "users" table.
     *
     * @param user The User object with updated information.
     * @return True if the user was successfully updated, false otherwise.
     */
    public boolean updateUser(User user) {
        // First, check if the username is already taken by another user
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ? AND user_id != ?";

        try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
            checkStmt.setString(1, user.getUsername());
            checkStmt.setInt(2, user.getUserId());

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    // Username is already taken by another user
                    return false;
                }
            }

            // If username is unique or belongs to the same user, proceed with update
            String updateQuery = "UPDATE users SET username = ?, password = ?, role = ?, first_name = ?, last_name = ? WHERE user_id = ?";

            try (PreparedStatement stmt = connection.prepareStatement(updateQuery)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getPassword());
                stmt.setString(3, user.getRole());
                stmt.setString(4, user.getFirstName());
                stmt.setString(5, user.getLastName());
                stmt.setInt(6, user.getUserId());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Deletes a user from the "users" table.
     *
     * @param userId The ID of the user to be deleted.
     * @return True if the user was successfully deleted, false otherwise.
     */
    public boolean deleteUser(int userId) {
        String query = "DELETE FROM users WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieves the count of new users created in the current month.
     *
     * @return The number of new users created in the current month.
     */
    public long getNewUsersThisMonth() {
        String query = "SELECT COUNT(*) FROM users WHERE MONTH(created_at) = MONTH(CURRENT_DATE()) " +
                "AND YEAR(created_at) = YEAR(CURRENT_DATE())";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Extracts a User object from the current row of a ResultSet.
     *
     * @param rs The ResultSet object containing the user data.
     * @return A User object populated with the data from the ResultSet.
     * @throws SQLException If an error occurs while accessing the ResultSet.
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        return user;
    }
}