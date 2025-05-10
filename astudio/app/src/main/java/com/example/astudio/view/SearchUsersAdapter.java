package com.example.astudio.view;

import android.util.Log; // Import Log for potential debugging
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Button, ImageView, TextView imports are no longer needed as views are accessed via binding
// import android.widget.Button;
// import android.widget.ImageView;
// import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.databinding.ItemUserSearchBinding; // Import the generated binding class
import com.example.astudio.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying search results of users, allowing follow/unfollow toggles.
 * This version uses ViewBinding in its ViewHolder.
 */
public class SearchUsersAdapter
        extends RecyclerView.Adapter<SearchUsersAdapter.UserViewHolder> {

    private static final String ADAPTER_TAG = "SearchUsersAdapter"; // For logging

    private final List<User> users;
    private final String myUid; // Changed from myUsername to myUid for clarity, assuming it's the UID
    private final ActionListener listener;
    private List<String> followingUsernames; // Stores usernames the current user is following

    /**
     * Interface for handling actions performed on user items,
     * such as toggling follow status or clicking on a user.
     */
    public interface ActionListener {
        /**
         * Called when the follow button is toggled.
         * @param user         The target user.
         * @param shouldFollow True if the action is to follow, false to unfollow.
         */
        void onFollowToggled(User user, boolean shouldFollow);

        /**
         * Called when the user item (the whole row) is clicked.
         * @param user The user that was clicked.
         */
        void onUserClicked(User user);
    }

    /**
     * Constructor for SearchUsersAdapter.
     * @param users Initial list of users to display.
     * @param currentLoggedInUserUid The UID of the currently logged-in user.
     * @param listener Listener for actions.
     */
    public SearchUsersAdapter(List<User> users,
                              String currentLoggedInUserUid, // Renamed parameter for clarity
                              ActionListener listener) {
        this.users = (users != null) ? users : new ArrayList<>();
        this.myUid = currentLoggedInUserUid; // Store the UID
        this.listener = listener;
        this.followingUsernames = new ArrayList<>(); // Initialize to avoid null issues
    }

    /**
     * Updates the list of usernames that the current user is following.
     * This is used to correctly display the "Follow" / "Following" state.
     * @param followedUsernames A list of usernames.
     */
    public void setFollowingUsernames(List<String> followedUsernames) {
        this.followingUsernames = (followedUsernames != null) ? new ArrayList<>(followedUsernames) : new ArrayList<>();
        notifyDataSetChanged(); // Refresh the whole list to update follow states
        Log.d(ADAPTER_TAG, "Following usernames updated. Count: " + this.followingUsernames.size());
    }

    /**
     * Exposes the current "following" list, primarily for optimistic UI updates
     * directly from the fragment if needed before a full refresh.
     * @return A list of usernames the current user is following.
     */
    public List<String> getFollowingUsernames() {
        return followingUsernames;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ItemUserSearchBinding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemUserSearchBinding binding = ItemUserSearchBinding.inflate(inflater, parent, false);
        return new UserViewHolder(binding); // Pass the binding to the ViewHolder
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        // The binding of data to views and setting listeners is now handled within the ViewHolder's bind method
        holder.bind(user, myUid, followingUsernames, listener);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * ViewHolder for user search result items. Uses ItemUserSearchBinding.
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final ItemUserSearchBinding binding; // Store the binding object

        /**
         * Constructor for the ViewHolder.
         * @param itemBinding The ViewBinding object for the item_user_search layout.
         */
        UserViewHolder(@NonNull ItemUserSearchBinding itemBinding) {
            super(itemBinding.getRoot());
            this.binding = itemBinding; // Assign the binding object
        }

        /**
         * Binds a User object to the views in the ViewHolder and sets up listeners.
         * @param user The User object to display.
         * @param currentLoggedInUserUid The UID of the currently logged-in user.
         * @param currentlyFollowedUsernames List of usernames the logged-in user is following.
         * @param listener ActionListener for interactions.
         */
        void bind(final User user, final String currentLoggedInUserUid, final List<String> currentlyFollowedUsernames, final ActionListener listener) {
            if (user == null) {
                Log.w(ADAPTER_TAG, "User object is null in bind().");
                // Optionally clear views or set to default state
                binding.tvUsername.setText("");
                binding.btnFollow.setVisibility(View.GONE);
                return;
            }

            binding.tvUsername.setText(user.getUsername());

            // Placeholder image for avatar
            Glide.with(binding.ivUserAvatar.getContext())
                    .load(R.drawable.profile_picture) // Using a static placeholder
                    .circleCrop() // Apply circular cropping
                    .placeholder(R.drawable.profile_picture) // Fallback placeholder
                    .error(R.drawable.profile_picture)       // Fallback on error
                    .into(binding.ivUserAvatar);

            // Determine follow button state and visibility
            if (user.getId() != null && user.getId().equals(currentLoggedInUserUid)) {
                // It's the current user's own profile in the search results, hide follow button
                binding.btnFollow.setVisibility(View.GONE);
            } else {
                binding.btnFollow.setVisibility(View.VISIBLE);
                boolean isFollowing = currentlyFollowedUsernames.contains(user.getUsername());
                binding.btnFollow.setText(isFollowing ? R.string.Following : R.string.follow); // Use string resources

                binding.btnFollow.setOnClickListener(v -> {
                    if (listener != null) {
                        // Toggle: if currently following, the action is to unfollow (false), else follow (true)
                        listener.onFollowToggled(user, !isFollowing);
                    }
                });
            }

            // Set click listener for the entire item view to navigate to the user's profile
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUserClicked(user);
                }
            });
        }
    }
}

