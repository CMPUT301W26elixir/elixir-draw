package com.example.allot.view.organizer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
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
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> AppNavigator.openExplore(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> AppNavigator.openSaved(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> AppNavigator.openMyEvents(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> AppNavigator.openProfile(this, true));
    }
}









