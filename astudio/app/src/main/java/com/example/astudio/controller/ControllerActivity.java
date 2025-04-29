package com.example.astudio.controller;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.astudio.databinding.FragmentBrowseBooksBinding;
import com.example.astudio.model.Book;
import com.example.astudio.model.BookResponse;
import com.example.astudio.model.Review;
import com.example.astudio.model.ReviewManager;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.example.astudio.view.ViewBookUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private final ReviewManager reviewManager = new ReviewManager();
    private String currentUsername; // Store the logged-in user's username

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
     * This method is called by the LoginFragment when the user successfully logs in.
     * It navigates to the home landing page (BrowseBooksFragment) and stores the username.
     */
    public void onLoginSuccess(String username) {
        BrowseBooksFragment landingFragment = new BrowseBooksFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        landingFragment.setArguments(args);
        landingFragment.setListener(this);
        mainUI.displayFragment(landingFragment);
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



    private static final String API_KEY = "AIzaSyD4CwbziYN_d65sQeyrk3F616yUHzYDe14";

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
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("username", review.getUsername());
        reviewData.put("rating", review.getRating());
        reviewData.put("comment", review.getComment());
        reviewData.put("timestamp", System.currentTimeMillis()); // Optional: to sort reviews later

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

                    // Correct the username before posting
                    Review correctedReview = new Review(username, newReview.getRating(), newReview.getComment());

                    reviewManager.postReview(selectedBook, correctedReview, new ReviewManager.OnReviewSavedListener() {
                        @Override
                        public void onReviewSaved() {
                            viewBookFragment.postReview(correctedReview);
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
                        String username = document.getString("username");
                        float rating = document.getDouble("rating").floatValue();
                        String comment = document.getString("comment");
                        Review review = new Review(username, rating, comment);
                        reviews.add(review);
                    }
                    viewBookUI.displayReviews(reviews); // New method in ViewBookUI to show reviews
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ControllerActivity.this, "Failed to load reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}