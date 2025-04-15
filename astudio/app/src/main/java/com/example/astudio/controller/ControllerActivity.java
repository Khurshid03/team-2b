package com.example.astudio.controller;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.astudio.R;
import com.example.astudio.model.Book;
import com.example.astudio.model.ReviewManager;
import com.example.astudio.view.BrowseBooksFragment;
import com.example.astudio.view.LoginFragment;
import com.example.astudio.view.MainUI;
import com.example.astudio.view.ViewBookFragment;
import com.example.astudio.view.BrowseBooksUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.Serializable;

public class ControllerActivity extends AppCompatActivity implements BrowseBooksUI.BrowseBooksListener {

    public MainUI mainUI;
    private final ReviewManager reviewManager = new ReviewManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize your MainUI class, which inflates your activity layout
        mainUI = new MainUI(this);
        setContentView(mainUI.getRootView());

        // Initialize the LoginFragment and display it as the default page
        LoginFragment loginfragment = new LoginFragment();
        mainUI.displayFragment(loginfragment);


        // Set up the BottomNavigationView listener.
        // Make sure that your MainUI's layout contains a BottomNavigationView with the id bottomNavigationView.
        BottomNavigationView bottomNavigationView = mainUI.getRootView().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                BrowseBooksFragment selectedFragment = null;
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    // When home is selected, create a new BrowseBooksFragment
                    selectedFragment = new BrowseBooksFragment();
                    selectedFragment.setListener(ControllerActivity.this);
                }
//                else if (id == R.id.nav_profile) {
//                    // When profile is selected, create a new profile fragment.
//                    selectedFragment = new ViewProfileFragment();
//                }

                if (selectedFragment != null) {
                    mainUI.displayFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * This method is called by the LoginFragment when the user successfully logs in.
     * It navigates to the home landing page (BrowseBooksFragment).
     */
    public void onLoginSuccess(String username) {
        // Create a new instance of BrowseBooksFragment and pass the username
        BrowseBooksFragment landingFragment = new BrowseBooksFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
        landingFragment.setArguments(args);
        landingFragment.setListener(this);
        mainUI.displayFragment(landingFragment);
    }

    @Override
    public void onBookSelected(Book book) {
        // Create the ViewBookFragment and pass the selected Book as a Serializable
        ViewBookFragment viewBookFragment = new ViewBookFragment();
        Bundle args = new Bundle();
        args.putSerializable("book", (Serializable) book);
        viewBookFragment.setArguments(args);

        args.putString("description", book.getDescription());
        args.putString("author", book.getAuthor());

        // Display the ViewBookFragment using your MainUI container
        mainUI.displayFragment(viewBookFragment);
    }

    @Override
    public void onGenreSelected(String genre) {
        // Handle genre selections if needed
    }
}