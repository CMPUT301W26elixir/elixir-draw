package com.example.allot.view.events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.controller.shared.UserController;
import com.example.allot.view.explore.ExploreActivity;
import com.example.allot.view.profile.NameActivity;
import com.google.android.material.button.MaterialButton;
/**
 * Shows the step in the profile setup flow where notification preferences are chosen.
 */
public class NotificationsActivity extends AppCompatActivity {
    private UserController userController;
    private MaterialButton turnOnNotificationsButton;
    private TextView notificationsNotNow;

    /**
     * Initializes the activity, binds views, creates the user controller,
     * and sets click listeners for the notification preference options.
     *
     * @param savedInstanceState the previously saved activity state, if one exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        userController = new UserController(this);
        turnOnNotificationsButton = findViewById(R.id.turnOnNotificationsButton);
        notificationsNotNow = findViewById(R.id.notificationsNotNow);

        turnOnNotificationsButton.setOnClickListener(view -> saveProfileAndOpenExplore(true));
        notificationsNotNow.setOnClickListener(view -> saveProfileAndOpenExplore(false));
    }

    /**
     * Saves the user's profile information along with their notification preference,
     * then opens the main activity if the save succeeds.
     *
     * <p>If the save fails, the buttons are re-enabled and an error message is shown.
     *
     * @param notificationsEnabled true if notifications should be enabled;
     *                             false otherwise
     */
    private void saveProfileAndOpenExplore(boolean notificationsEnabled) {
        setButtonsEnabled(false);

        String firstName = getIntent().getStringExtra(NameActivity.EXTRA_FIRST_NAME);
        String lastName = getIntent().getStringExtra(NameActivity.EXTRA_LAST_NAME);
        String email = getIntent().getStringExtra(NameActivity.EXTRA_EMAIL);
        String phone = getIntent().getStringExtra(NameActivity.EXTRA_PHONE);

        userController.updateUserProfile(firstName, lastName, email, phone, notificationsEnabled,
                (user, success) -> {
                    if (success && user != null) {
                        Intent intent = new Intent(NotificationsActivity.this, ExploreActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        return;
                    }

                    setButtonsEnabled(true);
                    Toast.makeText(NotificationsActivity.this,
                            "Could not save your profile. Please try again.",
                            Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Enables or disables the notification choice controls.
     *
     * <p>When disabled, the "Not now" text is also visually dimmed.
     *
     * @param enabled true to enable the controls; false to disable them
     */
    private void setButtonsEnabled(boolean enabled) {
        turnOnNotificationsButton.setEnabled(enabled);
        notificationsNotNow.setEnabled(enabled);
        notificationsNotNow.setAlpha(enabled ? 1f : 0.6f);
    }
}








