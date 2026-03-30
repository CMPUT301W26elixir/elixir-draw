package com.example.allot.view.events;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.NotificationRepository;
import com.example.allot.model.Notification;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Displays a list of notifications sent to the current user.
 */
public class UserNotificationsActivity extends AppCompatActivity {

    private NotificationRepository notificationRepository;
    private UserController userController;
    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private NotificationAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notifications);

        notificationRepository = new NotificationRepository();
        userController = new UserController(this);

        recyclerView = findViewById(R.id.notificationsRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        String deviceId = userController.getCurrentDeviceId();
        notificationRepository.getNotifications(deviceId, (notifications, success) -> {
            if (success && notifications != null && !notifications.isEmpty()) {
                emptyStateText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter.updateNotifications(notifications);
            } else {
                emptyStateText.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
        private final List<Notification> notifications;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault());

        public NotificationAdapter(List<Notification> notifications) {
            this.notifications = notifications;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_notification, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Notification notification = notifications.get(position);
            holder.titleText.setText(notification.getTitle());
            holder.bodyText.setText(notification.getBody());
            if (notification.getTimestamp() != null) {
                holder.dateText.setText(dateFormat.format(notification.getTimestamp()));
            }
        }

        @Override
        public int getItemCount() {
            return notifications.size();
        }

        public void updateNotifications(List<Notification> newNotifications) {
            this.notifications.clear();
            this.notifications.addAll(newNotifications);
            notifyDataSetChanged();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleText, bodyText, dateText;

            ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.notificationTitleText);
                bodyText = itemView.findViewById(R.id.notificationBodyText);
                dateText = itemView.findViewById(R.id.notificationDateText);
            }
        }
    }
}
