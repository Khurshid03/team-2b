package com.example.astudio.view;

import com.example.astudio.model.Book;
import com.example.astudio.model.Review;

import java.util.List;

/**
 * Interface that defines the methods that the user interface should contain.
 */
public interface UI {

    /**
     * Interface that controllers should implement to respond to UI actions.
     */
    interface Listener {

        /**
         * Called when a user starts the review process.
         */
        void onStartReview();

        /**
         * Called when a user selects a genre.
         *
         * @param genre the selected genre
         */
        void onGenreSelected(String genre);

        /**
         * Called when a user selects a book by ID.
         *
         * @param bookId the selected book ID
         */
        void onBookSelected(int bookId);

        /**
         * Called when the user submits a rating and comment.
         *
         * @param rating  rating value
         * @param comment review comment
         */
        void onReviewSubmitted(double rating, String comment);
    }

    /**
     * Assigns the listener that reacts to UI events.
     */
    void setListener(Listener listener);

    void run();

    void showGenres(List<String> genres);

    void showBooksInGenre(List<Book> books);

    void showSelectedBook(Book book);

    void showMessage(String message);

    void showReviews(List<Review> reviews, String context);
}
