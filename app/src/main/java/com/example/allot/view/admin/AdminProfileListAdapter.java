package com.example.allot.view.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.model.profile.User;
import java.util.List;

/**
 * Adapter for displaying user profiles in the admin panel with delete functionality.
 */
public class AdminProfileListAdapter extends RecyclerView.Adapter<AdminProfileListAdapter.ViewHolder> {
    private final List<User> profiles;
    private final OnDeleteClickListener onDeleteClickListener;

    /**
     * Interface for handling delete button clicks.
     */
    public interface OnDeleteClickListener {
        /**
         * Called when the delete button is clicked for a profile.
         *
         * @param user the user profile to delete
         * @param position the position in the list
         */
        void onDeleteClick(User user, int position);
    }

    /**
     * Creates an AdminProfileListAdapter with the given profiles and click listener.
     *
     * @param profiles the list of user profiles to display
     * @param onDeleteClickListener the listener for delete button clicks
     */
    public AdminProfileListAdapter(List<User> profiles, OnDeleteClickListener onDeleteClickListener) {
        this.profiles = profiles;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_profile, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = profiles.get(position);
        holder.bind(user, position, onDeleteClickListener);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    /**
     * ViewHolder for admin profile list items.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView userNameText;
        private final TextView userEmailText;
        private final TextView userDeviceIdText;
        private final TextView userRoleText;
        private final Button deleteButton;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.userNameText);
            userEmailText = itemView.findViewById(R.id.userEmailText);
            userDeviceIdText = itemView.findViewById(R.id.userDeviceIdText);
            userRoleText = itemView.findViewById(R.id.userRoleText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        /**
         * Binds a user profile to the view holder.
         *
         * @param user the user profile to bind
         * @param position the position in the list
         * @param listener the delete click listener
         */
        void bind(User user, int position, OnDeleteClickListener listener) {
            String displayName = (user.getFirstName() != null && !user.getFirstName().isEmpty() ?
                    user.getFirstName() : "") +
                    (user.getLastName() != null && !user.getLastName().isEmpty() ?
                    " " + user.getLastName() : "");
            if (displayName.trim().isEmpty()) {
                displayName = "No Name";
            }
            userNameText.setText(displayName);

            String email = user.getEmail() != null && !user.getEmail().isEmpty() ?
                    user.getEmail() : "No Email";
            userEmailText.setText("Email: " + email);

            String deviceId = user.getDeviceId() != null && !user.getDeviceId().isEmpty() ?
                    user.getDeviceId() : "Unknown";
            userDeviceIdText.setText("Device ID: " + deviceId);

            String role = user.getRole() != null && !user.getRole().isEmpty() ?
                    user.getRole() : "user";
            userRoleText.setText("Role: " + role);

            deleteButton.setOnClickListener(view -> {
                if (listener != null) {
                    listener.onDeleteClick(user, position);
                }
            });
        }
    }
}
