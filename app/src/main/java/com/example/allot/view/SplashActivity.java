package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.controller.UserController;
import com.example.allot.model.User;

public class SplashActivity extends AppCompatActivity {
    private static final long MIN_SPLASH_DURATION_MS = 900L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long    startedAtMs;
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startedAtMs = System.currentTimeMillis();

        UserController userController = new UserController(this);

        if (userController.isNewDeviceId()) {
            // Brand new device — go straight to profile setup
            navigateAfterDelay(Destination.WELCOME);
            return;
        }

        userController.loadOrCreateUser((user, success) -> {
            Destination destination = resolveDestination(user, success);
            navigateAfterDelay(destination);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private Destination resolveDestination(User user, boolean success) {
        if (!success || user == null) {
            return Destination.WELCOME;
        }

        if (user.isAdmin()) {
            return Destination.ADMIN;
        }

        if (requiresProfileSetup(user)) {
            return Destination.WELCOME;
        }

        return Destination.MAIN;
    }

    private void navigateAfterDelay(Destination destination) {
        if (navigated) return;

        long elapsed   = System.currentTimeMillis() - startedAtMs;
        long remaining = Math.max(0L, MIN_SPLASH_DURATION_MS - elapsed);
        handler.postDelayed(() -> openDestination(destination), remaining);
    }

    private void openDestination(Destination destination) {
        if (navigated || isFinishing()) return;
        navigated = true;

        Class<?> target;
        switch (destination) {
            case ADMIN:   target = AdminEventListActivity.class; break;
            case WELCOME: target = WelcomeActivity.class;        break;
            default:      target = MainActivity.class;           break;
        }

        Intent intent = new Intent(this, target);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private boolean requiresProfileSetup(User user) {
        return isBlank(user.getFirstName())
                || isBlank(user.getLastName())
                || isBlank(user.getEmail());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private enum Destination {
        ADMIN,
        WELCOME,
        MAIN
    }
}