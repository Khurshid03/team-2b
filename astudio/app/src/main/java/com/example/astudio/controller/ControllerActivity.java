package com.example.astudio.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull; // For Javadoc @NonNull where applicable
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.astudio.R;
import com.example.astudio.databinding.FragmentBrowseBooksBinding;
import com.example.astudio.model.Book;
import com.example.astudio.model.Review;
import com.example.astudio.model.ReviewManager;
import com.example.astudio.model.User;
import com.example.astudio.persistence.FirestoreFacade;
import com.example.astudio.view.BrowseBooksFragment;
import com.example.astudio.view.CreateAccountFragment;
import com.example.astudio.view.CreateAccountUI;
import com.example.astudio.view.LoginFragment;
import com.example.astudio.view.LoginUI;
import com.example.astudio.view.MainUI;
import com.example.astudio.view.ViewBookFragment;
import com.example.astudio.view.BrowseBooksUI;
import com.example.astudio.view.ViewBookUI;
import com.example.astudio.view.ViewProfileUI;
import com.example.astudio.view.ViewSavedBooksUI;
import com.example.astudio.view.ViewSearchUsersUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.astudio.view.SearchBooksUI;
// import com.example.astudio.view.SearchBooksFragment; // Not strictly needed if only used for type in implements

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.example.astudio.persistence.GoogleApiFacade;

/**
 * This class acts as the main controller for the application, extending AppCompatActivity.
 * It orchestrates the application flow, manages interactions between the UI (Fragments and UI interfaces)
 * and the data layer (Facades for Firestore and Google Books API). It handles user authentication,
 * navigation, and data fetching requests, then updates the UI accordingly.
 * <p>
 * This controller implements various listener interfaces defined by the UI components
 * to respond to user actions and lifecycle events.
 */
