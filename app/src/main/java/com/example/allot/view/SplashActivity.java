package com.example.allot.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.allot.R;
import com.example.allot.common.TextHelper;
import com.example.allot.controller.shared.UserController;
import com.example.allot.model.profile.User;
import com.example.allot.view.explore.ExploreActivity;

@SuppressLint("CustomSplashScreen")
/**
 * Decides the first screen to show after the splash delay finishes.
 */
public class SplashActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startedAtMs = System.currentTimeMillis();

        UserController userController = new UserController(this);
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
        navigateAfterDelay(this.requiresProfileSetup);
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
