package edu.vassar.litlore.view;

import android.content.Context; // Import Context for onAttach
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
import edu.vassar.litlore.R;
import edu.vassar.litlore.databinding.FragmentViewBookBinding; // Main fragment binding
import edu.vassar.litlore.databinding.ItemReviewBinding; // Binding for item_review.xml
import edu.vassar.litlore.model.Book;
import edu.vassar.litlore.model.Review;
// Firebase Auth
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
// Java utilities
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays detailed information about a selected book, including its title, author,
 * description, rating, and reviews.
 * Allows users to submit, edit, and delete their own reviews.
 * Uses ViewBinding throughout.
 */
public class ViewBookFragment extends Fragment implements ViewBookUI {

    private static final String FRAGMENT_TAG = "ViewBookFragment";
    /** ViewBinding for the fragment's layout. */
    private FragmentViewBookBinding binding;
    /** The book currently being displayed. */
    private Book selectedBook;
    /** Listener for communication with the hosting Activity/Controller. */
    private ViewBookListener listener;
    /** Flag to track the description expansion state. */
    private boolean isDescriptionExpanded = false;

    /** List to hold the reviews for the current book. */
    private final List<Review> reviewsList = new ArrayList<>();
    /** Adapter for displaying reviews in the RecyclerView. */
    private ReviewsAdapter reviewsAdapter;
    /** The unique ID of the currently logged-in user. */
    private String currentLoggedInUserUid;
    /** The username of the currently logged-in user. */
    private String currentLoggedInUsername;

    /** Flag indicating if the current book is saved by the logged-in user. */
    private boolean isSaved = false;

    /**
     * Required empty public constructor.
     */
    public ViewBookFragment() {
        // Required empty public constructor.
    }

    /**
     * Called when the fragment is attached to its host context.
     * Attaches the {@link ViewBookListener}.
     *
     * @param context The context of the host.
     */
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

    /**
     * Called to have the fragment instantiate its user interface view.
     * Inflates the layout using ViewBinding and retrieves the selected book from arguments.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
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

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned.
     * Sets up the UI elements, RecyclerView, adapter, and initiates data fetching.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
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
                // Check if the current user is the author of the review
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
                // Check if the current user is the author of the review
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
        }, currentLoggedInUserUid); // Pass current user UID to adapter

        binding.reviewsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.reviewsRecycler.setAdapter(reviewsAdapter);
        Log.d(FRAGMENT_TAG, "RecyclerView and Adapter initialized.");

        // Fetch data if book and listener are available
        if (listener != null && selectedBook != null) {
            Log.i(FRAGMENT_TAG, "Requesting reviews for: " + selectedBook.getTitle());
            listener.fetchReviews(selectedBook, this);
            listener.isBookSaved(selectedBook, this); // Check if the book is saved
        }

        // Set up button listeners
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

    /**
     * Opens the dialog for posting a new review.
     */
    private void openPostReviewDialog() {
        if (selectedBook == null) {
            Toast.makeText(getContext(), "Cannot post review: Book data missing.", Toast.LENGTH_SHORT).show();
            return;
        }
        PostReviewDialogFragment dialog = new PostReviewDialogFragment();
        dialog.setOnReviewSubmittedListener((rating, comment) -> {
            // Use the determined logged-in username or a fallback
            String authorForNewReviewObject = (currentLoggedInUsername != null && !currentLoggedInUsername.isEmpty()) ? currentLoggedInUsername : "Current User";
            Review newReview = new Review(
                    authorForNewReviewObject, // Username for the new review object
                    rating,
                    comment,
                    "", // Review ID will be assigned by Firestore
                    selectedBook.getTitle(), // Book ID (using title)
                    selectedBook.getThumbnailUrl(), // Thumbnail URL
                    currentLoggedInUserUid // Author UID
            );
            if (listener != null) {
                listener.onReviewSubmitted(selectedBook, newReview, this);
            }
        });
        dialog.show(getChildFragmentManager(), "PostReviewDialog");
    }

