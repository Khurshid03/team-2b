package com.example.astudio.model;

import java.io.Serializable;
import com.example.astudio.model.User;

public class Review implements Serializable {
    private final String username;
    private final float rating;
    private final String comment;

    public Review(String username, float rating, String comment) {
        this.username = username;
        this.rating = rating;
        this.comment = comment;

    }

    public String getUsername() {
        return username;
    }

    public float getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

}