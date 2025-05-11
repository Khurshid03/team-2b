package com.example.astudio.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.astudio.R;
import com.example.astudio.databinding.FragmentEditReviewDialogBinding; // Import the generated binding class
import com.example.astudio.model.Review;

/**
 * A DialogFragment for editing an existing book review.
 * Displays the current rating and comment and allows the user to modify them.
 */
public class EditReviewDialogFragment extends DialogFragment {

    /**
     * Listener interface for when a review is edited and submitted.
     */
    public interface OnReviewEditedListener {
        /**
         * Called when the user confirms the edited review.
         * @param newRating The new rating entered by the user.
         * @param newComment The new comment entered by the user.
         */
        void onReviewEdited(float newRating, String newComment);
    }

    private static final String ARG_REVIEW = "arg_review"; // Argument key for the original review
    private OnReviewEditedListener listener;
    private Review originalReview;

    private FragmentEditReviewDialogBinding binding;

    /**
     * Factory method to create a new instance of this fragment and pass the Review object.
     *
     * @param review The Review object to be edited.
     * @return A new instance of EditReviewDialogFragment.
     */
    public static EditReviewDialogFragment newInstance(Review review) {
        EditReviewDialogFragment fragment = new EditReviewDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_REVIEW, review); // Pass the review as a serializable argument
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Sets the listener that will be notified when the review is edited.
     *
     * @param l The listener object.
     */
    public void setOnReviewEditedListener(OnReviewEditedListener l) {
        this.listener = l;
    }

    /**
     * Called to do initial creation of the fragment.
     * Retrieves the original review from arguments.
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the original review from fragment arguments
        if (getArguments() != null) {
            originalReview = (Review) getArguments().getSerializable(ARG_REVIEW);
        }
    }

    /**
     * Override to build your own custom Dialog object.
     * Creates an AlertDialog with input fields pre-filled with the original review data.
     *
     * @param savedState The last saved instance state of the Fragment, or null if this is a freshly created Fragment.
     * @return A new Dialog instance to be displayed by the Fragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedState) {

        binding = FragmentEditReviewDialogBinding.inflate(LayoutInflater.from(requireContext()));

        // Pre-fill the dialog with the original review values
        if (originalReview != null) {
            binding.editReviewRating.setRating(originalReview.getRating()); // Access via binding
            binding.editReviewComment.setText(originalReview.getComment()); // Access via binding
        }

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.edit_review) // Set the dialog title
                .setView(binding.getRoot()) // Set the custom view from binding
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // When the "OK" button is clicked, notify the listener with the new values
                    if (listener != null) {
                        float newRating = binding.editReviewRating.getRating(); // Get value via binding
                        String newComment = binding.editReviewComment.getText().toString().trim(); // Get value via binding
                        listener.onReviewEdited(newRating, newComment);
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    // User cancelled the dialog, no action needed, dialog will dismiss
                    dismiss();
                });
        return builder.create();
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Release the binding here to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Important to null out the binding
    }
}