public class ControllerActivity extends AppCompatActivity implements BrowseBooksUI.BrowseBooksListener,
        CreateAccountUI.CreateAccountListener, LoginUI.LoginListener, ViewBookUI.ViewBookListener {

    private static final String TAG = "ControllerActivity"; // Logcat tag for this class

    /**
     * Manages the main user interface, including fragment display and bottom navigation.
     * This instance is public to allow fragments to request UI changes, such as displaying new fragments.
     */
    public MainUI mainUI;

    /**
     * Manages review-specific operations, potentially with its own data logic separate from FirestoreFacade.
     */
    private final ReviewManager reviewManager = new ReviewManager();

    /**
     * Facade for all interactions with the Firebase Firestore database.
     * Used for operations like user data storage, review management (if not handled by ReviewManager),
     * saved books, and follow/unfollow system.
     */
    private FirestoreFacade firestoreFacade;

    /**
     * Firebase Authentication instance for managing user sign-in, sign-up, and current user state.
     */
    private FirebaseAuth mAuth;

    /**
     * Facade for all interactions with the Google Books API.
     * Used for searching books, fetching top-rated books, and fetching books by genre.
     */
    private GoogleApiFacade googleApiFacade;


    /**
     * Called when the activity is first created.
     * Initializes Firebase services (Authentication), data facades (FirestoreFacade, GoogleApiFacade),
     * and the main UI (MainUI). It then sets the content view and displays the initial
     * {@link CreateAccountFragment}.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in {@link #onSaveInstanceState}. Otherwise, it is null,
     * indicating a fresh start.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize facades and auth
        this.firestoreFacade = new FirestoreFacade();
        this.googleApiFacade = new GoogleApiFacade();
        this.mAuth = FirebaseAuth.getInstance();

        this.mainUI = new MainUI(this);
        setContentView(this.mainUI.getRootView());

        // Display CreateAccountFragment initially if not restoring from a saved state
        if (savedInstanceState == null) {
            CreateAccountFragment createAccountFragment = new CreateAccountFragment();
            // The fragment itself should acquire its listener in onAttach.
            createAccountFragment.setListener(this); // This line was present in the user's provided code.
            this.mainUI.displayFragment(createAccountFragment);
        }
    }

    /**
     * Fetches books based on a search query using the {@link GoogleApiFacade}.
     * This method is typically called from a UI component (e.g., {@link com.example.astudio.view.SearchBooksFragment})
     * that allows users to search for books.
     *
     * @param query The search query string entered by the user.
     * @param ui    The {@link SearchBooksUI} instance (usually a Fragment) that will be updated
     * with the search results or an error message.
     */
    public void fetchSearchBooks(String query, SearchBooksUI ui) {
        Log.d(TAG, "fetchSearchBooks called with query: " + query);
        googleApiFacade.searchBooks(
                query,
                21, // Max results for general search
                books -> { // onSuccess callback
                    if (ui != null) {
                        Log.d(TAG, "fetchSearchBooks successful, " + books.size() + " books found.");
                        ui.onSearchBooksSuccess(books);
                    }
                },
                error -> { // onFailure callback
                    if (ui != null) {
                        Log.e(TAG, "fetchSearchBooks failed: " + error);
                        ui.onSearchBooksFailure(error);
                    }
                }
        );
    }

    /**
     * Handles the selection of a book from a list (e.g., in {@link BrowseBooksFragment}).
     * Navigates to {@link ViewBookFragment} to display the details of the selected book.
     * This method is an implementation of {@link BrowseBooksUI.BrowseBooksListener}.
     *
     * @param book The {@link Book} object that was selected by the user.
     */
    @Override
    public void onBookSelected(Book book) {
        Log.d(TAG, "onBookSelected: " + book.getTitle());
        ViewBookFragment viewBookFragment = new ViewBookFragment();
        viewBookFragment.setListener(this); // This line was present in the user's provided code.
        Bundle args = new Bundle();
        args.putSerializable("book", book);
        args.putString("description", book.getDescription());
        args.putString("author", book.getAuthor());
        viewBookFragment.setArguments(args);
        mainUI.displayFragment(viewBookFragment);
    }

    /**
     * Handles the selection of a genre (e.g., from a list of genre buttons in {@link BrowseBooksFragment}).
     * Fetches and displays books belonging to the selected genre.
     * This method is an implementation of {@link BrowseBooksUI.BrowseBooksListener}.
     *
     * @param genre The name of the genre selected by the user.
     */
    @Override
    public void onGenreSelected(String genre) {
        Log.d(TAG, "Genre selected: " + genre + ". Attempting to fetch books.");
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (currentFragment instanceof BrowseBooksUI) {
            fetchBooksByGenre(genre, (BrowseBooksUI) currentFragment);
        } else {
            Log.w(TAG, "onGenreSelected: Current fragment does not implement BrowseBooksUI or is not BrowseBooksFragment.");
            // If not on BrowseBooksUI, the original code didn't specify navigation here.
            // One might navigate to BrowseBooksFragment with the genre:
            // BrowseBooksFragment browseFragment = BrowseBooksFragment.newInstanceWithGenre(genre);
            // mainUI.displayFragment(browseFragment);
        }
    }

    /**
     * Handles the request to create a new user account.
     * Uses Firebase Authentication to create the user and then saves additional user details
     * to Firestore via {@link FirestoreFacade}.
     * This method is an implementation of {@link CreateAccountUI.CreateAccountListener}.
     *
     * @param username The desired username for the new account.
     * @param email    The email address for the new account.
     * @param password The password for the new account.
     * @param ui       The {@link CreateAccountUI} instance (usually {@link CreateAccountFragment}) that initiated the request.
     */
    @Override
    public void onCreateAccount(String username, String email, String password, CreateAccountUI ui) {
        Log.d(TAG, "onCreateAccount called for email: " + email);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            firestoreFacade.saveNewUser(uid, username, email,
                                    () -> { // onSuccess for saveNewUser
                                        Log.i(TAG, "User account created and data saved for UID: " + uid);
                                        BrowseBooksFragment fragment = new BrowseBooksFragment();
                                        fragment.setListener(this); // This line was present.
                                        mainUI.displayFragment(fragment);
                                    },
                                    e -> { // onFailure for saveNewUser (OnFailureListener)
                                        Log.e(TAG, "Failed to save user data to Firestore after account creation.", e);
                                        Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                            );
                        } else {
                            Log.e(TAG, "Firebase user is null after successful account creation task.");
                            Toast.makeText(this, "Account creation partially failed. Please try logging in.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w(TAG, "Account creation failed for email: " + email, task.getException());
                        Toast.makeText(this, "Account creation failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Handles the navigation from the account creation screen to the login screen.
     * Displays the {@link LoginFragment}.
     * This method is an implementation of {@link CreateAccountUI.CreateAccountListener}.
     */
    @Override
    public void onProceedToLogin() {
        Log.d(TAG, "onProceedToLogin called. Navigating to LoginFragment.");
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setListener(this); // This line was present.
        mainUI.displayFragment(loginFragment);
    }

    /**
     * Handles login attempts using only a username.
     * Note: This specific login flow might be deprecated or intended for a different authentication mechanism.
     * This method is an implementation of {@link LoginUI.LoginListener}.
     *
     * @param username The username entered by the user.
     */
    @Override
    public void onLogin(String username) {
        Log.d(TAG, "onLogin with username only called (currently not implemented for navigation): " + username);
    }

    /**
     * Handles login attempts using email and password.
     * Uses Firebase Authentication to sign in the user. On success, navigates to the {@link BrowseBooksFragment}.
     * This method is an implementation of {@link LoginUI.LoginListener}.
     *
     * @param email    The email address entered by the user.
     * @param password The password entered by the user.
     * @param ui       The {@link LoginUI} instance (usually {@link LoginFragment}) that initiated the request.
     */
    @Override
    public void onLogin(String email, String password, LoginUI ui) {
        Log.d(TAG, "onLogin called for email: " + email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.i(TAG, "Login successful for email: " + email);
                        BrowseBooksFragment fragment = new BrowseBooksFragment();
                        fragment.setListener(this); // This line was present.
                        mainUI.displayFragment(fragment);
                    } else {
                        Log.w(TAG, "Login failed for email: " + email, task.getException());
                        Toast.makeText(this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Fetches and displays a welcome message for the current user (or "Guest" if no user is logged in).
     * This is typically called by {@link BrowseBooksFragment} to personalize its greeting.
     *
     * @param ui The {@link BrowseBooksUI} instance to update with the welcome message.
     */
    public void fetchWelcomeMessage(BrowseBooksUI ui) {
        FirebaseUser me = mAuth.getCurrentUser();
        if (me == null) {
            Log.d(TAG, "fetchWelcomeMessage: No user logged in, displaying 'Guest'.");
            if (ui != null) ui.displayWelcomeMessage(getString(R.string.welcome_message, "Guest"));
            return;
        }
        String uid = me.getUid();
        Log.d(TAG, "fetchWelcomeMessage: Fetching profile for UID: " + uid);
        firestoreFacade.fetchUserProfile(uid, new FirestoreFacade.OnUserProfileFetchedListener() {
            @Override public void onFetched(User user) {
                String name = (user != null && user.getUsername() != null && !user.getUsername().isEmpty())
                        ? user.getUsername()
                        : "User";
                Log.d(TAG, "fetchWelcomeMessage: Welcome message for: " + name);
                if (ui != null) ui.displayWelcomeMessage(getString(R.string.welcome_message, name));
            }
            @Override public void onError(String err) {
                Log.w(TAG, "fetchWelcomeMessage: onError fetching profile: " + err);
                if (ui != null) ui.displayWelcomeMessage(getString(R.string.welcome_message, "User"));
            }
        });
    }

    /**
     * Fetches a list of top-rated books from the Google Books API via {@link GoogleApiFacade}.
     * Updates the provided {@link BrowseBooksUI} with the results.
     * Manages a loading spinner visibility on the UI.
     *
     * @param ui The UI interface to update with books or error messages.
     */
    public void fetchTopRatedBooks(BrowseBooksUI ui) {
        if (ui == null || ui.getRootView() == null) {
            Log.e(TAG, "fetchTopRatedBooks: UI or its root view is null. Cannot proceed.");
            return;
        }
        FragmentBrowseBooksBinding.bind(ui.getRootView()).loadingSpinner.setVisibility(View.VISIBLE);
        Log.d(TAG, "fetchTopRatedBooks: Requesting top rated books.");

        googleApiFacade.fetchTopRatedBooks(
                10,
                books -> {
                    if (ui.getRootView() != null) {
                        FragmentBrowseBooksBinding.bind(ui.getRootView()).loadingSpinner.setVisibility(View.GONE);
                        Log.i(TAG, "fetchTopRatedBooks: Successfully fetched " + books.size() + " books.");
                        ui.updateHotBooks(books);
                    }
                },
                error -> {
                    if (ui.getRootView() != null) {
                        FragmentBrowseBooksBinding.bind(ui.getRootView()).loadingSpinner.setVisibility(View.GONE);
                        Log.e(TAG, "fetchTopRatedBooks: Failed to load books: " + error);
                        Toast.makeText(ControllerActivity.this, "Failed to load hot books: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Fetches books for a specific genre from the Google Books API via {@link GoogleApiFacade}.
     * Updates the provided {@link BrowseBooksUI} with the results.
     * Manages a loading spinner visibility on the UI.
     *
     * @param genre The genre to search for.
     * @param ui The UI interface to update with books or error messages.
     */
    public void fetchBooksByGenre(String genre, BrowseBooksUI ui) {
        if (ui == null || ui.getRootView() == null) {
            Log.e(TAG, "fetchBooksByGenre: UI or its root view is null for genre: " + genre);
            return;
        }
        FragmentBrowseBooksBinding.bind(ui.getRootView()).loadingSpinner.setVisibility(View.VISIBLE);
        Log.d(TAG, "fetchBooksByGenre: Requesting books for genre: " + genre);

        googleApiFacade.fetchBooksByGenre(
                genre,
                12,
                books -> {
                    if (ui.getRootView() != null) {
                        FragmentBrowseBooksBinding.bind(ui.getRootView()).loadingSpinner.setVisibility(View.GONE);
                        Log.i(TAG, "fetchBooksByGenre: Successfully fetched " + books.size() + " books for genre '" + genre + "'.");
                        ui.updateGenreBooks(books);
                    }
                },
                error -> {
                    if (ui.getRootView() != null) {
                        FragmentBrowseBooksBinding.bind(ui.getRootView()).loadingSpinner.setVisibility(View.GONE);
                        Log.e(TAG, "fetchBooksByGenre: Failed to load books for genre '" + genre + "': " + error);
                        Toast.makeText(ControllerActivity.this, "Failed to load " + genre + " books: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Handles navigation from a search context back to the main book browsing screen.
     * This method is intended to be called by {@link com.example.astudio.view.SearchBooksFragment}
     * (e.g., via a listener interface that this Activity would implement).
     */
    public void onSearchBooksNavigateBack() {
        Log.d(TAG, "onSearchBooksNavigateBack: Navigating to BrowseBooksFragment.");
        BrowseBooksFragment browseFragment = new BrowseBooksFragment();
        mainUI.displayFragment(browseFragment);
    }

    /**
     * Handles the submission of a review when a partially formed {@link Review} object is provided by the UI.
     * This method completes the review object with author UID and the definitive username from Firestore
     * before passing it to {@link #proceedWithReviewSubmission(Book, Review, ViewBookUI)} for saving via {@link FirestoreFacade}.
     * This is an implementation of {@link ViewBookUI.ViewBookListener}.
     *
     * @param book The {@link Book} being reviewed.
     * @param reviewFromFragment A {@link Review} object from the UI, potentially incomplete.
     * @param viewBookUI The {@link ViewBookUI} (typically {@link ViewBookFragment}) to update after submission.
     */
    @Override
    public void onReviewSubmitted(Book book, Review reviewFromFragment, ViewBookUI viewBookUI) {
        Log.d(TAG, "onReviewSubmitted called for book: " + book.getTitle());
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Cannot submit review.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "onReviewSubmitted: User not logged in.");
            return;
        }
        String uid = currentUser.getUid();
        reviewFromFragment.setAuthorUid(uid);

        firestoreFacade.fetchUsernameForUid(uid,
                username -> {
                    reviewFromFragment.setUsername(username);
                    Log.i(TAG, "Proceeding with review submission by " + username + " for book '" + book.getTitle() + "'.");
                    proceedWithReviewSubmission(book, reviewFromFragment, viewBookUI);
                },
                e -> {
                    Log.e(TAG, "Could not fetch username for review submission: " + e.getMessage(), e);
                    Toast.makeText(this, "Could not fetch username. Review not posted: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Helper method to save a fully constructed review to Firestore via {@link FirestoreFacade}.
     *
     * @param book The book the review is for.
     * @param reviewToSave The complete {@link Review} object to save.
     * @param viewBookUI The UI to notify after the review is posted.
     */
    private void proceedWithReviewSubmission(Book book, Review reviewToSave, ViewBookUI viewBookUI) {
        firestoreFacade.submitReview(book, reviewToSave,
                documentId -> {
                    reviewToSave.setReviewId(documentId);
                    Log.i(TAG, "Review posted via facade, new docID: " + documentId + " for book: " + book.getTitle());
                    if (viewBookUI != null) {
                        viewBookUI.postReview(reviewToSave);
                    }
                    Toast.makeText(this, "Review posted!", Toast.LENGTH_SHORT).show();
                },
                e -> {
                    Log.e(TAG, "Failed to save review via facade for book: " + book.getTitle(), e);
                    Toast.makeText(this, "Failed to save review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Fetches reviews for a specific book via {@link FirestoreFacade}.
     * This method is an implementation of {@link ViewBookUI.ViewBookListener}.
     *
     * @param book The {@link Book} for which to fetch reviews.
     * @param viewBookUI The {@link ViewBookUI} to update with the fetched reviews.
     */
    @Override
    public void fetchReviews(Book book, ViewBookUI viewBookUI) {
        Log.d(TAG, "fetchReviews called for book: " + book.getTitle());
        firestoreFacade.fetchReviewsForBook(book, new FirestoreFacade.OnReviewsFetchedListener() {
            @Override
            public void onFetched(List<Review> reviews) {
                Log.i(TAG, "Successfully fetched " + reviews.size() + " reviews for book: " + book.getTitle());
                if (viewBookUI != null) {
                    viewBookUI.displayReviews(reviews);
                }
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load reviews for book " + book.getTitle() + ": " + error);
                Toast.makeText(ControllerActivity.this, "Failed to load reviews: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles requests to fetch reviews for a book. This method delegates to {@link #fetchReviews(Book, ViewBookUI)}.
     * This method is an implementation of {@link ViewBookUI.ViewBookListener}.
     *
     * @param book The book for which reviews are requested.
     * @param viewBookUI The UI to update after reviews are fetched.
     */
    @Override
    public void fetchReviewsForBook(Book book, ViewBookUI viewBookUI) {
        Log.d(TAG, "fetchReviewsForBook called for book: " + book.getTitle() + ". Delegating to fetchReviews.");
        fetchReviews(book, viewBookUI);
    }


    /**
     * Handles submission of a review when a (partially-filled) {@link Review} object is provided by {@link ViewBookFragment}.
     * This method ensures the review object is completed with the author's UID and definitive username
     * before delegating to {@link ReviewManager} for posting.
     * This method is an implementation of {@link ViewBookUI.ViewBookListener}.
     *
     * @param selectedBook The {@link Book} being reviewed.
     * @param newReview A {@link Review} object, typically containing only rating and comment from the UI.
     * @param viewBookFragment The {@link ViewBookFragment} that initiated the review, used for UI callbacks.
     */
    @Override
    public void onSubmitReview(Book selectedBook, Review newReview, ViewBookFragment viewBookFragment) {
        Log.d(TAG, "onSubmitReview called by ViewBookFragment for book: " + selectedBook.getTitle());
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Cannot post review.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "onSubmitReview: User not logged in.");
            return;
        }
        String uid = currentUser.getUid();
        newReview.setAuthorUid(uid);

        firestoreFacade.fetchUsernameForUid(uid,
                username -> {
                    Log.d(TAG, "Fetched username: " + username + " for review submission via ReviewManager.");
                    newReview.setUsername(username);
                    reviewManager.postReview(selectedBook, newReview, new ReviewManager.OnReviewSavedListener() {
                        @Override
                        public void onReviewSaved() {
                            Log.i(TAG, "Review submitted via ReviewManager successfully for book: " + selectedBook.getTitle());
                            if (viewBookFragment != null && viewBookFragment.isAdded()) {
                                viewBookFragment.postReview(newReview);
                            }
                            Toast.makeText(ControllerActivity.this, "Review submitted!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onReviewSaveFailed(Exception e) {
                            Log.e(TAG, "Failed to post review via ReviewManager for book: " + selectedBook.getTitle(), e);
                            Toast.makeText(ControllerActivity.this, "Failed to post review (RM): " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                },
                e -> {
                    Log.e(TAG, "Failed to fetch user info for review submission (ReviewManager path): " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to fetch user info for review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Handles requests to edit an existing review, typically from {@link ViewBookFragment}.
     * Delegates the update operation to {@link FirestoreFacade}.
     * This method is an implementation of {@link ViewBookUI.ViewBookListener}.
     *
     * @param book The book associated with the review.
     * @param review The {@link Review} object containing the updated data and original IDs.
     * @param viewUI The {@link ViewBookUI} to refresh after the update.
     */
    @Override
    public void onEditReviewRequested(Book book, Review review, ViewBookUI viewUI) {
        Log.d(TAG, "onEditReviewRequested for review ID: " + review.getReviewId() + " for book: " + book.getTitle());
        firestoreFacade.updateReview(
                review,
                () -> {
                    Log.i(TAG, "Review ID " + review.getReviewId() + " updated successfully via facade.");
                    if (viewUI != null) {
                        fetchReviews(book, viewUI);
                    }
                    Toast.makeText(ControllerActivity.this, "Review updated!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    Log.e(TAG, "Failed to update review ID " + review.getReviewId() + " via facade: " + err);
                    Toast.makeText(ControllerActivity.this, "Update failed: " + err, Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Handles requests to delete an existing review, typically from {@link ViewBookFragment}.
     * Delegates the delete operation to {@link FirestoreFacade}.
     * This method is an implementation of {@link ViewBookUI.ViewBookListener}.
     *
     * @param book The book associated with the review.
     * @param review The {@link Review} object to be deleted.
     * @param viewUI The {@link ViewBookUI} to refresh after deletion.
     */
    @Override
    public void onDeleteReviewRequested(Book book, Review review, ViewBookUI viewUI) {
        Log.d(TAG, "onDeleteReviewRequested for review ID: " + review.getReviewId() + " for book: " + book.getTitle());
        firestoreFacade.deleteReview(
                review,
                () -> {
                    Log.i(TAG, "Review ID " + review.getReviewId() + " deleted successfully via facade.");
                    if (viewUI != null) {
                        fetchReviews(book, viewUI);
                    }
                    Toast.makeText(ControllerActivity.this, "Review deleted!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    Log.e(TAG, "Failed to delete review ID " + review.getReviewId() + " via facade: " + err);
                    Toast.makeText(ControllerActivity.this, "Delete failed: " + err, Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Fetches all reviews written by a specific user, typically for display on a user's profile.
     *
     * @param username The username of the user whose reviews are to be fetched.
     * @param ui The {@link ViewProfileUI} to update with the fetched reviews.
     */
    public void fetchUserReviews(String username, ViewProfileUI ui) {
        Log.d(TAG, "fetchUserReviews: Requesting reviews for username: " + username);
        firestoreFacade.fetchUserReviewsByUsername(username, new FirestoreFacade.OnReviewsFetchedListener() {
            @Override
            public void onFetched(List<Review> userReviews) {
                Log.i(TAG, "fetchUserReviews: Successfully fetched " + userReviews.size() + " reviews for user: " + username);
                if (ui != null) {
                    ui.displayUserReviews(userReviews);
                }
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "fetchUserReviews: Failed to load reviews for user " + username + ": " + error);
                Toast.makeText(ControllerActivity.this, "Error loading user reviews: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * Handles requests to edit a review from a user's profile screen
     *
     * @param currentUsername The username of the profile being viewed (whose review is being edited).
     * @param review The {@link Review} object with updated content.
     * @param ui The {@link ViewProfileUI} to refresh.
     */
    public void onEditUserReviewRequested(String currentUsername, Review review, ViewProfileUI ui) {
        Log.d(TAG, "onEditUserReviewRequested (from profile) for review ID: " + review.getReviewId() + " by user " + currentUsername);
        firestoreFacade.updateReview(
                review,
                () -> {
                    Log.i(TAG, "User review ID " + review.getReviewId() + " updated successfully for " + currentUsername);
                    if (ui != null) {
                        fetchUserReviews(currentUsername, ui);
                    }
                    Toast.makeText(ControllerActivity.this, "Your review updated!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    Log.e(TAG, "User review update failed for " + currentUsername + ", review ID " + review.getReviewId() + ": " + err);
                    Toast.makeText(ControllerActivity.this, "Update failed: " + err, Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Handles requests to delete a review from a user's profile screen
     *
     * @param currentUsername The username of the profile being viewed (whose review is being deleted).
     * @param review The {@link Review} object to delete.
     * @param ui The {@link ViewProfileUI} to refresh.
     */
    public void onDeleteUserReviewRequested(String currentUsername, Review review, ViewProfileUI ui) {
        Log.d(TAG, "onDeleteUserReviewRequested (from profile) for review ID: " + review.getReviewId() + " by user " + currentUsername);
        firestoreFacade.deleteReview(
                review,
                () -> {
                    Log.i(TAG, "User review ID " + review.getReviewId() + " deleted successfully for " + currentUsername);
                    if (ui != null) {
                        fetchUserReviews(currentUsername, ui);
                    }
                    Toast.makeText(ControllerActivity.this, "Your review deleted!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    Log.e(TAG, "User review delete failed for " + currentUsername + ", review ID " + review.getReviewId() + ": " + err);
                    Toast.makeText(ControllerActivity.this, "Delete failed: " + err, Toast.LENGTH_SHORT).show();
                }
        );
    }

    /**
     * Fetches the list of books saved by the current logged-in user.
     *
     * @param ui The {@link ViewSavedBooksUI} to update with the fetched books.
     */
    public void fetchSavedBooks(ViewSavedBooksUI ui) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "fetchSavedBooks: User not signed in.");
            if (ui != null) ui.showError("Not signed in");
            return;
        }
        String uid = user.getUid();
        Log.d(TAG, "fetchSavedBooks for UID: " + uid);
        firestoreFacade.fetchSavedBooks(uid, new FirestoreFacade.OnSavedBooksFetchedListener() {
            @Override
            public void onFetched(List<Book> books) {
                Log.i(TAG, "fetchSavedBooks: Successfully fetched " + books.size() + " saved books for UID: " + uid);
                if (ui != null) ui.displaySavedBooks(books);
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "fetchSavedBooks: Error fetching saved books for UID: " + uid, new Exception(error));
                if (ui != null) ui.showError(error);
            }
        });
    }

    /**
     * Saves a book to the current user's list of saved books.
     * This method is an implementation of {@link ViewBookUI.ViewBookListener}.
     *
     * @param book The {@link Book} to save.
     * @param ui The {@link ViewBookUI} to notify of the save state.
     */
    @Override
    public void saveBook(Book book, ViewBookUI ui) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "saveBook: User not signed in.");
            if (ui != null) ui.onBookSaveError("Not signed in");
            return;
        }
        String uid = user.getUid();
        Log.d(TAG, "Saving book: " + book.getTitle() + " for UID: " + uid);
        firestoreFacade.saveBook(uid, book, new FirestoreFacade.OnBookSaveOpListener() {
            @Override
            public void onSuccess(boolean isSaved) {
                Log.i(TAG, "Book saved successfully: " + book.getTitle() + " for UID: " + uid);
                if (ui != null) ui.onBookSaveState(true);
                Toast.makeText(ControllerActivity.this, "Book saved!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error saving book: " + book.getTitle() + " for UID: " + uid, new Exception(error));
                if (ui != null) ui.onBookSaveError(error);
            }
        });
    }

    /**
     * Removes a book from the current user's list of saved books.
     * This method is an implementation of {@link ViewBookUI.ViewBookListener}.
     *
     * @param book The {@link Book} to remove.
     * @param ui The {@link ViewBookUI} to notify of the save state.
     */
    @Override
    public void removeSavedBook(Book book, ViewBookUI ui) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "removeSavedBook: User not signed in.");
            if (ui != null) ui.onBookSaveError("Not signed in");
            return;
        }
        String uid = user.getUid();
        Log.d(TAG, "Removing saved book: " + book.getTitle() + " for UID: " + uid);
        firestoreFacade.removeSavedBook(uid, book, new FirestoreFacade.OnBookSaveOpListener() {
            @Override
            public void onSuccess(boolean isSaved) {
                Log.i(TAG, "Book removed from saved successfully: " + book.getTitle() + " for UID: " + uid);
                if (ui != null) ui.onBookSaveState(false);
                Toast.makeText(ControllerActivity.this, "Book removed from saved!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error removing saved book: " + book.getTitle() + " for UID: " + uid, new Exception(error));
                if (ui != null) ui.onBookSaveError(error);
            }
        });
    }

    /**
     * Checks if a book is currently saved by the logged-in user.
     * This method is an implementation of {@link ViewBookUI.ViewBookListener}.
     *
     * @param book The {@link Book} to check.
     * @param ui The {@link ViewBookUI} to notify of the save state.
     */
    @Override
    public void isBookSaved(Book book, ViewBookUI ui) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "isBookSaved: User not signed in. Reporting book as not saved.");
            if (ui != null) ui.onBookSaveState(false);
            return;
        }
        String uid = user.getUid();
        Log.d(TAG, "Checking if book is saved: '" + book.getTitle() + "' for UID: " + uid);
        firestoreFacade.isBookSaved(uid, book, isSaved -> {
            Log.d(TAG, "Book '" + book.getTitle() + (isSaved ? "' is saved." : "' is not saved.") + " for UID: " + uid);
            if (ui != null) {
                ui.onBookSaveState(isSaved);
            }
        });
    }

    /**
     * Searches for users based on a query string (username prefix).
     *
     * @param query The search query for usernames.
     * @param ui The {@link ViewSearchUsersUI} to update with results.
     */
    public void searchUsers(String query, ViewSearchUsersUI ui) {
        Log.d(TAG, "searchUsers: Query: '" + query + "'");
        firestoreFacade.searchUsers(query, new FirestoreFacade.OnUserSearchListener() {
            @Override
            public void onResults(List<User> users) {
                Log.i(TAG, "searchUsers: Found " + users.size() + " users for query: '" + query + "'");
                if (ui != null) ui.displaySearchResults(users);
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "searchUsers: Error searching users with query '" + query + "': " + error);
                if (ui != null) ui.showSearchError(error);
            }
        });
    }

    /**
     * Fetches a user's complete profile information from Firestore.
     *
     * @param userId The UID of the user whose profile is to be fetched.
     * @param listener The listener to handle the fetched {@link User} object or an error.
     */
    public void fetchUserProfile(String userId, FirestoreFacade.OnUserProfileFetchedListener listener) {
        Log.d(TAG, "fetchUserProfile for UID: " + userId);
        firestoreFacade.fetchUserProfile(userId, listener);
    }

    /**
     * Fetches the count of users that a specific user is following ("following" count).
     *
     * @param userId The UID of the user whose "following" count is needed.
     * @param listener The listener to handle the fetched count or an error.
     */
    public void fetchFollowingCount(String userId, FirestoreFacade.OnCountFetchedListener listener) {
        Log.d(TAG, "fetchFollowingCount for UID: " + userId);
        firestoreFacade.fetchFollowingCount(userId, listener);
    }

    /**
     * Fetches the count of users who are following a specific user ("followers" count).
     *
     * @param username The username of the user whose "followers" count is needed.
     * @param listener The listener to handle the fetched count or an error.
     */
    public void fetchFollowersCount(String username, FirestoreFacade.OnCountFetchedListener listener) {
        Log.d(TAG, "fetchFollowersCount for username: " + username);
        firestoreFacade.fetchFollowersCount(username, listener);
    }

    /**
     * Allows the current logged-in user to follow another user.
     *
     * @param followedUsername The username of the user to be followed.
     * @param onSuccess Callback for successful follow operation.
     * @param onError Callback for error during follow operation, accepting an error message string.
     */
    public void follow(String followedUsername, Runnable onSuccess, Consumer<String> onError) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "follow: User not logged in. Cannot follow " + followedUsername);
            onError.accept("User not logged in.");
            return;
        }
        String myId = currentUser.getUid();
        Log.d(TAG, "User " + myId + " attempting to follow " + followedUsername);
        firestoreFacade.followUser(myId, followedUsername, onSuccess, onError);
    }

    /**
     * Allows the current logged-in user to unfollow another user.
     *
     * @param followedUsername The username of the user to be unfollowed.
     * @param onSuccess Callback for successful unfollow operation.
     * @param onError Callback for error during unfollow operation, accepting an error message string.
     */
    public void unfollow(String followedUsername, Runnable onSuccess, Consumer<String> onError) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "unfollow: User not logged in. Cannot unfollow " + followedUsername);
            onError.accept("User not logged in.");
            return;
        }
        String myId = currentUser.getUid();
        Log.d(TAG, "User " + myId + " attempting to unfollow " + followedUsername);
        firestoreFacade.unfollowUser(myId, followedUsername, onSuccess, onError);
    }

    /**
     * Fetches the list of usernames that a specific user is following.
     *
     * @param userId The UID of the user whose "following" list is to be fetched.
     * @param listener The listener to handle the fetched list of usernames or an error.
     */
    public void fetchFollowingUsernames(String userId, FirestoreFacade.OnFollowedListFetchedListener listener) {
        Log.d(TAG, "fetchFollowingUsernames for UID: " + userId);
        firestoreFacade.fetchFollowingUsernames(userId, listener);
    }
}
