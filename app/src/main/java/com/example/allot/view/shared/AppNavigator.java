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

    /**
     * Creates a new AppNavigator instance.
     */
    private AppNavigator() {
    }

    /**
     * Performs open explore.
     *
     * @param activity the activity
     * @param finishCurrent the finish current
     */
    public static void openExplore(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, ExploreActivity.class), finishCurrent);
    }

    /**
     * Performs open saved.
     *
     * @param activity the activity
     * @param finishCurrent the finish current
     */
    public static void openSaved(Activity activity, boolean finishCurrent) {
        Intent intent = new Intent(activity, ExploreActivity.class);
        intent.putExtra("navigate_to", "saved");
        navigate(activity, intent, finishCurrent);
    }

    /**
     * Performs open my events.
     *
     * @param activity the activity
     * @param finishCurrent the finish current
     */
    public static void openMyEvents(Activity activity, boolean finishCurrent) {
        openAccountRequired(activity, finishCurrent, DeferredOnboardingNavigator.DESTINATION_MY_EVENTS, null);
    }

    /**
     * Performs open my events hosting.
     *
     * @param activity the activity
     * @param finishCurrent the finish current
     */
    public static void openMyEventsHosting(Activity activity, boolean finishCurrent) {
        Bundle extras = new Bundle();
        extras.putString(DeferredOnboardingNavigator.EXTRA_POST_MY_EVENTS_INITIAL_TAB,
                UserEventsActivity.INITIAL_TAB_HOSTING);
        openAccountRequired(activity, finishCurrent, DeferredOnboardingNavigator.DESTINATION_MY_EVENTS, extras);
    }

    /**
     * Performs open profile.
     *
     * @param activity the activity
     * @param finishCurrent the finish current
     */
    public static void openProfile(Activity activity, boolean finishCurrent) {
        openAccountRequired(activity, finishCurrent, DeferredOnboardingNavigator.DESTINATION_PROFILE, null);
    }

    /**
     * Performs open scan.
     *
     * @param activity the activity
     * @param finishCurrent the finish current
     */
    public static void openScan(Activity activity, boolean finishCurrent) {
        navigate(activity, new Intent(activity, ScanActivity.class), finishCurrent);
    }

    /**
     * Performs navigate.
     *
     * @param activity the activity
     * @param intent the intent
     * @param finishCurrent the finish current
     */
    private static void navigate(Activity activity, Intent intent, boolean finishCurrent) {
        intent.addFlags(NAV_FLAGS);
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        if (finishCurrent) {
            activity.finish();
        }
    }

    /**
     * Performs open account required.
     *
     * <p>AI usage note: This Javadoc was drafted with AI assistance and then reviewed
     * against the implementation by the development team.
     *
     * @param activity the activity
     * @param finishCurrent the finish current
     * @param destination the destination
     * @param extras the extras
     */
    private static void openAccountRequired(Activity activity,
                                            boolean finishCurrent,
                                            String destination,
                                            Bundle extras) {
        if (shouldForceDeferredOnboarding(activity)) {
            navigate(activity, buildOnboardingIntent(activity, destination, extras), finishCurrent);
            return;
        }

        UserController userController = new UserController(activity);
        userController.loadCurrentUser((user, success) -> {
            Intent intent;
            if (success && userController.hasCompletedProfile(user)) {
                intent = buildDestinationIntent(activity, destination, extras);
            } else {
                intent = buildOnboardingIntent(activity, destination, extras);
            }
            navigate(activity, intent, finishCurrent);
        });
    }

    /**
     * Returns the result of build destination intent.
     *
     * <p>AI usage note: This Javadoc was drafted with AI assistance and then reviewed
     * against the implementation by the development team.
     *
     * @param activity the activity
     * @param destination the destination
     * @param extras the extras
     * @return the result of this call
     */
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

    /**
     * Returns the result of build onboarding intent.
     *
     * <p>AI usage note: This Javadoc was drafted with AI assistance and then reviewed
     * against the implementation by the development team.
     *
     * @param activity the activity
     * @param destination the destination
     * @param extras the extras
     * @return the result of this call
     */
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

    /**
     * Returns whether this instance should force deferred onboarding.
     *
     * <p>AI usage note: This Javadoc was drafted with AI assistance and then reviewed
     * against the implementation by the development team.
     *
     * @param activity the activity
     * @return whether this instance should force deferred onboarding
     */
    private static boolean shouldForceDeferredOnboarding(Activity activity) {
        return activity != null
                && activity.getIntent() != null
                && activity.getIntent().getBooleanExtra(
                DeferredOnboardingNavigator.EXTRA_UI_TEST_COMPLETE_DEFERRED_ONBOARDING,
                false
        );
    }
}









