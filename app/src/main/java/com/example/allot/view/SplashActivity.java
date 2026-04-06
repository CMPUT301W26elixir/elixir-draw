package com.example.allot.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.allot.R;
import com.example.allot.controller.shared.UserController;
import com.example.allot.view.event.OfferResponseActivity;
import com.example.allot.view.explore.ExploreActivity;
import com.google.firebase.messaging.FirebaseMessaging;

@SuppressLint("CustomSplashScreen")
/**
 * Decides the first screen to show after the splash delay finishes.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long MIN_SPLASH_DURATION_MS = 900L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startedAtMs;
    private boolean navigated;
    private boolean canOpenOfferRedirect;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // We proceed even if permission is denied, as notifications are optional
                proceedToNextStep();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startedAtMs = System.currentTimeMillis();

        UserController userController = new UserController(this);

        if (userController.isNewDeviceId()) {
            checkNotificationPermission();
            return;
        }

        userController.loadCurrentUser((user, success) -> {
            canOpenOfferRedirect = success && user != null && userController.hasCompletedProfile(user);
            if (success && user != null) {
                registerFcmToken(userController);
            }
            checkNotificationPermission();
        });
    }

    /**
     * Fetches the current FCM token and saves it to Firestore.
     * This ensures the device is reachable for push notifications.
     */
    private void registerFcmToken(UserController userController) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();
            Log.d(TAG, "Current FCM Token: " + token);

            userController.updateCurrentFcmToken(token);
        });
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                proceedToNextStep();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            proceedToNextStep();
        }
    }

    private void proceedToNextStep() {
        navigateAfterDelay();
    }

    private void navigateAfterDelay() {
        if (navigated) {
            return;
        }

        long elapsedMs = System.currentTimeMillis() - startedAtMs;
        long remainingMs = Math.max(0L, MIN_SPLASH_DURATION_MS - elapsedMs);
        handler.postDelayed(this::openNextScreen, remainingMs);
    }

    private void openNextScreen() {
        if (navigated || isFinishing()) {
            return;
        }

        navigated = true;
        
        Intent nextIntent;
        String redirectTo = getIntent().getStringExtra("redirect_to");
        
        if ("offer".equals(redirectTo) && canOpenOfferRedirect) {
            nextIntent = new Intent(this, OfferResponseActivity.class);
            nextIntent.putExtra(OfferResponseActivity.EXTRA_EVENT_ID, getIntent().getStringExtra("event_id"));
            nextIntent.putExtra(OfferResponseActivity.EXTRA_EVENT_TITLE, getIntent().getStringExtra("event_title"));
        } else {
            nextIntent = new Intent(this, ExploreActivity.class);
        }
        
        startActivity(nextIntent);
        finish();
    }
}
