package com.example.astudio.view;

/**
 * Interface defining methods for displaying and interacting with book data in the Browse Books view.
 * It provides listeners for selecting books and genres, and updating the display of hot and genre-specific books.
 */
import com.example.astudio.model.Book;
import java.util.List;

public interface BrowseBooksUI {
    /**
     * Listener interface for handling events related to book and genre selection.
     */
    interface BrowseBooksListener {
        void onBookSelected(Book book);
        void onGenreSelected(String genre);
    }

    /**
     * Listener interface for handling clicks on hot books.
     */
    interface OnHotBookClickListener {
        void onHotBookClick(Book book);
    }

    /**
     * Listener interface for handling clicks on books from a specific genre.
     */
    interface OnGenreBookClickListener {
        void onGenreBookClick(Book book);
    }

    /**
     * Sets the listener for handling book and genre selection events.
     *
     * @param listener The listener to be set.
     */
    void setListener(BrowseBooksListener listener);

    /**
     * Updates the view with a list of hot books.
     *
     * @param books The list of books to be displayed as hot books.
     */
    void updateHotBooks(List<Book> books);

    /**
     * Updates the view with a list of books from a specific genre.
     *
     * @param books The list of books to be displayed as genre-specific books.
     */
    void updateGenreBooks(List<Book> books);
}