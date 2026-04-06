package com.example.allot.view.profile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.view.events.NotificationsActivity;
import com.example.allot.view.shared.DeferredOnboardingNavigator;
/**
 * Collects the user's phone number during profile setup.
 */
public class PhoneActivity extends AppCompatActivity {

    /**
     * Initializes the activity, binds the phone input and action buttons,
     * and sets click listeners for continuing or skipping phone entry.
     *
     * @param savedInstanceState the previously saved activity state, if one exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        EditText phoneInput = findViewById(R.id.phoneInput);
        Button notNowButton = findViewById(R.id.notNowButton);
        Button phoneNextButton = findViewById(R.id.phoneNextButton);
        ImageButton backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        notNowButton.setOnClickListener(view -> openNotificationsScreen(""));
        phoneNextButton.setOnClickListener(view ->
                openNotificationsScreen(phoneInput.getText().toString().trim()));
    }

    /**
     * Opens the notifications setup screen and passes along the profile data
     * collected from previous steps, including the provided phone number.
     *
     * @param phone the phone number entered by the user, or an empty string if skipped
     */
    private void openNotificationsScreen(String phone) {
        Intent intent = new Intent(PhoneActivity.this, NotificationsActivity.class);
        intent.putExtra(NameActivity.EXTRA_FIRST_NAME, getIntent().getStringExtra(NameActivity.EXTRA_FIRST_NAME));
        intent.putExtra(NameActivity.EXTRA_LAST_NAME, getIntent().getStringExtra(NameActivity.EXTRA_LAST_NAME));
        intent.putExtra(NameActivity.EXTRA_EMAIL, getIntent().getStringExtra(NameActivity.EXTRA_EMAIL));
        intent.putExtra(NameActivity.EXTRA_PHONE, phone);
        DeferredOnboardingNavigator.copyDeferredExtras(getIntent(), intent);
        startActivity(intent);
    }
}








