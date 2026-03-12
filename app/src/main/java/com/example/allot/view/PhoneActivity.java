package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

public class PhoneActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        EditText phoneInput = findViewById(R.id.phoneInput);
        Button notNowButton = findViewById(R.id.notNowButton);
        Button phoneNextButton = findViewById(R.id.phoneNextButton);

        notNowButton.setOnClickListener(view -> openNotificationsScreen(""));
        phoneNextButton.setOnClickListener(view ->
                openNotificationsScreen(phoneInput.getText().toString().trim()));
    }

    private void openNotificationsScreen(String phone) {
        Intent intent = new Intent(PhoneActivity.this, NotificationsActivity.class);
        intent.putExtra(NameActivity.EXTRA_FIRST_NAME, getIntent().getStringExtra(NameActivity.EXTRA_FIRST_NAME));
        intent.putExtra(NameActivity.EXTRA_LAST_NAME, getIntent().getStringExtra(NameActivity.EXTRA_LAST_NAME));
        intent.putExtra(NameActivity.EXTRA_EMAIL, getIntent().getStringExtra(NameActivity.EXTRA_EMAIL));
        intent.putExtra(NameActivity.EXTRA_PHONE, phone);
        startActivity(intent);
    }
}
