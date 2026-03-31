package com.example.allot.data;

import com.example.allot.model.notification.NotificationItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.allot.common.OnCompleteListener;

/**
 * Handles Firestore reads and writes for notifications.
 */
public class NotificationRepository {
    private static final String COLLECTION = "notifications";
    private final FirebaseFirestore db;

    public NotificationRepository() {
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     * Saves a notification to Firestore for a specific user.
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
     * Fetches all notifications for a given user.
     *
     * @param userId   the device ID of the user
     * @param listener called with the list of notifications on success
     */
    public void getNotificationsForUser(String userId, OnCompleteListener<java.util.List<NotificationItem>> listener) {
        db.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    java.util.List<NotificationItem> items = new java.util.ArrayList<>();
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