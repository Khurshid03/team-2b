package com.example.astudio.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
// View import might not be strictly necessary if binding.getRoot() is always used
// import android.view.View;
// Button, EditText, RatingBar imports are no longer needed as views are accessed via binding
// import android.widget.Button;
// import android.widget.EditText;
// import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.astudio.R;
import com.example.astudio.databinding.DialogPostReviewBinding; // Import the generated binding class

/**
 * DialogFragment that presents a UI for submitting a review. It includes a RatingBar for rating and
 * an EditText for leaving a comment. When the review is submitted, the provided listener is notified.
 * This version uses ViewBinding to access views.
 */
public class PostReviewDialogFragment extends DialogFragment {

    /**
     * Listener interface for receiving the submitted review's rating and comment.
     */
    public interface OnReviewSubmittedListener {
        void onReviewSubmitted(float rating, String comment);
    }

    private OnReviewSubmittedListener listener;
    private DialogPostReviewBinding binding; // Declare the binding object

    /**
     * Sets the listener for handling the submission of the review.
     *
     * @param listener The listener that will be notified when a review is submitted.
     */
    public void setOnReviewSubmittedListener(OnReviewSubmittedListener listener) {
        this.listener = listener;
    }

    /**
     * Creates and returns the dialog for posting a review. This method inflates the layout using ViewBinding,
     * sets up the RatingBar and EditText, and configures the submit button.
     *
     * @param savedInstanceState A bundle containing saved instance state, if any.
     * @return The created AlertDialog for posting a review.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        binding = DialogPostReviewBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot())
                .setTitle(R.string.post_review_title)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // User cancelled the dialog
                    dismiss();
                });


        final AlertDialog dialog = builder.create();

        binding.dialogSubmitButton.setOnClickListener(v -> {
            // Access views via the binding object
            float rating = binding.dialogRatingBar.getRating();
            String comment = binding.dialogComment.getText().toString().trim();

            // 1) Require at least 1 star
            if (rating == 0f) {
                Toast.makeText(getContext(),
                        "Please give at least one star", Toast.LENGTH_SHORT).show();
                return; // Keep dialog open
            }

            // 2) Require non-empty comment
            if (comment.isEmpty()) {
                binding.dialogComment.setError("Comment cannot be empty");
                binding.dialogComment.requestFocus();
                return; // Keep dialog open
            }
            if (listener != null) {
                listener.onReviewSubmitted(rating, comment);
            }
            dialog.dismiss();
        });

        return dialog;
    }

    /**
     * Called when the view previously created by a DialogFragment (including the one set in onCreateDialog)
     * has been detached from the fragment.
     * Release the binding here to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}


