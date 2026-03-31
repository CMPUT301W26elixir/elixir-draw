package com.example.allot.view;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.common.NotificationHelper;
import com.example.allot.common.TextHelper;
import com.example.allot.controller.shared.UserController;
import com.example.allot.model.profile.User;
import com.example.allot.view.explore.ExploreActivity;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;

@SuppressLint("CustomSplashScreen")
/**
 * Decides the first screen to show after the splash delay finishes.
 * Also initializes background notification listeners.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long MIN_SPLASH_DURATION_MS = 900L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startedAtMs;
    private boolean navigated;
    private static boolean notificationListenerStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startedAtMs = System.currentTimeMillis();

        UserController userController = new UserController(this);
        if (userController.isNewDeviceId()) {
            navigateAfterDelay(true);
            return;
        }

        userController.loadOrCreateUser((user, success) -> {
            if (success && user != null) {
                startNotificationListener(user.getDeviceId());
            }
            boolean requiresProfileSetup = !success || requiresProfileSetup(user);
            navigateAfterDelay(requiresProfileSetup);
        });
    }

    /**
     * Starts a real-time listener for new notifications in Firestore.
     * Triggers a system pop-up using NotificationHelper.
     *
     * @param deviceId the current user's device ID
     */
    private void startNotificationListener(String deviceId) {
        if (notificationListenerStarted || deviceId == null) {
            return;
        }
        notificationListenerStarted = true;

        NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
        long appStartTime = System.currentTimeMillis();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(deviceId)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
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

    private void navigateAfterDelay(boolean requiresProfileSetup) {
        if (navigated) {
            return;
        }

        long elapsedMs = System.currentTimeMillis() - startedAtMs;
        long remainingMs = Math.max(0L, MIN_SPLASH_DURATION_MS - elapsedMs);
        handler.postDelayed(() -> openNextScreen(requiresProfileSetup), remainingMs);
    }

    private void openNextScreen(boolean requiresProfileSetup) {
        if (navigated || isFinishing()) {
            return;
        }

        navigated = true;
        Intent intent = new Intent(
                this,
                requiresProfileSetup ? WelcomeActivity.class : ExploreActivity.class
        );
        startActivity(intent);
        finish();
    }

    private boolean requiresProfileSetup(User user) {
        if (user == null) {
            return true;
        }

        return isBlank(user.getFirstName())
                || isBlank(user.getLastName())
                || isBlank(user.getEmail());
    }

    private boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }
}
