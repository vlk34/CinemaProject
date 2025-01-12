package com.group18.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class manages the connection to the database.
 * It provides methods to establish and close a connection to the MySQL database.
 * The connection is established lazily and reused throughout the application.
 */
public class DBConnection {
    private static Connection connection;

    // Database credentials and URL
    private static final String DB_URL = "jdbc:mysql://localhost/cinemadb";
    private static final String DB_USERNAME = "myuser";
    private static final String DB_PASSWORD = "1234";

    /**
     * Constructor that initializes the connection to the database.
     * It calls the connect method to establish a connection when an instance is created.
     */
    public DBConnection() {
        connect();
    }

    /**
     * Establishes a connection to the database if not already connected.
     * The connection is established lazily when needed.
     */
    private static void connect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Failed to connect to the database. \n");
        }
    }

    /**
     * Provides the connection to the database.
     * If the connection is not already established, it calls the connect method to ensure the connection is active.
     *
     * @return The active database connection.
     */
    public static Connection getConnection() {
        connect();
        return connection;
    }

    /**
     * Closes the database connection if it is open.
     * This method is called to clean up resources when the connection is no longer needed.
     */
    private static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
