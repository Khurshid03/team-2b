package com.example.astudio.controller;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.astudio.R;
import com.example.astudio.databinding.FragmentBrowseBooksBinding;
import com.example.astudio.model.Book;
import com.example.astudio.model.BookResponse;
import com.example.astudio.model.Review;
import com.example.astudio.model.ReviewManager;
import com.example.astudio.model.User;
import com.example.astudio.network.GoogleBooksApi;
import com.example.astudio.network.RetrofitClient;
import com.example.astudio.persistence.FirestoreFacade; // Import the facade
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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer; // Import Consumer for isBookSaved

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.example.astudio.persistence.GoogleApiFacade;

/**
 * This class acts as the controller for the entire application. It keeps track of the application
 * state, directs UI updates, and listens for UI-generated events.
 */
public class ControllerActivity extends AppCompatActivity implements BrowseBooksUI.BrowseBooksListener,
        CreateAccountUI.CreateAccountListener, LoginUI.LoginListener, ViewBookUI.ViewBookListener {

    private static final String TAG = "ControllerActivity";
    public MainUI mainUI;
    private final ReviewManager reviewManager = new ReviewManager(); // Handles review-specific logic (CRUD via its own Firestore calls)
    private FirestoreFacade firestoreFacade; // Facade for other Firestore operations
    private FirebaseAuth mAuth;
    private GoogleApiFacade apiFacade = new GoogleApiFacade();


    // Google Books API Key - consider moving to a secure configuration file
    private static final String API_KEY = "API_KEY"; // TODO: Replace with your actual API key


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.firestoreFacade = new FirestoreFacade();
        this.apiFacade = new GoogleApiFacade();
        this.mAuth = FirebaseAuth.getInstance();

        this.mainUI = new MainUI(this);
        setContentView(this.mainUI.getRootView());

        this.firestoreFacade = new FirestoreFacade(); // Initialize the facade
        this.mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        // Display CreateAccountFragment initially
        CreateAccountFragment createAccountFragment = new CreateAccountFragment();
        createAccountFragment.setListener(this);
        this.mainUI.displayFragment(createAccountFragment);
    }

    public void fetchSearchBooks(String query, SearchBooksUI ui) {
        apiFacade.searchBooks(
                query,
                21,
                ui::onSearchBooksSuccess,
                ui::onSearchBooksFailure
        );
    }

    @Override
    public void onBookSelected(Book book) {
        ViewBookFragment viewBookFragment = new ViewBookFragment();
        viewBookFragment.setListener(this); // 'this' implements ViewBookUI.ViewBookListener
        Bundle args = new Bundle();
        args.putSerializable("book", book); // Book class must implement Serializable
        // Pass other necessary details if they are not part of the Book object or for convenience
        args.putString("description", book.getDescription());
        args.putString("author", book.getAuthor());
        viewBookFragment.setArguments(args);
        mainUI.displayFragment(viewBookFragment);
    }

    @Override
    public void onGenreSelected(String genre) {
        // This method is called when a genre is selected in the BrowseBooksFragment.
        Log.d(TAG, "Genre selected: " + genre);
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
        // reuse our facade’s fetchUserProfile:
        firestoreFacade.fetchUserProfile(uid, new FirestoreFacade.OnUserProfileFetchedListener() {
            @Override public void onFetched(User user) {
                String name = (user != null && user.getUsername() != null)
                        ? user.getUsername()
                        : "there";
                String welcome = getString(R.string.welcome_message, name);
                ui.displayWelcomeMessage(welcome);
            }
            @Override public void onError(String err) {
                // fallback
                String welcome = getString(R.string.welcome_message, "there");
                ui.displayWelcomeMessage(welcome);
            }
        });
    }

    public void fetchTopRatedBooks(BrowseBooksUI ui) {
        View rootView = ui.getRootView();
        if (rootView == null) {
            Log.e(TAG, "fetchTopRatedBooks: UI root view is null.");
            return;
        }
        FragmentBrowseBooksBinding binding = FragmentBrowseBooksBinding.bind(rootView);
        binding.loadingSpinner.setVisibility(View.VISIBLE);

        GoogleBooksApi api = RetrofitClient.getInstance();
        api.searchBooks("top rated fiction", API_KEY, 10).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookResponse> call, @NonNull Response<BookResponse> response) {
                if (ui.getRootView() == null) return;
                binding.loadingSpinner.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().items != null) {
                    List<Book> books = new ArrayList<>();
                    for (BookResponse.Item item : response.body().items) {
                        if (item.volumeInfo == null) continue;
                        String title = item.volumeInfo.title != null ? item.volumeInfo.title : "No Title";
                        String thumb = (item.volumeInfo.imageLinks != null && item.volumeInfo.imageLinks.thumbnail != null)
                                ? item.volumeInfo.imageLinks.thumbnail.replace("http://", "https://") : "";
                        float rating = (item.volumeInfo.averageRating != null) ? item.volumeInfo.averageRating : 0f;
                        String author = (item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty())
                                ? String.join(", ", item.volumeInfo.authors) : "Unknown Author";
                        String description = (item.volumeInfo.description != null) ? item.volumeInfo.description : "No description available.";
                        books.add(new Book(title, thumb, rating, author, description));
                    }
                    ui.updateHotBooks(books);
                } else {
                    Log.e(TAG, "Failed to load hot books. Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(ControllerActivity.this, "Failed to load hot books: " + (response.message() != null && !response.message().isEmpty() ? response.message() : "No items found or error in response"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookResponse> call, @NonNull Throwable t) {
                if (ui.getRootView() == null) return;
                binding.loadingSpinner.setVisibility(View.GONE);
                Log.e(TAG, "API call failed for top rated books", t);
                Toast.makeText(ControllerActivity.this, "Failed to load hot books: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void fetchBooksByGenre(String genre, BrowseBooksUI ui) {
        View rootView = ui.getRootView();
        if (rootView == null) {
            Log.e(TAG, "fetchBooksByGenre: UI root view is null.");
            return;
        }
        FragmentBrowseBooksBinding binding = FragmentBrowseBooksBinding.bind(rootView);
        binding.loadingSpinner.setVisibility(View.VISIBLE);

        GoogleBooksApi api = RetrofitClient.getInstance();
        api.searchBooks("subject:" + genre, API_KEY, 12).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookResponse> call, @NonNull Response<BookResponse> response) {
                if (ui.getRootView() == null) return;
                binding.loadingSpinner.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().items != null) {
                    List<Book> books = new ArrayList<>();
                    for (BookResponse.Item item : response.body().items) {
                        if (item.volumeInfo == null) continue;
                        String title = item.volumeInfo.title != null ? item.volumeInfo.title : "No Title";
                        String thumb = (item.volumeInfo.imageLinks != null && item.volumeInfo.imageLinks.thumbnail != null)
                                ? item.volumeInfo.imageLinks.thumbnail.replace("http://", "https://") : "";
                        float rating = (item.volumeInfo.averageRating != null) ? item.volumeInfo.averageRating : 0f;
                        String author = (item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty())
                                ? String.join(", ", item.volumeInfo.authors) : "Unknown Author";
                        String description = (item.volumeInfo.description != null) ? item.volumeInfo.description : "No description available.";
                        books.add(new Book(title, thumb, rating, author, description));
                    }
                    ui.updateGenreBooks(books);
                } else {
                    Log.e(TAG, "Failed to load genre books. Code: " + response.code() + ", Message: " + response.message());
                    Toast.makeText(ControllerActivity.this, "Failed to load genre books: " + (response.message() != null && !response.message().isEmpty() ? response.message() : "No items found or error in response"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookResponse> call, @NonNull Throwable t) {
                if (ui.getRootView() == null) return;
                binding.loadingSpinner.setVisibility(View.GONE);
                Log.e(TAG, "API call failed for genre books: " + genre, t);
                Toast.makeText(ControllerActivity.this, "Failed to load " + genre + " books: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReviewSubmitted(Book book, Review review, ViewBookUI viewBookUI) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in. Cannot submit review.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "onReviewSubmitted: User not logged in.");
            return;
        }

        // 1) Always set the authorUid
        String uid = currentUser.getUid();
        review.setAuthorUid(uid);

        // 2) Always fetch the real username from Firestore
        firestoreFacade.fetchUsernameForUid(uid,
                username -> {
                    review.setUsername(username);
                    proceedWithReviewSubmission(book, review, viewBookUI);
                },
                e -> {
                    Log.e(TAG, "Could not fetch username for review submission", e);
                    Toast.makeText(this, "Could not fetch username. Review not posted.", Toast.LENGTH_SHORT).show();
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
        firestoreFacade.fetchUsernameForUid(uid,
                username -> {
                    Log.d(TAG, "Fetched username: " + username + " for review submission via ReviewManager.");
                    Review reviewToSave = new Review(
                            username, newReview.getRating(), newReview.getComment(),
                            null, selectedBook.getTitle(), selectedBook.getThumbnailUrl(), uid);
                    reviewManager.postReview(selectedBook, reviewToSave, new ReviewManager.OnReviewSavedListener() {
                        @Override
                        public void onReviewSaved() {
                            Log.d(TAG, "Review submitted via ReviewManager successfully.");
                            if (viewBookFragment != null && viewBookFragment.isAdded()) {
                                viewBookFragment.postReview(reviewToSave);
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
        firestoreFacade.updateReview(
                review,
                () -> {
                    Log.d(TAG, "Review updated via facade for book: " + book.getTitle());
                    if (viewUI != null) {
                        // pull down the very same collection the facade writes to
                        fetchReviews(book, viewUI);
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
        reviewManager.deleteReview(review, new ReviewManager.OnReviewDeletedListener() {
            @Override
            public void onReviewDeleted() {
                Log.d(TAG, "Review deleted successfully via ReviewManager.");
                if (viewUI != null) {
                    fetchReviews(book, viewUI);
                }
                Toast.makeText(ControllerActivity.this, "Review deleted!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onReviewDeleteFailed(Exception e) {
                Log.e(TAG, "Review delete failed via ReviewManager", e);
                Toast.makeText(ControllerActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                    // ui.showError("Error loading user reviews: " + error); // If ViewProfileUI has showError
                }
                Toast.makeText(ControllerActivity.this, "Error loading user reviews: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }


    public void onEditUserReviewRequested(String currentUsername, Review review, ViewProfileUI ui) {
        Log.d(TAG, "Edit user review requested for review ID: " + review.getReviewId() + " by user " + currentUsername);

        // 1) Update via the same facade that you use for reads:
        firestoreFacade.updateReview(
                review,
                () -> {
                    Log.d(TAG, "User review updated successfully via facade for " + currentUsername);

                    // 2a) Refresh the profile screen
                    if (ui != null) {
                        fetchUserReviews(currentUsername, ui);
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
        reviewManager.deleteReview(review, new ReviewManager.OnReviewDeletedListener() {
            @Override
            public void onReviewDeleted() {
                Log.d(TAG, "User review deleted successfully by " + currentUsername);
                if (ui != null) {
                    fetchUserReviews(review.getUsername(), ui);
                }
                Toast.makeText(ControllerActivity.this, "Your review deleted!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onReviewDeleteFailed(Exception e) {
                Log.e(TAG, "User review delete failed for " + currentUsername, e);
                Toast.makeText(ControllerActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        // Corrected to use OnBookSaveOpListener
        firestoreFacade.saveBook(uid, book, new FirestoreFacade.OnBookSaveOpListener() {
            @Override
            public void onSuccess(boolean isSaved) {
                Log.d(TAG, "Book saved successfully: " + book.getTitle() + " for UID: " + uid);
                if (ui != null) ui.onBookSaveState(true); // isSaved will be true
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
        // Corrected to use OnBookSaveOpListener
        firestoreFacade.removeSavedBook(uid, book, new FirestoreFacade.OnBookSaveOpListener() {
            @Override
            public void onSuccess(boolean isSaved) {
                Log.d(TAG, "Book removed from saved successfully: " + book.getTitle() + " for UID: " + uid);
                if (ui != null) ui.onBookSaveState(false); // isSaved will be false
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
        // Using Consumer<Boolean> as expected by FirestoreFacade.isBookSaved
        firestoreFacade.isBookSaved(uid, book, isSaved -> {
            Log.d(TAG, "Book " + book.getTitle() + (isSaved ? " is saved." : " is not saved.") + " for UID: " + uid);
            if (ui != null) {
                ui.onBookSaveState(isSaved);
                // Note: The facade's isBookSaved(Consumer<Boolean>) doesn't directly provide an error message string here.
                // Errors are logged in the facade and result in 'isSaved' being false.
                // If a distinct error message is needed in the UI for this check,
                // FirestoreFacade.isBookSaved would need a listener with an onError callback.
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
