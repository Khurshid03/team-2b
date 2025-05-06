package com.example.astudio.controller;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.astudio.databinding.FragmentBrowseBooksBinding;
import com.example.astudio.model.Book;
import com.example.astudio.model.BookResponse;
import com.example.astudio.model.Review;
import com.example.astudio.model.ReviewManager; // Assuming ReviewManager exists and handles Firestore interaction
import com.example.astudio.model.User;
import com.example.astudio.network.GoogleBooksApi;
import com.example.astudio.network.RetrofitClient;
import com.example.astudio.view.BrowseBooksFragment;
import com.example.astudio.view.CreateAccountFragment;
import com.example.astudio.view.CreateAccountUI;
import com.example.astudio.view.LoginFragment;
import com.example.astudio.view.LoginUI;
import com.example.astudio.view.MainUI;
import com.example.astudio.view.ViewBookFragment;
import com.example.astudio.view.BrowseBooksUI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.astudio.view.ViewBookUI;
import com.example.astudio.view.ViewProfileUI;
import com.example.astudio.view.ViewSavedBooksUI;
import com.example.astudio.view.ViewSearchUsersUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot; // Import QueryDocumentSnapshot

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This class acts as the controller for the entire application. It keeps track of the application
 * state, directs UI updates, and listens for UI-generated events.
 */

