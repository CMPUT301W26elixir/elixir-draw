package com.example.allot;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EntrantController entrantController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize our Entrant Controller
        entrantController = new EntrantController(this);

        // 2. Identify the person using the app
        entrantController.getOrCreateEntrant(new EntrantController.EntrantCallback() {
            @Override
            public void onCallback(Entrant entrant) {
                // Now we have the person's info!
                Log.d("Allot_Logic", "Welcome, " + entrant.getName());
                Log.d("Allot_Logic", "Role: " + entrant.getRole());
            }
        });
    }
}