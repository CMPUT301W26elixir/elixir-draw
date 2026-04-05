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
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.allot.R;
import com.example.allot.common.TextHelper;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.UserRepository;
import com.example.allot.model.profile.User;
import com.example.allot.view.event.OfferResponseActivity;
import com.example.allot.view.explore.ExploreActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.messaging.FirebaseMessaging;

@SuppressLint("CustomSplashScreen")
/**
 * Decides the first screen to show after the splash delay finishes.
 * Also handles notification permissions, FCM token registration, and Play Services checks.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long MIN_SPLASH_DURATION_MS = 900L;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startedAtMs;
    private boolean navigated;
    private boolean requiresProfileSetup;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // We proceed even if permission is denied, as notifications are optional
                proceedToNextStep();
            });

    /**
     * Initializes the activity, sets the content view, and performs initial checks.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startedAtMs = System.currentTimeMillis();

        // Ensure device supports Google Play Services before proceeding
        if (checkPlayServices()) {
            initApp();
        }
    }

    /**
     * Initializes app-specific logic such as user loading and FCM token registration.
     */
    private void initApp() {
        UserController userController = new UserController(this);

        // Ensure token is registered for push notifications
        registerFcmToken(userController.getCurrentDeviceId());

        if (userController.isNewDeviceId()) {
            this.requiresProfileSetup = true;
            checkNotificationPermission();
            return;
        }

        userController.loadOrCreateUser((user, success) -> {
            this.requiresProfileSetup = !success || requiresProfileSetup(user);
            checkNotificationPermission();
        });
    }

    /**
     * Verifies that Google Play services is available on this device.
     *
     * @return true if services are available, false otherwise
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, 9000).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Re-checks Play Services on resume to ensure continuity.
     */
    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    /**
     * Fetches the current FCM token and saves it to Firestore.
     * This ensures the device is reachable for push notifications.
     *
     * @param deviceId the unique device identifier
     */
    private void registerFcmToken(String deviceId) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            // Get new FCM registration token
            String token = task.getResult();
            Log.d(TAG, "Current FCM Token: " + token);

            // Save to Firestore for push notification routing
            new UserRepository().updateFcmToken(deviceId, token);
        });
    }

    /**
     * Checks if notification permission is granted for Android 13+ and requests it if not.
     */
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

    /**
     * Proceeds to the navigation delay step after checks are complete.
     */
    private void proceedToNextStep() {
        navigateAfterDelay(this.requiresProfileSetup);
    }

    /**
     * Delays navigation until the minimum splash duration has elapsed.
     *
     * @param requiresProfileSetup true if the user needs to complete their profile
     */
    private void navigateAfterDelay(boolean requiresProfileSetup) {
        if (navigated) {
            return;
        }

        long elapsedMs = System.currentTimeMillis() - startedAtMs;
        long remainingMs = Math.max(0L, MIN_SPLASH_DURATION_MS - elapsedMs);
        handler.postDelayed(() -> openNextScreen(requiresProfileSetup), remainingMs);
    }

    /**
     * Opens the next screen after the splash delay.
     * Handles normal app entry or deep-linked redirection from notifications.
     *
     * @param requiresProfileSetup true if profile setup is required
     */
    private void openNextScreen(boolean requiresProfileSetup) {
        if (navigated || isFinishing()) {
            return;
        }

        navigated = true;

        Intent nextIntent;
        String redirectTo = getIntent().getStringExtra("redirect_to");

        // Handle redirection for push notifications (e.g. going directly to an offer)
        if ("offer".equals(redirectTo) && !requiresProfileSetup) {
            nextIntent = new Intent(this, OfferResponseActivity.class);
            nextIntent.putExtra(OfferResponseActivity.EXTRA_EVENT_ID, getIntent().getStringExtra("event_id"));
            nextIntent.putExtra(OfferResponseActivity.EXTRA_EVENT_TITLE, getIntent().getStringExtra("event_title"));
        } else {
            nextIntent = new Intent(
                    this,
                    requiresProfileSetup ? WelcomeActivity.class : ExploreActivity.class
            );
        }

        startActivity(nextIntent);
        finish();
    }

    /**
     * Checks if the user profile is incomplete.
     *
     * @param user the user to check
     * @return true if profile setup is required, false otherwise
     */
    private boolean requiresProfileSetup(User user) {
        if (user == null) {
            return true;
        }

        return isBlank(user.getFirstName())
                || isBlank(user.getLastName())
                || isBlank(user.getEmail());
    }

    /**
     * Helper to check if a string is blank.
     */
    private boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }
}
