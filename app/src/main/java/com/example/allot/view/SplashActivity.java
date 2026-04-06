package com.example.allot.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.UserRepository;
import com.example.allot.view.explore.ExploreActivity;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Entry point for the application.
 * Manages notification permissions and directs the user to the correct starting screen.
 */
public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Log.d(TAG, "Notification permission granted");
                } else {
                    Log.d(TAG, "Notification permission denied");
                }
                proceedToApp();
            });

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Using a themed splash screen, no layout needed

        askNotificationPermission();
    }

    /**
     * Performs ask notification permission.
     */
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                proceedToApp();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            proceedToApp();
        }
    }

    /**
     * Performs proceed to app.
     */
    private void proceedToApp() {
        new Handler().postDelayed(() -> {
            DeviceSessionManager sessionManager = new DeviceSessionManager(this);
            String deviceId = sessionManager.getCurrentDeviceId();

            // Refresh FCM token on startup
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    String token = task.getResult();
                    Log.d(TAG, "Current FCM token: " + token);
                    if (deviceId != null) {
                        new UserRepository().updateFcmToken(deviceId, token);
                    }
                }
            });

            if (sessionManager.isNewDeviceId()) {
                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, ExploreActivity.class));
            }
            finish();
        }, 1500);
    }
}
