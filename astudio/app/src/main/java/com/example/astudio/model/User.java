package com.example.astudio.model;

import java.io.Serializable;

/**
 * Represents a user in the system, containing the username and email.
 */

public class User implements Serializable {
    private String id;            // new
    private String username;
    private String email;
    private String bio;
    private int followersCount;
    private int followingCount;
    // … any other fields …

    // 1a) No-arg constructor for Firestore
    public User() {}

    // 1b) Full constructor (optional)
    public User(String id, String username, String email,
                String bio, int followersCount, int followingCount) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
    }

    // 1c) Getter & setter for the new id
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    // 1d) Existing getters/setters
    public String getUsername() { return username; }
    public void setUsername(String u) { this.username = u; }

    public int getFollowersCount() { return followersCount; }
    public void setFollowersCount(int followersCount) { this.followersCount = followersCount; }


    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int followingCount) { this.followingCount = followingCount; }
}