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
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
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
     * Performs open notifications screen.
     *
     * @param phone the phone
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








