package com.example.astudio.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a review made by a user on a book.
 */
public class Review {
    private final User user;
    private final Book book;
    private final double rating;
    private final String comment;
    private final String timestamp;

    public Review(User user, Book book, double rating, String comment) {
        this.user = user;
        this.book = book;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public User getUser() { return user; }

    public Book getBook() { return book; }

    public double getRating() { return rating; }

    public String getComment() { return comment; }

    public String getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "\"" + comment + "\" — " + user.getUsername()
                + " (" + String.format("%.1f", rating) + "/5, " + timestamp + ")";
    }
}

