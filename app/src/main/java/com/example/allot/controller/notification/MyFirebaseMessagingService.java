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
        
        // Fetch user preferences before showing the notification
        DeviceSessionManager sessionManager = new DeviceSessionManager(this);
        String deviceId = sessionManager.getCurrentDeviceId();
        
        new UserRepository().getUserByDeviceId(deviceId, (user, success) -> {
            if (success && user != null && !user.isNotiEnabled()) {
                Log.d(TAG, "Notifications are disabled for this user. Skipping.");
                return;
            }

            // Handle data payload if present
            if (remoteMessage.getData().size() > 0) {
                String title = remoteMessage.getData().get("title");
                String body = remoteMessage.getData().get("body");
                showNotification(title, body);
            }

            // Handle notification payload if present
            if (remoteMessage.getNotification() != null) {
                showNotification(remoteMessage.getNotification().getTitle(), 
                               remoteMessage.getNotification().getBody());
            }
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

    private void showNotification(String title, String body) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Allot Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }
}
