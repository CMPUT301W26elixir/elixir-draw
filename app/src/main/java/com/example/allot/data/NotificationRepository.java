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

    /**
     * Creates a new NotificationRepository instance.
     */
    public NotificationRepository() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Creates a new NotificationRepository instance.
     *
     * @param db the db
     */
    public NotificationRepository(FirebaseFirestore db) {
        this.db = db;
    }

    /**
     * Performs save notification.
     *
     * @param notification the notification
     * @param listener the listener
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
     * Performs get notifications for user.
     *
     * @param userId the user id
     * @param listener the listener
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

    /**
     * Performs get all notifications.
     *
     * @param listener the listener
     */
    public void getAllNotifications(OnCompleteListener<java.util.List<NotificationItem>> listener) {
        db.collection(COLLECTION)
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