    /**
     * Adds a newly posted review to the list and updates the UI.
     * Implements {@link ViewBookUI#postReview(Review)}.
     *
     * @param review The {@link Review} object to add.
     */
    @Override
    public void postReview(Review review) {
        if (!isAdded() || binding == null) return; // Check fragment state
        Log.d(FRAGMENT_TAG, "postReview callback: Adding review by " + review.getUsername());
        reviewsList.add(0, review); // Add to the top
        reviewsAdapter.notifyItemInserted(0);
        binding.reviewsRecycler.scrollToPosition(0); // Scroll to the new review
    }

    /**
     * Displays a list of fetched reviews for the current book.
     * Implements {@link ViewBookUI#displayReviews(List)}.
     *
     * @param fetchedReviews The list of {@link Review} objects to display.
     */
    @Override
    public void displayReviews(List<Review> fetchedReviews) {
        if (!isAdded() || binding == null) { // Check fragment state
            Log.w(FRAGMENT_TAG, "displayReviews called but fragment not added or binding is null.");
            return;
        }
        Log.i(FRAGMENT_TAG, "displayReviews callback: Received " + (fetchedReviews != null ? fetchedReviews.size() : "null") + " reviews.");
        reviewsList.clear();
        if (fetchedReviews != null) {
            reviewsList.addAll(fetchedReviews);
        }
        reviewsAdapter.notifyDataSetChanged(); // Update the adapter
        if (reviewsList.isEmpty()) {
            Log.d(FRAGMENT_TAG, "No reviews to display for this book.");
        }
    }

