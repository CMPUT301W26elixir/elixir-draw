package com.example.allot.controller.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.allot.R;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.model.notification.NotificationItem;
import com.example.allot.view.SplashActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

/**
 * Listens for new notifications in Firestore for the current user 
 * and displays them as local push notifications.
 * This acts as a client-side "push" system.
 */
public class NotificationService {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "allot_notifications";
    
    private final Context context;
    private final String deviceId;
    private ListenerRegistration listenerRegistration;

    /**
     * Creates a new NotificationService instance.
     */
    public NotificationService(Context context) {
        this.context = context;
        this.deviceId = new DeviceSessionManager(context).getCurrentDeviceId();
    }

    /**
     * Starts listening for new notifications in Firestore.
     */
    public void startListening() {
        if (listenerRegistration != null) {
            Log.d(TAG, "Already listening for notifications.");
            return;
        }

        Log.d(TAG, "Starting notification listener for device: " + deviceId);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        
        // Simplified query to avoid index requirements
        Query query = db.collection("notifications")
                .whereEqualTo("userId", deviceId);

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                Log.w(TAG, "Listen failed: " + e.getMessage(), e);
                return;
            }

            if (snapshots == null) {
                Log.d(TAG, "Snapshots are null.");
                return;
            }

            Log.d(TAG, "Notification snapshot received. Count: " + snapshots.getDocumentChanges().size());

            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                if (dc.getType() == DocumentChange.Type.ADDED) {
                    NotificationItem item = dc.getDocument().toObject(NotificationItem.class);
                    if (item != null && !item.isRead()) {
                        // Check if this is a "recent" notification to avoid showing all old ones on start
                        long now = System.currentTimeMillis();
                        long createdTime = item.getCreatedAt() != null ? item.getCreatedAt().toDate().getTime() : 0;
                        
                        // If it's brand new (within last 30 seconds)
                        if (createdTime > 0 && (now - createdTime) < 30000) {
                            Log.d(TAG, "Showing notification: " + item.getTitle());
                            showLocalNotification(item);
                        } else {
                            Log.d(TAG, "Skipping old notification: " + item.getTitle() + " (age: " + (now - createdTime)/1000 + "s)");
                        }
                    }
                }
            }
        });
    }

    /**
     * Stops the Firestore listener.
     */
    public void stopListening() {
        if (listenerRegistration != null) {
            Log.d(TAG, "Stopping notification listener.");
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    /**
     * Shows local notification.
     */
    private void showLocalNotification(NotificationItem item) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Allot Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("System notifications for event selections");
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        
        // Setup redirection to Offer screen
        intent.putExtra("redirect_to", "offer");
        intent.putExtra("event_id", item.getEventId());
        intent.putExtra("event_title", "Event Update"); // Default title extra if item doesn't have it

        PendingIntent pendingIntent = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(item.getTitle())
                .setContentText(item.getMessage())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
