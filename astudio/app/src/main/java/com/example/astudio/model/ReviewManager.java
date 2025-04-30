package com.example.astudio.model;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages storage and retrieval of book reviews.
 */
public class ReviewManager {

    public void postReview(Book book, Review review, OnReviewSavedListener listener) {
        // Save the review to Firestore
        // Firestore path: /books/{bookId}/reviews/{reviewId}
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Reviews")
                .document(book.getTitle()) // or book.getId() if you have unique IDs
                .collection("UserReviews")
                .add(review)
                .addOnSuccessListener(documentReference -> listener.onReviewSaved())
                .addOnFailureListener(listener::onReviewSaveFailed);
    }


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

    public interface OnReviewSavedListener {
        void onReviewSaved();
        void onReviewSaveFailed(Exception e);
    }

    public interface OnReviewUpdatedListener {
        void onReviewUpdated();
        void onReviewUpdateFailed(Exception e);
    }

    public interface OnReviewDeletedListener {
        void onReviewDeleted();
        void onReviewDeleteFailed(Exception e);
    }

}