public class ControllerActivity extends AppCompatActivity implements BrowseBooksUI.BrowseBooksListener,
        CreateAccountUI.CreateAccountListener, LoginUI.LoginListener, ViewBookUI.ViewBookListener{

    public MainUI mainUI;
    private final ReviewManager reviewManager = new ReviewManager(); // Assuming ReviewManager handles the actual Firestore review operations
    /**
     * This method is called when the activity is created. It initializes the UI and sets up the
     * default fragment (LoginFragment).
     *
     * @param savedInstanceState The saved instance state.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mainUI = new MainUI(this);
        setContentView(this.mainUI.getRootView());

        // Display CreateAccountFragment initially
        CreateAccountFragment createAccountFragment = new CreateAccountFragment();
        createAccountFragment.setListener(this);
        this.mainUI.displayFragment(createAccountFragment);
    }

    /**
     * This method is called by the BrowseBooksFragment when the user selects a book.
     * It navigates to the ViewBookFragment and passes the selected book details.
     *
     * @param book The selected book.
     */
    @Override
    public void onBookSelected(Book book) {
        // Create the ViewBookFragment and pass the selected Book as a Serializable
        ViewBookFragment viewBookFragment = new ViewBookFragment();
        viewBookFragment.setListener((ViewBookUI.ViewBookListener) this);
        Bundle args = new Bundle();
        args.putSerializable("book", (Serializable) book);
        // Optionally, pass additional book details if needed
        args.putString("description", book.getDescription());
        args.putString("author", book.getAuthor());
        viewBookFragment.setArguments(args);
        mainUI.displayFragment(viewBookFragment);
    }

    /**
     * This method is called by the BrowseBooksFragment when the user selects a genre.
     * It can be used to filter books based on the selected genre.
     *
     * @param genre The selected genre.
     */

    @Override
    public void onGenreSelected(String genre) {
        // Handle genre selections if needed
    }


    /**
     * Creates a new account for the user.
     * This method is called when the user clicks the "Create Account" button in the CreateAccountFragment.
     *
     */

    @Override
    public void onCreateAccount(String username, String email, String password, CreateAccountUI ui) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();  // Firestore reference

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        // Create a new user object
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", username);
                        user.put("email", email);
                        user.put("bio", "");  // Start with empty bio

                        // Save the user object to Firestore
                        db.collection("Users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    // Successfully saved user data
                                    BrowseBooksFragment fragment = new BrowseBooksFragment();
                                    fragment.setListener(this);
                                    mainUI.displayFragment(fragment);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "Account creation failed: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onProceedToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        loginFragment.setListener((LoginUI.LoginListener) this);
        mainUI.displayFragment(loginFragment);
    }

    @Override
    public void onLogin(String username) {

    }

    @Override
    public void onLogin(String email, String password, LoginUI ui) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Successfully logged in
                        BrowseBooksFragment fragment = new BrowseBooksFragment();
                        fragment.setListener(this);
                        mainUI.displayFragment(fragment);
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private static final String API_KEY = "PUT_KEY_HERE";

    /**
     * Fetches the top-rated books from the Google Books API and updates the hot books RecyclerView.
     * This method makes an asynchronous API call to fetch the books.
     *
     * @param ui The UI interface to update the hot books.
     */
    public void fetchTopRatedBooks(BrowseBooksUI ui) {
        FragmentBrowseBooksBinding binding = FragmentBrowseBooksBinding.bind(ui.getRootView());
        binding.loadingSpinner.setVisibility(View.VISIBLE);


        GoogleBooksApi api = RetrofitClient.getInstance();
        api.searchBooks("top rated fiction", API_KEY, 10).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookResponse> call, @NonNull Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = new ArrayList<>();
                    for (BookResponse.Item item : response.body().items) {
                        String title = item.volumeInfo.title;
                        String thumb = item.volumeInfo.imageLinks != null
                                ? item.volumeInfo.imageLinks.thumbnail : "";
                        if (!thumb.isEmpty()) {
                            thumb = thumb.replace("http://", "https://");
                        }
                        float rating = item.volumeInfo.averageRating != null ? item.volumeInfo.averageRating : 0f;
                        String author = (item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty())
                                ? item.volumeInfo.authors.get(0) : "Unknown Author";
                        String description = item.volumeInfo.description != null ? item.volumeInfo.description : "No description available";
                        books.add(new Book(title, thumb, rating, author, description));
                    }
                    ui.updateHotBooks(books); // <- Call view method to update
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookResponse> call, @NonNull Throwable t) {
                Toast.makeText(ControllerActivity.this, "Failed to load hot books", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Fetches books of a specific genre from the Google Books API and updates the genre books RecyclerView.
     * This method makes an asynchronous API call to fetch the books.
     *
     * @param genre The genre of books to be fetched.
     */

    public void fetchBooksByGenre(String genre, BrowseBooksUI ui) {
        FragmentBrowseBooksBinding binding = FragmentBrowseBooksBinding.bind(ui.getRootView());
        binding.loadingSpinner.setVisibility(View.VISIBLE);


        GoogleBooksApi api = RetrofitClient.getInstance();
        api.searchBooks(genre, API_KEY, 12).enqueue(new Callback<BookResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookResponse> call, @NonNull Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Book> books = new ArrayList<>();
                    for (BookResponse.Item item : response.body().items) {
                        String title = item.volumeInfo.title;
                        String thumb = item.volumeInfo.imageLinks != null
                                ? item.volumeInfo.imageLinks.thumbnail : "";
                        if (!thumb.isEmpty()) {
                            thumb = thumb.replace("http://", "https://");
                        }
                        float rating = item.volumeInfo.averageRating != null ? item.volumeInfo.averageRating : 0f;
                        String author = (item.volumeInfo.authors != null && !item.volumeInfo.authors.isEmpty())
                                ? item.volumeInfo.authors.get(0) : "Unknown Author";
                        String description = item.volumeInfo.description != null ? item.volumeInfo.description : "No description available";
                        books.add(new Book(title, thumb, rating, author, description));
                    }
                    ui.updateGenreBooks(books); // <- Call view method to update
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookResponse> call, @NonNull Throwable t) {
                Toast.makeText(ControllerActivity.this, "Failed to load genre books", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * This method is called when the user clicks the "Post Review" button in the ViewBookFragment.
     * It opens a dialog for the user to submit their review.
     *
     * @param book The book for which the review is being posted.
     */

    @Override
    public void onReviewSubmitted(Book book, Review review, ViewBookUI viewBookUI) {
        // This method seems to be an older version of review submission.
        // The onSubmitReview method below is likely the one being used.
        // Keeping for now, but consider consolidating review submission logic.
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("username", review.getUsername());
        reviewData.put("rating", review.getRating());
        reviewData.put("comment", review.getComment());
        reviewData.put("timestamp", System.currentTimeMillis());
        reviewData.put("thumbnailUrl", book.getThumbnailUrl());
        // TODO: Add authorUid here when saving reviews in this method if it's still used.

        db.collection("Reviews")
                .document(book.getTitle()) // Use book title as document ID for now (better would be a real ID)
                .collection("UserReviews")  // Sub-collection of reviews
                .add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    viewBookUI.postReview(review); // Update UI immediately after success
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ControllerActivity.this, "Failed to save review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void fetchReviews(Book book, ViewBookUI viewBookUI) {
        fetchReviewsForBook(book, viewBookUI);
    }

    @Override
    public void onSubmitReview(Book selectedBook, Review newReview, ViewBookFragment viewBookFragment) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = documentSnapshot.getString("username");

                    // Create the Review object with the authorUid
                    Review reviewToSave = new Review(
                            username,
                            newReview.getRating(),
                            newReview.getComment(),
                            "",  // placeholder for reviewId, will be set by Firestore
                            selectedBook.getTitle(), // or selectedBook.getId() if using a unique ID field
                            selectedBook.getThumbnailUrl(), // Assuming thumbnail URL is part of the book object
                            uid // <--- Pass the current user's UID as authorUid
                    );

                    // Pass the Review object with authorUid to the ReviewManager
                    // Assuming ReviewManager.postReview saves the authorUid field to Firestore
                    reviewManager.postReview(selectedBook, reviewToSave, new ReviewManager.OnReviewSavedListener() {
                        @Override
                        public void onReviewSaved() {
                            // Update the UI with the review that includes the authorUid
                            viewBookFragment.postReview(reviewToSave);
                        }

                        @Override
                        public void onReviewSaveFailed(Exception e) {
                            Toast.makeText(ControllerActivity.this, "Failed to post review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user info: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void fetchReviewsForBook(Book book, ViewBookUI viewBookUI) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Reviews")
                .document(book.getTitle())
                .collection("UserReviews")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Review> reviews = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // Use QueryDocumentSnapshot for easier access to data
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) document;
                        String username = doc.getString("username");
                        // Handle potential null for rating (Firestore might store as Long or Double)
                        Number ratingNumber = doc.getDouble("rating");
                        float rating = ratingNumber != null ? ratingNumber.floatValue() : 0f;
                        String comment  = doc.getString("comment");
                        String reviewId = doc.getId();
                        String bookId   = doc.getReference()
                                .getParent()
                                .getParent()
                                .getId();
                        String authorUid = doc.getString("authorUid"); // <--- Fetch authorUid

                        // Use the new constructor that includes authorUid
                        reviews.add(new Review(username,
                                rating,
                                comment,
                                reviewId,
                                bookId,
                                doc.getString("thumbnailUrl"), // Assuming thumbnail URL is stored in the review document
                                authorUid)); // <--- Pass authorUid
                    }
                    viewBookUI.displayReviews(reviews); // New method in ViewBookUI to show reviews
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ControllerActivity.this, "Failed to load reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void onEditReviewRequested(Book book, Review review, ViewBookUI viewUI) {
        reviewManager.updateReview(review, new ReviewManager.OnReviewUpdatedListener() {
            @Override
            public void onReviewUpdated() {
                // After update, re-fetch reviews to refresh the list
                fetchReviewsForBook(book, viewUI);
            }
            @Override
            public void onReviewUpdateFailed(Exception e) {
                Toast.makeText(ControllerActivity.this,
                        "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDeleteReviewRequested(Book book, Review review, ViewBookUI viewUI) {
        reviewManager.deleteReview(review, new ReviewManager.OnReviewDeletedListener() {
            @Override
            public void onReviewDeleted() {
                // After deletion, re-fetch reviews to refresh the list
                fetchReviewsForBook(book, viewUI);
            }
            @Override
            public void onReviewDeleteFailed(Exception e) {
                Toast.makeText(ControllerActivity.this,
                        "Delete failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void fetchUserReviews(String username, ViewProfileUI ui) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collectionGroup("UserReviews")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Review> userReviews = new ArrayList<>();
                    for (DocumentSnapshot document : snapshots) {
                        // Use QueryDocumentSnapshot for easier access to data
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) document;
                        String reviewUsername = doc.getString("username"); // Get username from doc
                        // Handle potential null for rating
                        Number ratingNumber = doc.getDouble("rating");
                        float rating  = ratingNumber != null ? ratingNumber.floatValue() : 0f;
                        String comment  = doc.getString("comment");
                        String reviewId = doc.getId();
                        String bookId   = doc.getReference()
                                .getParent()
                                .getParent()
                                .getId();
                        String thumb = doc.getString("thumbnailUrl");
                        String authorUid = doc.getString("authorUid"); // <--- Fetch authorUid from the document

                        // Use the new constructor that includes authorUid
                        userReviews.add(new Review(reviewUsername, // Use username from doc
                                rating,
                                comment,
                                reviewId,
                                bookId,
                                thumb,
                                authorUid)); // <--- Pass authorUid
                    }
                    ui.displayUserReviews(userReviews);
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileFetch", "FAILED", e);
                    Toast.makeText(this, "Error loading user reviews: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

    }



    public void onEditUserReviewRequested(String currentUsername, Review review, ViewProfileUI ui) {
        // Assuming review object already has the updated rating/comment
        // Assuming ReviewManager.updateReview updates the correct review in Firestore
        reviewManager.updateReview(review, new ReviewManager.OnReviewUpdatedListener() {
            @Override
            public void onReviewUpdated() {
                // After update, re-fetch user reviews to refresh the list on the profile page
                // Need to get the username from the review object or the UI to refetch
                // Since we are now using UID for profile fetching, we should ideally refetch by UID.
                // For now, keeping username based refetch as per existing structure.
                fetchUserReviews(review.getUsername(), ui);
            }
            @Override
            public void onReviewUpdateFailed(Exception e) {
                Toast.makeText(ControllerActivity.this,
                        "Update failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void onDeleteUserReviewRequested(String currentUsername, Review review, ViewProfileUI ui) {
        // Assuming review object has the reviewId needed for deletion
        // Assuming ReviewManager.deleteReview deletes the correct review from Firestore
        reviewManager.deleteReview(review, new ReviewManager.OnReviewDeletedListener() {
            @Override
            public void onReviewDeleted() {
                // After deletion, re-fetch user reviews to refresh the list on the profile page
                // Need to get the username from the review object or the UI to refetch
                // Since we are now using UID for profile fetching, we should ideally refetch by UID.
                // For now, keeping username based refetch as per existing structure.
                fetchUserReviews(review.getUsername(), ui);
            }
            @Override
            public void onReviewDeleteFailed(Exception e) {
                Toast.makeText(ControllerActivity.this,
                        "Delete failed: "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    public void fetchSavedBooks(ViewSavedBooksUI ui) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { ui.showError("Not signed in"); return; }
        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("Users").document(uid)
                .collection("SavedBooks")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<Book> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        list.add(doc.toObject(Book.class));
                    }
                    ui.displaySavedBooks(list);
                })
                .addOnFailureListener(e -> ui.showError(e.getMessage()));
    }


    public void saveBook(Book book, ViewBookUI ui) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { ui.onBookSaveError("Not signed in"); return; }
        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("Users").document(uid)
                .collection("SavedBooks").document(book.getTitle())
                .set(book)
                .addOnSuccessListener(a -> ui.onBookSaveState(true))
                .addOnFailureListener(e -> ui.onBookSaveError(e.getMessage()));
    }


    public void removeSavedBook(Book book, ViewBookUI ui) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { ui.onBookSaveError("Not signed in"); return; }
        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("Users").document(uid)
                .collection("SavedBooks").document(book.getTitle())
                .delete()
                .addOnSuccessListener(a -> ui.onBookSaveState(false))
                .addOnFailureListener(e -> ui.onBookSaveError(e.getMessage()));
    }


    public void isBookSaved(Book book, ViewBookUI ui) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) { ui.onBookSaveState(false); return; }
        String uid = user.getUid();

        FirebaseFirestore.getInstance()
                .collection("Users").document(uid)
                .collection("SavedBooks").document(book.getTitle())
                .get()
                .addOnSuccessListener(doc -> ui.onBookSaveState(doc.exists()))
                .addOnFailureListener(e -> ui.onBookSaveError(e.getMessage()));
    }



    // A) Search users by prefix
    public void searchUsers(String query, ViewSearchUsersUI ui) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .orderBy("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<User> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots) {
                        User u = doc.toObject(User.class);
                        if (u != null) { // Add null check
                            u.setId(doc.getId()); // Make sure User model has setId
                            list.add(u);
                        }
                    }
                    ui.displaySearchResults(list);
                })
                .addOnFailureListener(e -> ui.showSearchError(e.getMessage()));
    }

    // B) Follow / unfollow a user
    public void followUser(String targetUserId, boolean follow, ViewSearchUsersUI ui) {
        String me = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference followCol = FirebaseFirestore.getInstance()
                .collection("Users").document(me)
                .collection("Following");

        Task<Void> task = follow
                ? followCol.document(targetUserId).set(Collections.singletonMap("since", System.currentTimeMillis()))
                : followCol.document(targetUserId).delete();

        task.addOnSuccessListener(a -> {/* no UI callback needed here; adapter will update */}
                )
                .addOnFailureListener(e -> ui.showSearchError("Follow error: " + e.getMessage()));
    }


    public interface OnFollowingFetchedListener {
        void onFetched(List<String> followingUids);
        void onError(String error);
    }

    public void fetchFollowingUids(String myUid, OnFollowingFetchedListener l) {
        FirebaseFirestore.getInstance()
                .collection("Users").document(myUid)
                .collection("Following")       // or whatever you called it
                .get()
                .addOnSuccessListener(snapshots -> {
                    List<String> uids = new ArrayList<>();
                    for (DocumentSnapshot d: snapshots) {
                        uids.add(d.getId());
                    }
                    l.onFetched(uids);
                })
                .addOnFailureListener(e -> l.onError(e.getMessage()));
    }

    // Interface for fetching user profile
    public interface OnUserProfileFetchedListener {
        void onFetched(User user);
        void onError(String error);
    }

    /**
     * Fetches a user's profile details from Firestore based on their User ID.
     *
     * @param userId The ID of the user whose profile to fetch.
     * @param listener The listener to handle the fetch result.
     */
    public void fetchUserProfile(String userId, OnUserProfileFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            user.setId(documentSnapshot.getId()); // Set the ID from the document
                            listener.onFetched(user);
                        } else {
                            listener.onError("Failed to parse user data.");
                        }
                    } else {
                        listener.onFetched(null); // User document not found
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ControllerActivity", "Error fetching user profile", e);
                    listener.onError(e.getMessage());
                });
    }

}
