package edu.vassar.litlore.view;

import edu.vassar.litlore.model.Review;
import java.util.List;

/**
 * Interface defining the UI contract for the ViewProfileFragment.
 * Outlines methods for displaying user reviews and handling review actions.
 */
public interface ViewProfileUI {

    /**
     * Displays the list of reviews written by the user.
     *
     * @param reviews The list of user's reviews.
     */
    void displayUserReviews(List<Review> reviews);

    /**
     * Called when a user requests to edit a review.
     *
     * @param review The review to be edited.
     * @param position The adapter position of the review.
     */
    void onEditReview(Review review, int position);

    /**
     * Called when a user requests to delete a review.
     *
     * @param review The review to be deleted.
     * @param position The adapter position of the review.
     */
    void onDeleteReview(Review review, int position);

    /**
     * Callback from the controller after an edit review request is processed.
     *
     * @param username The username of the profile user.
     * @param review The edited review.
     * @param ui The UI instance.
     */
    void onEditUserReviewRequested(String username, Review review, ViewProfileUI ui);

    /**
     * Callback from the controller after a delete review request is processed.
     *
     * @param username The username of the profile user.
     * @param review The deleted review.
     * @param ui The UI instance.
     */
    void onDeleteUserReviewRequested(String username, Review review, ViewProfileUI ui);
}
