package com.example.astudio.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.example.astudio.R;



/**
 * DialogFragment that presents a UI for submitting a review. It includes a RatingBar for rating and
 * an EditText for leaving a comment. When the review is submitted, the provided listener is notified.
 */

public class PostReviewDialogFragment extends DialogFragment {

    /**
     * Listener interface for receiving the submitted review's rating and comment.
     */
    public interface OnReviewSubmittedListener {
        void onReviewSubmitted(float rating, String comment);
    }

    private OnReviewSubmittedListener listener;

    /**
     * Sets the listener for handling the submission of the review.
     *
     * @param listener The listener that will be notified when a review is submitted.
     */
    public void setOnReviewSubmittedListener(OnReviewSubmittedListener listener) {
        this.listener = listener;
    }

    /**
     * Creates and returns the dialog for posting a review. This method inflates the layout, sets up
     * the RatingBar and EditText, and configures the submit button.
     *
     * @param savedInstanceState A bundle containing saved instance state, if any.
     * @return The created AlertDialog for posting a review.
     */
    @NonNull
    @Override
    public AlertDialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_post_review, null);

        RatingBar ratingBar = view.findViewById(R.id.dialog_rating_bar);
        EditText commentEditText = view.findViewById(R.id.dialog_comment);
        Button submitButton = view.findViewById(R.id.dialog_submit_button);

        builder.setView(view)
                .setTitle(R.string.post_review_title)
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            submitButton.setOnClickListener(v -> {
                float rating = ratingBar.getRating();
                String comment = commentEditText.getText().toString().trim();
                // 1) Require at least 1 star
                if (rating == 0f) {
                    Toast.makeText(getContext(),
                            "Please give at least one star", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 2) Require non-empty comment
                if (comment.isEmpty()) {
                    commentEditText.setError("Comment cannot be empty");
                    commentEditText.requestFocus();
                    return;
                }

                // All good, pass back to listener
                if (listener != null) {
                    listener.onReviewSubmitted(rating, comment);
                }
                dialog.dismiss();
            });
        });

        return dialog;
    }
}