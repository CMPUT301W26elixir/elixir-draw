package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

public class PhoneActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        Button notNowButton = findViewById(R.id.notNowButton);
        Button phoneNextButton = findViewById(R.id.phoneNextButton);

        notNowButton.setOnClickListener(view ->
                startActivity(new Intent(PhoneActivity.this, NotificationsActivity.class)));
        phoneNextButton.setOnClickListener(view ->
                startActivity(new Intent(PhoneActivity.this, NotificationsActivity.class)));
    }
}
