package com.example.astudio.view;

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

import com.example.astudio.databinding.MainBinding;

/**
 * Class to manage components shared among all screens and the fragments being displayed.
 */
public class MainUI {

    private final MainBinding binding;
    private final FragmentManager fmanager;

    /**
     * Constructor method.
     *
     * @param factivity The activity this UI is associated with.
     */
    public MainUI(@NonNull FragmentActivity factivity) {
        this.binding = MainBinding.inflate(LayoutInflater.from(factivity));
        this.fmanager = factivity.getSupportFragmentManager();

        // Handles insets (status bar, nav bar) cleanly
        ViewCompat.setOnApplyWindowInsetsListener(this.binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Replaces the current fragment with the one passed in.
     *
     * @param frag The fragment to display.
     */
    public void displayFragment(@NonNull Fragment frag) {
        FragmentTransaction ftrans = this.fmanager.beginTransaction();
        ftrans.replace(this.binding.fragmentContainerView.getId(), frag);
        ftrans.commit();
    }

    /**
     * Gets the root view to attach in setContentView().
     *
     * @return The root View of this UI.
     */
    @NonNull
    public View getRootView() {
        return this.binding.getRoot();
    }
}