package com.example.astudio.model;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Manages storage and retrieval of book reviews using Firestore.
 * This class provides methods to post, update, and delete book reviews.
 */
public class ReviewManager {

    /**
     * Saves a new review to Firestore.
     * The review is stored under a subcollection specific to the book.
     * Firestore path: /Reviews/{bookId}/UserReviews/{reviewId}
     */
    public void postReview(Book book, Review review, OnReviewSavedListener listener) {
        // Save the review to Firestore
        // Firestore path: /books/{bookId}/reviews/{reviewId}
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Reviews")
                .document(book.getTitle()) // or book.getId() if you have unique IDs for books
                .collection("UserReviews")
                .add(review)
                .addOnSuccessListener(documentReference -> listener.onReviewSaved())
                .addOnFailureListener(listener::onReviewSaveFailed);
    }

    /**
     * Updates an existing review in Firestore.
     * The review is identified by its book ID and review ID.
     */
    public void updateReview(Review review, OnReviewUpdatedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Reviews")
                .document(review.getBookId())
                .collection("UserReviews")
                .document(review.getReviewId())
                .set(review)
                .addOnSuccessListener(aVoid -> listener.onReviewUpdated())
                .addOnFailureListener(listener::onReviewUpdateFailed);
    }

    /**
     * Deletes a review from Firestore.
     * The review is identified by its book ID and review ID.
     */
    public void deleteReview(Review review, OnReviewDeletedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Reviews")
                .document(review.getBookId())
                .collection("UserReviews")
                .document(review.getReviewId())
                .delete()
                .addOnSuccessListener(aVoid -> listener.onReviewDeleted())
                .addOnFailureListener(listener::onReviewDeleteFailed);
    }

    /**
     * Interface definition for a callback to be invoked when a review save operation completes.
     */
    public interface OnReviewSavedListener {
        /**
         * Called when the review has been successfully saved to Firestore.
         */
        void onReviewSaved();

        /**
         * Called when the review save operation fails.
         *
         * @param e The exception that occurred during the save operation.
         */
        void onReviewSaveFailed(Exception e);
    }

    /**
     * Interface definition for a callback to be invoked when a review update operation completes.
     */
    public interface OnReviewUpdatedListener {
        /**
         * Called when the review has been successfully updated in Firestore.
         */
        void onReviewUpdated();

        /**
         * Called when the review update operation fails.
         *
         * @param e The exception that occurred during the update operation.
         */
        void onReviewUpdateFailed(Exception e);
    }

    /**
     * Interface definition for a callback to be invoked when a review delete operation completes.
     */
    public interface OnReviewDeletedListener {
        /**
         * Called when the review has been successfully deleted from Firestore.
         */
        void onReviewDeleted();

        /**
         * Called when the review delete operation fails.
         *
         * @param e The exception that occurred during the delete operation.
         */
        void onReviewDeleteFailed(Exception e);
    }
}
