package com.example.astudio.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages storage and retrieval of book reviews.
 */
public class ReviewManager {
    private final List<Review> reviews = new ArrayList<>();

    /**
     * Posts a review and notifies the caller via the provided callback.
     *
     * @param review   The review to post.
     * @param callback The callback to notify when the review is posted.
     */
    public void postReview(Review review, ReviewCallback callback) {
        reviews.add(review);
        if (callback != null) {
            // Return a new list to avoid accidental modifications.
            callback.onReviewPosted(new ArrayList<>(reviews));
        }
    }

    public interface ReviewCallback {
        void onReviewPosted(List<Review> updatedReviews);
    }
}