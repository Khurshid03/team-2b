package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.astudio.R;
import com.example.astudio.databinding.MainBinding;
import com.example.astudio.model.UserManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Class to manage components shared among all screens and the fragments being displayed.
 */
public class MainUI {

    private final FragmentActivity activity;
    private final View rootView;
    private final FragmentManager fragmentManager;

    /**
     * Constructor method.
     *
     * @param activity The activity this UI is associated with.
     */
    public MainUI(FragmentActivity activity) {
        this.activity = activity;
        this.fragmentManager = activity.getSupportFragmentManager();
        // Inflate your main UI layout that contains the fragment container and the BottomNavigationView.
        rootView = LayoutInflater.from(activity).inflate(R.layout.main, null);
        initBottomNavigation();
    }

    /**
     * Initializes the BottomNavigationView and sets up the listener for navigation item selection.
     */
    private void initBottomNavigation() {
        BottomNavigationView bottomNavigationView = rootView.findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // For home, create a new instance of BrowseBooksFragment.
                selectedFragment = new BrowseBooksFragment();
                // Optionally pass the username from UserManager:
                String username = (UserManager.getInstance().getCurrentUser() != null)
                        ? UserManager.getInstance().getCurrentUser().getUsername() : "";
                Bundle args = new Bundle();
                args.putString("username", username);
                selectedFragment.setArguments(args);
                // You may also set the listener for BrowseBooksFragment here if needed.
            }
            // Else, add additional cases for other navigation items.
            // e.g., if (id == R.id.nav_profile) { selectedFragment = new ProfileFragment(); }

            if (selectedFragment != null) {
                // Replace the current fragment in the container.
                fragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainerView, selectedFragment, "MAIN_FRAGMENT")
                        .commit();
                return true;
            }
            return false;
        });
    }

    /**
     * Replaces the current fragment with the one passed in.
     *
     * @param fragment The fragment to display.
     */
    public void displayFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .commit();
    }

    /**
     * Gets the root view to attach in setContentView().
     *
     * @return The root View of this UI.
     */
    @NonNull
    public View getRootView() {
        return rootView;
    }
}