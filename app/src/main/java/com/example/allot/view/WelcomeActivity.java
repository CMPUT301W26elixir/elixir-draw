package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.view.profile.NameActivity;
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







