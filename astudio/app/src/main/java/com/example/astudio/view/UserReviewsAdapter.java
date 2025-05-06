package com.example.astudio.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.model.Review; // Assuming Review model exists

import java.util.List;

/**
 * Adapter for showing a user's reviews in their profile.
 * Uses item_user_review.xml (with an ImageView for the book cover).
 * This version includes logic to show edit/delete buttons only for the current user's reviews
 * and includes null checks for views.
 */
public class UserReviewsAdapter
        extends RecyclerView.Adapter<UserReviewsAdapter.ViewHolder> {

    public interface ReviewActionListener {
        void onEditReview(Review review, int position);
        void onDeleteReview(Review review, int position);
    }

    private final List<Review> reviews;
    private final ReviewActionListener actionListener;
    private final String currentUserId; // Store the current user's UID

    /**
     * Constructor for the adapter.
     *
     * @param reviews The list of reviews to display.
     * @param actionListener The listener for review actions.
     * @param currentUserId The UID of the currently logged-in user.
     */
    public UserReviewsAdapter(List<Review> reviews, ReviewActionListener actionListener, String currentUserId) {
        this.reviews = reviews;
        this.actionListener = actionListener;
        this.currentUserId = currentUserId; // Initialize currentUserId
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review r = reviews.get(position);

        // Add null checks before setting text/properties
        if (holder.username != null) {
            holder.username.setText(r.getUsername()); // Display the review author's username
        }
        if (holder.comment != null) {
            holder.comment.setText(r.getComment());
        }
        if (holder.rating != null) {
            holder.rating.setRating(r.getRating());
        }


        // Load cover thumbnail
        if (holder.cover != null) {
            Glide.with(holder.cover.getContext())
                    .load(r.getThumbnailUrl())
                    .placeholder(R.drawable.placeholder_cover) // Make sure you have a placeholder_cover drawable
                    .into(holder.cover);
        }


        // --- Implement show/hide logic for edit/delete buttons ---
        // IMPORTANT: This assumes your Review model has a getAuthorUid() method
        // that returns the UID of the user who wrote the review.
        boolean isCurrentUserReview = currentUserId != null && r.getAuthorUid() != null && currentUserId.equals(r.getAuthorUid());

        if (isCurrentUserReview) {
            if (holder.editBtn != null) holder.editBtn.setVisibility(View.VISIBLE);
            if (holder.deleteBtn != null) holder.deleteBtn.setVisibility(View.VISIBLE);

            // Set click listeners only if the buttons are visible and not null
            if (holder.editBtn != null) {
                holder.editBtn.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onEditReview(r, holder.getAdapterPosition());
                    }
                });
            }

            if (holder.deleteBtn != null) {
                holder.deleteBtn.setOnClickListener(v -> {
                    if (actionListener != null) {
                        actionListener.onDeleteReview(r, holder.getAdapterPosition());
                    }
                });
            }

        } else {
            // Hide buttons and remove click listeners for reviews by other users
            if (holder.editBtn != null) holder.editBtn.setVisibility(View.GONE); // Use GONE to remove from layout flow
            if (holder.deleteBtn != null) holder.deleteBtn.setVisibility(View.GONE);

            if (holder.editBtn != null) holder.editBtn.setOnClickListener(null);
            if (holder.deleteBtn != null) holder.deleteBtn.setOnClickListener(null);
        }
        // --- End of show/hide logic ---
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView username;
        TextView comment;
        RatingBar rating;
        ImageButton editBtn;
        ImageButton deleteBtn;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Double check these IDs match your item_user_review.xml
            cover      = itemView.findViewById(R.id.review_book_cover);
            username   = itemView.findViewById(R.id.review_username);
            comment    = itemView.findViewById(R.id.review_comment);
            rating     = itemView.findViewById(R.id.review_rating);
            editBtn    = itemView.findViewById(R.id.editReviewButton);
            deleteBtn  = itemView.findViewById(R.id.deleteReviewButton);
        }
    }
}
