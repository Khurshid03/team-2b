package com.example.astudio.model;
import java.io.Serializable;

/**
 * Represents a book with its essential details such as title, author, description,
 * rating, and thumbnail URL.
 * This class implements {@link Serializable} to allow Book objects to be passed
 * between components, for example, as arguments in a {@link android.os.Bundle}.
 */
public class Book implements Serializable {
    /**
     * A unique version identifier for serialization, to ensure class compatibility
     * during deserialization.
     */
    private static final long serialVersionUID = 1L;

    private String title;
    private String thumbnailUrl;
    private float rating;
    private String author;
    private String description;

    /**
     * Default constructor for the Book class.
     * Required for certain deserialization processes (e.g., by some Firebase libraries if used directly).
     */
    public Book() {
        // Default constructor
    }

    /**
     * Constructs a new Book object with specified details.
     *
     * @param title        The title of the book.
     * @param thumbnailUrl The URL of the book's cover image thumbnail.
     * @param rating       The average rating of the book (e.g., out of 5).
     * @param author       The author(s) of the book.
     * @param description  A brief description or synopsis of the book.
     */
    public Book(String title, String thumbnailUrl, float rating, String author, String description) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.rating = rating;
        this.author = author;
        this.description = description;
    }

    /**
     * Returns the title of the book.
     *
     * @return A {@link String} representing the book's title.
     */
    public String getTitle() { return title; }

    /**
     * Returns the URL of the book's thumbnail image.
     * This URL can be used with image loading libraries like Glide or Picasso.
     *
     * @return A {@link String} representing the URL for the book's thumbnail.
     */
    public String getThumbnailUrl() { return thumbnailUrl; }

    /**
     * Returns the average rating of the book.
     *
     * @return A float representing the book's rating.
     */
    public float getRating() { return rating; }


    /**
     * Returns the author(s) of the book.
     *
     * @return A {@link String} representing the book's author(s).
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Returns a brief description or synopsis of the book.
     *
     * @return A {@link String} containing the book's description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the URL for the book's thumbnail image.
     *
     * @param url The new thumbnail URL string.
     */
    public void setThumbnailUrl(String url) { this.thumbnailUrl = url; }

    /**
     * Sets the rating for the book.
     *
     * @param r The new rating value (float).
     */
    public void setRating(float r)          { this.rating = r; }
}
