package com.example.allot.common;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.UserRepository;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Handles incoming Firebase Cloud Messaging (FCM) events.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "MyFirebaseMessaging";

    /**
     * Called when a message is received while the app is in the foreground.
     *
     * @param remoteMessage the message received from FCM
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Title: " + title);
            Log.d(TAG, "Message Notification Body: " + body);

            // Show pop-up using our existing helper
            NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
            notificationHelper.showNotification(title, body);
        }
    }

    /**
     * Called when a new FCM registration token is generated for the device.
     *
     * @param token the new registration token
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        
        // Update Firestore with the new token
        DeviceSessionManager sessionManager = new DeviceSessionManager(getApplicationContext());
        String deviceId = sessionManager.getCurrentDeviceId();
        if (deviceId != null) {
            new UserRepository().updateFcmToken(deviceId, token);
        }
    }
}
