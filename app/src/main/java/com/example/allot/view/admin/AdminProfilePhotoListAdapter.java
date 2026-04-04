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
 * Adapter for displaying uploaded profile photos in the admin panel.
 */
public class AdminProfilePhotoListAdapter extends RecyclerView.Adapter<AdminProfilePhotoListAdapter.ViewHolder> {
    private final List<User> users;

    public AdminProfilePhotoListAdapter(List<User> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profilePhotoImage;
        private final TextView userNameText;
        private final TextView userEmailText;
        private final TextView userDeviceIdText;
        private final TextView userRoleText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePhotoImage = itemView.findViewById(R.id.profilePhotoImage);
            userNameText = itemView.findViewById(R.id.userNameText);
            userEmailText = itemView.findViewById(R.id.userEmailText);
            userDeviceIdText = itemView.findViewById(R.id.userDeviceIdText);
            userRoleText = itemView.findViewById(R.id.userRoleText);
        }

        void bind(User user) {
            String displayName = user == null ? "" : user.getName();
            if (TextUtils.isEmpty(displayName)) {
                displayName = "No Name";
            }
            userNameText.setText(displayName);

            String email = user == null ? null : user.getEmail();
            userEmailText.setText("Email: " + (TextUtils.isEmpty(email) ? "No Email" : email));

            String deviceId = user == null ? null : user.getDeviceId();
            userDeviceIdText.setText("Device ID: " + (TextUtils.isEmpty(deviceId) ? "Unknown" : deviceId));

            String role = user == null ? null : user.getRole();
            userRoleText.setText("Role: " + (TextUtils.isEmpty(role) ? "user" : role));

            Glide.with(itemView.getContext())
                    .load(user == null ? null : user.getProfilePhotoUrl())
                    .placeholder(R.drawable.bg_profile_avatar_placeholder)
                    .error(R.drawable.bg_profile_avatar_placeholder)
                    .centerCrop()
                    .into(profilePhotoImage);
        }
    }
}