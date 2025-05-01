package com.example.astudio.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.astudio.R;
import com.example.astudio.model.Review;

import java.util.List; /**
     * Adapter for displaying a list of user reviews with options to edit and delete.
     */
    public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

        private final List<Review> reviewList;
        private final ReviewActionListener actionListener;

        /**
         * Interface for handling review actions.
         */
        public interface ReviewActionListener {
            void onEditReview(Review review, int position);
            void onDeleteReview(Review review, int position);
        }

        /**
         * Constructor for the adapter.
         *
         * @param reviews        List of reviews to display.
         * @param actionListener Listener to handle edit/delete actions.
         */
        public ReviewsAdapter(List<Review> reviews, ReviewActionListener actionListener) {
            this.reviewList = reviews;
            this.actionListener = actionListener;
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_review, parent, false);
            return new ReviewViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Review review = reviewList.get(position);
            holder.bind(review);

            holder.editButton.setOnClickListener(v -> actionListener.onEditReview(review, position));
            holder.deleteButton.setOnClickListener(v -> actionListener.onDeleteReview(review, position));
        }

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        /**
         * ViewHolder for each review item.
         */
        static class ReviewViewHolder extends RecyclerView.ViewHolder {
            private final TextView usernameText;
            private final TextView commentText;
            private final RatingBar ratingBar;
            final ImageButton editButton;
            final ImageButton deleteButton;

            public ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameText = itemView.findViewById(R.id.review_username);
                commentText = itemView.findViewById(R.id.review_comment);
                ratingBar = itemView.findViewById(R.id.review_rating);
                editButton = itemView.findViewById(R.id.editReviewButton);
                deleteButton = itemView.findViewById(R.id.deleteReviewButton);
            }

            /**
             * Binds review data to the UI elements.
             *
             * @param review The review to display.
             */
            public void bind(Review review) {
                usernameText.setText(review.getUsername());
                commentText.setText(review.getComment());
                ratingBar.setRating(review.getRating());
            }
        }
}
