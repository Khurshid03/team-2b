package com.example.astudio.view;

import com.example.astudio.model.Book;
import com.example.astudio.model.Review;

import java.util.List;


/**
 * Interface defining methods for displaying and interacting with book details in the ViewBookFragment.
 * This includes updating the book's details and handling user interactions such as clicking the back button.
 */

public interface ViewBookUI {

    // Listener interface for ViewBookFragment events.
    interface ViewBookListener {
        /**
         * Called when a user submits a new review for a book.
         *
         * @param book The book being reviewed.
         * @param review The review that the user submitted.
         * @param viewBookUI The view (fragment) that generated the event,
         *                   so the controller can update the UI if needed.
         */
        void onReviewSubmitted(Book book, Review review, ViewBookUI viewBookUI);
        void fetchReviews(Book book, ViewBookUI viewBookUI);

        void onSubmitReview(Book selectedBook, Review newReview, ViewBookFragment viewBookFragment);

        /**
         * Called when the UI needs to fetch reviews for a particular book.
         *
         * @param book The book for which reviews are requested.
         * @param viewBookUI The UI to update after reviews are fetched.
         */
        void fetchReviewsForBook(Book book, ViewBookUI viewBookUI);

        void onEditReviewRequested(Book book, Review review, ViewBookUI viewBookUI);

        void onDeleteReviewRequested(Book book, Review review, ViewBookUI viewBookUI);

        // In ViewBookUI.java, inside public interface ViewBookListener { … }
        void saveBook(Book book, ViewBookUI ui);
        void removeSavedBook(Book book, ViewBookUI ui);
        void isBookSaved(Book book, ViewBookUI ui);
    }

    /**
     * Adds a new review to the UI (e.g., updates the RecyclerView).
     *
     * @param review The new review to add to the UI.
     */
    void postReview(Review review);

    /**
     * Updates the UI with the details of the specified book, including title, author, description, and other relevant information.
     *
     * @param book The book whose details are to be displayed.
     */

    void updateBookDetails(Book book);

    /**
     * Sets the listener to handle events such as the back button click in the ViewBookFragment.
     *
     * @param listener The listener to be set for handling fragment events.
     */
    void setListener(ViewBookListener listener);

    /**
     * Display multiple reviews for the book.
     *
     * @param reviews A list of reviews fetched from Firestore.
     */
    void displayReviews(List<Review> reviews);

    void onBookSaveState(boolean b);

    void onBookSaveError(String message);
}