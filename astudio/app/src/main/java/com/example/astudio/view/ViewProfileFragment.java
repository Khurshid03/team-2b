package com.example.astudio.view;

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
    public void onEditReview(Review review, int position) {
        // You can implement opening an edit dialog and delegate update to the controller
    }

    @Override
    public void onDeleteReview(Review review, int position) {

    }

}



