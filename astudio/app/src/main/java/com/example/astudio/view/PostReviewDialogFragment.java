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
import com.example.astudio.R;

public class PostReviewDialogFragment extends DialogFragment {

    public interface OnReviewSubmittedListener {
        void onReviewSubmitted(float rating, String comment);
    }

    private OnReviewSubmittedListener listener;

    public void setOnReviewSubmittedListener(OnReviewSubmittedListener listener) {
        this.listener = listener;
    }

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
                if (listener != null) {
                    listener.onReviewSubmitted(rating, comment);
                }
                dialog.dismiss();
            });
        });
        return dialog;
    }
}