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
     * Retrieves all reviews.
     *
     * @return A list of all reviews.
     */

    public interface ReviewCallback {
        void onReviewPosted(List<Review> updatedReviews);
    }
}