package com.example.allot.view.shared;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.example.allot.controller.shared.UserController;
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
        openAccountRequired(activity, finishCurrent, DeferredOnboardingNavigator.DESTINATION_MY_EVENTS, null);
    }

    /**
     * Opens the My Events screen on the hosting tab.
     *
     * @param activity the current activity
     * @param finishCurrent true when the current activity should be finished
     */
    public static void openMyEventsHosting(Activity activity, boolean finishCurrent) {
        Bundle extras = new Bundle();
        extras.putString(DeferredOnboardingNavigator.EXTRA_POST_MY_EVENTS_INITIAL_TAB,
                UserEventsActivity.INITIAL_TAB_HOSTING);
        openAccountRequired(activity, finishCurrent, DeferredOnboardingNavigator.DESTINATION_MY_EVENTS, extras);
    }

    /**
     * Opens the profile screen.
     *
     * @param activity the current activity
     * @param finishCurrent true when the current activity should be finished
     */
    public static void openProfile(Activity activity, boolean finishCurrent) {
        openAccountRequired(activity, finishCurrent, DeferredOnboardingNavigator.DESTINATION_PROFILE, null);
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

    private static void openAccountRequired(Activity activity,
                                            boolean finishCurrent,
                                            String destination,
                                            Bundle extras) {
        UserController userController = new UserController(activity);
        userController.loadCurrentUser((user, success) -> {
            Intent intent;
            if (shouldForceDeferredOnboarding(activity)) {
                intent = buildOnboardingIntent(activity, destination, extras);
            } else if (success && userController.hasCompletedProfile(user)) {
                intent = buildDestinationIntent(activity, destination, extras);
            } else {
                intent = buildOnboardingIntent(activity, destination, extras);
            }
            navigate(activity, intent, finishCurrent);
        });
    }

    private static Intent buildDestinationIntent(Activity activity, String destination, Bundle extras) {
        if (DeferredOnboardingNavigator.DESTINATION_PROFILE.equals(destination)) {
            return new Intent(activity, ProfileActivity.class);
        }

        Intent intent = new Intent(activity, UserEventsActivity.class);
        if (extras != null) {
            String initialTab = extras.getString(DeferredOnboardingNavigator.EXTRA_POST_MY_EVENTS_INITIAL_TAB);
            if (initialTab != null) {
                intent.putExtra(UserEventsActivity.EXTRA_INITIAL_TAB, initialTab);
            }
        }
        return intent;
    }

    private static Intent buildOnboardingIntent(Activity activity, String destination, Bundle extras) {
        Intent intent;
        if (DeferredOnboardingNavigator.DESTINATION_MY_EVENTS.equals(destination)) {
            String initialTab = extras == null
                    ? null
                    : extras.getString(DeferredOnboardingNavigator.EXTRA_POST_MY_EVENTS_INITIAL_TAB);
            intent = DeferredOnboardingNavigator.createMyEventsIntent(activity, initialTab);
        } else {
            intent = DeferredOnboardingNavigator.createIntent(activity, destination);
        }

        if (shouldForceDeferredOnboarding(activity)) {
            intent.putExtra(DeferredOnboardingNavigator.EXTRA_UI_TEST_COMPLETE_DEFERRED_ONBOARDING, true);
        }
        return intent;
    }

    private static boolean shouldForceDeferredOnboarding(Activity activity) {
        return activity != null
                && activity.getIntent() != null
                && activity.getIntent().getBooleanExtra(
                DeferredOnboardingNavigator.EXTRA_UI_TEST_COMPLETE_DEFERRED_ONBOARDING,
                false
        );
    }
}









