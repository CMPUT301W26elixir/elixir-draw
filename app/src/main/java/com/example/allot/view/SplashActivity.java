package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.common.TextHelper;
import com.example.allot.controller.shared.UserController;
import com.example.allot.model.profile.User;
import com.example.allot.view.explore.ExploreActivity;
public class SplashActivity extends AppCompatActivity {

    /**
     * Intent extra key indicating whether the user still needs to complete
     * profile setup.
     */
    public static final String EXTRA_REQUIRES_PROFILE_SETUP = "com.example.allot.REQUIRES_PROFILE_SETUP";

    /**
     * The minimum amount of time, in milliseconds, that the splash screen
     * should remain visible.
     */
    private static final long MIN_SPLASH_DURATION_MS = 900L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private long startedAtMs;
    private boolean navigated;

    /**
     * Initializes the splash screen, records the start time, checks whether
     * the device is new, and determines whether the user should be sent to
     * profile setup or the main app screen.
     *
     * @param savedInstanceState the previously saved activity state, if one exists
     */
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
            boolean requiresProfileSetup = !success || requiresProfileSetup(user);
            navigateAfterDelay(requiresProfileSetup);
        });
    }

    /**
     * Delays navigation until the minimum splash duration has elapsed.
     *
     * <p>If navigation has already occurred, this method returns immediately.
     *
     * @param requiresProfileSetup true if the next screen should be the
     *                             profile setup flow; false if the main screen
     *                             should be opened instead
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
     * Opens the next screen after the splash screen.
     *
     * <p>If profile setup is required, this method opens {@link WelcomeActivity}.
     * Otherwise, it opens {@link ExploreActivity}. Navigation only occurs once,
     * and it is skipped if the activity is already finishing.
     *
     * @param requiresProfileSetup true if the user should be sent to the
     *                             welcome/profile setup screen; false otherwise
     */
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

    /**
     * Determines whether the given user still needs to complete profile setup.
     *
     * <p>A profile is considered incomplete if the user is null or is missing
     * a first name, last name, or email address.
     *
     * @param user the user to evaluate
     * @return true if profile setup is still required; false otherwise
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
     * Determines whether a string is null, empty, or only contains whitespace.
     *
     * @param value the string to check
     * @return true if the string is blank; false otherwise
     */
    private boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }
}









