package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.controller.UserController;
import com.google.android.material.button.MaterialButton;

public class NotificationsActivity extends AppCompatActivity {
    private UserController userController;
    private MaterialButton turnOnNotificationsButton;
    private TextView notificationsNotNow;

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

    private void saveProfileAndOpenExplore(boolean notificationsEnabled) {
        setButtonsEnabled(false);

        String firstName = getIntent().getStringExtra(NameActivity.EXTRA_FIRST_NAME);
        String lastName = getIntent().getStringExtra(NameActivity.EXTRA_LAST_NAME);
        String email = getIntent().getStringExtra(NameActivity.EXTRA_EMAIL);
        String phone = getIntent().getStringExtra(NameActivity.EXTRA_PHONE);

        userController.updateUserProfile(firstName, lastName, email, phone, notificationsEnabled,
                (user, success) -> {
                    if (success && user != null) {
                        Intent intent = new Intent(NotificationsActivity.this, MainActivity.class);
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

    private void setButtonsEnabled(boolean enabled) {
        turnOnNotificationsButton.setEnabled(enabled);
        notificationsNotNow.setEnabled(enabled);
        notificationsNotNow.setAlpha(enabled ? 1f : 0.6f);
    }
}
