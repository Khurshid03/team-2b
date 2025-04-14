package com.example.astudio.model;
import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String title;
    private String thumbnailUrl;
    private float rating;
    private String author;
    private String description;


    public Book(String title, String thumbnailUrl, float rating, String author, String description) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
        this.rating = rating;
        this.author = author;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public float getRating() { return rating; }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }
}