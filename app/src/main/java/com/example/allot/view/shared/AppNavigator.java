package com.example.allot.view.shared;

import android.app.Activity;
import android.content.Intent;
import com.example.allot.view.events.UserEventsActivity;
import com.example.allot.view.explore.ExploreActivity;
import com.example.allot.view.organizer.ScanActivity;
import com.example.allot.view.profile.ProfileActivity;
public final class AppNavigator {
    private static final int NAV_FLAGS = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP;

    private AppNavigator() {
    }

    public static void openExplore(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, ExploreActivity.class), finishCurrent);
    }

    public static void openSaved(Activity activity, boolean finishCurrent) {
        Intent intent = new Intent(activity, ExploreActivity.class);
        intent.putExtra("navigate_to", "saved");
        navigate(activity, intent, finishCurrent);
    }

    public static void openMyEvents(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, UserEventsActivity.class), finishCurrent);
    }

    public static void openMyEventsHosting(Activity activity, boolean finishCurrent) {
        Intent intent = new Intent(activity, UserEventsActivity.class);
        intent.putExtra(UserEventsActivity.EXTRA_INITIAL_TAB, UserEventsActivity.INITIAL_TAB_HOSTING);
        navigate(activity, intent, finishCurrent);
    }

    public static void openProfile(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, ProfileActivity.class), finishCurrent);
    }

    public static void openScan(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, ScanActivity.class), finishCurrent);
    }

    private static void navigate(Activity activity, Intent intent, boolean finishCurrent) {
        intent.addFlags(NAV_FLAGS);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        if (finishCurrent) {
            activity.finish();
        }
    }
}









