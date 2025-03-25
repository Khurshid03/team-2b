package model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a user who can write reviews.
 */
public class User {
    private static final AtomicInteger idGenerator = new AtomicInteger(1);

    private final int id;
    private final String username;
    private final String email;

    public User(String username, String email) {
        this.id = idGenerator.getAndIncrement();
        this.username = username;
        this.email = email;
    }

    public int getId() { return id; }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    /**
     * Creates a new review by this user.
     */
    public Review writeReview(Book book, double rating, String comment) {
        return new Review(this, book, rating, comment);
    }

    @Override
    public String toString() {
        return username + " (" + email + ")";
    }
}

