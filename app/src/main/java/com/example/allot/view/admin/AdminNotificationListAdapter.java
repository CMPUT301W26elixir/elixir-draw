package com.example.allot.view.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.model.notification.NotificationItem;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Adapter for displaying sent entrant notifications in the admin panel.
 */
public class AdminNotificationListAdapter extends RecyclerView.Adapter<AdminNotificationListAdapter.ViewHolder> {
    private final List<NotificationItem> notifications;
    private final Map<String, String> userNamesById = new HashMap<>();

    /**
     * Creates a new AdminNotificationListAdapter instance.
     *
     * @param notifications the notifications
     */
    public AdminNotificationListAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    /**
     * Updates the user names by id.
     *
     * @param namesById the names by id
     */
    public void setUserNamesById(Map<String, String> namesById) {
        userNamesById.clear();
        if (namesById != null) {
            userNamesById.putAll(namesById);
        }
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
                .inflate(R.layout.item_admin_notification, parent, false);
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
        holder.bind(notifications.get(position), userNamesById);
    }

    /**
     * Returns the item count.
     *
     * @return the item count
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    /**
     * Represents the view holder.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView messageText;
        private final TextView userIdText;
        private final TextView eventIdText;
        private final TextView sentAtText;

        /**
         * Creates a new ViewHolder instance.
         *
         * @param itemView the item view
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.notificationTitleText);
            messageText = itemView.findViewById(R.id.notificationMessageText);
            userIdText = itemView.findViewById(R.id.notificationUserIdText);
            eventIdText = itemView.findViewById(R.id.notificationEventIdText);
            sentAtText = itemView.findViewById(R.id.notificationSentAtText);
        }

        /**
         * Performs bind.
         *
         * @param item the item
         * @param userNamesById the user names by id
         */
        void bind(NotificationItem item, Map<String, String> userNamesById) {
            String safeTitle = item == null || isBlank(item.getTitle()) ? "Notification" : item.getTitle();
            String safeMessage = item == null || isBlank(item.getMessage()) ? "No message" : item.getMessage();
            String safeUserId = item == null || isBlank(item.getUserId()) ? "Unknown" : item.getUserId();
            String safeEventId = item == null || isBlank(item.getEventId()) ? "Unknown" : item.getEventId();
            String safeUserName = userNamesById == null ? null : userNamesById.get(safeUserId);
            if (isBlank(safeUserName)) {
                safeUserName = "Unknown User";
            }

            titleText.setText(safeTitle);
            messageText.setText(safeMessage);
            userIdText.setText("User: " + safeUserName);
            eventIdText.setText("Event ID: " + safeEventId);
            sentAtText.setText("Sent: " + formatTimestamp(item));
        }

        /**
         * Returns the result of format timestamp.
         *
         * @param item the item
         * @return the result of this call
         */
        private String formatTimestamp(NotificationItem item) {
            if (item == null || item.getCreatedAt() == null || item.getCreatedAt().toDate() == null) {
                return "Unknown";
            }
            Date date = item.getCreatedAt().toDate();
            return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(date);
        }

        /**
         * Returns whether blank.
         *
         * @param value the value
         * @return whether blank
         */
        private boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }
    }
}
