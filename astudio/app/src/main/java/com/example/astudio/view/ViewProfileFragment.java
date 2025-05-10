package com.example.astudio.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
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
import com.example.astudio.model.User;
// Import listener interface from FirestoreFacade
import com.example.astudio.persistence.FirestoreFacade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration; // For managing snapshot listeners

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display any user's profile: their username, follower/following counts,
 * and their submitted reviews. Supports editing/deleting reviews if viewing own profile.
 */
public class ViewProfileFragment extends Fragment
        implements ViewProfileUI, UserReviewsAdapter.ReviewActionListener {

    private static final String FRAGMENT_TAG = "ViewProfileFragment"; // For logging
    // Made binding accessible for SearchUsersFragment to update follower count
    // Consider a more robust callback mechanism or ViewModel for cross-fragment communication
    protected FragmentViewProfileBinding binding;
    private UserReviewsAdapter reviewsAdapter;
    private final List<Review> userReviews = new ArrayList<>();
    private ControllerActivity controller;
    private String profileUserId; // The ID of the user whose profile is being viewed
    private String currentLoggedInUserId; // The ID of the currently logged-in user

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration followersListenerRegistration; // To remove listener later
    private ListenerRegistration followingListenerRegistration; // To remove listener later

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewProfileBinding.inflate(inflater, container, false);
        controller = (ControllerActivity) requireActivity();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentLoggedInUserId = (currentUser != null ? currentUser.getUid() : null);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey("userId")) {
            profileUserId = args.getString("userId");
        } else {
            profileUserId = currentLoggedInUserId; // Default to own profile if no userId is passed
        }

        if (profileUserId == null) {
            Toast.makeText(getContext(), "User ID not available. Cannot load profile.", Toast.LENGTH_LONG).show();
            Log.e(FRAGMENT_TAG, "profileUserId is null. Cannot proceed.");
            // Optionally, navigate back or show an error state
            return;
        }

        Log.d(FRAGMENT_TAG, "Viewing profile for userId: " + profileUserId);
        Log.d(FRAGMENT_TAG, "Current logged in userId: " + currentLoggedInUserId);


        // Set up reviews RecyclerView
        reviewsAdapter = new UserReviewsAdapter(userReviews, this, currentLoggedInUserId);
        binding.Reviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.Reviews.setAdapter(reviewsAdapter);

        // Fetch profile info
        // Use FirestoreFacade.OnUserProfileFetchedListener
        controller.fetchUserProfile(profileUserId, new FirestoreFacade.OnUserProfileFetchedListener() {
            @Override
            public void onFetched(User user) {
                if (!isAdded() || binding == null) return; // Check if fragment is still active

                if (user == null) {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    Log.w(FRAGMENT_TAG, "User not found for ID: " + profileUserId);
                    return;
                }
                String username = user.getUsername();
                if (username == null) {
                    Log.e(FRAGMENT_TAG, "Username is null for user ID: " + profileUserId);
                    Toast.makeText(getContext(), "Username not available.", Toast.LENGTH_SHORT).show();
                    binding.tvUsername.setText("Error: No Username");
                    return; // Cannot proceed without username for other queries
                }
                binding.tvUsername.setText(username);
                Log.d(FRAGMENT_TAG, "Profile loaded for username: " + username);

                // Set up real-time follower count
                // This direct Firestore call is kept for real-time updates.
                // The controller's fetchFollowersCount is a one-time fetch.
                if (followersListenerRegistration != null) followersListenerRegistration.remove(); // Remove previous listener
                followersListenerRegistration = db.collectionGroup("Follow")
                        .whereEqualTo("followed", username) // Query by username being followed
                        .addSnapshotListener((snapshots, e) -> {
                            if (!isAdded() || binding == null) return; // Check fragment state
                            if (e != null) {
                                Log.w(FRAGMENT_TAG, "Followers count listen failed for " + username, e);
                                return;
                            }
                            if (snapshots != null) {
                                Log.d(FRAGMENT_TAG, "Followers count for " + username + ": " + snapshots.size());
                                binding.followersButton.setText(getString(R.string.followers_count, snapshots.size()));
                            }
                        });

                // Set up real-time following count
                // This direct Firestore call is kept for real-time updates.
                if (followingListenerRegistration != null) followingListenerRegistration.remove(); // Remove previous listener
                followingListenerRegistration = db.collection("Users")
                        .document(profileUserId) // The user whose profile we are viewing
                        .collection("Follow")
                        .addSnapshotListener((snapshots, e) -> {
                            if (!isAdded() || binding == null) return; // Check fragment state
                            if (e != null) {
                                Log.w(FRAGMENT_TAG, "Following count listen failed for " + profileUserId, e);
                                return;
                            }
                            if (snapshots != null) {
                                Log.d(FRAGMENT_TAG, "Following count for " + profileUserId + ": " + snapshots.size());
                                binding.followingButton.setText(getString(R.string.following_count, snapshots.size()));
                            }
                        });

                // Load this user's reviews using the controller
                controller.fetchUserReviews(user.getUsername(), ViewProfileFragment.this); // 'this' implements ViewProfileUI
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error loading profile: " + error, Toast.LENGTH_LONG).show();
                Log.e(FRAGMENT_TAG, "Error loading profile for ID " + profileUserId + ": " + error);
            }
        });
    }

    @Override
    public void displayUserReviews(List<Review> reviews) {
        if (!isAdded() || binding == null) return;
        userReviews.clear();
        if (reviews != null) {
            userReviews.addAll(reviews);
            Log.d(FRAGMENT_TAG, "Displayed " + reviews.size() + " user reviews.");
        } else {
            Log.d(FRAGMENT_TAG, "No user reviews to display.");
        }
        reviewsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditReview(Review review, int pos) {
        if (review == null || review.getAuthorUid() == null) {
            Log.e(FRAGMENT_TAG, "Review or Author UID is null, cannot edit.");
            return;
        }
        // Only allow editing own reviews
        if (!review.getAuthorUid().equals(currentLoggedInUserId)) {
            Toast.makeText(getContext(), "You can only edit your own reviews.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(FRAGMENT_TAG, "Editing review ID: " + review.getReviewId());
        EditReviewDialogFragment dlg = EditReviewDialogFragment.newInstance(review);
        dlg.setOnReviewEditedListener((newRating, newComment) -> {
            review.setRating(newRating);
            review.setComment(newComment);
            // The adapter might be updated visually, but the actual data update happens via controller
            // reviewsAdapter.notifyItemChanged(pos); // This will be handled when reviews are re-fetched

            // The username for onEditUserReviewRequested should be the profile user's username
            String profileUsername = binding.tvUsername.getText().toString();
            if (profileUsername.isEmpty() || profileUsername.equals("Error: No Username")) {
                Log.e(FRAGMENT_TAG, "Cannot edit review, profile username not available.");
                Toast.makeText(getContext(), "Error: Profile username not available.", Toast.LENGTH_SHORT).show();
                return;
            }

            controller.onEditUserReviewRequested(
                    profileUsername, // Username of the user whose reviews are being shown
                    review,
                    this // ViewProfileFragment itself as ViewProfileUI
            );
        });
        // Use getChildFragmentManager() for dialogs shown from a Fragment
        dlg.show(getChildFragmentManager(), "EditReviewDialog");
    }

    @Override
    public void onDeleteReview(Review review, int pos) {
        if (review == null || review.getAuthorUid() == null) {
            Log.e(FRAGMENT_TAG, "Review or Author UID is null, cannot delete.");
            return;
        }
        if (!review.getAuthorUid().equals(currentLoggedInUserId)) {
            Toast.makeText(getContext(), "You can only delete your own reviews.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(FRAGMENT_TAG, "Deleting review ID: " + review.getReviewId());
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Delete")
                .setMessage(R.string.confirm_delete_review)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    // The username for onDeleteUserReviewRequested should be the profile user's username
                    String profileUsername = binding.tvUsername.getText().toString();
                    if (profileUsername.isEmpty() || profileUsername.equals("Error: No Username")) {
                        Log.e(FRAGMENT_TAG, "Cannot delete review, profile username not available.");
                        Toast.makeText(getContext(), "Error: Profile username not available.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // The list update will happen when reviews are re-fetched by the controller
                    controller.onDeleteUserReviewRequested(
                            profileUsername, // Username of the user whose reviews are being shown
                            review,
                            this // ViewProfileFragment itself as ViewProfileUI
                    );
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    // These methods are part of ViewProfileUI but their primary logic is handled
    // by onEditReview and onDeleteReview which then call the controller.
    // The controller calls back to displayUserReviews to refresh.
    @Override
    public void onEditUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        // This is a callback from controller, but the action originates in this fragment's
        // onEditReview method. No specific action needed here as controller will call displayUserReviews.
        Log.d(FRAGMENT_TAG, "Controller callback onEditUserReviewRequested for " + username);
    }

    @Override
    public void onDeleteUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        // Similar to onEditUserReviewRequested, primary logic is in onDeleteReview.
        Log.d(FRAGMENT_TAG, "Controller callback onDeleteUserReviewRequested for " + username);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firestore listeners to prevent memory leaks and unwanted background updates
        if (followersListenerRegistration != null) {
            followersListenerRegistration.remove();
            followersListenerRegistration = null;
        }
        if (followingListenerRegistration != null) {
            followingListenerRegistration.remove();
            followingListenerRegistration = null;
        }
        binding = null; // Important for preventing memory leaks
        Log.d(FRAGMENT_TAG, "onDestroyView called, listeners removed.");
    }
}
