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
        // Inflate the layout using ViewBinding
        // Use requireActivity().getLayoutInflater() or LayoutInflater.from(requireContext())
        binding = DialogPostReviewBinding.inflate(LayoutInflater.from(requireContext()));

        // Set the custom view for the dialog using the root of the binding
        builder.setView(binding.getRoot())
                .setTitle(R.string.post_review_title)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    // User cancelled the dialog
                    dismiss();
                });
        // The positive button ("Submit") is handled by the custom button inside the layout

        final AlertDialog dialog = builder.create();

        // Set up the click listener for the custom submit button from the binding
        // It's common to set this up in onShowListener to override default dismiss behavior if needed,
        // or directly if the button is only for submission and then dismissal.
        // For this case, direct setup is fine as we manually dismiss.
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

            // All good, pass back to listener
            if (listener != null) {
                listener.onReviewSubmitted(rating, comment);
            }
            dialog.dismiss(); // Dismiss the dialog after successful submission
        });


        // If you don't want the dialog to automatically dismiss on positive/negative button clicks
        // (which is not the case here since we are using a custom button for submit),
        // you would typically set the positive/negative buttons to null in the builder
        // and handle them entirely with custom button listeners.
        // Here, setNegativeButton is fine as it just dismisses.

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
        // Nullify the binding object when the view is destroyed
        // This is important to prevent memory leaks, especially with DialogFragments.
        // Even though we use onCreateDialog, the view hierarchy is still managed,
        // and onDestroyView is called when the dialog is dismissed and its view is destroyed.
        binding = null;
    }
}


