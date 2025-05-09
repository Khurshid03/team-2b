package com.example.astudio.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying search results of users, allowing follow/unfollow toggles.
 */
public class SearchUsersAdapter
        extends RecyclerView.Adapter<SearchUsersAdapter.UserViewHolder> {

    private final List<User> users;
    private final String myUsername;
    private final ActionListener listener;
    private List<String> followingUsernames;

    public interface ActionListener {
        /**
         * Called when the follow button is toggled.
         * @param user         the target user
         * @param shouldFollow true = follow, false = unfollow
         */
        void onFollowToggled(User user, boolean shouldFollow);

        /**
         * Called when the user item is clicked.
         */
        void onUserClicked(User user);
    }

    public SearchUsersAdapter(List<User> users,
                              String myUsername,
                              ActionListener listener) {
        this.users = users;
        this.myUsername = myUsername;
        this.listener = listener;
        this.followingUsernames = new ArrayList<>();
    }

    /**
     * Replace the current set of usernames you follow.
     */
    public void setFollowingUsernames(List<String> followedUsernames) {
        this.followingUsernames = new ArrayList<>(followedUsernames);
    }

    /**
     * Expose the current "following" list for optimistic UI updates.
     */
    public List<String> getFollowingUsernames() {
        return followingUsernames;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.username.setText(user.getUsername());
        // placeholder image
        Glide.with(holder.avatar.getContext())
                .load(R.drawable.profile_picture)
                .circleCrop()
                .into(holder.avatar);

        boolean isFollowing = followingUsernames.contains(user.getUsername());
        holder.followButton.setText(isFollowing ? "Following" : "Follow");

        holder.followButton.setOnClickListener(v -> {
            // toggle: if currently following, unfollow (false), else follow (true)
            listener.onFollowToggled(user, !isFollowing);
        });

        holder.itemView.setOnClickListener(v -> listener.onUserClicked(user));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView username;
        Button followButton;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.ivUserAvatar);
            username = itemView.findViewById(R.id.tvUsername);
            followButton = itemView.findViewById(R.id.btnFollow);
        }
    }
}