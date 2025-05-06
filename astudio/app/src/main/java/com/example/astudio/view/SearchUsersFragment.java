package com.example.astudio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.astudio.R;
import com.example.astudio.controller.ControllerActivity;
import com.example.astudio.databinding.FragmentSearchUsersBinding;
import com.example.astudio.model.User; // Assuming User model exists
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that allows searching for users and following/unfollowing them.
 */
public class SearchUsersFragment extends Fragment implements ViewSearchUsersUI {

    private FragmentSearchUsersBinding binding;
    private SearchUsersAdapter adapter;
    private final List<User> results = new ArrayList<>();
    private ControllerActivity controller;
    private String myUid; // Store current user's UID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        controller = (ControllerActivity) requireActivity();

        // Get current user's UID
        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize adapter BEFORE setting it to the RecyclerView
        adapter = new SearchUsersAdapter(results, myUid, new SearchUsersAdapter.ActionListener() {
            @Override
            public void onFollowToggled(User user, boolean shouldFollow) {
                controller.followUser(user.getId(), shouldFollow, SearchUsersFragment.this);
                // Optimistically update adapter state...
                if (shouldFollow) adapter.getFollowingUids().add(user.getId());
                else             adapter.getFollowingUids().remove(user.getId());
                adapter.notifyItemChanged(results.indexOf(user));

                // Show a proper Toast
                String msg = shouldFollow
                        ? "Following " + user.getUsername()
                        : "Unfollowed " + user.getUsername();
                Toast.makeText(
                        getContext(),         // or `requireContext()`
                        msg,
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onUserClicked(User user) {
                // Navigate to user's profile page or details
                // Pass the user's UID instead of username
                ViewProfileFragment fragment = new ViewProfileFragment();
                Bundle args = new Bundle();
                args.putString("userId", user.getId()); // Pass user ID
                fragment.setArguments(args);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Set up RecyclerView and adapter
        binding.searchUsersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchUsersRecycler.setAdapter(adapter); // Set the initialized adapter

        // 1) First load “who I follow” AFTER adapter is set:
        controller.fetchFollowingUids(myUid, new ControllerActivity.OnFollowingFetchedListener() {
            @Override
            public void onFetched(List<String> uids) {
                adapter.setFollowingUids(uids);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onError(String err) {
                Toast.makeText(getContext(), "Error loading follow list: " + err, Toast.LENGTH_SHORT).show();
            }
        });


        // Handle search button click
        binding.goButton.setOnClickListener(v -> {
            String query = binding.searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                controller.searchUsers(query, SearchUsersFragment.this);
            }
        });
    }

    @Override
    public void displaySearchResults(List<User> users) {
        results.clear();
        if (users != null) {
            results.addAll(users);
        }
        // Check if adapter is initialized before notifying
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showSearchError(String message) {
        Toast.makeText(getContext(), "Search error: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
