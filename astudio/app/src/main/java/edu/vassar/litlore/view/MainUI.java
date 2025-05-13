package edu.vassar.litlore.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
// Import FragmentManager and Fragment if they are not automatically resolved
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import edu.vassar.litlore.R;
import edu.vassar.litlore.databinding.MainBinding; // Import the generated MainBinding class
import edu.vassar.litlore.model.UserManager; // Assuming this is still used for getCurrentUsername
// Remove BottomNavigationView import if only accessed via binding
// import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth; // For navigateToSearchUsers
import com.google.firebase.auth.FirebaseUser;

import edu.vassar.litlore.model.User;

/**
 * MainUI handles the shared layout with a fragment container and bottom navigation.
 * This class manages fragment transactions and bottom navigation visibility.
 */
public class MainUI {
    /** The host activity for managing fragments. */
    private final FragmentActivity activity;
    /** ViewBinding instance for the main layout. */
    private final MainBinding binding; // Declare the binding object
    /** FragmentManager for performing fragment transactions. */
    private final FragmentManager fragmentManager;
    /** The ID of the container View where fragments are displayed. */
    private static final int CONTAINER_ID = R.id.fragmentContainerView;

    /**
     * Constructs a new MainUI instance.
     * Initializes ViewBinding, sets up bottom navigation, and loads the home fragment.
     * @param activity The host FragmentActivity.
     */
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

    /**
     * Gets the root view of the main layout.
     * @return The root View from the binding object.
     */
    @NonNull
    public View getRootView() {
        return binding.getRoot(); // Return the root view from the binding object
    }

    /**
     * Displays a fragment in the main container and manages BottomNavigationView visibility.
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
     * Sets up bottom navigation item selected listener.
     * Navigates to different fragments based on the selected item.
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

    /**
     * Navigates to the Home (Browse Books) fragment.
     * @param addToBackStack True if this transaction should be added to the back stack.
     */
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

    /**
     * Navigates to the User Profile fragment for the current user.
     * @param addToBackStack True if this transaction should be added to the back stack.
     */
    private void navigateToProfile(boolean addToBackStack) {
        ViewProfileFragment profile = new ViewProfileFragment();
        Bundle args = new Bundle();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            args.putString("userId", currentUser.getUid());
        }
        profile.setArguments(args);
        showFragment(profile, addToBackStack);
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
    }

    /**
     * Navigates to the Saved Books fragment.
     * @param addToBackStack True if this transaction should be added to the back stack.
     */
    private void navigateToSavedBooks(boolean addToBackStack) {
        ViewSavedBooksFragment saved = new ViewSavedBooksFragment();
        showFragment(saved, addToBackStack);
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
    }

    /**
     * Navigates to the Search Users fragment.
     * @param addToBackStack True if this transaction should be added to the back stack.
     */
    private void navigateToSearchUsers(boolean addToBackStack) {
        SearchUsersFragment search = new SearchUsersFragment();
        showFragment(search, addToBackStack);
        binding.bottomNavigationView.setVisibility(View.VISIBLE);
    }

    /**
     * Performs the fragment transaction to display a fragment.
     * Replaces the current fragment in the container.
     * @param fragment The Fragment to show.
     * @param addToBackStack True if this transaction should be added to the back stack.
     */
    private void showFragment(Fragment fragment, boolean addToBackStack) {
        FragmentManager.BackStackEntry currentEntry = null;
        if (fragmentManager.getBackStackEntryCount() > 0) {
            currentEntry = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1);
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
     * Retrieves the signed-in username from UserManager.
     * @return The current username or an empty string if not available.
     */
    private String getCurrentUsername() {
        // Assuming UserManager.getInstance().getCurrentUser() returns your User model
        // which has a getUsername() method.
        User currentUserModel = UserManager.getInstance().getCurrentUser();
        return (currentUserModel != null && currentUserModel.getUsername() != null) ? currentUserModel.getUsername() : "";
    }
}
