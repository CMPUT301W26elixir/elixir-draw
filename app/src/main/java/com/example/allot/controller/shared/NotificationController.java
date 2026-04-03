package com.example.allot.controller.shared;

import android.content.Context;
import com.example.allot.common.NotificationHelper;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import java.util.Date;

/**
 * Controller that listens for new notifications in Firestore and triggers system pop-ups.
 */
public class NotificationController {
    private final Context context;
    private final NotificationHelper notificationHelper;
    private ListenerRegistration listenerRegistration;
    private static boolean isListening = false;

    public NotificationController(Context context) {
        this.context = context;
        this.notificationHelper = new NotificationHelper(context);
    }

    /**
     * Starts listening for new notification documents added to the user's collection.
     *
     * @param deviceId the current user's device ID
     */
    public void startListening(String deviceId) {
        if (isListening || deviceId == null) {
            return;
        }
        isListening = true;

        long appStartTime = System.currentTimeMillis();

        listenerRegistration = FirebaseFirestore.getInstance()
                .collection("users")
                .document(deviceId)
                .collection("notifications")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            Date timestamp = dc.getDocument().getDate("timestamp");
                            // Only show pop-up if the notification was created after the app started
                            if (timestamp != null && timestamp.getTime() > appStartTime) {
                                String title = dc.getDocument().getString("title");
                                String body = dc.getDocument().getString("body");
                                notificationHelper.showNotification(title, body);
                            }
                        }
                    }
                });
    }

    /**
     * Stops the notification listener.
     */
    public void stopListening() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            isListening = false;
        }
    }
}