    /**
     * Updates the UI elements with the details of the selected book.
     * Implements {@link ViewBookUI#updateBookDetails(Book)}.
     *
     * @param book The {@link Book} object containing the details.
     */
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
        // Set up the "Show More/Less" button for description
        binding.showMoreButton.setOnClickListener(v -> {
            isDescriptionExpanded = !isDescriptionExpanded;
            binding.bookDescription.setMaxLines(isDescriptionExpanded ? Integer.MAX_VALUE : 5); // Toggle max lines
            binding.showMoreButton.setText(
                    isDescriptionExpanded ? R.string.show_less : R.string.show_more); // Toggle button text
        });
    }

    /**
     * Sets the listener for this fragment.
     * Implements {@link ViewBookUI#setListener(ViewBookListener)}.
     *
     * @param listener The {@link ViewBookListener} to set.
     */
    @Override
    public void setListener(ViewBookListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the UI state of the "Save Book" button based on whether the book is saved.
     * Implements {@link ViewBookUI#onBookSaveState(boolean)}.
     *
     * @param saved True if the book is saved, false otherwise.
     */
    @Override
    public void onBookSaveState(boolean saved) {
        if (!isAdded() || binding == null) return; // Check fragment state
        this.isSaved = saved;
        // Update button appearance (background tint, stroke, text, icon)
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

    /**
     * Displays an error message related to saving or removing a book.
     * Implements {@link ViewBookUI#onBookSaveError(String)}.
     *
     * @param message The error message string.
     */
    @Override
    public void onBookSaveError(String message) {
        if (!isAdded()) return; // Check fragment state
        Toast.makeText(getContext(), "Save error: " + message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Cleans up the binding reference to prevent memory leaks.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(FRAGMENT_TAG, "onDestroyView called.");
        binding = null; // Release binding
    }

    // Inner adapter
    /**
     * RecyclerView Adapter for displaying book reviews.
     * Includes logic for showing edit/delete options only for the review author.
     */
    static class ReviewsAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
        /** The list of reviews to display. */
        private final List<Review> reviewList;
        /** Listener for edit/delete actions on reviews. */
        private final ReviewActionListener actionListener;
        /** The unique ID of the currently logged-in user. */
        private final String currentLoggedInUserUid;

        /**
         * Constructs a new ReviewsAdapter.
         * @param list The list of reviews.
         * @param listener The action listener for review interactions.
         * @param currentLoggedInUserUid The UID of the currently logged-in user (for showing edit/delete options).
         */
        ReviewsAdapter(List<Review> list, ReviewActionListener listener, String currentLoggedInUserUid) {
            this.reviewList = list;
            this.actionListener = listener;
            this.currentLoggedInUserUid = currentLoggedInUserUid;
        }

        /**
         * Called when RecyclerView needs a new {@link ReviewViewHolder} of the given type to represent an item.
         * Inflates the item layout using ViewBinding.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ReviewViewHolder that holds a View of the given view type.
         */
        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Inflate using ItemReviewBinding
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemReviewBinding itemBinding = ItemReviewBinding.inflate(inflater, parent, false);
            return new ReviewViewHolder(itemBinding); // Pass the binding to the ViewHolder
        }

        /**
         * Called by RecyclerView to display the data at the specified position.
         * Binds review data to the ViewHolder and sets up edit/delete button visibility and listeners.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
            Review review = reviewList.get(position);
            holder.bind(review);

            // Show edit/delete buttons only if the current user is the author
            if (currentLoggedInUserUid != null && review.getAuthorUid() != null && review.getAuthorUid().equals(currentLoggedInUserUid)) {
                holder.binding.editReviewButton.setVisibility(View.VISIBLE);
                holder.binding.deleteReviewButton.setVisibility(View.VISIBLE);

                if (actionListener != null) {
                    holder.binding.editReviewButton.setOnClickListener(v -> actionListener.onEditReview(review, holder.getAdapterPosition()));
                    holder.binding.deleteReviewButton.setOnClickListener(v -> actionListener.onDeleteReview(review, holder.getAdapterPosition()));
                } else {
                    // Hide if no action listener is provided
                    holder.binding.editReviewButton.setVisibility(View.GONE);
                    holder.binding.deleteReviewButton.setVisibility(View.GONE);
                }
            } else {
                // Hide for reviews not authored by the current user
                holder.binding.editReviewButton.setVisibility(View.GONE);
                holder.binding.deleteReviewButton.setVisibility(View.GONE);
            }
        }

        /**
         * Returns the total number of items in the data set held by the adapter.
         * @return The total number of items.
         */
        @Override
        public int getItemCount() {
            return reviewList != null ? reviewList.size() : 0;
        }

        /**
         * ViewHolder for the Review items.
         * Uses ItemReviewBinding to access item views.
         */
        static class ReviewViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            /** ViewBinding for the item layout. */
            private final ItemReviewBinding binding; // Store the binding

            /**
             * Constructs a new ReviewViewHolder.
             * @param itemBinding The ViewBinding for the item layout.
             */
            ReviewViewHolder(@NonNull ItemReviewBinding itemBinding) { // Constructor accepts ItemReviewBinding
                super(itemBinding.getRoot());
                this.binding = itemBinding; // Assign it
            }

            /**
             * Binds review data to the UI elements.
             * @param review The {@link Review} object to bind.
             */
            void bind(Review review) {
                if (review != null) {
                    // Access views via the binding object
                    binding.reviewUsername.setText(review.getUsername());
                    binding.reviewComment.setText(review.getComment());
                    binding.reviewRating.setRating(review.getRating());
                } else {
                    // Clear views if review is null
                    binding.reviewUsername.setText("");
                    binding.reviewComment.setText("");
                    binding.reviewRating.setRating(0);
                }
            }
        }
    }

    /**
     * Interface for actions performed on a review item (edit/delete).
     */
    public interface ReviewActionListener {
        /**
         * Called when the edit button for a review is clicked.
         * @param review The {@link Review} to be edited.
         * @param position The adapter position of the review.
         */
        void onEditReview(Review review, int position);
        /**
         * Called when the delete button for a review is clicked.
         * @param review The {@link Review} to be deleted.
         * @param position The adapter position of the review.
         */
        void onDeleteReview(Review review, int position);
    }
}
