package com.example.astudio.model;

import java.util.List;

/**
 * Represents the response for a book, which contains a list of items.
 */
public class BookResponse {
    /**
     * List of items in the book response.
     */
    public List<Item> items;

    /**
     * Represents an individual item in the book response, containing volume information.
     */
    public static class Item {
        /**
         * Volume information for the book item.
         */
        public VolumeInfo volumeInfo;
    }

    /**
     * Contains detailed information about a book's volume, including title, authors, description, etc.
     */
    public static class VolumeInfo {
        /**
         * The title of the book.
         */
        public String title;

        /**
         * The image links for the book (e.g., thumbnail).
         */
        public ImageLinks imageLinks;

        /**
         * The average rating of the book.
         */
        public Float averageRating;

        /**
         * List of authors of the book.
         */
        public List<String> authors;

        /**
         * A description of the book.
         */
        public String description;
    }

    /**
     * Contains image link information for the book, specifically the thumbnail image.
     */
    public static class ImageLinks {
        /**
         * The thumbnail image link for the book.
         */
        public String thumbnail;
    }
}