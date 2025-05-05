package com.example.astudio.model;
import java.io.Serializable;

/**
 * Book class represents a book with its details.
 * It implements Serializable to allow the object to be serialized.
 */

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String thumbnailUrl;
    private float rating;
    private String author;
    private String description;
    public Book() {}

    /**
     * Constructor to initialize a Book object.
     *
     * @param title        The title of the book.
     * @param thumbnailUrl The URL of the book's thumbnail image.
     * @param rating       The rating of the book.
     * @param author       The author of the book.
     * @param description  A brief description of the book.
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
     * @return the book's title
     */
    public String getTitle() { return title; }

    /**
     * Returns the URL of the book's thumbnail image.
     *
     * @return the book's thumbnail URL
     */
    public String getThumbnailUrl() { return thumbnailUrl; }

    /**
     * Returns the rating of the book.
     *
     * @return the book's rating
     */
    public float getRating() { return rating; }


    /**
     * Returns the author of the book.
     *
     * @return the book's author
     */
    public String getAuthor() {
        return author;
    }
    /**
     * Returns a brief description of the book.
     *
     * @return the book's description
     */

    public String getDescription() {
        return description;
    }


    public void setThumbnailUrl(String url) { this.thumbnailUrl = url; }
    public void setRating(float r)          { this.rating = r; }
}