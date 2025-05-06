package com.example.astudio.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log; // Import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentViewProfileBinding;
import com.example.astudio.model.Review;
import com.example.astudio.model.User; // Assuming User model exists
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display the user's profile, including their username and reviews.
 */
public class ViewProfileFragment extends Fragment implements ViewProfileUI, UserReviewsAdapter.ReviewActionListener {

    private static final String TAG = "ViewProfileFragment"; // Tag for logging

    private FragmentViewProfileBinding binding;
    private UserReviewsAdapter reviewsAdapter;
    private final List<Review> userReviews = new ArrayList<>();
    private ControllerActivity controller;
    private String profileUserId; // Store the UID of the profile being viewed

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = FragmentViewProfileBinding.inflate(inflater, container, false);
        controller = (ControllerActivity) getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        // Get the user ID from arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("userId")) {
            profileUserId = args.getString("userId");
            Log.d(TAG, "Profile User ID from arguments: " + profileUserId);
        }

        // If no user ID is supplied in arguments, default to the currently signed-in user's ID
        if (profileUserId == null) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null) {
                profileUserId = currentUser.getUid();
                Log.d(TAG, "Profile User ID defaulted to current user: " + profileUserId);
            } else {
                // Handle case where no user is signed in and no ID is provided
                Log.e(TAG, "No user signed in and no user ID provided in arguments.");
                Toast.makeText(getContext(), "User not signed in.", Toast.LENGTH_SHORT).show();
                // Optionally navigate back or show an error state
                return;
            }
        }

        // Initialize adapter BEFORE fetching data
        reviewsAdapter = new UserReviewsAdapter(userReviews, this);
        binding.Reviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.Reviews.setAdapter(reviewsAdapter); // Set the initialized adapter
        Log.d(TAG, "Reviews adapter initialized and set.");

        // Fetch the user's profile details (including username) using the UID
        Log.d(TAG, "Attempting to fetch user profile for ID: " + profileUserId);
        controller.fetchUserProfile(profileUserId, new ControllerActivity.OnUserProfileFetchedListener() {
            @Override
            public void onFetched(User user) {
                if (user != null) {
                    Log.d(TAG, "User profile fetched successfully. Username: " + user.getUsername());
                    binding.tvUsername.setText(user.getUsername());
                    // Now fetch reviews for this user
                    Log.d(TAG, "Attempting to fetch user reviews for username: " + user.getUsername());
                    controller.fetchUserReviews(user.getUsername(), ViewProfileFragment.this); // Assuming fetchUserReviews still uses username
                } else {
                    // Handle case where user profile couldn't be fetched (document doesn't exist)
                    Log.w(TAG, "User profile document not found for ID: " + profileUserId);
                    binding.tvUsername.setText("User Not Found");
                    Toast.makeText(getContext(), "Could not load user profile.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String err) {
                Log.e(TAG, "Error fetching user profile: " + err);
                binding.tvUsername.setText("Error Loading User");
                Toast.makeText(getContext(), "Error loading user profile: " + err, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void displayUserReviews(List<Review> reviews) {
        Log.d(TAG, "displayUserReviews called. Number of reviews: " + (reviews != null ? reviews.size() : 0));
        userReviews.clear();
        if (reviews != null) {
            userReviews.addAll(reviews);
        }
        // Check if adapter is initialized before notifying
        if (reviewsAdapter != null) {
            reviewsAdapter.notifyDataSetChanged();
            Log.d(TAG, "Reviews adapter notified of data change.");
        } else {
            Log.w(TAG, "Reviews adapter is null when trying to display reviews.");
        }
    }

    @Override
    public void onDeleteReview(Review review, int pos) {
        Log.d(TAG, "onDeleteReview called for review ID: " + review.getReviewId() + " at position: " + pos);
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.confirm_delete_review)
                .setPositiveButton(android.R.string.yes, (d, w) -> {
                    Log.d(TAG, "Delete confirmed.");
                    // 1) Remove just that one item locally:
                    if (pos >= 0 && pos < userReviews.size()) {
                        userReviews.remove(pos);
                        reviewsAdapter.notifyItemRemoved(pos);
                        Log.d(TAG, "Review removed locally at position: " + pos);
                    } else {
                        Log.w(TAG, "Attempted to remove review at invalid position: " + pos);
                    }


                    // 2) Tell your controller to delete in Firestore (no extra UI clearing):
                    // Pass the username from the UI, which should now be set after fetching the profile
                    String currentUsername = binding.tvUsername.getText().toString();
                    if (!currentUsername.equals("Loading...") && !currentUsername.equals("Error Loading User") && !currentUsername.equals("User Not Found")) {
                        controller.onDeleteUserReviewRequested(
                                currentUsername,
                                review,
                                this
                        );
                        Log.d(TAG, "Delete review requested from controller for username: " + currentUsername);
                    } else {
                        Log.w(TAG, "Cannot request review delete, username not properly loaded.");
                        Toast.makeText(getContext(), "Cannot delete review, profile not fully loaded.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, (d, w) -> Log.d(TAG, "Delete cancelled."))
                .show();
    }

    @Override
    public void onEditReview(Review review, int pos) {
        Log.d(TAG, "onEditReview called for review ID: " + review.getReviewId() + " at position: " + pos);
        EditReviewDialogFragment dlg = EditReviewDialogFragment.newInstance(review);
        dlg.setOnReviewEditedListener((newRating, newComment) -> {
            Log.d(TAG, "Review edited in dialog. New rating: " + newRating + ", New comment: " + newComment);
            // 1) Immediately update that one item in the list:
            if (pos >= 0 && pos < userReviews.size()) {
                review.setRating(newRating);
                review.setComment(newComment);
                userReviews.set(pos, review);
                reviewsAdapter.notifyItemChanged(pos);
                Log.d(TAG, "Review updated locally at position: " + pos);
            } else {
                Log.w(TAG, "Attempted to edit review at invalid position: " + pos);
            }


            // 2) Fire off the backend update (no UI clearing):
            // Pass the username from the UI, which should now be set after fetching the profile
            String currentUsername = binding.tvUsername.getText().toString();
            if (!currentUsername.equals("Loading...") && !currentUsername.equals("Error Loading User") && !currentUsername.equals("User Not Found")) {
                controller.onEditUserReviewRequested(
                        currentUsername,
                        review,
                        this
                );
                Log.d(TAG, "Edit review requested from controller for username: " + currentUsername);
            } else {
                Log.w(TAG, "Cannot request review edit, username not properly loaded.");
                Toast.makeText(getContext(), "Cannot edit review, profile not fully loaded.", Toast.LENGTH_SHORT).show();
            }
        });
        dlg.show(getChildFragmentManager(), "EditReviewDialog");
    }

    // Implementing the required methods from ViewProfileUI
    @Override
    public void onEditUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        // This method is required by the ViewProfileUI interface.
        // It seems like the logic for editing is already handled in onEditReview,
        // which calls the controller directly.
        // You can keep this method empty or add logging if needed for debugging the interface implementation.
        Log.d(TAG, "onEditUserReviewRequested called (interface implementation)");
        // The actual controller call is made in onEditReview
    }

    @Override
    public void onDeleteUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        // This method is required by the ViewProfileUI interface.
        // Similar to editing, the logic for deleting is handled in onDeleteReview,
        // which calls the controller directly.
        Log.d(TAG, "onDeleteUserReviewRequested called (interface implementation)");
        // The actual controller call is made in onDeleteReview
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        binding = null;
    }
}
