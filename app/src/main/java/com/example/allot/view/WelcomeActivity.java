package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

/**
 * Activity that serves as the welcome screen for new users.
 *
 * <p>This screen introduces the app and starts the profile setup flow
 * when the user presses the get started button.
 */
public class WelcomeActivity extends AppCompatActivity {

    /**
     * Initializes the welcome screen and sets up the get started button
     * to open the name entry screen.
     *
     * @param savedInstanceState the previously saved activity state, if one exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Button getStartedButton = findViewById(R.id.getStartedButton);
        getStartedButton.setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, NameActivity.class));
        });
    }
}