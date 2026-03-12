package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

public class NameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        Button nameNextButton = findViewById(R.id.nameNextButton);
        nameNextButton.setOnClickListener(view ->
                startActivity(new Intent(NameActivity.this, EmailActivity.class)));
    }
}
