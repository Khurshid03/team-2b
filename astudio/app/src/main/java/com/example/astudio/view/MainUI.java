package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
// Import FragmentManager and Fragment if they are not automatically resolved
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.astudio.R;
import com.example.astudio.databinding.MainBinding; // Import the generated MainBinding class
import com.example.astudio.model.UserManager; // Assuming this is still used for getCurrentUsername
// Remove BottomNavigationView import if only accessed via binding
// import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth; // For navigateToSearchUsers
import com.google.firebase.auth.FirebaseUser;

/**
 * MainUI handles the shared layout with a fragment container and bottom navigation.
 * This version uses ViewBinding to access views from the main.xml layout.
 */
public class MainUI {
    private final FragmentActivity activity;
    private final MainBinding binding; // Declare the binding object
    private final FragmentManager fragmentManager;
    // CONTAINER_ID is still used directly for fragment transactions
    private static final int CONTAINER_ID = R.id.fragmentContainerView;

    public MainUI(FragmentActivity activity) {
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();

        // Inflate the layout using MainBinding
        LayoutInflater inflater = LayoutInflater.from(activity);
        this.binding = MainBinding.inflate(inflater);
        // The rootView is now binding.getRoot()

        setupBottomNavigation();
        // Load home by default without adding to back stack
        navigateToHome(false);
    }

    @NonNull
    public View getRootView() {
        return binding.getRoot(); // Return the root view from the binding object
    }

    /**
     * Programmatically display a fragment (e.g. after login)
     * and manage BottomNavigationView visibility.
     * @param fragment The Fragment to display.
     */
    public void displayFragment(Fragment fragment) {
        showFragment(fragment, false); // By default, don't add to back stack for initial display

        // Show or hide bottom nav based on the type of fragment
        if (fragment instanceof LoginFragment || fragment instanceof CreateAccountFragment) {
            binding.bottomNavigationView.setVisibility(View.GONE); // Access via binding
        } else {
            binding.bottomNavigationView.setVisibility(View.VISIBLE); // Access via binding
        }
    }

    /**
     * Set up bottom navigation item selected listener using if-else.
     */
    private void setupBottomNavigation() {
        // Access bottomNavigationView via the binding object
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            // Using if-else if-else structure as per original code
            if (id == R.id.nav_home) {
                navigateToHome(true); // Add to back stack when user navigates
                return true;
            } else if (id == R.id.nav_profile) {
                navigateToProfile(true);
                return true;
            } else if (id == R.id.nav_saved_books) {
                navigateToSavedBooks(true);
                return true;
            } else if (id == R.id.search_users) {
                navigateToSearchUsers(true);
                return true;
            }
            return false;
        });
    }

    private void navigateToHome(boolean addToBackStack) {
        BrowseBooksFragment browse = new BrowseBooksFragment();
        Bundle args = new Bundle();
        // Assuming getCurrentUsername() is correctly implemented
        args.putString("username", getCurrentUsername());
        browse.setArguments(args);
        // Set listener if activity implements it
        if (activity instanceof BrowseBooksUI.BrowseBooksListener) {
            browse.setListener((BrowseBooksUI.BrowseBooksListener) activity);
        }
        showFragment(browse, addToBackStack);
        binding.bottomNavigationView.setVisibility(View.VISIBLE); // Ensure nav is visible for home
    }

    private void navigateToProfile(boolean addToBackStack) {
        ViewProfileFragment profile = new ViewProfileFragment();
        Bundle args = new Bundle();
        // Pass current user's ID to view their own profile by default
        // The ViewProfileFragment itself handles logic if no ID is passed (defaults to current user)
        // or if a specific ID is passed (to view another user's profile).
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            args.putString("userId", currentUser.getUid());
        }
        // args.putString("username", getCurrentUsername()); // Username might not be needed directly here if UID is primary key
        profile.setArguments(args);
        showFragment(profile, addToBackStack);
        binding.bottomNavigationView.setVisibility(View.VISIBLE); // Ensure nav is visible
    }

    private void navigateToSavedBooks(boolean addToBackStack) {
        ViewSavedBooksFragment saved = new ViewSavedBooksFragment();
        showFragment(saved, addToBackStack);
        binding.bottomNavigationView.setVisibility(View.VISIBLE); // Ensure nav is visible
    }

    private void navigateToSearchUsers(boolean addToBackStack) {
        SearchUsersFragment search = new SearchUsersFragment();
        // SearchUsersFragment typically doesn't need arguments to start,
        // but if it did, they'd be set here.
        // Example:
        // Bundle args = new Bundle();
        // args.putString("initialQuery", "some default query");
        // search.setArguments(args);
        showFragment(search, addToBackStack);
        binding.bottomNavigationView.setVisibility(View.VISIBLE); // Ensure nav is visible
    }

    /**
     * Perform the fragment transaction.
     * @param fragment The Fragment to show.
     * @param addToBackStack True if this transaction should be added to the back stack.
     */
    private void showFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager.BackStackEntry currentEntry = null;
        if (fragmentManager.getBackStackEntryCount() > 0) {
            currentEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
        }

        // Avoid adding the same fragment type to the back stack consecutively if not desired
        // This is a simple check; more complex navigation might require more robust logic
        if (currentEntry != null && fragment.getClass().getName().equals(currentEntry.getName()) && !addToBackStack) {
            // If we are not adding to backstack and it's the same fragment, maybe do nothing or pop
            // For now, let's proceed as the original logic implies replacing.
        }

        androidx.fragment.app.FragmentTransaction transaction = fragmentManager.beginTransaction()
                .replace(CONTAINER_ID, fragment, fragment.getClass().getName()); // Added fragment tag

        if (addToBackStack) {
            // Only add to back stack if it's not the same as the current top fragment
            // to prevent multiple identical entries from bottom nav clicks.
            Fragment currentFragment = fragmentManager.findFragmentById(CONTAINER_ID);
            if (currentFragment == null || !currentFragment.getClass().getName().equals(fragment.getClass().getName())) {
                transaction.addToBackStack(fragment.getClass().getName()); // Use class name as back stack name
            }
        }
        transaction.commit();
    }

    /**
     * Retrieve the signed-in username or empty string.
     * This relies on your UserManager implementation.
     * @return Current username or empty string.
     */
    private String getCurrentUsername() {
        // Assuming UserManager.getInstance().getCurrentUser() returns your User model
        // which has a getUsername() method.
        com.example.astudio.model.User currentUserModel = UserManager.getInstance().getCurrentUser();
        return (currentUserModel != null && currentUserModel.getUsername() != null) ? currentUserModel.getUsername() : "";
    }
}
