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
import com.example.astudio.persistence.FirestoreFacade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display any user's profile.
 * Shows username, follower/following counts, and submitted reviews.
 * Supports editing/deleting own reviews and following/unfollowing other users.
 */
public class ViewProfileFragment extends Fragment
        implements ViewProfileUI, UserReviewsAdapter.ReviewActionListener {

    private static final String FRAGMENT_TAG = "ViewProfileFragment";
    /** ViewBinding instance for the fragment layout. */
    protected FragmentViewProfileBinding binding;
    /** Adapter for displaying user reviews. */
    private UserReviewsAdapter reviewsAdapter;
    /** List to hold the reviews of the user whose profile is being viewed. */
    private final List<Review> userReviews = new ArrayList<>();
    /** Reference to the hosting ControllerActivity. */
    private ControllerActivity controller;
    /** The ID of the user whose profile is currently being viewed. */
    private String profileUserId;
    /** The ID of the currently logged-in user. */
    private String currentLoggedInUserId;
    /** The username of the profile being viewed. */
    private String profileUsername;

    /** Firestore database instance. */
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    /** Listener registration for real-time follower count updates. */
    private ListenerRegistration followersListenerRegistration;
    /** Listener registration for real-time following count updates. */
    private ListenerRegistration followingListenerRegistration;

    /** Flag to track if the current user is following the profile user. */
    private boolean isCurrentlyFollowingProfileUser = false;

    /**
     * Required empty public constructor.
     */
    public ViewProfileFragment() {
        // Required empty public constructor.
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Inflates the layout using ViewBinding and gets the current user's UID.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
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

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned.
     * Determines the profile user ID, sets up the reviews RecyclerView, and fetches profile data.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null && args.containsKey("userId")) {
            profileUserId = args.getString("userId");
        } else {
            profileUserId = currentLoggedInUserId; // Default to own profile if no userId is passed
        }

        if (profileUserId == null && currentLoggedInUserId == null) { // Check if we can't determine any profile
            Toast.makeText(getContext(), "User ID not available. Cannot load profile.", Toast.LENGTH_LONG).show();
            Log.e(FRAGMENT_TAG, "profileUserId and currentLoggedInUserId are null. Cannot proceed.");
            return;
        }
        if (profileUserId == null) { // If profileUserId was not in args, it defaults to currentLoggedInUserId
            profileUserId = currentLoggedInUserId;
        }

        Log.d(FRAGMENT_TAG, "Viewing profile for userId: " + profileUserId);
        Log.d(FRAGMENT_TAG, "Current logged in userId: " + currentLoggedInUserId);

        // Initialize adapter with the list, action listener, and current user's UID
        reviewsAdapter = new UserReviewsAdapter(userReviews, this, currentLoggedInUserId);
        binding.Reviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.Reviews.setAdapter(reviewsAdapter);

        fetchProfileAndSetupFollowButton();
    }

    /**
     * Fetches the profile data for the specified user ID and sets up the follow/unfollow button.
     */
    private void fetchProfileAndSetupFollowButton() {
        if (profileUserId == null) {
            Log.e(FRAGMENT_TAG, "fetchProfileAndSetupFollowButton: profileUserId is null.");
            Toast.makeText(getContext(), "Error: Profile ID missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        controller.fetchUserProfile(profileUserId, new FirestoreFacade.OnUserProfileFetchedListener() {
            @Override
            public void onFetched(User user) {
                if (!isAdded() || binding == null) return;

                if (user == null) {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    Log.w(FRAGMENT_TAG, "User not found for ID: " + profileUserId);
                    binding.btnFollow.setVisibility(View.GONE); // Hide follow button if user not found
                    return;
                }
                profileUsername = user.getUsername(); // Store the fetched username
                if (profileUsername == null) {
                    Log.e(FRAGMENT_TAG, "Username is null for user ID: " + profileUserId);
                    Toast.makeText(getContext(), "Username not available.", Toast.LENGTH_SHORT).show();
                    binding.tvUsername.setText("Error: No Username");
                    binding.btnFollow.setVisibility(View.GONE);
                    return;
                }
                binding.tvUsername.setText(profileUsername);
                Log.d(FRAGMENT_TAG, "Profile loaded for username: " + profileUsername);

                setupRealtimeCounts(profileUsername); // Setup listeners for counts
                controller.fetchUserReviews(profileUsername, ViewProfileFragment.this); // Fetch reviews

                // Setup Follow Button visibility and initial state
                if (currentLoggedInUserId != null && !profileUserId.equals(currentLoggedInUserId)) {
                    binding.btnFollow.setVisibility(View.VISIBLE);
                    // Check initial follow state
                    controller.fetchFollowingUsernames(currentLoggedInUserId, new FirestoreFacade.OnFollowedListFetchedListener() {
                        @Override
                        public void onFetched(List<String> currentlyFollowingUsernames) {
                            if (!isAdded() || binding == null) return;
                            isCurrentlyFollowingProfileUser = currentlyFollowingUsernames.contains(profileUsername);
                            updateFollowButtonUI(isCurrentlyFollowingProfileUser);
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded() || binding == null) return;
                            Log.e(FRAGMENT_TAG, "Error fetching following list: " + error);
                            Toast.makeText(getContext(), "Could not determine follow status.", Toast.LENGTH_SHORT).show();
                            updateFollowButtonUI(false); // Default to "Follow" state on error
                        }
                    });
                } else {
                    binding.btnFollow.setVisibility(View.GONE); // Hide follow button for own profile or if not logged in
                    Log.d(FRAGMENT_TAG, "Viewing own profile or not logged in, hiding follow button.");
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error loading profile: " + error, Toast.LENGTH_LONG).show();
                Log.e(FRAGMENT_TAG, "Error loading profile for ID " + profileUserId + ": " + error);
                binding.btnFollow.setVisibility(View.GONE); // Hide follow button on profile load error
            }
        });
    }

    /**
     * Sets up real-time Firestore listeners for follower and following counts.
     *
     * @param usernameToQuery The username of the profile user for follower count queries.
     */
    private void setupRealtimeCounts(String usernameToQuery) {
        // Real-time follower count
        if (followersListenerRegistration != null) followersListenerRegistration.remove(); // Remove previous listener if any
        followersListenerRegistration = db.collectionGroup("Follow")
                .whereEqualTo("followed", usernameToQuery)
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded() || binding == null) return; // Check fragment state
                    if (e != null) {
                        Log.w(FRAGMENT_TAG, "Followers count listen failed for " + usernameToQuery, e);
                        return;
                    }
                    if (snapshots != null) {
                        binding.followersButton.setText(getString(R.string.followers_count, snapshots.size()));
                    }
                });

        // Real-time following count (for the profile being viewed)
        if (followingListenerRegistration != null) followingListenerRegistration.remove(); // Remove previous listener if any
        followingListenerRegistration = db.collection("Users")
                .document(profileUserId) // ID of the user whose profile is being viewed
                .collection("Follow")
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded() || binding == null) return; // Check fragment state
                    if (e != null) {
                        Log.w(FRAGMENT_TAG, "Following count listen failed for " + profileUserId, e);
                        return;
                    }
                    if (snapshots != null) {
                        binding.followingButton.setText(getString(R.string.following_count, snapshots.size()));
                    }
                });
    }

    /**
     * Updates the UI of the follow button based on the current follow state.
     * Sets the text and click listener.
     *
     * @param isFollowing True if the current user is following the profile user, false otherwise.
     */
    private void updateFollowButtonUI(boolean isFollowing) {
        // Ensure fragment is added, binding is available, and profile username is known before updating UI
        if (!isAdded() || binding == null || profileUsername == null) {
            if (binding != null) binding.btnFollow.setVisibility(View.GONE); // Hide if essential info missing
            return;
        }

        isCurrentlyFollowingProfileUser = isFollowing; // Update the state variable

        if (isFollowing) {
            binding.btnFollow.setText(getString(R.string.Following));
            // Optionally change button style for "Following" state
            // binding.btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.grey));
        } else {
            binding.btnFollow.setText(getString(R.string.follow));
            // Optionally reset button style for "Follow" state
            // binding.btnFollow.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white)); // Assuming white is default
        }

        // Set the click listener for the follow/unfollow button
        binding.btnFollow.setOnClickListener(v -> {
            if (currentLoggedInUserId == null) {
                Toast.makeText(getContext(), "Please log in to follow users.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Disable button temporarily to prevent multiple clicks
            binding.btnFollow.setEnabled(false);

            if (isCurrentlyFollowingProfileUser) {
                // Unfollow action
                controller.unfollow(profileUsername,
                        () -> { // onSuccess callback
                            if (!isAdded()) return; // Check fragment state before UI updates
                            Log.d(FRAGMENT_TAG, "Successfully unfollowed " + profileUsername);
                            updateFollowButtonUI(false); // Update button state to "Follow"
                            binding.btnFollow.setEnabled(true); // Re-enable button
                            // Follower count for the profile user will update automatically via snapshot listener
                        },
                        error -> { // onError callback
                            if (!isAdded()) return; // Check fragment state
                            Log.e(FRAGMENT_TAG, "Failed to unfollow " + profileUsername + ": " + error);
                            Toast.makeText(getContext(), "Unfollow failed: " + error, Toast.LENGTH_SHORT).show();
                            binding.btnFollow.setEnabled(true); // Re-enable button
                        }
                );
            } else {
                // Follow action
                controller.follow(profileUsername,
                        () -> { // onSuccess callback
                            if (!isAdded()) return; // Check fragment state before UI updates
                            Log.d(FRAGMENT_TAG, "Successfully followed " + profileUsername);
                            updateFollowButtonUI(true); // Update button state to "Following"
                            binding.btnFollow.setEnabled(true); // Re-enable button
                            // Follower count for the profile user will update automatically via snapshot listener
                        },
                        error -> { // onError callback
                            if (!isAdded()) return; // Check fragment state
                            Log.e(FRAGMENT_TAG, "Failed to follow " + profileUsername + ": " + error);
                            Toast.makeText(getContext(), "Follow failed: " + error, Toast.LENGTH_SHORT).show();
                            binding.btnFollow.setEnabled(true); // Re-enable button
                        }
                );
            }
        });
    }


    /**
     * Displays the list of reviews submitted by the user whose profile is being viewed.
     * Implements {@link ViewProfileUI#displayUserReviews(List)}.
     *
     * @param reviews The list of {@link Review} objects to display.
     */
    @Override
    public void displayUserReviews(List<Review> reviews) {
        if (!isAdded() || binding == null) return; // Check fragment state
        userReviews.clear();
        if (reviews != null) {
            userReviews.addAll(reviews);
            Log.d(FRAGMENT_TAG, "Displayed " + reviews.size() + " user reviews.");
        } else {
            Log.d(FRAGMENT_TAG, "No user reviews to display.");
        }
        reviewsAdapter.notifyDataSetChanged(); // Update the adapter
    }

    /**
     * Handles the action when a user clicks the edit button for a review.
     * Opens the Edit Review dialog if the current user is the author.
     * Implements {@link UserReviewsAdapter.ReviewActionListener#onEditReview(Review, int)}.
     *
     * @param review The {@link Review} to be edited.
     * @param pos The adapter position of the review.
     */
    @Override
    public void onEditReview(Review review, int pos) {
        // Check if the current user is the author of the review
        if (review == null || review.getAuthorUid() == null || currentLoggedInUserId == null) {
            Log.e(FRAGMENT_TAG, "Review, Author UID, or currentLoggedInUserId is null, cannot edit.");
            return;
        }
        if (!review.getAuthorUid().equals(currentLoggedInUserId)) {
            Toast.makeText(getContext(), "You can only edit your own reviews.", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(FRAGMENT_TAG, "Editing review ID: " + review.getReviewId());
        EditReviewDialogFragment dlg = EditReviewDialogFragment.newInstance(review);
        dlg.setOnReviewEditedListener((newRating, newComment) -> {
            review.setRating(newRating);
            review.setComment(newComment);
            String usernameForController = binding.tvUsername.getText().toString();
            if (usernameForController.isEmpty() || usernameForController.equals("Error: No Username")) {
                Log.e(FRAGMENT_TAG, "Cannot edit review, profile username not available.");
                Toast.makeText(getContext(), "Error: Profile username not available.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Delegate the update request to the controller
            controller.onEditUserReviewRequested(usernameForController, review, this);
        });
        dlg.show(getChildFragmentManager(), "EditReviewDialog"); // Show dialog using child fragment manager
    }

    /**
     * Handles the action when a user clicks the delete button for a review.
     * Shows a confirmation dialog and delegates the deletion request to the controller.
     * Implements {@link UserReviewsAdapter.ReviewActionListener#onDeleteReview(Review, int)}.
     *
     * @param review The {@link Review} to be deleted.
     * @param pos The adapter position of the review.
     */
    @Override
    public void onDeleteReview(Review review, int pos) {
        // Check if the current user is the author of the review
        if (review == null || review.getAuthorUid() == null || currentLoggedInUserId == null) {
            Log.e(FRAGMENT_TAG, "Review, Author UID, or currentLoggedInUserId is null, cannot delete.");
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
                    String usernameForController = binding.tvUsername.getText().toString();
                    if (usernameForController.isEmpty() || usernameForController.equals("Error: No Username")) {
                        Log.e(FRAGMENT_TAG, "Cannot delete review, profile username not available.");
                        Toast.makeText(getContext(), "Error: Profile username not available.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Delegate the delete request to the controller
                    controller.onDeleteUserReviewRequested(usernameForController, review, this);
                })
                .setNegativeButton(android.R.string.no, null) // Dismiss dialog on "No"
                .show();
    }

    /**
     * Callback from the controller after an edit review request is processed.
     * Implements {@link ViewProfileUI#onEditUserReviewRequested(String, Review, ViewProfileUI)}.
     *
     * @param username The username of the profile user.
     * @param review The edited review.
     * @param ui The UI instance.
     */
    @Override
    public void onEditUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        Log.d(FRAGMENT_TAG, "Controller callback onEditUserReviewRequested for " + username);
        // The UI should be updated by fetching reviews again or updating the specific item if position is known.
        // For simplicity, re-fetching reviews is a common approach after changes.
        if (profileUsername != null) {
            controller.fetchUserReviews(profileUsername, this);
        }
    }

    /**
     * Callback from the controller after a delete review request is processed.
     * Implements {@link ViewProfileUI#onDeleteUserReviewRequested(String, Review, ViewProfileUI)}.
     *
     * @param username The username of the profile user.
     * @param review The deleted review.
     * @param ui The UI instance.
     */
    @Override
    public void onDeleteUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        Log.d(FRAGMENT_TAG, "Controller callback onDeleteUserReviewRequested for " + username);
        // Re-fetch reviews to update the list after deletion
        if (profileUsername != null) {
            controller.fetchUserReviews(profileUsername, this);
        }
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Removes Firestore snapshot listeners and cleans up the binding reference.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove real-time listeners to prevent memory leaks and unexpected updates
        if (followersListenerRegistration != null) {
            followersListenerRegistration.remove();
            followersListenerRegistration = null;
        }
        if (followingListenerRegistration != null) {
            followingListenerRegistration.remove();
            followingListenerRegistration = null;
        }
        binding = null; // Release binding
        Log.d(FRAGMENT_TAG, "onDestroyView called, listeners removed.");
    }
}
