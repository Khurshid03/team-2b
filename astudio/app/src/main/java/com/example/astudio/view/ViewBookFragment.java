package com.example.astudio.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentViewBookBinding;
import com.example.astudio.model.Book;
import com.example.astudio.model.Review;
import com.example.astudio.view.ViewBookUI.ViewBookListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays detailed information about a selected book, including its title, author,
 * description, rating, and reviews. Users can also submit their reviews via a dialog.
 */
public class ViewBookFragment extends Fragment implements ViewBookUI {

    private static final String FRAGMENT_TAG = "ViewBookFragment";
    private FragmentViewBookBinding binding;
    private Book selectedBook;
    private ViewBookListener listener;
    private boolean isDescriptionExpanded = false;

    // Reviews list and adapter.
    private final List<Review> reviewsList = new ArrayList<>();
    private ReviewsAdapter reviewsAdapter;
    private String currentUsername;
    private boolean isSaved = false;

    public ViewBookFragment() {
        // Required empty public constructor.
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ViewBookListener) {
            listener = (ViewBookListener) context;
            Log.d(FRAGMENT_TAG, "Listener (ControllerActivity) attached.");
        } else {
            Log.e(FRAGMENT_TAG, "Host activity must implement ViewBookListener.");
            throw new IllegalStateException("Host must implement ViewBookListener");
        }
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentViewBookBinding.inflate(inflater, container, false);
        Log.d(FRAGMENT_TAG, "onCreateView called.");

        if (getArguments() != null) {
            selectedBook = (Book) getArguments().getSerializable("book");
            if (selectedBook != null) {
                Log.d(FRAGMENT_TAG, "Selected book retrieved: " + selectedBook.getTitle());
            } else {
                Log.e(FRAGMENT_TAG, "Selected book is NULL.");
            }
            if (getArguments().containsKey("username")) {
                currentUsername = getArguments().getString("username");
                Log.d(FRAGMENT_TAG, "Current username from args: " + currentUsername);
            }
        } else {
            Log.e(FRAGMENT_TAG, "getArguments() is null.");
        }
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(FRAGMENT_TAG, "onViewCreated called.");

        if (selectedBook != null) {
            updateBookDetails(selectedBook);
        } else {
            Log.e(FRAGMENT_TAG, "selectedBook is null.");
            Toast.makeText(getContext(), "Error: Book data not found.", Toast.LENGTH_LONG).show();
        }

