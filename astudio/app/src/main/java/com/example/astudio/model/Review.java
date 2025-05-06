package com.example.astudio.model;

import java.io.Serializable;

/**
 * Represents a review given by a user, containing the username, rating, and comment.
 * This version includes the author's User ID (UID) for permission checks.
 */
public class Review implements Serializable {
    private String username; // Changed to non-final to allow no-arg constructor
    private float rating;
    private String comment;
    private String reviewId;
    private String bookId;
    private String thumbnailUrl;
    private String authorUid; // Field to store the author's User ID

    /**
     * Constructs a new, empty Review object.
     * Required for Firestore deserialization.
     */
    public Review() {
        // Default constructor required for calls to DataSnapshot.getValue(Review.class)
    }

    /**
     * Constructs a Review object with the specified details.
     *
     * @param username     The username of the reviewer.
     * @param rating       The rating given by the reviewer.
     * @param comment      The comment provided by the reviewer.
     * @param reviewId     The unique ID of the review document in Firestore.
     * @param bookId       The ID of the book being reviewed.
     * @param thumbnailUrl The URL of the book's thumbnail image.
     * @param authorUid    The User ID (UID) of the review's author.
     */
    public Review(String username, float rating, String comment, String reviewId, String bookId, String thumbnailUrl, String authorUid) {
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.thumbnailUrl = thumbnailUrl;
        this.authorUid = authorUid; // Initialize authorUid
    }

    // Note: Kept the previous constructor for compatibility if needed,
    // but the one above is preferred when authorUid is available.
    /**
     * Constructs a Review object with the specified details (without author UID).
     * This constructor might be used during initial data creation before UID is available,
     * but it's recommended to use the constructor that includes authorUid.
     *
     * @param username     The username of the reviewer.
     * @param rating       The rating given by the reviewer.
     * @param comment      The comment provided by the reviewer.
     * @param reviewId     The unique ID of the review document in Firestore.
     * @param bookId       The ID of the book being reviewed.
     * @param thumbnailUrl The URL of the book's thumbnail image.
     */
    public Review(String username, float rating, String comment, String reviewId, String bookId, String thumbnailUrl) {
        this.username = username;
        this.rating = rating;
        this.comment = comment;
        this.reviewId = reviewId;
        this.bookId = bookId;
        this.thumbnailUrl = thumbnailUrl;
        // authorUid is not set here
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
     * Sets the username of the reviewer.
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
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
     * Sets the rating given by the reviewer.
     * @param rating The rating to set.
     */
    public void setRating(float rating) {
        this.rating = rating;
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
     * Sets the comment provided by the reviewer.
     * @param comment The comment to set.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns the unique ID of the review document in Firestore.
     *
     * @return The review ID.
     */
    public String getReviewId() {
        return reviewId;
    }

    /**
     * Sets the unique ID of the review document in Firestore.
     * @param reviewId The review ID to set.
     */
    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    /**
     * Returns the ID of the book being reviewed.
     *
     * @return The book ID.
     */
    public String getBookId() {
        return bookId;
    }

    /**
     * Sets the ID of the book being reviewed.
     * @param bookId The book ID to set.
     */
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    /**
     * Returns the URL of the book's thumbnail image.
     *
     * @return The thumbnail URL.
     */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /**
     * Sets the URL of the book's thumbnail image.
     * @param thumbnailUrl The thumbnail URL to set.
     */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * Returns the User ID (UID) of the review's author.
     *
     * @return The author's UID.
     */
    public String getAuthorUid() {
        return authorUid;
    }

    /**
     * Sets the User ID (UID) of the review's author.
     * @param authorUid The author's UID to set.
     */
    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }
}
