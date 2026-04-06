package com.example.allot.controller.notification;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Sends real push notifications via FCM HTTP Legacy API.
 */
public class FcmNotificationSender {
    private static final String TAG = "FcmNotificationSender";
    
    // TODO: Replace with your actual Server Key from Firebase Console
    private static final String SERVER_KEY = "YOUR_SERVER_KEY_HERE";

    /**
     * Performs send push notification.
     *
     * @param userId the user id
     * @param title the title
     * @param body the body
     */
    public void sendPushNotification(String userId, String title, String body) {
        if (userId == null || userId.isEmpty()) return;

        FirebaseFirestore.getInstance().collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String fcmToken = documentSnapshot.getString("fcmToken");
                    if (fcmToken != null && !fcmToken.isEmpty()) {
                        triggerFcmRequest(fcmToken, title, body);
                    } else {
                        Log.w(TAG, "No FCM token found for user: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching user for FCM", e));
    }

    /**
     * Performs trigger fcm request.
     *
     * @param token the token
     * @param title the title
     * @param body the body
     */
    private void triggerFcmRequest(String token, String title, String body) {
        new Thread(() -> {
            try {
                URL url = new URL("https://fcm.googleapis.com/fcm/send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);
                conn.setDoOutput(true);

                // Build the notification payload
                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", body);
                notification.put("sound", "default");

                // Build the data payload (used by your MyFirebaseMessagingService)
                JSONObject data = new JSONObject();
                data.put("title", title);
                data.put("body", body);

                JSONObject payload = new JSONObject();
                payload.put("to", token);
                payload.put("notification", notification);
                payload.put("data", data);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "FCM Notification Sent! Response Code: " + responseCode);
            } catch (Exception e) {
                Log.e(TAG, "Error sending FCM message", e);
            }
        }).start();
    }
}
