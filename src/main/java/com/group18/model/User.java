package com.group18.model;

/**
 * Represents a user in the system with properties such as username, password, role,
 * and personal details like first name and last name.
 *
 * This class is primarily used to manage user information and is associated with
 * functionalities like adding a new user or editing an existing user's details.
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String role;
    private String firstName;
    private String lastName;

    /**
     * Default constructor for the User class.
     *
     * Initializes a new instance of the User without any predefined properties.
     * This constructor can be used when creating a User object that will have its
     * attributes set individually using setters.
     */
    public User() {}

    /**
     * Constructs a new User instance with the specified username, password, role, first name, and last name.
     *
     * @param username the unique identifier for the user
     * @param password the user's password for authentication
     * @param role the role assigned to the user within the system
     * @param firstName the user's first name
     * @param lastName the user's last name
     */
    public User(String username, String password, String role, String firstName, String lastName) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Retrieves the unique identifier of the user.
     *
     * @return The user's unique identifier as an integer.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the unique identifier for the user.
     *
     * @param userId The unique identifier to be assigned to the user.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Retrieves the username of the user.
     *
     * @return The username associated with the user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Updates the username of the user.
     *
     * @param username the new username to set for the user
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the password for the user.
     *
     * @return The password of the user as a String.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the user.
     *
     * @param password the new password to set for the user
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retrieves the role of the user.
     *
     * @return The role of the user as a String.
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the user.
     *
     * @param role the role to assign to the user, typically representing their
     *             authorization level (e.g., "admin", "manager", "cashier").
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Retrieves the first name of the user.
     *
     * @return the first name of the user as a String
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName the first name to set for the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Retrieves the last name of the user.
     *
     * @return The last name of the user as a String.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user.
     *
     * @param lastName the last name to set for the user.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}