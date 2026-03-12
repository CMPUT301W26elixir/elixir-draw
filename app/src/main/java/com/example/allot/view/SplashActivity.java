package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.controller.EntrantController;
import com.example.allot.model.Entrant;

public class SplashActivity extends AppCompatActivity {
    public static final String EXTRA_REQUIRES_PROFILE_SETUP = "com.example.allot.REQUIRES_PROFILE_SETUP";
    private static final long MIN_SPLASH_DURATION_MS = 900L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startedAtMs;
    private boolean navigated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        startedAtMs = System.currentTimeMillis();

        EntrantController entrantController = new EntrantController(this);
        if (entrantController.isNewDeviceId()) {
            navigateAfterDelay(true);
            return;
        }

        entrantController.loadOrCreateEntrant((entrant, success) -> {
            boolean requiresProfileSetup = !success || requiresProfileSetup(entrant);
            navigateAfterDelay(requiresProfileSetup);
        });
    }

    private void navigateAfterDelay(boolean requiresProfileSetup) {
        if (navigated) {
            return;
        }

        long elapsedMs = System.currentTimeMillis() - startedAtMs;
        long remainingMs = Math.max(0L, MIN_SPLASH_DURATION_MS - elapsedMs);
        handler.postDelayed(() -> openMainScreen(requiresProfileSetup), remainingMs);
    }

    private void openMainScreen(boolean requiresProfileSetup) {
        if (navigated || isFinishing()) {
            return;
        }

        navigated = true;
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_REQUIRES_PROFILE_SETUP, requiresProfileSetup);
        startActivity(intent);
        finish();
    }

    private boolean requiresProfileSetup(Entrant entrant) {
        if (entrant == null) {
            return true;
        }

        return isBlank(entrant.getFirstName())
                || isBlank(entrant.getLastName())
                || isBlank(entrant.getEmail());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
