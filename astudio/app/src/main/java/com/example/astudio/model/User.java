package com.example.astudio.model;

import java.io.Serializable;

/**
 * Represents a user in the system, containing the username and email.
 */

public class User implements Serializable {
    private String username;
    private String email;
    // email will be used in the next iteration

    /**
     * Constructs a User object with the specified username and email.
     *
     * @param username The username of the user.
     */
    public User(String username) {
        this.username = username;
        this.email = email;
    }

    /**
     * Returns the username of the user.
     *
     * @return The username of the user.
     */

    public String getUsername() {
        return username;
    }
}