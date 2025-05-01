package com.example.astudio.view;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentViewProfileBinding;
import com.example.astudio.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.astudio.view.UserReviewsAdapter;
import com.example.astudio.view.EditReviewDialogFragment;
import com.example.astudio.view.ViewProfileUI;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment to display the user's profile, including their username and reviews.
 */
public class ViewProfileFragment extends Fragment implements ViewProfileUI, UserReviewsAdapter.ReviewActionListener {

    private FragmentViewProfileBinding binding;
    private UserReviewsAdapter reviewsAdapter;
    private final List<Review> userReviews = new ArrayList<>();
    private ControllerActivity controller;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewProfileBinding.inflate(inflater, container, false);
        controller = (ControllerActivity) getActivity();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();

            FirebaseFirestore.getInstance()
                    .collection("Users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String username = documentSnapshot.getString("username");
                            binding.tvUsername.setText("Username: " + username);
                            if (controller != null) {
                                controller.fetchUserReviews(username, this);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.tvUsername.setText("Username: Unknown");
                    });
        }

        reviewsAdapter = new UserReviewsAdapter(userReviews, this);
        binding.Reviews.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.Reviews.setAdapter(reviewsAdapter);

    }

    @Override
    public void displayUserReviews(List<Review> reviews) {
        userReviews.clear();
        userReviews.addAll(reviews);
        reviewsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDeleteReview(Review review, int pos) {
        new AlertDialog.Builder(requireContext())
                .setMessage(R.string.confirm_delete_review)
                .setPositiveButton(android.R.string.yes, (d, w) -> {
                    // 1) Remove just that one item locally:
                    userReviews.remove(pos);
                    reviewsAdapter.notifyItemRemoved(pos);

                    // 2) Tell your controller to delete in Firestore (no extra UI clearing):
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
    public void onEditReview(Review review, int pos) {
        EditReviewDialogFragment dlg = EditReviewDialogFragment.newInstance(review);
        dlg.setOnReviewEditedListener((newRating, newComment) -> {
            // 1) Immediately update that one item in the list:
            review.setRating(newRating);
            review.setComment(newComment);
            userReviews.set(pos, review);
            reviewsAdapter.notifyItemChanged(pos);

            // 2) Fire off the backend update (no UI clearing):
            controller.onEditUserReviewRequested(
                    binding.tvUsername.getText().toString(),
                    review,
                    this
            );
        });
        dlg.show(getChildFragmentManager(), "EditReviewDialog");
    }

    @Override
    public void onEditUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        controller.onEditUserReviewRequested(username, review, ui);
    }

    @Override
    public void onDeleteUserReviewRequested(String username, Review review, ViewProfileUI ui) {
        controller.onDeleteUserReviewRequested(username, review, ui);
    }

}



