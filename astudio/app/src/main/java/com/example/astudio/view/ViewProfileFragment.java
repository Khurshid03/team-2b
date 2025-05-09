package com.example.astudio.view;

import android.app.AlertDialog;
import android.os.Bundle;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display any user's profile: their username, follower/following counts,
 * and their submitted reviews. Supports editing/deleting reviews if viewing own profile.
 */
public class ViewProfileFragment extends Fragment
        implements ViewProfileUI, UserReviewsAdapter.ReviewActionListener {

    protected FragmentViewProfileBinding binding;
    private UserReviewsAdapter reviewsAdapter;
    private final List<Review> userReviews = new ArrayList<>();
    private ControllerActivity controller;
    private String profileUserId;
    private String currentUserId;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentViewProfileBinding.inflate(inflater, container, false);
        controller = (ControllerActivity) requireActivity();
        FirebaseUser current = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = (current != null ? current.getUid() : null);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Determine which profile to show
        Bundle args = getArguments();
        profileUserId = (args != null && args.containsKey("userId"))
                ? args.getString("userId")
                : currentUserId;

        // Set up reviews RecyclerView
        reviewsAdapter = new UserReviewsAdapter(userReviews, this, currentUserId);
        binding.Reviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.Reviews.setAdapter(reviewsAdapter);

        // Fetch profile info
        controller.fetchUserProfile(profileUserId, new ControllerActivity.OnUserProfileFetchedListener() {
            @Override
            public void onFetched(User user) {
                if (user == null) {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }
                String username = user.getUsername();
                binding.tvUsername.setText(username);

                // Real-time follower count
                db.collectionGroup("Follow")
                        .whereEqualTo("followed", username)
                        .addSnapshotListener((snapshots, e) -> {
                            if (e != null || snapshots == null || !isAdded()) return;
                            binding.followersButton.setText(
                                    getString(R.string.followers_count, snapshots.size())
                            );
                        });

                // Real-time following count
                db.collection("Users")
                        .document(profileUserId)
                        .collection("Follow")
                        .addSnapshotListener((snapshots, e) -> {
                            if (e != null || snapshots == null || !isAdded()) return;
                            binding.followingButton.setText(
                                    getString(R.string.following_count, snapshots.size())
                            );
                        });

                // Load this user's reviews
                controller.fetchUserReviews(username, ViewProfileFragment.this);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(getContext(), "Error loading profile: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void displayUserReviews(List<Review> reviews) {
        userReviews.clear();
        if (reviews != null) userReviews.addAll(reviews);
        reviewsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onEditReview(Review review, int pos) {
        // Only allow editing own reviews
        if (!profileUserId.equals(currentUserId)) return;
        EditReviewDialogFragment dlg = EditReviewDialogFragment.newInstance(review);
        dlg.setOnReviewEditedListener((newRating, newComment) -> {
            review.setRating(newRating);
            review.setComment(newComment);
            reviewsAdapter.notifyItemChanged(pos);
            controller.onEditUserReviewRequested(
                    binding.tvUsername.getText().toString(),
                    review,
                    this
            );
        });
        dlg.show(getChildFragmentManager(), "EditReviewDialog");
    }

    @Override
    public void onDeleteReview(Review review, int pos) {
        if (!profileUserId.equals(currentUserId)) return;
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.confirm_delete_review)
                .setPositiveButton(android.R.string.yes, (d, w) -> {
                    userReviews.remove(pos);
                    reviewsAdapter.notifyItemRemoved(pos);
                    controller.onDeleteUserReviewRequested(
                            binding.tvUsername.getText().toString(),
                            review,
                            this
                    );
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    @Override
    public void onEditUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        // no-op: handled in onEditReview
    }

    @Override
    public void onDeleteUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        // no-op: handled in onDeleteReview
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
