package com.example.allot.data;

import android.util.Log;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.model.Notification;
import com.example.allot.model.notification.NotificationItem;
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
    private static final String COLLECTION = "notifications";
    private final CollectionReference usersCollection;
    private final FirebaseFirestore db;

    /**
     * Creates a NotificationRepository and connects it to the default Firestore instance.
     */
    public NotificationRepository() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Creates a NotificationRepository with a provided Firestore instance.
     *
     * @param database the Firestore instance to use
     */
    public NotificationRepository(FirebaseFirestore database) {
        this.db = database;
        this.usersCollection = database.collection("users");
    }

    /**
     * Sends a notification to a specific user by saving it to their notifications sub-collection.
     * (Used by the Cancel/Draw replacement features)
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
     * Fetches all notifications for a specific user from their sub-collection.
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
                        Log.e(TAG, TAG + " Failed to fetch notifications", task.getException());
                        listener.onComplete(null, false);
                    }
                });
    }

    /**
     * Saves a notification to the top-level notifications collection.
     * (Team's added logic)
     *
     * @param notification the notification to save
     * @param listener     called with true on success, false on failure
     */
    public void saveNotification(NotificationItem notification, OnCompleteListener<Boolean> listener) {
        db.collection(COLLECTION)
                .add(notification)
                .addOnSuccessListener(docRef -> {
                    notification.setId(docRef.getId());
                    listener.onComplete(true, true);
                })
                .addOnFailureListener(e -> listener.onComplete(false, false));
    }

    /**
     * Fetches all notifications for a given user from the top-level collection.
     * (Team's added logic)
     *
     * @param userId   the device ID of the user
     * @param listener called with the list of notifications on success
     */
    public void getNotificationsForUser(String userId, OnCompleteListener<List<NotificationItem>> listener) {
        db.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<NotificationItem> items = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        NotificationItem item = doc.toObject(NotificationItem.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    }
                    listener.onComplete(items, true);
                })
                .addOnFailureListener(e -> listener.onComplete(null, false));
    }

    /**
     * Fetches all notifications for admin browsing from the top-level collection.
     *
     * @param listener called with the list of notifications on success
     */
    public void getAllNotifications(OnCompleteListener<List<NotificationItem>> listener) {
        db.collection(COLLECTION)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<NotificationItem> items = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        NotificationItem item = doc.toObject(NotificationItem.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    }
                    listener.onComplete(items, true);
                })
                .addOnFailureListener(e -> listener.onComplete(null, false));
    }
}