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
     */
    public AdminNotificationListAdapter(List<NotificationItem> notifications) {
        this.notifications = notifications;
    }

    /**
     * Replaces the user ID to display-name mapping used by this adapter.
     */
    public void setUserNamesById(Map<String, String> namesById) {
        userNamesById.clear();
        if (namesById != null) {
            userNamesById.putAll(namesById);
        }
    }

    /**
     * Handles on Create View Holder.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_notification, parent, false);
        return new ViewHolder(view);
    }

    /**
     * Handles on Bind View Holder.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(notifications.get(position), userNamesById);
    }

    /**
     * Returns whether g.et Item Count
     */
    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView messageText;
        private final TextView userIdText;
        private final TextView eventIdText;
        private final TextView sentAtText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.notificationTitleText);
            messageText = itemView.findViewById(R.id.notificationMessageText);
            userIdText = itemView.findViewById(R.id.notificationUserIdText);
            eventIdText = itemView.findViewById(R.id.notificationEventIdText);
            sentAtText = itemView.findViewById(R.id.notificationSentAtText);
        }

        /**
         * Binds .
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
         * Handles format Timestamp.
         */
        private String formatTimestamp(NotificationItem item) {
            if (item == null || item.getCreatedAt() == null || item.getCreatedAt().toDate() == null) {
                return "Unknown";
            }
            Date date = item.getCreatedAt().toDate();
            return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(date);
        }

        /**
         * Returns whether i.s Blank
         */
        private boolean isBlank(String value) {
            return value == null || value.trim().isEmpty();
        }
    }
}
