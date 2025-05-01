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
import com.example.astudio.model.Review;

import java.util.List;

/**
 * Adapter for showing a user's reviews in their profile.
 * Uses item_user_review.xml (with an ImageView for the book cover).
 */
public class UserReviewsAdapter
        extends RecyclerView.Adapter<UserReviewsAdapter.ViewHolder> {

    public interface ReviewActionListener {
        void onEditReview(Review review, int position);
        void onDeleteReview(Review review, int position);
    }

    private final List<Review> reviews;
    private final ReviewActionListener actionListener;

    public UserReviewsAdapter(List<Review> reviews, ReviewActionListener actionListener) {
        this.reviews = reviews;
        this.actionListener = actionListener;
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

        holder.comment.setText(r.getComment());
        holder.rating.setRating(r.getRating());

        // Load cover thumbnail
        Glide.with(holder.cover.getContext())
                .load(r.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_cover)
                .into(holder.cover);

        holder.editBtn.setOnClickListener(v -> actionListener.onEditReview(r, position));
        holder.deleteBtn.setOnClickListener(v -> actionListener.onDeleteReview(r, position));
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
            cover      = itemView.findViewById(R.id.review_book_cover);
            username   = itemView.findViewById(R.id.review_username);
            comment    = itemView.findViewById(R.id.review_comment);
            rating     = itemView.findViewById(R.id.review_rating);
            editBtn    = itemView.findViewById(R.id.editReviewButton);
            deleteBtn  = itemView.findViewById(R.id.deleteReviewButton);
        }
    }
}