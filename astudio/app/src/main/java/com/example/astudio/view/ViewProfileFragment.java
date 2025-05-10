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
import androidx.core.content.ContextCompat;
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
 * Fragment to display any user's profile: their username, follower/following counts,
 * and their submitted reviews. Supports editing/deleting reviews if viewing own profile,
 * and following/unfollowing other users.
 */
public class ViewProfileFragment extends Fragment
        implements ViewProfileUI, UserReviewsAdapter.ReviewActionListener {

    private static final String FRAGMENT_TAG = "ViewProfileFragment";
    protected FragmentViewProfileBinding binding;
    private UserReviewsAdapter reviewsAdapter;
    private final List<Review> userReviews = new ArrayList<>();
    private ControllerActivity controller;
    private String profileUserId; // The ID of the user whose profile is being viewed
    private String currentLoggedInUserId; // The ID of the currently logged-in user
    private String profileUsername; // The username of the profile being viewed

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration followersListenerRegistration;
    private ListenerRegistration followingListenerRegistration;

    // To keep track of the current follow state for the displayed profile
    private boolean isCurrentlyFollowingProfileUser = false;

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

        reviewsAdapter = new UserReviewsAdapter(userReviews, this, currentLoggedInUserId);
        binding.Reviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.Reviews.setAdapter(reviewsAdapter);

        fetchProfileAndSetupFollowButton();
    }

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

                setupRealtimeCounts(profileUsername);
                controller.fetchUserReviews(profileUsername, ViewProfileFragment.this);

                // Setup Follow Button
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
                    binding.btnFollow.setVisibility(View.GONE);
                    Log.d(FRAGMENT_TAG, "Viewing own profile or not logged in, hiding follow button.");
                }
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Error loading profile: " + error, Toast.LENGTH_LONG).show();
                Log.e(FRAGMENT_TAG, "Error loading profile for ID " + profileUserId + ": " + error);
                binding.btnFollow.setVisibility(View.GONE);
            }
        });
    }
    private void setupRealtimeCounts(String usernameToQuery) {
        // Real-time follower count
        if (followersListenerRegistration != null) followersListenerRegistration.remove();
        followersListenerRegistration = db.collectionGroup("Follow")
                .whereEqualTo("followed", usernameToQuery)
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded() || binding == null) return;
                    if (e != null) {
                        Log.w(FRAGMENT_TAG, "Followers count listen failed for " + usernameToQuery, e);
                        return;
                    }
                    if (snapshots != null) {
                        binding.followersButton.setText(getString(R.string.followers_count, snapshots.size()));
                    }
                });

        // Real-time following count (for the profile being viewed)
        if (followingListenerRegistration != null) followingListenerRegistration.remove();
        followingListenerRegistration = db.collection("Users")
                .document(profileUserId) // ID of the user whose profile is being viewed
                .collection("Follow")
                .addSnapshotListener((snapshots, e) -> {
                    if (!isAdded() || binding == null) return;
                    if (e != null) {
                        Log.w(FRAGMENT_TAG, "Following count listen failed for " + profileUserId, e);
                        return;
                    }
                    if (snapshots != null) {
                        binding.followingButton.setText(getString(R.string.following_count, snapshots.size()));
                    }
                });
    }


    private void updateFollowButtonUI(boolean isFollowing) {
        if (!isAdded() || binding == null || profileUsername == null) {
            // If profileUsername is null, we can't set up the click listener correctly.
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
                        () -> { // onSuccess
                            if (!isAdded()) return;
                            Log.d(FRAGMENT_TAG, "Successfully unfollowed " + profileUsername);
                            updateFollowButtonUI(false);
                            binding.btnFollow.setEnabled(true);
                            // Follower count for the profile user will update via snapshot listener
                        },
                        error -> { // onError
                            if (!isAdded()) return;
                            Log.e(FRAGMENT_TAG, "Failed to unfollow " + profileUsername + ": " + error);
                            Toast.makeText(getContext(), "Unfollow failed: " + error, Toast.LENGTH_SHORT).show();
                            binding.btnFollow.setEnabled(true);
                        }
                );
            } else {
                // Follow action
                controller.follow(profileUsername,
                        () -> { // onSuccess
                            if (!isAdded()) return;
                            Log.d(FRAGMENT_TAG, "Successfully followed " + profileUsername);
                            updateFollowButtonUI(true);
                            binding.btnFollow.setEnabled(true);
                            // Follower count for the profile user will update via snapshot listener
                        },
                        error -> { // onError
                            if (!isAdded()) return;
                            Log.e(FRAGMENT_TAG, "Failed to follow " + profileUsername + ": " + error);
                            Toast.makeText(getContext(), "Follow failed: " + error, Toast.LENGTH_SHORT).show();
                            binding.btnFollow.setEnabled(true);
                        }
                );
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
            controller.onEditUserReviewRequested(usernameForController, review, this);
        });
        dlg.show(getChildFragmentManager(), "EditReviewDialog");
    }

    @Override
    public void onDeleteReview(Review review, int pos) {
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
                    controller.onDeleteUserReviewRequested(usernameForController, review, this);
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onEditUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        Log.d(FRAGMENT_TAG, "Controller callback onEditUserReviewRequested for " + username);
    }

    @Override
    public void onDeleteUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        Log.d(FRAGMENT_TAG, "Controller callback onDeleteUserReviewRequested for " + username);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (followersListenerRegistration != null) {
            followersListenerRegistration.remove();
            followersListenerRegistration = null;
        }
        if (followingListenerRegistration != null) {
            followingListenerRegistration.remove();
            followingListenerRegistration = null;
        }
        binding = null;
        Log.d(FRAGMENT_TAG, "onDestroyView called, listeners removed.");
    }
}
