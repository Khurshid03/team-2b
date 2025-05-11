package com.example.astudio.model;

import java.io.Serializable;

/**
 * Represents a user in the system, containing profile information.
 */
public class User implements Serializable {
    /**
     * Unique identifier for the user.
     */
    private String id;
    /**
     * The user's chosen username.
     */
    private String username;
    /**
     * The user's email address.
     */
    private String email;
    /**
     * A short biographical description of the user.
     */
    private String bio;
    /**
     * The number of users following this user.
     */
    private int followersCount;
    /**
     * The number of users this user is following.
     */
    private int followingCount;
    // ... any other fields ...

    /**
     * No-argument constructor required for Firestore deserialization.
     */
    public User() {}

    /**
     * Constructs a new User object with all profile details.
     *
     * @param id The unique identifier for the user.
     * @param username The user's chosen username.
     * @param email The user's email address.
     * @param bio A short biographical description.
     * @param followersCount The number of followers.
     * @param followingCount The number of users being followed.
     */
    public User(String id, String username, String email,
                String bio, int followersCount, int followingCount) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
    }

    /**
     * Gets the unique identifier of the user.
     *
     * @return The user's ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the user.
     *
     * @param id The user's ID to set.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the username of the user.
     *
     * @return The user's username.
     */
    public String getUsername() { return username; }

    /**
     * Sets the username of the user.
     *
     * @param u The username to set.
     */
    public void setUsername(String u) { this.username = u; }

    /**
     * Gets the number of followers the user has.
     *
     * @return The number of followers.
     */
    public int getFollowersCount() { return followersCount; }

    /**
     * Sets the number of followers the user has.
     *
     * @param followersCount The number of followers to set.
     */
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }

    /**
     * Gets the number of users this user is following.
     *
     * @return The number of users being followed.
     */
    public int getFollowingCount() { return followingCount; }

    /**
     * Sets the number of users this user is following.
     *
     * @param followingCount The number of users being followed to set.
     */
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }

    // ... add javadoc for other getters/setters if any ...

    /**
     * Gets the email address of the user.
     *
     * @return The user's email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the biographical description of the user.
     *
     * @return The user's bio.
     */
    public String getBio() {
        return bio;
    }

    /**
     * Sets the biographical description of the user.
     *
     * @param bio The bio to set.
     */
    public void setBio(String bio) {
        this.bio = bio;
    }
}
