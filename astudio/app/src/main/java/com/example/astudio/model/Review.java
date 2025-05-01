package com.example.astudio.model;

import java.io.Serializable;

/**
 * Represents a review given by a user, containing the username, rating, and comment.
 */
public class Review implements Serializable {
    private final String username;
    private float rating;
    private String comment;
    private String reviewId;
    private String bookId;
    private String thumbnailUrl;


    /**
     * Constructs a Review object with the specified username, rating, and comment.
     *
     * @param username The username of the reviewer.
     * @param rating The rating given by the reviewer.
     * @param comment The comment provided by the reviewer.
     */
    public Review(String username, float rating, String comment, String reviewId, String bookId, String thumbnailUrl) {
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.thumbnailUrl = thumbnailUrl;
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

    /**
     * Returns the review ID.
     *
     * @return The review ID.
     */
    public String getReviewId() {
        return reviewId;
    }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String url) { this.thumbnailUrl = url; }

    public void setReviewId(String reviewId) { this.reviewId = reviewId; }


    public String getBookId() {return bookId; }

    public void setBookId(String bookId) { this.bookId = bookId; }

    public void setRating(float r) { this.rating = r; }
    public void setComment(String c) { this.comment = c; }

}