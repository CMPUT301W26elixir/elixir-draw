package com.example.allot.view.events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.controller.shared.UserController;
import com.example.allot.view.profile.NameActivity;
import com.example.allot.view.shared.DeferredOnboardingNavigator;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.messaging.FirebaseMessaging;
/**
 * Shows the step in the profile setup flow where notification preferences are chosen.
 */
public class NotificationsActivity extends AppCompatActivity {
    private UserController userController;
    private MaterialButton turnOnNotificationsButton;
    private TextView notificationsNotNow;

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        userController = new UserController(this);
        turnOnNotificationsButton = findViewById(R.id.turnOnNotificationsButton);
        notificationsNotNow = findViewById(R.id.notificationsNotNow);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        turnOnNotificationsButton.setOnClickListener(view -> saveProfileAndOpenExplore(true));
        notificationsNotNow.setOnClickListener(view -> saveProfileAndOpenExplore(false));
    }

    /**
     * Performs save profile and open explore.
     *
     * @param notificationsEnabled the notifications enabled
     */
    private void saveProfileAndOpenExplore(boolean notificationsEnabled) {
        setButtonsEnabled(false);

        /**
         * Returns whether get Boolean Extra.
         */
        if (getIntent().getBooleanExtra(
                DeferredOnboardingNavigator.EXTRA_UI_TEST_COMPLETE_DEFERRED_ONBOARDING,
                false
        )) {
            openDeferredDestination();
            return;
        }

        String firstName = getIntent().getStringExtra(NameActivity.EXTRA_FIRST_NAME);
        String lastName = getIntent().getStringExtra(NameActivity.EXTRA_LAST_NAME);
        String email = getIntent().getStringExtra(NameActivity.EXTRA_EMAIL);
        String phone = getIntent().getStringExtra(NameActivity.EXTRA_PHONE);

        userController.updateUserProfile(firstName, lastName, email, phone, notificationsEnabled,
                (user, success) -> {
                    if (success && user != null) {
                        FirebaseMessaging.getInstance().getToken()
                                .addOnSuccessListener(userController::updateCurrentFcmToken);
                        openDeferredDestination();
                        return;
                    }

                    setButtonsEnabled(true);
                    Toast.makeText(NotificationsActivity.this,
                            "Could not save your profile. Please try again.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Performs open deferred destination.
     */
    private void openDeferredDestination() {
        Intent intent = DeferredOnboardingNavigator.buildPostOnboardingIntent(
                NotificationsActivity.this,
                getIntent()
        );
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Updates the buttons enabled.
     *
     * @param enabled the enabled
     */
    private void setButtonsEnabled(boolean enabled) {
        turnOnNotificationsButton.setEnabled(enabled);
        notificationsNotNow.setEnabled(enabled);
        notificationsNotNow.setAlpha(enabled ? 1f : 0.6f);
    }
}








