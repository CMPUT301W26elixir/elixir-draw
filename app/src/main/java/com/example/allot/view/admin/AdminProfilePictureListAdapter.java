package com.example.allot.view.admin;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.allot.R;
import com.example.allot.model.profile.User;
import java.util.List;

/**
 * Adapter for displaying uploaded profile pictures in the admin panel.
 */
public class AdminProfilePictureListAdapter extends RecyclerView.Adapter<AdminProfilePictureListAdapter.ViewHolder> {
    private final List<User> users;

    /**
     * Creates a new AdminProfilePictureListAdapter instance.
     *
     * @param users the users
     */
    public AdminProfilePictureListAdapter(List<User> users) {
        this.users = users;
    }

    /**
     * Returns the result of on create view holder.
     *
     * @param parent the parent
     * @param viewType the view type
     * @return the result of this call
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile_picture, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Handles the bind view holder callback.
     *
     * @param holder the holder
     * @param position the position
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    /**
     * Returns the item count.
     *
     * @return the item count
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profilePictureImage;
        private final TextView userNameText;
        private final TextView userDeviceIdText;

        /**
         * Creates a new ViewHolder instance.
         *
         * @param itemView the item view
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePictureImage = itemView.findViewById(R.id.profilePictureImage);
            userNameText = itemView.findViewById(R.id.userNameText);
            userDeviceIdText = itemView.findViewById(R.id.userDeviceIdText);
        }

        /**
         * Performs bind.
         *
         * @param user the user
         */
        void bind(User user) {
            String displayName = user == null ? null : user.getName();
            if (TextUtils.isEmpty(displayName)) {
                displayName = "No Name";
            }
            userNameText.setText(displayName);

            String deviceId = user == null ? null : user.getDeviceId();
            userDeviceIdText.setText("Device ID: " + (TextUtils.isEmpty(deviceId) ? "Unknown" : deviceId));

            Glide.with(itemView.getContext())
                    .load(user == null ? null : user.getProfilePhotoUrl())
                    .placeholder(R.drawable.bg_profile_avatar_placeholder)
                    .error(R.drawable.bg_profile_avatar_placeholder)
                    .centerCrop()
                    .into(profilePictureImage);
        }
    }
}
