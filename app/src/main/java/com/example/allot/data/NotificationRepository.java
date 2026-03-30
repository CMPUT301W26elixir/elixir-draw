package com.example.allot.data;

import android.util.Log;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.model.Notification;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles Firestore reads and writes for user notifications.
 */
public class NotificationRepository {
    private static final String TAG = "NotificationRepository";
    private final CollectionReference usersCollection;

    public NotificationRepository() {
        this(FirebaseFirestore.getInstance());
    }

    public NotificationRepository(FirebaseFirestore database) {
        this.usersCollection = database.collection("users");
    }

    /**
     * Sends a notification to a specific user by saving it to their notifications sub-collection.
     *
     * @param userId       the device ID of the user to notify
     * @param notification the notification details
     * @param listener     the listener that receives the result
     */
    public void sendNotification(String userId, Notification notification, OnCompleteListener<Void> listener) {
        if (userId == null || notification == null) {
            if (listener != null) listener.onComplete(null, false);
            return;
        }

        // Save to users/{userId}/notifications/{autoId}
        usersCollection.document(userId)
                .collection("notifications")
                .add(notification)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Notification sent to user: " + userId);
                        if (listener != null) listener.onComplete(null, true);
                    } else {
                        Log.e(TAG, "Failed to send notification", task.getException());
                        if (listener != null) listener.onComplete(null, false);
                    }
                });
    }

    /**
     * Fetches all notifications for a specific user, ordered by newest first.
     *
     * @param userId   the device ID of the user
     * @param listener the listener that receives the list of notifications
     */
    public void getNotifications(String userId, OnCompleteListener<List<Notification>> listener) {
        usersCollection.document(userId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Notification> notifications = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Notification notification = document.toObject(Notification.class);
                            notification.setId(document.getId());
                            notifications.add(notification);
                        }
                        listener.onComplete(notifications, true);
                    } else {
                        Log.e(TAG, "Failed to fetch notifications", task.getException());
                        listener.onComplete(null, false);
                    }
                });
    }
}
