package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

/**
 * Activity that represents the scan screen of the application.
 *
 * <p>This screen sets up the bottom navigation bar and allows the user
 * to navigate to the explore, saved, my events, and profile screens.
 */
public class ScanActivity extends AppCompatActivity {
    private BottomNavBarView bottomNavBar;

    /**
     * Initializes the activity, sets the layout, binds the bottom navigation bar,
     * and configures navigation listeners.
     *
     * @param savedInstanceState the previously saved activity state, if one exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        bottomNavBar = findViewById(R.id.bottomNavBar);
        setupBottomNav();
    }

    /**
     * Finishes the activity without applying a transition animation.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Configures the bottom navigation bar and assigns click handlers
     * for switching to other app screens.
     */
    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.SCAN);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> openSavedScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> openMyEventsScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> openProfileScreen());
    }

    /**
     * Opens the explore screen by launching {@link MainActivity}.
     */
    private void openExploreScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the saved screen by launching {@link MainActivity}
     * and requesting navigation to the saved tab.
     */
    private void openSavedScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("navigate_to", "saved");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the My Events screen.
     */
    private void openMyEventsScreen() {
        Intent intent = new Intent(this, MyEventsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the profile screen.
     */
    private void openProfileScreen() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }
}