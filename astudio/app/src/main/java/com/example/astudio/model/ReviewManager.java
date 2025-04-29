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

    public interface OnReviewSavedListener {
        void onReviewSaved();
        void onReviewSaveFailed(Exception e);
    }

}