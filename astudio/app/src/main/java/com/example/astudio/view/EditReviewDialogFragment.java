package com.example.astudio.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
// Import View if still needed for other purposes, though not for accessing views directly
// import android.view.View;
// EditText and RatingBar imports are no longer needed if accessed only via binding
// import android.widget.EditText;
// import android.widget.RatingBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.astudio.R;
import com.example.astudio.databinding.FragmentEditReviewDialogBinding; // Import the generated binding class
import com.example.astudio.model.Review;

public class EditReviewDialogFragment extends DialogFragment {

    // Listener interface for when a review is edited and submitted.
    public interface OnReviewEditedListener {
        void onReviewEdited(float newRating, String newComment);
    }

    private static final String ARG_REVIEW = "arg_review"; // Argument key for the original review
    private OnReviewEditedListener listener; // Listener to communicate back to the calling fragment/activity
    private Review originalReview; // The review object being edited

    // ViewBinding instance for the dialog's layout
    private FragmentEditReviewDialogBinding binding;

    /**
     * Factory method to create a new instance of this fragment and pass the Review object.
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
     * @param l The listener object.
     */
    public void setOnReviewEditedListener(OnReviewEditedListener l) {
        this.listener = l;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the original review from fragment arguments
        if (getArguments() != null) {
            originalReview = (Review) getArguments().getSerializable(ARG_REVIEW);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedState) {
        // Inflate the custom view for the dialog using ViewBinding
        // Use requireActivity().getLayoutInflater() or LayoutInflater.from(requireContext())
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
