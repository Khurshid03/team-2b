package com.example.astudio.view;

import com.example.astudio.model.Review;
import java.util.List;

/**
 * Interface defining the UI contract for the ViewProfileFragment.
 * This follows the MVC pattern where the controller interacts with the view via this interface.
 */
public interface ViewProfileUI {

    /**
     * Displays the list of reviews written by the current user.
     *
     * @param reviews The list of user's reviews fetched from the backend.
     */
    void displayUserReviews(List<Review> reviews);

    void onEditReview(Review review, int position);

    void onDeleteReview(Review review, int position);

    void onEditUserReviewRequested(String username, Review review, ViewProfileUI ui);
    void onDeleteUserReviewRequested(String username, Review review, ViewProfileUI ui);
}