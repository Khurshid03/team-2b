package com.example.astudio.model;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages storage and retrieval of book reviews using Firestore,
 * and also keeps an in-memory store for fast local fetch (for unit tests).
 */
public class ReviewManager {

    // in-memory caches for unit-testing
    private final Map<String, List<Review>> reviewsByBook = new HashMap<>();
    private final Map<String, List<Review>> reviewsByUser = new HashMap<>();

    /**
     * Callback for in-memory fetch methods.
     */
    public interface OnReviewsFetchedListener {
        void onFetched(List<Review> reviews);
        void onError(String error);
    }

    /**
     * Saves a new review to Firestore and also to the local cache.
     */
    public void postReview(Book book, Review review, OnReviewSavedListener listener) {
        // 1) update local cache
        reviewsByBook
                .computeIfAbsent(book.getTitle(), k -> new ArrayList<>())
                .add(review);
        reviewsByUser
                .computeIfAbsent(review.getUsername(), k -> new ArrayList<>())
                .add(review);

        // 2) original Firestore write
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Reviews")
                .document(book.getTitle())
                .collection("UserReviews")
                .add(review)
                .addOnSuccessListener(docRef -> listener.onReviewSaved())
                .addOnFailureListener(listener::onReviewSaveFailed);
    }

    /**
     * Updates an existing review in Firestore and in the local cache.
     */
    public void updateReview(Review review, OnReviewUpdatedListener listener) {
        // 1) update local cache
        // — replace in reviewsByBook
        List<Review> bookList = reviewsByBook.get(review.getBookId());
        if (bookList != null) {
            for (int i = 0; i < bookList.size(); i++) {
                if (bookList.get(i).getReviewId().equals(review.getReviewId())) {
                    bookList.set(i, review);
                    break;
                }
            }
        }
        // — replace in reviewsByUser
        List<Review> userList = reviewsByUser.get(review.getUsername());
        if (userList != null) {
            for (int i = 0; i < userList.size(); i++) {
                if (userList.get(i).getReviewId().equals(review.getReviewId())) {
                    userList.set(i, review);
                    break;
                }
            }
        }

        // 2) original Firestore write
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
     * Deletes a review from Firestore and from the local cache.
     */
    public void deleteReview(Review review, OnReviewDeletedListener listener) {
        // 1) remove from local cache
        List<Review> bookList = reviewsByBook.get(review.getBookId());
        if (bookList != null) bookList.removeIf(r -> r.getReviewId().equals(review.getReviewId()));

        List<Review> userList = reviewsByUser.get(review.getUsername());
        if (userList != null) userList.removeIf(r -> r.getReviewId().equals(review.getReviewId()));

        // 2) original Firestore delete
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
     * Fetches all reviews for a given book from the in-memory cache.
     */
    public void fetchReviewsForBook(Book book, OnReviewsFetchedListener listener) {
        List<Review> list = reviewsByBook.getOrDefault(book.getTitle(), new ArrayList<>());
        listener.onFetched(new ArrayList<>(list));
    }

    /**
     * Fetches all reviews by a given user from the in-memory cache.
     */
    public void fetchUserReviewsByUsername(String username, OnReviewsFetchedListener listener) {
        List<Review> list = reviewsByUser.getOrDefault(username, new ArrayList<>());
        listener.onFetched(new ArrayList<>(list));
    }

    // --- your existing Firestore callback interfaces unchanged ---

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
