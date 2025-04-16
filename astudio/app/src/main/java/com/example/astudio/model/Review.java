package com.example.astudio.model;

import java.io.Serializable;
import com.example.astudio.model.User;

/**
 * Represents a review given by a user, containing the username, rating, and comment.
 */
public class Review implements Serializable {
    private final String username;
    private final float rating;
    private final String comment;

    /**
     * Constructs a Review object with the specified username, rating, and comment.
     *
     * @param username The username of the reviewer.
     * @param rating The rating given by the reviewer.
     * @param comment The comment provided by the reviewer.
     */
    public Review(String username, float rating, String comment) {
        this.username = username;
        this.rating = rating;
        this.comment = comment;

    }

    /**
     * Returns the username of the reviewer.
     *
     * @return The username of the reviewer.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the rating given by the reviewer.
     *
     * @return The rating of the review.
     */
    public float getRating() {
        return rating;
    }

    /**
     * Returns the comment provided by the reviewer.
     *
     * @return The comment of the review.
     */
    public String getComment() {
        return comment;
    }

}