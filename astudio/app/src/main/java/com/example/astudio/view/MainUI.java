package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.astudio.R;
import com.example.astudio.databinding.MainBinding;
import com.example.astudio.model.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainUI handles the shared layout with a fragment container and bottom navigation.
 */
public class MainUI {
    private final FragmentActivity activity;
    private final View rootView;
    private final FragmentManager fragmentManager;
    private final BottomNavigationView bottomNav;
    private static final int CONTAINER_ID = R.id.fragmentContainerView;

    public MainUI(FragmentActivity activity) {
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
        this.rootView = LayoutInflater.from(activity).inflate(R.layout.main, null, false);
        this.bottomNav = rootView.findViewById(R.id.bottomNavigationView);
        setupBottomNavigation();
        // Load home by default without adding to back stack
        navigateToHome(false);
    }

    @NonNull
    public View getRootView() {
        return rootView;
    }

    /**
     * Programmatically display a fragment (e.g. after login)
     */
    public void displayFragment(Fragment fragment) {
        showFragment(fragment, false);
        // Show or hide bottom nav for auth screens
        if (fragment instanceof LoginFragment || fragment instanceof CreateAccountFragment) {
            bottomNav.setVisibility(View.GONE);
        } else {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up bottom navigation item selected listener using if-else (no switch)
     */
    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                navigateToHome(true);
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
        args.putString("username", getCurrentUsername());
        browse.setArguments(args);
        if (activity instanceof BrowseBooksUI.BrowseBooksListener) {
            browse.setListener((BrowseBooksUI.BrowseBooksListener) activity);
        }
        showFragment(browse, addToBackStack);
    }

    private void navigateToProfile(boolean addToBackStack) {
        ViewProfileFragment profile = new ViewProfileFragment();
        Bundle args = new Bundle();
        args.putString("username", getCurrentUsername());
        profile.setArguments(args);
        showFragment(profile, addToBackStack);
    }

    private void navigateToSavedBooks(boolean addToBackStack) {
        ViewSavedBooksFragment saved = new ViewSavedBooksFragment();
        showFragment(saved, addToBackStack);
    }

    private void navigateToSearchUsers(boolean addToBackStack) {
        SearchUsersFragment search = new SearchUsersFragment();
        showFragment(search, addToBackStack);
    }

    /**
     * Perform the fragment transaction.
     */
    private void showFragment(Fragment fragment, boolean addToBackStack) {
        var transaction = fragmentManager.beginTransaction()
                .replace(CONTAINER_ID, fragment);
        if (addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * Retrieve the signed-in username or empty string
     */
    private String getCurrentUsername() {
        var user = UserManager.getInstance().getCurrentUser();
        return (user != null) ? user.getUsername() : "";
    }
}
