package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentViewBookBinding;
import com.example.astudio.model.Book;
import com.example.astudio.model.Review;
import com.example.astudio.model.UserManager;

import java.util.ArrayList;
import java.util.List;

public class ViewBookFragment extends Fragment implements ViewBookUI {

    private FragmentViewBookBinding binding;
    private Book selectedBook;
    private ViewBookListener listener;
    private boolean isDescriptionExpanded = false;

    // Reviews list and adapter.
    private final List<Review> reviews = new ArrayList<>();
    private ReviewsAdapter reviewsAdapter;
    private String currentUsername;

    public ViewBookFragment() {
        // Required empty public constructor.
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewBookBinding.inflate(inflater, container, false);
        // Retrieve the book from the fragment arguments (if available)
        if (getArguments() != null) {
            selectedBook = (Book) getArguments().getSerializable("book");
            if(getArguments().containsKey("username")) {
                currentUsername = getArguments().getString("username");
            }
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (selectedBook != null) {
            updateBookDetails(selectedBook);
        }
        // Initialize the RecyclerView for reviews.
        reviewsAdapter = new ReviewsAdapter(reviews);
        binding.reviewsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reviewsRecycler.setAdapter(reviewsAdapter);

        // Set up the Post Review button to open the review dialog.
        binding.postReviewButton.setOnClickListener(v -> openPostReviewDialog());
    }

    /**
     * Opens the PostReviewDialogFragment to collect a review from the user.
     */
    private void openPostReviewDialog() {
        PostReviewDialogFragment dialog = new PostReviewDialogFragment();
        dialog.setOnReviewSubmittedListener((rating, comment) -> {
            // Retrieve the current username from UserManager.
            String currentUsername = (UserManager.getInstance().getCurrentUser() != null)
                    ? UserManager.getInstance().getCurrentUser().getUsername()
                    : "Anonymous";
            Review newReview = new Review(currentUsername, rating, comment);
            reviews.add(newReview);
            reviewsAdapter.notifyItemInserted(reviews.size() - 1);
        });
        dialog.show(getChildFragmentManager(), "PostReviewDialog");
    }

    @Override
    public void updateBookDetails(Book book) {
        binding.bookTitle.setText(book.getTitle());
        binding.bookAuthor.setText(getString(R.string.book_author, book.getAuthor()));
        binding.bookDescription.setText(book.getDescription());
        binding.bookRating.setRating(book.getRating());
        Glide.with(requireContext())
                .load(book.getThumbnailUrl())
                .placeholder(R.drawable.placeholder_cover)
                .into(binding.bookCover);

        // Toggle expand/collapse on the description.
        binding.showMoreButton.setOnClickListener(v -> {
            isDescriptionExpanded = !isDescriptionExpanded;
            if (isDescriptionExpanded) {
                binding.bookDescription.setMaxLines(Integer.MAX_VALUE);
                binding.showMoreButton.setText(R.string.show_less);
            } else {
                binding.bookDescription.setMaxLines(5);
                binding.showMoreButton.setText(R.string.show_more);
            }
        });
    }

    @Override
    public void setListener(ViewBookListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Inner adapter for reviews.
    private static class ReviewsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
        private final List<Review> reviewList;

        public ReviewsAdapter(List<Review> reviewList) {
            this.reviewList = reviewList;
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
        }

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        static class ReviewViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            private final android.widget.TextView usernameText;
            private final android.widget.RatingBar ratingBar;
            private final android.widget.TextView commentText;

            public ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameText = itemView.findViewById(R.id.review_username);
                ratingBar = itemView.findViewById(R.id.review_rating);
                commentText = itemView.findViewById(R.id.review_comment);
            }

            public void bind(Review review) {
                usernameText.setText(review.getUsername());
                commentText.setText(review.getComment());
                ratingBar.setRating(review.getRating());
            }
        }
    }
}