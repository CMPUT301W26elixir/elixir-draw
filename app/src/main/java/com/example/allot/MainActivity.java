package com.example.allot;

import android.os.Bundle;
import android.util.Log; // Required for logging

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        FirestoreManager fm = new FirestoreManager(this);
        fm.checkOrCreateUser(new FirestoreManager.UserCallback() {
            @Override
            public void onUserFound(User user) {
                String userName = user.getName();
                String userId = user.getDeviceId();

                Log.d("FirebaseCheck", "User connected: " + userName + " with ID: " + userId);
            }
        });

    }
}