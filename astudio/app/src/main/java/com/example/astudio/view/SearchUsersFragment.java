package com.example.astudio.view;

import android.os.Bundle;
import android.util.Log;
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
import com.example.astudio.model.User;
// Import listener interfaces from FirestoreFacade
import com.example.astudio.persistence.FirestoreFacade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that allows searching for users and following/unfollowing them.
 */
public class SearchUsersFragment extends Fragment implements ViewSearchUsersUI {

    private static final String FRAGMENT_TAG = "SearchUsersFragment"; // For logging
    private FragmentSearchUsersBinding binding;
    private SearchUsersAdapter adapter;
    private final List<User> results = new ArrayList<>();
    private ControllerActivity controller;
    private String myUid; // Current user's UID

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

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(getContext(), "User not logged in.", Toast.LENGTH_SHORT).show();
            Log.e(FRAGMENT_TAG, "Current user is null. Cannot proceed.");
            // Optionally, navigate away or disable functionality
            return;
        }
        myUid = currentUser.getUid();

        // Initialize adapter BEFORE setting it to the RecyclerView
        adapter = new SearchUsersAdapter(results, myUid, new SearchUsersAdapter.ActionListener() {
            @Override
            public void onFollowToggled(User user, boolean shouldFollow) {
                if (user == null || user.getUsername() == null) {
                    Log.e(FRAGMENT_TAG, "User or username is null in onFollowToggled.");
                    Toast.makeText(getContext(), "Error: User data is missing.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (shouldFollow) {
                    // myUid is no longer passed; controller handles it
                    controller.follow(
                            user.getUsername(),
                            () -> {
                                Log.d(FRAGMENT_TAG, "Successfully followed user: " + user.getUsername());
                                if (adapter.getFollowingUsernames().add(user.getUsername())) {
                                    adapter.notifyItemChanged(results.indexOf(user));
                                }

                                // Refresh the other user's profile badge if it's currently visible
                                Fragment topFragment = getParentFragmentManager().findFragmentById(R.id.fragmentContainerView);
                                if (topFragment instanceof ViewProfileFragment) {
                                    ViewProfileFragment profileFragment = (ViewProfileFragment) topFragment;
                                    // Check if the displayed profile is the one being followed
                                    if (profileFragment.binding != null && user.getUsername().equals(profileFragment.binding.tvUsername.getText().toString())) {
                                        controller.fetchFollowersCount(
                                                user.getUsername(),
                                                // Use FirestoreFacade.OnCountFetchedListener
                                                new FirestoreFacade.OnCountFetchedListener() {
                                                    @Override
                                                    public void onCount(int count) {
                                                        if (profileFragment.isAdded() && profileFragment.binding != null) {
                                                            profileFragment.binding.followersButton.setText(getString(R.string.followers_count, count));
                                                        }
                                                    }
                                                    @Override
                                                    public void onError(String error) {
                                                        Log.e(FRAGMENT_TAG, "Error fetching followers count for " + user.getUsername() + ": " + error);
                                                        // Optionally show a toast or log
                                                    }
                                                }
                                        );
                                    }
                                }
                            },
                            err -> {
                                Log.e(FRAGMENT_TAG, "Follow failed for " + user.getUsername() + ": " + err);
                                Toast.makeText(getContext(), "Follow failed: " + err, Toast.LENGTH_SHORT).show();
                            }
                    );
                } else {
                    // myUid is no longer passed; controller handles it
                    controller.unfollow(
                            user.getUsername(),
                            () -> {
                                Log.d(FRAGMENT_TAG, "Successfully unfollowed user: " + user.getUsername());
                                if(adapter.getFollowingUsernames().remove(user.getUsername())) {
                                    adapter.notifyItemChanged(results.indexOf(user));
                                }
                            },
                            err -> {
                                Log.e(FRAGMENT_TAG, "Unfollow failed for " + user.getUsername() + ": " + err);
                                Toast.makeText(getContext(), "Unfollow failed: " + err, Toast.LENGTH_SHORT).show();
                            }
                    );
                }
            }

            @Override
            public void onUserClicked(User user) {
                if (user == null || user.getId() == null) {
                    Log.e(FRAGMENT_TAG, "User or user ID is null in onUserClicked.");
                    Toast.makeText(getContext(), "Cannot view profile: User data missing.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.d(FRAGMENT_TAG, "User clicked: " + user.getUsername() + " with ID: " + user.getId());
                ViewProfileFragment fragment = new ViewProfileFragment();
                Bundle args = new Bundle();
                args.putString("userId", user.getId());
                fragment.setArguments(args);

                // Use getParentFragmentManager() for fragment transactions from within a fragment
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.searchUsersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchUsersRecycler.setAdapter(adapter);

        // Load who I currently follow
        // myUid should be valid here due to the check above
        controller.fetchFollowingUsernames(myUid, new FirestoreFacade.OnFollowedListFetchedListener() {
            @Override
            public void onFetched(List<String> usernames) {
                Log.d(FRAGMENT_TAG, "Fetched " + usernames.size() + " followed usernames.");
                adapter.setFollowingUsernames(usernames);
                // adapter.notifyDataSetChanged(); // Already done in setFollowingUsernames or should be if it modifies data
            }
            @Override
            public void onError(String err) {
                Log.e(FRAGMENT_TAG, "Error loading follow list: " + err);
                Toast.makeText(getContext(), "Error loading follow list: " + err, Toast.LENGTH_SHORT).show();
            }
        });

        // Handle search button click
        binding.goButton.setOnClickListener(v -> {
            String query = binding.searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                Log.d(FRAGMENT_TAG, "Searching for users with query: " + query);
                controller.searchUsers(query, SearchUsersFragment.this); // 'this' implements ViewSearchUsersUI
            } else {
                Toast.makeText(getContext(), "Please enter a search query.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void displaySearchResults(List<User> users) {
        results.clear();
        if (users != null) {
            results.addAll(users);
            Log.d(FRAGMENT_TAG, "Displaying " + users.size() + " search results.");
        } else {
            Log.d(FRAGMENT_TAG, "No search results to display.");
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void showSearchError(String message) {
        Log.e(FRAGMENT_TAG, "Search error: " + message);
        Toast.makeText(getContext(), "Search error: " + message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Important to prevent memory leaks
        Log.d(FRAGMENT_TAG, "onDestroyView called.");
    }
}
