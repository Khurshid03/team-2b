package edu.vassar.litlore.view;

import android.util.Log; // Import Log for potential debugging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// ImageView, TextView, RatingBar, ImageButton imports are no longer needed if accessed only via binding
// import android.widget.ImageButton;
// import android.widget.ImageView;
// import android.widget.RatingBar;
// import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import edu.vassar.litlore.R;
import edu.vassar.litlore.databinding.ItemUserReviewBinding; // Import the generated binding class
import edu.vassar.litlore.model.Review;

import java.util.List;
import java.util.ArrayList; // Good practice to initialize lists

/**
 * Adapter for showing a user's reviews in their profile.
 * Uses item_user_review.xml (with an ImageView for the book cover).
 * This version includes logic to show edit/delete buttons only for the current user's reviews
 * and uses ViewBinding in its ViewHolder.
 */
public class UserReviewsAdapter
        extends RecyclerView.Adapter<UserReviewsAdapter.ViewHolder> {

    private static final String ADAPTER_TAG = "UserReviewsAdapter"; // For logging

    /**
     * Interface for handling actions performed on review items,
     * such as editing or deleting a review.
     */
    public interface ReviewActionListener {
        void onEditReview(Review review, int position);
        void onDeleteReview(Review review, int position);
    }

    private final List<Review> reviews;
    private final ReviewActionListener actionListener;
    private final String currentUserId; // UID of the currently logged-in user

    /**
     * Constructor for the adapter.
     *
     * @param reviews The list of reviews to display.
     * @param actionListener The listener for review actions (edit/delete).
     * @param currentUserId The UID of the currently logged-in user, used to determine authorship.
     */
    public UserReviewsAdapter(List<Review> reviews, ReviewActionListener actionListener, String currentUserId) {
        this.reviews = (reviews != null) ? reviews : new ArrayList<>();
        this.actionListener = actionListener;
        this.currentUserId = currentUserId;
        Log.d(ADAPTER_TAG, "Adapter created. Current User ID for edit/delete: " + currentUserId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ItemUserReviewBinding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemUserReviewBinding binding = ItemUserReviewBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding); // Pass the binding to the ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        // The binding of data to views and setting listeners is now handled within the ViewHolder's bind method
        holder.bind(review, currentUserId, actionListener);
    }

    @Override
    public int getItemCount() {
        return reviews != null ? reviews.size() : 0;
    }

    /**
     * ViewHolder for user review items. Uses ItemUserReviewBinding.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserReviewBinding binding; // Store the binding object

        /**
         * Constructor for the ViewHolder.
         * @param itemBinding The ViewBinding object for the item_user_review layout.
         */
        ViewHolder(@NonNull ItemUserReviewBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding; // Assign the binding object
        }

        /**
         * Binds a Review object to the views in the ViewHolder and sets up listeners.
         * @param review The Review object to display.
         * @param loggedInUserId The UID of the currently logged-in user.
         * @param listener ReviewActionListener for edit/delete actions.
         */
        void bind(final Review review, final String loggedInUserId, final ReviewActionListener listener) {
            if (review == null) {
                Log.w(ADAPTER_TAG, "Review object is null in bind(). Clearing views.");
                // Optionally clear views or set to a default "empty" state
                binding.reviewComment.setText("");
                binding.reviewRating.setRating(0);
                binding.reviewBookCover.setImageResource(R.drawable.placeholder_cover);
                binding.editReviewButton.setVisibility(View.GONE);
                binding.deleteReviewButton.setVisibility(View.GONE);
                return;
            }

            // Set review details
            binding.reviewComment.setText(review.getComment());
            binding.reviewRating.setRating(review.getRating());

            // Load book cover thumbnail for the review
            Glide.with(binding.reviewBookCover.getContext())
                    .load(review.getThumbnailUrl()) // Assuming Review model has getThumbnailUrl() for the book cover
                    .placeholder(R.drawable.placeholder_cover) // Ensure this drawable exists
                    .error(R.drawable.placeholder_cover)       // Fallback image on error
                    .into(binding.reviewBookCover);

            // Determine if the current logged-in user is the author of this review
            boolean isCurrentUserReview = loggedInUserId != null &&
                    review.getAuthorUid() != null &&
                    loggedInUserId.equals(review.getAuthorUid());

            if (isCurrentUserReview) {
                binding.editReviewButton.setVisibility(View.VISIBLE);
                binding.deleteReviewButton.setVisibility(View.VISIBLE);

                // Set click listeners only if the buttons are visible and an actionListener is provided
                if (listener != null) {
                    binding.editReviewButton.setOnClickListener(v ->
                            listener.onEditReview(review, getAdapterPosition()));
                    binding.deleteReviewButton.setOnClickListener(v ->
                            listener.onDeleteReview(review, getAdapterPosition()));
                } else {
                    // If no listener, hide buttons as they wouldn't do anything
                    binding.editReviewButton.setVisibility(View.GONE);
                    binding.deleteReviewButton.setVisibility(View.GONE);
                    Log.w(ADAPTER_TAG, "ReviewActionListener is null, hiding edit/delete buttons for review by " + review.getUsername());
                }
            } else {
                // Hide buttons for reviews not authored by the current user
                binding.editReviewButton.setVisibility(View.GONE);
                binding.deleteReviewButton.setVisibility(View.GONE);
            }
        }
    }
}

