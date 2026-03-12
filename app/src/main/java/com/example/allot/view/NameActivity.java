package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

public class NameActivity extends AppCompatActivity {
    public static final String EXTRA_FIRST_NAME = "com.example.allot.EXTRA_FIRST_NAME";
    public static final String EXTRA_LAST_NAME = "com.example.allot.EXTRA_LAST_NAME";
    public static final String EXTRA_EMAIL = "com.example.allot.EXTRA_EMAIL";
    public static final String EXTRA_PHONE = "com.example.allot.EXTRA_PHONE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        EditText firstNameInput = findViewById(R.id.firstNameInput);
        EditText lastNameInput = findViewById(R.id.lastNameInput);
        Button nameNextButton = findViewById(R.id.nameNextButton);

        nameNextButton.setOnClickListener(view -> {
            String firstName = firstNameInput.getText().toString().trim();
            String lastName = lastNameInput.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, "Enter your first and last name.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(NameActivity.this, EmailActivity.class);
            intent.putExtra(EXTRA_FIRST_NAME, firstName);
            intent.putExtra(EXTRA_LAST_NAME, lastName);
            startActivity(intent);
        });
    }
}