        // Initialize adapter and RecyclerView
        reviewsAdapter = new ReviewsAdapter(reviewsList, new ReviewActionListener() {
            @Override
            public void onEditReview(Review review, int pos) {
                EditReviewDialogFragment dlg = EditReviewDialogFragment.newInstance(review);
                dlg.setOnReviewEditedListener((newRating, newComment) -> {
                    review.setRating(newRating);
                    review.setComment(newComment);
                    if (listener != null) {
                        listener.onEditReviewRequested(selectedBook, review, ViewBookFragment.this);
                    }
                });
                dlg.show(getChildFragmentManager(), "EditReviewDialog");
            }

            @Override
            public void onDeleteReview(Review review, int pos) {
                new AlertDialog.Builder(requireContext())
                        .setMessage(R.string.confirm_delete_review)
                        .setPositiveButton(android.R.string.yes, (d, w) -> {
                            if (listener != null) {
                                listener.onDeleteReviewRequested(selectedBook, review, ViewBookFragment.this);
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });
        binding.reviewsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reviewsRecycler.setAdapter(reviewsAdapter);
        Log.d(FRAGMENT_TAG, "RecyclerView and Adapter initialized.");

        // Now request reviews
        if (listener != null && selectedBook != null) {
            Log.i(FRAGMENT_TAG, "Requesting reviews for: " + selectedBook.getTitle());
            listener.fetchReviews(selectedBook, this);
        }

        binding.postReviewButton.setOnClickListener(v -> openPostReviewDialog());

        if (listener != null && selectedBook != null) {
            listener.isBookSaved(selectedBook, this);
        }

        binding.savedBooksButton.setOnClickListener(v -> {
            if (selectedBook == null || listener == null) {
                Toast.makeText(getContext(), "Error performing action.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isSaved) {
                listener.removeSavedBook(selectedBook, this);
            } else {
                listener.saveBook(selectedBook, this);
            }
        });
    }

    private void openPostReviewDialog() {
        if (selectedBook == null) {
            Toast.makeText(getContext(), "Cannot post review: Book data missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        PostReviewDialogFragment dialog = new PostReviewDialogFragment();
        dialog.setOnReviewSubmittedListener((rating, comment) -> {
            String author = currentUsername != null ? currentUsername : "Anonymous";
            Review newReview = new Review(
                    author,
                    rating,
                    comment,
                    "",
                    selectedBook.getTitle(),
                    selectedBook.getThumbnailUrl()
            );
            if (listener != null) listener.onReviewSubmitted(selectedBook, newReview, this);
        });
        dialog.show(getChildFragmentManager(), "PostReviewDialog");
    }

    @Override
    public void postReview(Review review) {
        reviewsList.add(review);
        reviewsAdapter.notifyItemInserted(reviewsList.size() - 1);
        binding.reviewsRecycler.scrollToPosition(reviewsList.size() - 1);
    }

    @Override
    public void displayReviews(List<Review> fetchedReviews) {
        if (!isAdded() || binding == null) return;
        reviewsList.clear();
        if (fetchedReviews != null) reviewsList.addAll(fetchedReviews);
        reviewsAdapter.notifyDataSetChanged();
    }

    @Override
    public void updateBookDetails(Book book) {
        binding.bookTitle.setText(book.getTitle());
        binding.bookAuthor.setText(getString(R.string.book_author, book.getAuthor()));
        binding.bookDescription.setText(book.getDescription());
        binding.bookRating.setRating(book.getRating());
        String thumb = book.getThumbnailUrl();
        if (thumb != null && !thumb.isEmpty()) {
            Glide.with(requireContext())
                    .load(thumb)
                    .placeholder(R.drawable.placeholder_cover)
                    .error(R.drawable.placeholder_cover)
                    .into(binding.bookCover);
        } else {
            binding.bookCover.setImageResource(R.drawable.placeholder_cover);
        }
        binding.showMoreButton.setOnClickListener(v -> {
            isDescriptionExpanded = !isDescriptionExpanded;
            binding.bookDescription.setMaxLines(isDescriptionExpanded ? Integer.MAX_VALUE : 5);
            binding.showMoreButton.setText(
                    isDescriptionExpanded ? R.string.show_less : R.string.show_more);
        });
    }

    @Override
    public void setListener(ViewBookListener listener) {
        this.listener = listener;
    }

    @Override
    public void onBookSaveState(boolean saved) {
        this.isSaved = saved;
        binding.savedBooksButton.setBackgroundTintList(
                ColorStateList.valueOf(
                        getResources().getColor(saved ? R.color.secondary : R.color.white, null)
                )
        );
        binding.savedBooksButton.setStrokeColor(
                ColorStateList.valueOf(
                        getResources().getColor(R.color.main, null)
                )
        );
        binding.savedBooksButton.setText(
                saved ? R.string.saved : R.string.save
        );
        binding.savedBooksButton.setIconResource(
                saved ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_outline
        );
    }

    @Override
    public void onBookSaveError(String message) {
        Toast.makeText(getContext(), "Save error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Inner adapter
    static class ReviewsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
        private final List<Review> reviewList;
        private final ReviewActionListener actionListener;

        ReviewsAdapter(List<Review> list, ReviewActionListener listener) {
            this.reviewList = list;
            this.actionListener = listener;
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
            if (actionListener != null) {
                holder.editButton.setOnClickListener(v -> actionListener.onEditReview(review, position));
                holder.deleteButton.setOnClickListener(v -> actionListener.onDeleteReview(review, position));
            } else {
                holder.editButton.setVisibility(View.GONE);
                holder.deleteButton.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return reviewList.size();
        }

        static class ReviewViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            private final android.widget.TextView usernameText;
            private final android.widget.RatingBar ratingBar;
            private final android.widget.TextView commentText;
            final android.widget.ImageButton editButton;
            final android.widget.ImageButton deleteButton;

            ReviewViewHolder(@NonNull View itemView) {
                super(itemView);
                usernameText = itemView.findViewById(R.id.review_username);
                ratingBar = itemView.findViewById(R.id.review_rating);
                commentText = itemView.findViewById(R.id.review_comment);
                editButton = itemView.findViewById(R.id.editReviewButton);
                deleteButton = itemView.findViewById(R.id.deleteReviewButton);
            }

            void bind(Review review) {
                usernameText.setText(review.getUsername());
                commentText.setText(review.getComment());
                ratingBar.setRating(review.getRating());
            }
        }
    }

    public interface ReviewActionListener {
        void onEditReview(Review review, int position);
        void onDeleteReview(Review review, int position);
    }
}
