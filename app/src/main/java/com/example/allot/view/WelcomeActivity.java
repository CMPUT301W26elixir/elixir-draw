package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.view.profile.NameActivity;
/**
 * Shows the welcome screen at the start of the onboarding flow.
 */
public class WelcomeActivity extends AppCompatActivity {

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
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







