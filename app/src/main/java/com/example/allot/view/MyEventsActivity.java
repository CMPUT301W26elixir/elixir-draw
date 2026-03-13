package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

public class MyEventsActivity extends AppCompatActivity {
    private BottomNavBarView bottomNavBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        bottomNavBar = findViewById(R.id.bottomNavBar);
        setupBottomNav();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> openProfileScreen());
    }

    private void openExploreScreen() {
        Intent intent = new Intent(MyEventsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void openProfileScreen() {
        Intent intent = new Intent(MyEventsActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}
