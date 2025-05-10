package com.example.astudio.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
// AndroidX imports
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
// Glide for image loading
import com.bumptech.glide.Glide;
// Project-specific imports
import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentViewBookBinding; // Main fragment binding
import com.example.astudio.databinding.ItemReviewBinding; // Binding for item_review.xml
import com.example.astudio.model.Book;
import com.example.astudio.model.Review;
import com.example.astudio.view.ViewBookUI.ViewBookListener;
// Firebase Auth
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// Java utilities
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays detailed information about a selected book, including its title, author,
 * description, rating, and reviews. Users can also submit their reviews via a dialog.
 * Edit and delete operations for reviews are restricted to the author of the review.
 * This version uses ViewBinding throughout, including in the RecyclerView ViewHolder.
 */
public class ViewBookFragment extends Fragment implements ViewBookUI {

    private static final String FRAGMENT_TAG = "ViewBookFragment";
    private FragmentViewBookBinding binding; // ViewBinding for the fragment's layout
    private Book selectedBook;
    private ViewBookListener listener;
    private boolean isDescriptionExpanded = false;

    private final List<Review> reviewsList = new ArrayList<>();
    private ReviewsAdapter reviewsAdapter;
    private String currentLoggedInUserUid;
    private String currentLoggedInUsername;

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
            throw new IllegalStateException("Host activity must implement ViewBookListener");
        }
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment using ViewBinding
        binding = FragmentViewBookBinding.inflate(inflater, container, false);
        Log.d(FRAGMENT_TAG, "onCreateView called and binding inflated.");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentLoggedInUserUid = currentUser.getUid();
            currentLoggedInUsername = currentUser.getDisplayName();
            if (currentLoggedInUsername == null || currentLoggedInUsername.isEmpty()) {
                Log.w(FRAGMENT_TAG, "Logged in user's display name is null or empty. Consider fetching from Firestore profile.");
                // If display name is crucial for new reviews, you might fetch it here or rely on controller.
                // For now, the controller handles definitive username setting on submission.
            }
            Log.d(FRAGMENT_TAG, "Current logged-in user UID: " + currentLoggedInUserUid);
        } else {
            Log.w(FRAGMENT_TAG, "No user logged in.");
            currentLoggedInUserUid = null;
            currentLoggedInUsername = "Anonymous"; // Default for posting if not logged in
        }

        if (getArguments() != null) {
            selectedBook = (Book) getArguments().getSerializable("book");
            if (selectedBook != null) {
                Log.d(FRAGMENT_TAG, "Selected book retrieved: " + selectedBook.getTitle());
            } else {
                Log.e(FRAGMENT_TAG, "Selected book is NULL from arguments.");
            }
        } else {
            Log.e(FRAGMENT_TAG, "getArguments() is null. Cannot retrieve selected book.");
        }
        return binding.getRoot(); // Return the root view from the binding
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(FRAGMENT_TAG, "onViewCreated called.");

        if (selectedBook != null) {
            updateBookDetails(selectedBook);
        } else {
            Log.e(FRAGMENT_TAG, "selectedBook is null in onViewCreated. Cannot display details.");
            Toast.makeText(getContext(), "Error: Book data not found.", Toast.LENGTH_LONG).show();
            return; // Early exit if no book data
        }

        // Initialize adapter and RecyclerView
        reviewsAdapter = new ReviewsAdapter(reviewsList, new ReviewActionListener() {
            @Override
            public void onEditReview(Review review, int pos) {
                if (currentLoggedInUserUid != null && review.getAuthorUid() != null && review.getAuthorUid().equals(currentLoggedInUserUid)) {
                    EditReviewDialogFragment dlg = EditReviewDialogFragment.newInstance(review);
                    dlg.setOnReviewEditedListener((newRating, newComment) -> {
                        review.setRating(newRating);
                        review.setComment(newComment);
                        if (listener != null && selectedBook != null) {
                            listener.onEditReviewRequested(selectedBook, review, ViewBookFragment.this);
                        }
                    });
                    dlg.show(getChildFragmentManager(), "EditReviewDialog");
                } else {
                    Toast.makeText(getContext(), "You can only edit your own reviews.", Toast.LENGTH_SHORT).show();
                    Log.w(FRAGMENT_TAG, "Attempt to edit review not authored by current user.");
                }
            }

            @Override
            public void onDeleteReview(Review review, int pos) {
                if (currentLoggedInUserUid != null && review.getAuthorUid() != null && review.getAuthorUid().equals(currentLoggedInUserUid)) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Confirm Delete")
                            .setMessage(R.string.confirm_delete_review)
                            .setPositiveButton(android.R.string.yes, (d, w) -> {
                                if (listener != null && selectedBook != null) {
                                    listener.onDeleteReviewRequested(selectedBook, review, ViewBookFragment.this);
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                } else {
                    Toast.makeText(getContext(), "You can only delete your own reviews.", Toast.LENGTH_SHORT).show();
                    Log.w(FRAGMENT_TAG, "Attempt to delete review not authored by current user.");
                }
            }
        }, currentLoggedInUserUid);

        binding.reviewsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reviewsRecycler.setAdapter(reviewsAdapter);
        Log.d(FRAGMENT_TAG, "RecyclerView and Adapter initialized.");

        if (listener != null && selectedBook != null) {
            Log.i(FRAGMENT_TAG, "Requesting reviews for: " + selectedBook.getTitle());
            listener.fetchReviews(selectedBook, this);
            listener.isBookSaved(selectedBook, this);
        }

        binding.postReviewButton.setOnClickListener(v -> openPostReviewDialog());
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
            String authorForNewReviewObject = (currentLoggedInUsername != null && !currentLoggedInUsername.isEmpty()) ? currentLoggedInUsername : "Current User";
            Review newReview = new Review(
                    authorForNewReviewObject,
                    rating,
                    comment,
                    "",
                    selectedBook.getTitle(),
                    selectedBook.getThumbnailUrl()
            );
            if (listener != null) {
                listener.onReviewSubmitted(selectedBook, newReview, this);
            }
        });
        dialog.show(getChildFragmentManager(), "PostReviewDialog");
    }

    @Override
    public void postReview(Review review) {
        if (!isAdded() || binding == null) return;
        Log.d(FRAGMENT_TAG, "postReview callback: Adding review by " + review.getUsername());
        reviewsList.add(0, review);
        reviewsAdapter.notifyItemInserted(0);
        binding.reviewsRecycler.scrollToPosition(0);
    }

    @Override
    public void displayReviews(List<Review> fetchedReviews) {
        if (!isAdded() || binding == null) {
            Log.w(FRAGMENT_TAG, "displayReviews called but fragment not added or binding is null.");
            return;
        }
        Log.i(FRAGMENT_TAG, "displayReviews callback: Received " + (fetchedReviews != null ? fetchedReviews.size() : "null") + " reviews.");
        reviewsList.clear();
        if (fetchedReviews != null) {
            reviewsList.addAll(fetchedReviews);
        }
        reviewsAdapter.notifyDataSetChanged();
        if (reviewsList.isEmpty()) {
            Log.d(FRAGMENT_TAG, "No reviews to display for this book.");
        }
    }

    @Override
    public void updateBookDetails(Book book) {
        if (binding == null || book == null) {
            Log.e(FRAGMENT_TAG, "updateBookDetails: Binding or book is null.");
            return;
        }
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
        if (!isAdded() || binding == null) return;
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
        if (!isAdded()) return;
        Toast.makeText(getContext(), "Save error: " + message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(FRAGMENT_TAG, "onDestroyView called.");
        binding = null; // Release binding
    }

    // Inner adapter
    static class ReviewsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
        private final List<Review> reviewList;
        private final ReviewActionListener actionListener;
        private final String currentLoggedInUserUid;

        ReviewsAdapter(List<Review> list, ReviewActionListener listener, String currentLoggedInUserUid) {
            this.reviewList = list;
            this.actionListener = listener;
            this.currentLoggedInUserUid = currentLoggedInUserUid;
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate using ItemReviewBinding
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemReviewBinding itemBinding = ItemReviewBinding.inflate(inflater, parent, false);
            return new ReviewViewHolder(itemBinding); // Pass the binding to the ViewHolder
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Review review = reviewList.get(position);
            holder.bind(review);

            if (currentLoggedInUserUid != null && review.getAuthorUid() != null && review.getAuthorUid().equals(currentLoggedInUserUid)) {
                holder.binding.editReviewButton.setVisibility(View.VISIBLE);
                holder.binding.deleteReviewButton.setVisibility(View.VISIBLE);

                if (actionListener != null) {
                    holder.binding.editReviewButton.setOnClickListener(v -> actionListener.onEditReview(review, holder.getAdapterPosition()));
                    holder.binding.deleteReviewButton.setOnClickListener(v -> actionListener.onDeleteReview(review, holder.getAdapterPosition()));
                } else {
                    holder.binding.editReviewButton.setVisibility(View.GONE);
                    holder.binding.deleteReviewButton.setVisibility(View.GONE);
                }
            } else {
                holder.binding.editReviewButton.setVisibility(View.GONE);
                holder.binding.deleteReviewButton.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return reviewList != null ? reviewList.size() : 0;
        }

        // ViewHolder now uses ItemReviewBinding
        static class ReviewViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            private final ItemReviewBinding binding; // Store the binding

            ReviewViewHolder(@NonNull ItemReviewBinding itemBinding) { // Constructor accepts ItemReviewBinding
                super(itemBinding.getRoot());
                this.binding = itemBinding; // Assign it
            }

            void bind(Review review) {
                if (review != null) {
                    // Access views via the binding object
                    binding.reviewUsername.setText(review.getUsername());
                    binding.reviewComment.setText(review.getComment());
                    binding.reviewRating.setRating(review.getRating());
                } else {
                    binding.reviewUsername.setText("");
                    binding.reviewComment.setText("");
                    binding.reviewRating.setRating(0);
                }
            }
        }
    }

    public interface ReviewActionListener {
        void onEditReview(Review review, int position);
        void onDeleteReview(Review review, int position);
    }
}
