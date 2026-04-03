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
import com.example.allot.common.TextHelper;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.UserRepository;
import com.example.allot.model.profile.User;
import com.example.allot.view.explore.ExploreActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.messaging.FirebaseMessaging;

@SuppressLint("CustomSplashScreen")
/**
 * Decides the first screen to show after the splash delay finishes.
 * Also handles notification permissions and FCM token registration.
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long MIN_SPLASH_DURATION_MS = 900L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startedAtMs;
    private boolean navigated;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // Permission handled
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startedAtMs = System.currentTimeMillis();

        if (checkPlayServices()) {
            askNotificationPermission();
            initApp();
        }
    }

    private void initApp() {
        UserController userController = new UserController(this);
        if (userController.isNewDeviceId()) {
            navigateAfterDelay(true);
            return;
        }

        userController.loadOrCreateUser((user, success) -> {
            if (success && user != null) {
                registerFcmToken(user.getDeviceId());
            }
            boolean requiresProfileSetup = !success || requiresProfileSetup(user);
            navigateAfterDelay(requiresProfileSetup);
        });
    }

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

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    private void registerFcmToken(String deviceId) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                return;
            }

            String token = task.getResult();
            Log.d(TAG, "FCM Token: " + token);
            
            new UserRepository().updateFcmToken(deviceId, token);
        });
    }

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
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
