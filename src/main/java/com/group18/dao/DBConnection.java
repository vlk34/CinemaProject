package com.group18.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static Connection connection;

    // Database credentials and URL
    private static final String DB_URL = "jdbc:mysql://localhost/cinemadb";
    private static final String DB_USERNAME = "myuser";
    private static final String DB_PASSWORD = "1234";

    public DBConnection() {
        connect();
    }

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

    public static Connection getConnection() {
        connect();
        return connection;
    }

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
