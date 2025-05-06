package com.example.astudio.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.astudio.R;
import com.example.astudio.model.User;
import com.google.android.material.button.MaterialButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchUsersAdapter
        extends RecyclerView.Adapter<SearchUsersAdapter.VH> {

    public Set<String> getFollowingUids() {
        return followingUids;
    }

    public interface ActionListener {
        void onFollowToggled(User user, boolean shouldFollow);
        void onUserClicked(User user);
    }

    private final List<User> data;
    private final ActionListener listener;
    private final String myUid;
    private Set<String> followingUids = new HashSet<>();

    public SearchUsersAdapter(List<User> data, String myUid, ActionListener l) {
        this.data = data;
        this.myUid = myUid;
        this.listener = l;
    }

    public void setFollowingUids(List<String> uids) {
        followingUids.clear();
        followingUids.addAll(uids);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int vt) {
        View v = LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_user_search, p, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        User u = data.get(pos);
        h.tvUsername.setText(u.getUsername());
        // placeholder → use Glide if you add real URLs later
        h.ivAvatar.setImageResource(R.drawable.profile_picture);

        // hide follow button on yourself
        if (u.getId().equals(myUid)) {
            h.btnFollow.setVisibility(View.GONE);
        } else {
            h.btnFollow.setVisibility(View.VISIBLE);
            // TODO: you might check a 'following' list to set text/icon
            h.btnFollow.setText("Follow");
            h.btnFollow.setOnClickListener(x ->
                    listener.onFollowToggled(u, true)
            );
        }

        h.itemView.setOnClickListener(x ->
                listener.onUserClicked(u)
        );

        boolean isFollowing = followingUids.contains(u.getId());
        h.btnFollow.setText(isFollowing ? "Following" : "Follow");
        h.btnFollow.setOnClickListener(v ->
                listener.onFollowToggled(u, !isFollowing)
        );
    }

    @Override public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        MaterialButton btnFollow;
        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar   = itemView.findViewById(R.id.ivUserAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnFollow  = itemView.findViewById(R.id.btnFollow);
        }
    }
}