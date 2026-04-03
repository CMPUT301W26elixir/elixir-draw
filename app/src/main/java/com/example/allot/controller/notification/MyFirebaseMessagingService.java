package com.example.allot.controller.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.allot.R;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.UserRepository;
import com.example.allot.view.SplashActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Handles incoming FCM messages and token refreshes.
 * Respects the user's notification preferences stored in Firestore.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessaging";
    private static final String CHANNEL_ID = "allot_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Extract data payload
        String title = null;
        String body = null;
        String eventId = null;
        String eventTitle = null;

        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");
            eventId = remoteMessage.getData().get("event_id");
            eventTitle = remoteMessage.getData().get("event_title");
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            if (title == null) title = remoteMessage.getNotification().getTitle();
            if (body == null) body = remoteMessage.getNotification().getBody();
        }

        final String finalTitle = title;
        final String finalBody = body;
        final String finalEventId = eventId;
        final String finalEventTitle = eventTitle;

        if (finalTitle == null || finalBody == null) return;

        // Fetch user preferences before showing the notification
        DeviceSessionManager sessionManager = new DeviceSessionManager(this);
        String deviceId = sessionManager.getCurrentDeviceId();
        
        new UserRepository().getUserByDeviceId(deviceId, (user, success) -> {
            // Default to showing if user fetch fails (to ensure critical notifications arrive)
            if (success && user != null && !user.isNotiEnabled()) {
                Log.d(TAG, "Notifications are disabled for this user. Skipping.");
                return;
            }

            showNotification(finalTitle, finalBody, finalEventId, finalEventTitle);
        });
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        
        DeviceSessionManager sessionManager = new DeviceSessionManager(this);
        String deviceId = sessionManager.getCurrentDeviceId();
        if (deviceId != null) {
            new UserRepository().updateFcmToken(deviceId, token);
        }
    }

    private void showNotification(String title, String body, String eventId, String eventTitle) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Allot Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for event selections and updates");
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        if (eventId != null) {
            intent.putExtra("redirect_to", "offer");
            intent.putExtra("event_id", eventId);
            intent.putExtra("event_title", eventTitle);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}
