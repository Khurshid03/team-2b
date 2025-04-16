package com.example.astudio.controller;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.astudio.R;
import com.example.astudio.model.Book;
import com.example.astudio.model.Review;
import com.example.astudio.model.ReviewManager;
import com.example.astudio.model.UserManager;
import com.example.astudio.view.BrowseBooksFragment;
import com.example.astudio.view.LoginFragment;
import com.example.astudio.view.MainUI;
import com.example.astudio.view.PostReviewDialogFragment;
import com.example.astudio.view.ViewBookFragment;
import com.example.astudio.view.BrowseBooksUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;
import java.util.List;

/**
 * This class acts as the controller for the entire application. It keeps track of the application
 * state, directs UI updates, and listens for UI-generated events.
 */

public class ControllerActivity extends AppCompatActivity implements BrowseBooksUI.BrowseBooksListener {

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
        // Initialize your MainUI class, which inflates your activity layout
        mainUI = new MainUI(this);
        setContentView(mainUI.getRootView());

        // Initialize the LoginFragment and display it as the default page
        LoginFragment loginfragment = new LoginFragment();
        mainUI.displayFragment(loginfragment);

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
     * This method is called by ViewBookFragment when the user taps the "Post Review" button.
     * It shows the post review dialog so the user can enter a rating and comment.
     * Once submitted, it uses ReviewManager to store the review.
     *
     * @param book The current book being viewed.
     */
//    public void onPostReview(Book book) {
//        PostReviewDialogFragment dialog = new PostReviewDialogFragment();
//        dialog.setOnReviewSubmittedListener((rating, comment) -> {
//            // Use the current user's username (retrieved from a global UserManager or currentUsername field)
//            String reviewer = (currentUsername != null && !currentUsername.isEmpty()) ? currentUsername : "Anonymous";
//            Review newReview = new Review(reviewer, rating, comment);
//            reviewManager.postReview(newReview, new ReviewManager.ReviewCallback() {
//                @Override
//                public void onReviewPosted(List<Review> updatedReviews) {
//
//                }
//            });
//        });
//        dialog.show(getSupportFragmentManager(), "PostReviewDialog");
//    }
}