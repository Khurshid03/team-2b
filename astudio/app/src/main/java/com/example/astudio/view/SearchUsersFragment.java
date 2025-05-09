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
import com.example.astudio.model.User;
import com.example.astudio.model.UserManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.HashMap;
import java.util.Map;

/**
 * Fragment that allows searching for users and following/unfollowing them.
 */
public class SearchUsersFragment extends Fragment implements ViewSearchUsersUI {

    private FragmentSearchUsersBinding binding;
    private SearchUsersAdapter adapter;
    private final List<User> results = new ArrayList<>();
    private ControllerActivity controller;
    private String myUid;

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

        // Get current user's username
// new—check for null first
        FirebaseUser me = FirebaseAuth.getInstance().getCurrentUser();
        myUid = (me != null ? me.getUid() : "");

        // Initialize adapter BEFORE setting it to the RecyclerView
        adapter = new SearchUsersAdapter(results, myUid, new SearchUsersAdapter.ActionListener() {
            @Override
            public void onFollowToggled(User user, boolean shouldFollow) {
                if (shouldFollow) {
                    controller.follow(
                            myUid,
                            user.getUsername(),
                            () -> {
                                // 1) update your Search list button
                                adapter.getFollowingUsernames().add(user.getUsername());
                                adapter.notifyItemChanged(results.indexOf(user));

                                // 2) refresh the other user's profile badge if it's currently visible
                                Fragment top = requireActivity()
                                        .getSupportFragmentManager()
                                        .findFragmentById(R.id.fragmentContainerView);
                                if (top instanceof ViewProfileFragment) {
                                    String displayed = ((ViewProfileFragment)top)
                                            .binding.tvUsername.getText().toString();
                                    if (displayed.equals(user.getUsername())) {
                                        controller.fetchFollowersCount(
                                                user.getUsername(),
                                                new ControllerActivity.OnCountFetchedListener() {
                                                    @Override public void onCount(int c) {
                                                        ((ViewProfileFragment)top)
                                                                .binding.followersButton
                                                                .setText(getString(R.string.followers_count, c));
                                                    }
                                                    @Override public void onError(String e) { /*…*/ }
                                                }
                                        );
                                    }
                                }
                            },
                            err -> Toast.makeText(getContext(), "Follow failed: "+err, Toast.LENGTH_SHORT).show()
                    );
                } else {
                    controller.unfollow(
                            myUid,
                            user.getUsername(),
                            () -> {
                                adapter.getFollowingUsernames().remove(user.getUsername());
                                adapter.notifyItemChanged(results.indexOf(user));
                            },
                            err -> Toast.makeText(getContext(), "Unfollow failed: " + err, Toast.LENGTH_SHORT).show()
                    );
                }
            }



            @Override
            public void onUserClicked(User user) {
                ViewProfileFragment fragment = new ViewProfileFragment();
                Bundle args = new Bundle();
                args.putString("userId", user.getId());
                fragment.setArguments(args);
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainerView, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.searchUsersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchUsersRecycler.setAdapter(adapter);

        String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Load who I currently follow
        controller.fetchFollowingUsernames(myUid, new ControllerActivity.OnFollowedListFetchedListener() {
            @Override
            public void onFetched(List<String> usernames) {
                adapter.setFollowingUsernames(usernames);

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
        if (adapter != null) adapter.notifyDataSetChanged();
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
