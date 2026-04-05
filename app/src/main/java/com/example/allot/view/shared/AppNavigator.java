package com.example.allot.view.shared;

import android.app.Activity;
import android.content.Intent;
import com.example.allot.view.events.UserEventsActivity;
import com.example.allot.view.explore.ExploreActivity;
import com.example.allot.view.organizer.ScanActivity;
import com.example.allot.view.profile.ProfileActivity;

/**
 * Holds simple screen moves used by the app's shared bottom bar.
 */
public final class AppNavigator {
    private static final int NAV_FLAGS = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP;

    private AppNavigator() {
    }

    /**
     * Opens the explore screen.
     *
     * @param activity the current activity
     * @param finishCurrent true when the current activity should be finished
     */
    public static void openExplore(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, ExploreActivity.class), finishCurrent);
    }

    /**
     * Opens the saved-events tab inside the explore screen.
     *
     * @param activity the current activity
     * @param finishCurrent true when the current activity should be finished
     */
    public static void openSaved(Activity activity, boolean finishCurrent) {
        Intent intent = new Intent(activity, ExploreActivity.class);
        intent.putExtra("navigate_to", "saved");
        navigate(activity, intent, finishCurrent);
    }

    /**
     * Opens the My Events screen.
     *
     * @param activity the current activity
     * @param finishCurrent true when the current activity should be finished
     */
    public static void openMyEvents(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, UserEventsActivity.class), finishCurrent);
    }

    /**
     * Opens the My Events screen on the hosting tab.
     *
     * @param activity the current activity
     * @param finishCurrent true when the current activity should be finished
     */
    public static void openMyEventsHosting(Activity activity, boolean finishCurrent) {
        Intent intent = new Intent(activity, UserEventsActivity.class);
        intent.putExtra(UserEventsActivity.EXTRA_INITIAL_TAB, UserEventsActivity.INITIAL_TAB_HOSTING);
        navigate(activity, intent, finishCurrent);
    }

    /**
     * Opens the profile screen.
     *
     * @param activity the current activity
     * @param finishCurrent true when the current activity should be finished
     */
    public static void openProfile(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, ProfileActivity.class), finishCurrent);
    }

    /**
     * Opens the scan screen.
     *
     * @param activity the current activity
     * @param finishCurrent true when the current activity should be finished
     */
    public static void openScan(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, ScanActivity.class), finishCurrent);
    }

    /**
     * Applies shared navigation flags and transitions before opening a new screen.
     *
     * @param activity the current activity
     * @param intent the intent used to open the next screen
     * @param finishCurrent true when the current activity should be finished
     */
    private static void navigate(Activity activity, Intent intent, boolean finishCurrent) {
        intent.addFlags(NAV_FLAGS);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        if (finishCurrent) {
            activity.finish();
        }
    }
}









