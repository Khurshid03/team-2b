package edu.vassar.litlore.view;

import edu.vassar.litlore.model.Book;
import edu.vassar.litlore.model.Review;

import java.util.List;


/**
 * Interface defining the UI contract for the View Book screen.
 * It outlines the methods the View should expose and the listener interface for handling UI events.
 */
public interface ViewBookUI {

    /**
     * Listener interface for handling user interactions and data requests from the View Book screen.
     */
    interface ViewBookListener {
        /**
         * Called when a user submits a new review for a book.
         *
         * @param book The book being reviewed.
         * @param review The review that the user submitted.
         * @param viewBookUI The UI that generated the event.
         */
        void onReviewSubmitted(Book book, Review review, ViewBookUI viewBookUI);

        /**
         * Requests fetching reviews for a specific book.
         *
         * @param book The book for which reviews are requested.
         * @param viewBookUI The UI to update after reviews are fetched.
         */
        void fetchReviews(Book book, ViewBookUI viewBookUI);

        /**
         * Called when a user submits a new review.
         * (Note: This seems like a duplicate of onReviewSubmitted, consider consolidating).
         *
         * @param selectedBook The book being reviewed.
         * @param newReview The review submitted by the user.
         * @param viewBookFragment The fragment instance.
         */
        void onSubmitReview(Book selectedBook, Review newReview, ViewBookFragment viewBookFragment);

        /**
         * Called when the UI needs to fetch reviews for a particular book.
         * (Note: This seems like a duplicate of fetchReviews, consider consolidating).
         *
         * @param book The book for which reviews are requested.
         * @param viewBookUI The UI to update after reviews are fetched.
         */
        void fetchReviewsForBook(Book book, ViewBookUI viewBookUI);

        /**
         * Called when a user requests to edit an existing review.
         *
         * @param book The book the review belongs to.
         * @param review The review to be edited.
         * @param viewBookUI The UI that generated the event.
         */
        void onEditReviewRequested(Book book, Review review, ViewBookUI viewBookUI);

        /**
         * Called when a user requests to delete an existing review.
         *
         * @param book The book the review belongs to.
         * @param review The review to be deleted.
         * @param viewBookUI The UI that generated the event.
         */
        void onDeleteReviewRequested(Book book, Review review, ViewBookUI viewBookUI);

        /**
         * Called when a user requests to save the current book.
         *
         * @param book The book to save.
         * @param ui The UI that generated the event.
         */
        void saveBook(Book book, ViewBookUI ui);

        /**
         * Called when a user requests to remove the current book from saved list.
         *
         * @param book The book to remove.
         * @param ui The UI that generated the event.
         */
        void removeSavedBook(Book book, ViewBookUI ui);

        /**
         * Called when the UI needs to check if the current book is saved by the user.
         *
         * @param book The book to check.
         * @param ui The UI to update with the save state.
         */
        void isBookSaved(Book book, ViewBookUI ui);
    }

    /**
     * Adds a new review to the UI (e.g., updates the RecyclerView).
     *
     * @param review The new review to add to the UI.
     */
    void postReview(Review review);

    /**
     * Updates the UI with the details of the specified book.
     *
     * @param book The book whose details are to be displayed.
     */
    void updateBookDetails(Book book);

    /**
     * Sets the listener for handling UI events from the View Book screen.
     *
     * @param listener The listener to be set.
     */
    void setListener(ViewBookListener listener);

    /**
     * Displays a list of reviews for the book.
     *
     * @param reviews A list of reviews to display.
     */
    void displayReviews(List<Review> reviews);

    /**
     * Updates the UI state of the book save button.
     *
     * @param saved True if the book is saved, false otherwise.
     */
    void onBookSaveState(boolean saved);

    /**
     * Displays an error message related to saving/removing the book.
     *
     * @param message The error message string.
     */
    void onBookSaveError(String message);
}
