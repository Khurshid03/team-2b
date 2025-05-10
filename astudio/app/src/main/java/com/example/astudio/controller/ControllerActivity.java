package com.example.astudio.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.example.astudio.persistence.GoogleApiFacade;

/**
 * This class acts as the controller for the entire application. It keeps track of the application
 * state, directs UI updates, and listens for UI-generated events.
 */
public class ControllerActivity extends AppCompatActivity implements BrowseBooksUI.BrowseBooksListener,
        CreateAccountUI.CreateAccountListener, LoginUI.LoginListener, ViewBookUI.ViewBookListener {

    private static final String TAG = "ControllerActivity";
    public MainUI mainUI;
    private final ReviewManager reviewManager = new ReviewManager();
    private FirestoreFacade firestoreFacade;
    private FirebaseAuth mAuth;
    private GoogleApiFacade googleApiFacade;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize facades and auth
        this.firestoreFacade = new FirestoreFacade();
        this.googleApiFacade = new GoogleApiFacade(); // Initialize here
        this.mAuth = FirebaseAuth.getInstance();

        this.mainUI = new MainUI(this);
        setContentView(this.mainUI.getRootView());

        // Display CreateAccountFragment initially
        CreateAccountFragment createAccountFragment = new CreateAccountFragment();
        createAccountFragment.setListener(this);
        this.mainUI.displayFragment(createAccountFragment);
    }

    /**
     * Fetches books based on a search query using the GoogleApiFacade.
     * This method was already refactored by the user.
     * @param query The search query.
     * @param ui The UI to update with search results or errors.
     */
    public void fetchSearchBooks(String query, SearchBooksUI ui) {
        Log.d(TAG, "fetchSearchBooks called with query: " + query);
        // Assuming SearchBooksUI has onSearchBooksSuccess and onSearchBooksFailure
        googleApiFacade.searchBooks(
                query,
                21, // Max results for general search
                books -> {
                    if (ui != null) {
                        Log.d(TAG, "fetchSearchBooks successful, " + books.size() + " books found.");
                        ui.onSearchBooksSuccess(books);
                    }
                },
                error -> {
                    if (ui != null) {
                        Log.e(TAG, "fetchSearchBooks failed: " + error);
                        ui.onSearchBooksFailure(error);
                    }
                }
        );
    }

    @Override
    public void onBookSelected(Book book) {
        ViewBookFragment viewBookFragment = new ViewBookFragment();
        viewBookFragment.setListener(this);
        Bundle args = new Bundle();
        args.putSerializable("book", book);
        args.putString("description", book.getDescription());
        args.putString("author", book.getAuthor());
        viewBookFragment.setArguments(args);
        mainUI.displayFragment(viewBookFragment);
    }

    @Override
    public void onGenreSelected(String genre) {
        Log.d(TAG, "Genre selected: " + genre);
        // If BrowseBooksFragment is the active UI that should display genre results
        // and implements BrowseBooksUI
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView); // Assuming R.id.fragmentContainerView is your main container
        if (currentFragment instanceof BrowseBooksUI) {
            fetchBooksByGenre(genre, (BrowseBooksUI) currentFragment);
        } else {
            Log.w(TAG, "onGenreSelected: Current fragment does not implement BrowseBooksUI or is not BrowseBooksFragment.");
            // Potentially navigate to BrowseBooksFragment first if not already there
        }
    }

    @Override
    public void onCreateAccount(String username, String email, String password, CreateAccountUI ui) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            firestoreFacade.saveNewUser(uid, username, email,
                                    () -> {
                                        Log.d(TAG, "User data saved to Firestore for UID: " + uid);
                                        BrowseBooksFragment fragment = new BrowseBooksFragment();
                                        fragment.setListener(this);
                                        mainUI.displayFragment(fragment);
                                    },
                                    e -> {
                                        Log.e(TAG, "Failed to save user data to Firestore", e);
                                        Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                            );
                        } else {
                            Log.e(TAG, "User created but mAuth.getCurrentUser() is null");
                            Toast.makeText(this, "Account creation partially failed. Please try logging in.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Account creation failed", task.getException());
                        Toast.makeText(this, "Account creation failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onProceedToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setListener(this);
        mainUI.displayFragment(loginFragment);
    }

    @Override
    public void onLogin(String username) {
        Log.d(TAG, "onLogin with username only called (currently not implemented for navigation): " + username);
    }

    @Override
    public void onLogin(String email, String password, LoginUI ui) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Login successful for email: " + email);
                        BrowseBooksFragment fragment = new BrowseBooksFragment();
                        fragment.setListener(this);
                        mainUI.displayFragment(fragment);
                    } else {
                        Log.e(TAG, "Login failed for email: " + email, task.getException());
                        Toast.makeText(this, "Login failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Called by BrowseBooksFragment to get “Welcome, <username>!” */
    public void fetchWelcomeMessage(BrowseBooksUI ui) {
        FirebaseUser me = mAuth.getCurrentUser();
        if (me == null) {
            ui.displayWelcomeMessage(getString(R.string.welcome_message, "Guest"));
            return;
        }
        String uid = me.getUid();
        firestoreFacade.fetchUserProfile(uid, new FirestoreFacade.OnUserProfileFetchedListener() {
            @Override public void onFetched(User user) {
                String name = (user != null && user.getUsername() != null)
                        ? user.getUsername()
                        : "there"; // Fallback name
                String welcome = getString(R.string.welcome_message, name);
                if (ui != null) { // Check if UI is still valid
                    ui.displayWelcomeMessage(welcome);
                }
            }
            @Override public void onError(String err) {
                Log.w(TAG, "fetchWelcomeMessage: onError: " + err);
                String welcome = getString(R.string.welcome_message, "there"); // Fallback name
                if (ui != null) { // Check if UI is still valid
                    ui.displayWelcomeMessage(welcome);
                }
            }
        });
    }

    /**
     * Fetches top-rated books using the GoogleApiFacade.
     * @param ui The UI (BrowseBooksUI) to update with the fetched books or show errors.
     */
    public void fetchTopRatedBooks(BrowseBooksUI ui) {
        if (ui == null || ui.getRootView() == null) {
            Log.e(TAG, "fetchTopRatedBooks: UI or its root view is null. Cannot proceed.");
            return;
        }
        FragmentBrowseBooksBinding binding = FragmentBrowseBooksBinding.bind(ui.getRootView());
        binding.loadingSpinner.setVisibility(View.VISIBLE);
        Log.d(TAG, "fetchTopRatedBooks called.");

        googleApiFacade.fetchTopRatedBooks(
                10, // Max results for top-rated
                books -> { // onSuccess
                    if (ui.getRootView() != null) { // Check if UI is still valid
                        binding.loadingSpinner.setVisibility(View.GONE);
                        Log.d(TAG, "fetchTopRatedBooks successful, " + books.size() + " books found.");
                        ui.updateHotBooks(books);
                    } else {
                        Log.w(TAG, "fetchTopRatedBooks onSuccess: UI root view became null.");
                    }
                },
                error -> { // onFailure
                    if (ui.getRootView() != null) { // Check if UI is still valid
                        binding.loadingSpinner.setVisibility(View.GONE);
                        Log.e(TAG, "fetchTopRatedBooks failed: " + error);
                        Toast.makeText(ControllerActivity.this, "Failed to load hot books: " + error, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "fetchTopRatedBooks onFailure: UI root view became null.");
                    }
                }
        );
    }

    /**
     * Fetches books by genre using the GoogleApiFacade.
     * @param genre The genre to search for.
     * @param ui The UI (BrowseBooksUI) to update with the fetched books or show errors.
     */
    public void fetchBooksByGenre(String genre, BrowseBooksUI ui) {
        if (ui == null || ui.getRootView() == null) {
            Log.e(TAG, "fetchBooksByGenre: UI or its root view is null. Cannot proceed for genre: " + genre);
            return;
        }
        FragmentBrowseBooksBinding binding = FragmentBrowseBooksBinding.bind(ui.getRootView());
        binding.loadingSpinner.setVisibility(View.VISIBLE);
        Log.d(TAG, "fetchBooksByGenre called for genre: " + genre);

        googleApiFacade.fetchBooksByGenre(
                genre,
                12, // Max results for genre search
                books -> { // onSuccess
                    if (ui.getRootView() != null) {
                        binding.loadingSpinner.setVisibility(View.GONE);
                        Log.d(TAG, "fetchBooksByGenre successful for genre '" + genre + "', " + books.size() + " books found.");
                        ui.updateGenreBooks(books);
                    } else {
                        Log.w(TAG, "fetchBooksByGenre onSuccess: UI root view became null for genre: " + genre);
                    }
                },
                error -> { // onFailure
                    if (ui.getRootView() != null) {
                        binding.loadingSpinner.setVisibility(View.GONE);
                        Log.e(TAG, "fetchBooksByGenre failed for genre '" + genre + "': " + error);
                        Toast.makeText(ControllerActivity.this, "Failed to load " + genre + " books: " + error, Toast.LENGTH_SHORT).show();
                    } else {
                        Log.w(TAG, "fetchBooksByGenre onFailure: UI root view became null for genre: " + genre);
                    }
                }
        );
    }

    @Override
    public void onReviewSubmitted(Book book, Review review, ViewBookUI viewBookUI) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Cannot submit review.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "onReviewSubmitted: User not logged in.");
            return;
        }
        String uid = currentUser.getUid();
        review.setAuthorUid(uid); // Ensure author UID is set

        firestoreFacade.fetchUsernameForUid(uid,
                username -> {
                    review.setUsername(username); // Set the fetched username
                    proceedWithReviewSubmission(book, review, viewBookUI);
                },
                e -> {
                    Log.e(TAG, "Could not fetch username for review submission", e);
                    Toast.makeText(this, "Could not fetch username. Review not posted: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    private void proceedWithReviewSubmission(Book book, Review review, ViewBookUI viewBookUI) {
        firestoreFacade.submitReview(book, review,
                documentId -> {
                    Log.d(TAG, "Review posted via facade, docID: " + documentId);
                    review.setReviewId(documentId);
                    if (viewBookUI != null) {
                        viewBookUI.postReview(review);
                    }
                    Toast.makeText(ControllerActivity.this, "Review posted!", Toast.LENGTH_SHORT).show();
                },
                e -> {
                    Log.e(TAG, "Failed to save review via facade", e);
                    Toast.makeText(ControllerActivity.this, "Failed to save review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    @Override
    public void fetchReviews(Book book, ViewBookUI viewBookUI) {
        Log.d(TAG, "fetchReviews called for book: " + book.getTitle());
        firestoreFacade.fetchReviewsForBook(book, new FirestoreFacade.OnReviewsFetchedListener() {
            @Override
            public void onFetched(List<Review> reviews) {
                Log.d(TAG, "Fetched " + reviews.size() + " reviews for " + book.getTitle() + " (via fetchReviews)");
                if (viewBookUI != null) {
                    viewBookUI.displayReviews(reviews);
                }
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load reviews for " + book.getTitle() + ": " + error + " (via fetchReviews)");
                Toast.makeText(ControllerActivity.this, "Failed to load reviews: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void fetchReviewsForBook(Book book, ViewBookUI viewBookUI) {
        Log.d(TAG, "fetchReviewsForBook called for book: " + book.getTitle() + ". Delegating to fetchReviews.");
        fetchReviews(book, viewBookUI);
    }


    @Override
    public void onSubmitReview(Book selectedBook, Review newReview, ViewBookFragment viewBookFragment) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "onSubmitReview: User not logged in.");
            return;
        }
        String uid = currentUser.getUid();
        newReview.setAuthorUid(uid); // Ensure author UID is set on the newReview object

        firestoreFacade.fetchUsernameForUid(uid,
                username -> {
                    Log.d(TAG, "Fetched username: " + username + " for review submission via ReviewManager.");
                    newReview.setUsername(username); // Set username on the newReview object
                    // The ReviewManager might handle its own Firestore interaction or could be refactored further
                    reviewManager.postReview(selectedBook, newReview, new ReviewManager.OnReviewSavedListener() {
                        @Override
                        public void onReviewSaved() {
                            Log.d(TAG, "Review submitted via ReviewManager successfully.");
                            if (viewBookFragment != null && viewBookFragment.isAdded()) {
                                viewBookFragment.postReview(newReview); // Pass the potentially updated newReview
                            }
                            Toast.makeText(ControllerActivity.this, "Review submitted!", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onReviewSaveFailed(Exception e) {
                            Log.e(TAG, "Failed to post review via ReviewManager", e);
                            Toast.makeText(ControllerActivity.this, "Failed to post review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                },
                e -> {
                    Log.e(TAG, "Failed to fetch user info for review submission via ReviewManager", e);
                    Toast.makeText(this, "Failed to fetch user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
        );
    }

    @Override
    public void onEditReviewRequested(Book book, Review review, ViewBookUI viewUI) {
        // Using FirestoreFacade for updating reviews consistently
        firestoreFacade.updateReview(
                review,
                () -> {
                    Log.d(TAG, "Review updated via facade for book: " + book.getTitle());
                    if (viewUI != null) {
                        fetchReviews(book, viewUI); // Refresh reviews list
                    }
                    Toast.makeText(ControllerActivity.this, "Review updated!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    Log.e(TAG, "Failed to update review via facade", new Exception(err));
                    Toast.makeText(ControllerActivity.this, "Update failed: " + err, Toast.LENGTH_SHORT).show();
                }
        );
    }

    @Override
    public void onDeleteReviewRequested(Book book, Review review, ViewBookUI viewUI) {
        Log.d(TAG, "Delete review requested for review ID: " + review.getReviewId());
        // Using FirestoreFacade for deleting reviews consistently
        firestoreFacade.deleteReview(
                review,
                () -> {
                    Log.d(TAG, "Review deleted successfully via facade for book: " + book.getTitle());
                    if (viewUI != null) {
                        fetchReviews(book, viewUI); // Refresh reviews list
                    }
                    Toast.makeText(ControllerActivity.this, "Review deleted!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    Log.e(TAG, "Review delete failed via facade for book: " + book.getTitle(), new Exception(err));
                    Toast.makeText(ControllerActivity.this, "Delete failed: " + err, Toast.LENGTH_SHORT).show();
                }
        );
    }

    public void fetchUserReviews(String username, ViewProfileUI ui) {
        Log.d(TAG, "Fetching user reviews for username: " + username);
        firestoreFacade.fetchUserReviewsByUsername(username, new FirestoreFacade.OnReviewsFetchedListener() {
            @Override
            public void onFetched(List<Review> userReviews) {
                Log.d(TAG, "Fetched " + userReviews.size() + " reviews for user " + username);
                if (ui != null) {
                    ui.displayUserReviews(userReviews);
                }
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to load user reviews for " + username + ": " + error);
                if (ui != null) {
                    // ui.showError("Error loading user reviews: " + error);
                }
                Toast.makeText(ControllerActivity.this, "Error loading user reviews: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }


    public void onEditUserReviewRequested(String currentUsername, Review review, ViewProfileUI ui) {
        Log.d(TAG, "Edit user review requested for review ID: " + review.getReviewId() + " by user " + currentUsername);
        firestoreFacade.updateReview(
                review,
                () -> {
                    Log.d(TAG, "User review updated successfully via facade for " + currentUsername);
                    if (ui != null) {
                        fetchUserReviews(currentUsername, ui); // Use currentUsername (profile user) to refresh
                    }
                    Toast.makeText(ControllerActivity.this, "Your review updated!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    Log.e(TAG, "User review update failed via facade for " + currentUsername, new Exception(err));
                    Toast.makeText(ControllerActivity.this, "Update failed: " + err, Toast.LENGTH_SHORT).show();
                }
        );
    }

    public void onDeleteUserReviewRequested(String currentUsername, Review review, ViewProfileUI ui) {
        Log.d(TAG, "Delete user review requested for review ID: " + review.getReviewId() + " by user " + currentUsername);
        firestoreFacade.deleteReview(
                review,
                () -> {
                    Log.d(TAG, "User review deleted successfully by " + currentUsername + " via facade.");
                    if (ui != null) {
                        fetchUserReviews(currentUsername, ui); // Use currentUsername (profile user) to refresh
                    }
                    Toast.makeText(ControllerActivity.this, "Your review deleted!", Toast.LENGTH_SHORT).show();
                },
                err -> {
                    Log.e(TAG, "User review delete failed for " + currentUsername + " via facade.", new Exception(err));
                    Toast.makeText(ControllerActivity.this, "Delete failed: " + err, Toast.LENGTH_SHORT).show();
                }
        );
    }

    public void fetchSavedBooks(ViewSavedBooksUI ui) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "fetchSavedBooks: User not signed in.");
            if (ui != null) ui.showError("Not signed in");
            return;
        }
        String uid = user.getUid();
        Log.d(TAG, "Fetching saved books for UID: " + uid);
        firestoreFacade.fetchSavedBooks(uid, new FirestoreFacade.OnSavedBooksFetchedListener() {
            @Override
            public void onFetched(List<Book> books) {
                Log.d(TAG, "Fetched " + books.size() + " saved books for UID: " + uid);
                if (ui != null) ui.displaySavedBooks(books);
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error fetching saved books for UID: " + uid, new Exception(error));
                if (ui != null) ui.showError(error);
            }
        });
    }

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
                Log.d(TAG, "Book saved successfully: " + book.getTitle() + " for UID: " + uid);
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
                Log.d(TAG, "Book removed from saved successfully: " + book.getTitle() + " for UID: " + uid);
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

    @Override
    public void isBookSaved(Book book, ViewBookUI ui) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Log.w(TAG, "isBookSaved: User not signed in. Reporting book as not saved.");
            if (ui != null) ui.onBookSaveState(false);
            return;
        }
        String uid = user.getUid();
        Log.d(TAG, "Checking if book is saved: " + book.getTitle() + " for UID: " + uid);
        firestoreFacade.isBookSaved(uid, book, isSaved -> {
            Log.d(TAG, "Book " + book.getTitle() + (isSaved ? " is saved." : " is not saved.") + " for UID: " + uid);
            if (ui != null) {
                ui.onBookSaveState(isSaved);
            }
        });
    }

    public void searchUsers(String query, ViewSearchUsersUI ui) {
        Log.d(TAG, "Searching users with query: " + query);
        firestoreFacade.searchUsers(query, new FirestoreFacade.OnUserSearchListener() {
            @Override
            public void onResults(List<User> users) {
                Log.d(TAG, "Found " + users.size() + " users for query: " + query);
                if (ui != null) ui.displaySearchResults(users);
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Error searching users with query: " + query, new Exception(error));
                if (ui != null) ui.showSearchError(error);
            }
        });
    }

    public void fetchUserProfile(String userId, FirestoreFacade.OnUserProfileFetchedListener listener) {
        Log.d(TAG, "Fetching user profile for UID: " + userId);
        firestoreFacade.fetchUserProfile(userId, listener);
    }

    public void fetchFollowingCount(String userId, FirestoreFacade.OnCountFetchedListener listener) {
        Log.d(TAG, "Fetching following count for UID: " + userId);
        firestoreFacade.fetchFollowingCount(userId, listener);
    }

    public void fetchFollowersCount(String username, FirestoreFacade.OnCountFetchedListener listener) {
        Log.d(TAG, "Fetching followers count for username: " + username);
        firestoreFacade.fetchFollowersCount(username, listener);
    }

    public void follow(String followedUsername, Runnable onSuccess, Consumer<String> onError) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Follow action: User not logged in.");
            onError.accept("User not logged in.");
            return;
        }
        String myId = currentUser.getUid();
        Log.d(TAG, "User UID: " + myId + " attempting to follow username: " + followedUsername);
        firestoreFacade.followUser(myId, followedUsername, onSuccess, onError);
    }

    public void unfollow(String followedUsername, Runnable onSuccess, Consumer<String> onError) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.w(TAG, "Unfollow action: User not logged in.");
            onError.accept("User not logged in.");
            return;
        }
        String myId = currentUser.getUid();
        Log.d(TAG, "User UID: " + myId + " attempting to unfollow username: " + followedUsername);
        firestoreFacade.unfollowUser(myId, followedUsername, onSuccess, onError);
    }

    public void fetchFollowingUsernames(String userId, FirestoreFacade.OnFollowedListFetchedListener listener) {
        Log.d(TAG, "Fetching list of followed usernames for UID: " + userId);
        firestoreFacade.fetchFollowingUsernames(userId, listener);
    }
}
