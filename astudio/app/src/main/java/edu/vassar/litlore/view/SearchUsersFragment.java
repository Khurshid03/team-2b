package edu.vassar.litlore.view;

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

import edu.vassar.litlore.R;
import edu.vassar.litlore.controller.ControllerActivity;
import edu.vassar.litlore.databinding.FragmentSearchUsersBinding;
import edu.vassar.litlore.model.User;
// Import listener interfaces from FirestoreFacade
import edu.vassar.litlore.persistence.FirestoreFacade;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that allows searching for users and following/unfollowing them.
 * Displays a search input, search results, and follow/unfollow actions.
 */
public class SearchUsersFragment extends Fragment implements ViewSearchUsersUI {

    private static final String FRAGMENT_TAG = "SearchUsersFragment"; // For logging
    /** ViewBinding instance for the fragment layout. */
    private FragmentSearchUsersBinding binding;
    /** Adapter for displaying user search results in the RecyclerView. */
    private SearchUsersAdapter adapter;
    /** List to hold the current user search results. */
    private final List<User> results = new ArrayList<>();
    /** Reference to the hosting ControllerActivity. */
    private ControllerActivity controller;
    /** The unique ID of the currently logged-in user. */
    private String myUid; // Current user's UID

    /**
     * Required empty public constructor.
     */
    public SearchUsersFragment() {
        // Required empty public constructor
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * Inflates the layout using ViewBinding.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchUsersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    /**
     * Called immediately after {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has returned.
     * Sets up the RecyclerView, adapter, button listeners, and fetches the current user's following list.
     *
     * @param view The View returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     */
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
                        .addToBackStack(null) // Add to back stack to allow returning to search results
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

    /**
     * Displays the list of user search results in the RecyclerView.
     * Implements {@link ViewSearchUsersUI#displaySearchResults(List)}.
     *
     * @param users The list of {@link User} objects found.
     */
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

    /**
     * Displays an error message related to a user search operation.
     * Implements {@link ViewSearchUsersUI#showSearchError(String)}.
     *
     * @param message The error message string.
     */
    @Override
    public void showSearchError(String message) {
        Log.e(FRAGMENT_TAG, "Search error: " + message);
        Toast.makeText(getContext(), "Search error: " + message, Toast.LENGTH_LONG).show();
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     * Cleans up the binding reference.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Important to prevent memory leaks
        Log.d(FRAGMENT_TAG, "onDestroyView called.");
    }
}
