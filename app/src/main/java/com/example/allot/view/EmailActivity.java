package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

public class EmailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        Button emailNextButton = findViewById(R.id.emailNextButton);
        emailNextButton.setOnClickListener(view ->
                startActivity(new Intent(EmailActivity.this, PhoneActivity.class)));
    }
}
