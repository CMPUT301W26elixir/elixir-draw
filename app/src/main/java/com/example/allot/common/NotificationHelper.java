package com.example.allot.common;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.allot.R;

/**
 * Helper class to manage and display Android system notifications.
 */
public class NotificationHelper {
    private static final String CHANNEL_ID = "allot_notifications";
    private static final String CHANNEL_NAME = "Allot Alerts";
    private static final String CHANNEL_DESC = "Notifications for lottery results and event updates";

    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    /**
     * Creates the notification channel required for Android 8.0+.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Displays a system notification pop-up.
     *
     * @param title the title of the notification
     * @param body  the body text of the notification
     */
    public void showNotification(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Use app icon
                .setContentTitle(title)
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            // Use a unique ID for each notification (timestamp works well)
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}
