package com.example.astudio.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.astudio.R;
import com.example.astudio.model.Review;

public class EditReviewDialogFragment extends DialogFragment {
    public interface OnReviewEditedListener {
        void onReviewEdited(float newRating, String newComment);
    }

    private static final String ARG_REVIEW = "arg_review";
    private OnReviewEditedListener listener;
    private Review originalReview;

    /** Factory method to pass in the Review to edit */
    public static EditReviewDialogFragment newInstance(Review review) {
        EditReviewDialogFragment fragment = new EditReviewDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REVIEW, review);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnReviewEditedListener(OnReviewEditedListener l) {
        listener = l;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            originalReview = (Review) getArguments().getSerializable(ARG_REVIEW);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedState) {
        // Inflate custom view
        View view = getLayoutInflater()
                .inflate(R.layout.fragment_edit_review_dialog, null);

        RatingBar ratingBar = view.findViewById(R.id.edit_review_rating);
        EditText commentEt = view.findViewById(R.id.edit_review_comment);

        // Pre-fill with original values
        if (originalReview != null) {
            ratingBar.setRating(originalReview.getRating());
            commentEt.setText(originalReview.getComment());
        }

        // Build AlertDialog
        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.edit_review)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    if (listener != null) {
                        float newRating = ratingBar.getRating();
                        String newComment = commentEt.getText().toString().trim();
                        listener.onReviewEdited(newRating, newComment);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }
}