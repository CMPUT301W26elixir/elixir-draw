package com.example.allot.view.shared;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.events.UserEventsActivity;
import com.example.allot.view.explore.ExploreActivity;
import com.example.allot.view.profile.NameActivity;
import com.example.allot.view.profile.ProfileActivity;

/**
 * Builds deferred onboarding intents and post-onboarding destinations.
 */
public final class DeferredOnboardingNavigator {
    public static final String EXTRA_UI_TEST_COMPLETE_DEFERRED_ONBOARDING =
            "ui_test_complete_deferred_onboarding";
    public static final String EXTRA_POST_ONBOARDING_DESTINATION = "post_onboarding_destination";
    public static final String EXTRA_POST_ONBOARDING_ACTION = "post_onboarding_action";
    public static final String EXTRA_POST_MY_EVENTS_INITIAL_TAB = "post_my_events_initial_tab";
    public static final String EXTRA_POST_EVENT_ID = "post_event_id";
    public static final String EXTRA_POST_EVENT_TITLE = "post_event_title";
    public static final String EXTRA_POST_EVENT_LOCATION = "post_event_location";
    public static final String EXTRA_POST_EVENT_DATE = "post_event_date";
    public static final String EXTRA_POST_EVENT_PRICE = "post_event_price";
    public static final String EXTRA_POST_EVENT_DEADLINE = "post_event_deadline";
    public static final String EXTRA_POST_EVENT_CATEGORY = "post_event_category";

    public static final String DESTINATION_EXPLORE = "explore";
    public static final String DESTINATION_PROFILE = "profile";
    public static final String DESTINATION_MY_EVENTS = "my_events";
    public static final String DESTINATION_EVENT_DETAIL = "event_detail";

    public static final String ACTION_NONE = "none";
    public static final String ACTION_AUTO_JOIN = "auto_join";
    public static final String ACTION_AUTO_SAVE = "auto_save";

    /**
     * Creates a new DeferredOnboardingNavigator instance.
     */
    private DeferredOnboardingNavigator() {
    }

    /**
     * Returns the result of create intent.
     *
     * @param context the context
     * @param destination the destination
     * @return the result of this call
     */
    public static Intent createIntent(Context context, String destination) {
        Intent intent = new Intent(context, NameActivity.class);
        intent.putExtra(EXTRA_POST_ONBOARDING_DESTINATION, destination);
        return intent;
    }

    /**
     * Returns the result of create my events intent.
     *
     * @param context the context
     * @param initialTab the initial tab
     * @return the result of this call
     */
    public static Intent createMyEventsIntent(Context context, String initialTab) {
        Intent intent = createIntent(context, DESTINATION_MY_EVENTS);
        if (initialTab != null) {
            intent.putExtra(EXTRA_POST_MY_EVENTS_INITIAL_TAB, initialTab);
        }
        return intent;
    }

    /**
     * Returns the result of create event action intent.
     *
     * @param context the context
     * @param eventId the event id
     * @param title the title
     * @param location the location
     * @param date the date
     * @param price the price
     * @param deadline the deadline
     * @param category the category
     * @param action the action
     * @return the result of this call
     */
    public static Intent createEventActionIntent(Context context,
                                                 String eventId,
                                                 String title,
                                                 String location,
                                                 String date,
                                                 String price,
                                                 String deadline,
                                                 String category,
                                                 String action) {
        Intent intent = createIntent(context, DESTINATION_EVENT_DETAIL);
        intent.putExtra(EXTRA_POST_ONBOARDING_ACTION, action);
        intent.putExtra(EXTRA_POST_EVENT_ID, eventId);
        intent.putExtra(EXTRA_POST_EVENT_TITLE, title);
        intent.putExtra(EXTRA_POST_EVENT_LOCATION, location);
        intent.putExtra(EXTRA_POST_EVENT_DATE, date);
        intent.putExtra(EXTRA_POST_EVENT_PRICE, price);
        intent.putExtra(EXTRA_POST_EVENT_DEADLINE, deadline);
        intent.putExtra(EXTRA_POST_EVENT_CATEGORY, category);
        return intent;
    }

    /**
     * Performs copy deferred extras.
     *
     * @param source the source
     * @param target the target
     */
    public static void copyDeferredExtras(Intent source, Intent target) {
        if (source == null || target == null) {
            return;
        }

        copyStringExtra(source, target, EXTRA_POST_ONBOARDING_DESTINATION);
        copyStringExtra(source, target, EXTRA_POST_ONBOARDING_ACTION);
        copyStringExtra(source, target, EXTRA_POST_MY_EVENTS_INITIAL_TAB);
        copyStringExtra(source, target, EXTRA_POST_EVENT_ID);
        copyStringExtra(source, target, EXTRA_POST_EVENT_TITLE);
        copyStringExtra(source, target, EXTRA_POST_EVENT_LOCATION);
        copyStringExtra(source, target, EXTRA_POST_EVENT_DATE);
        copyStringExtra(source, target, EXTRA_POST_EVENT_PRICE);
        copyStringExtra(source, target, EXTRA_POST_EVENT_DEADLINE);
        copyStringExtra(source, target, EXTRA_POST_EVENT_CATEGORY);
        copyBooleanExtra(source, target, EXTRA_UI_TEST_COMPLETE_DEFERRED_ONBOARDING);
    }

    /**
     * Returns the result of build post onboarding intent.
     *
     * @param context the context
     * @param sourceIntent the source intent
     * @return the result of this call
     */
    public static Intent buildPostOnboardingIntent(Context context, Intent sourceIntent) {
        String destination = sourceIntent == null
                ? DESTINATION_EXPLORE
                : sourceIntent.getStringExtra(EXTRA_POST_ONBOARDING_DESTINATION);

        if (DESTINATION_PROFILE.equals(destination)) {
            return new Intent(context, ProfileActivity.class);
        }

        if (DESTINATION_MY_EVENTS.equals(destination)) {
            Intent intent = new Intent(context, UserEventsActivity.class);
            String initialTab = sourceIntent.getStringExtra(EXTRA_POST_MY_EVENTS_INITIAL_TAB);
            if (initialTab != null) {
                intent.putExtra(UserEventsActivity.EXTRA_INITIAL_TAB, initialTab);
            }
            return intent;
        }

        if (DESTINATION_EVENT_DETAIL.equals(destination)) {
            Intent intent = new Intent(context, EventDetailActivity.class);
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, sourceIntent.getStringExtra(EXTRA_POST_EVENT_ID));
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, sourceIntent.getStringExtra(EXTRA_POST_EVENT_TITLE));
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, sourceIntent.getStringExtra(EXTRA_POST_EVENT_LOCATION));
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE, sourceIntent.getStringExtra(EXTRA_POST_EVENT_DATE));
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE, sourceIntent.getStringExtra(EXTRA_POST_EVENT_PRICE));
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE, sourceIntent.getStringExtra(EXTRA_POST_EVENT_DEADLINE));
            intent.putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, sourceIntent.getStringExtra(EXTRA_POST_EVENT_CATEGORY));
            String action = sourceIntent.getStringExtra(EXTRA_POST_ONBOARDING_ACTION);
            intent.putExtra(EventDetailActivity.EXTRA_AUTO_OPEN_JOIN_DIALOG, ACTION_AUTO_JOIN.equals(action));
            intent.putExtra(EventDetailActivity.EXTRA_AUTO_SAVE_EVENT, ACTION_AUTO_SAVE.equals(action));
            return intent;
        }

        return new Intent(context, ExploreActivity.class);
    }

    /**
     * Performs open onboarding.
     *
     * @param activity the activity
     * @param intent the intent
     * @param finishCurrent the finish current
     */
    public static void openOnboarding(Activity activity, Intent intent, boolean finishCurrent) {
        if (activity == null || intent == null) {
            return;
        }

        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
        if (finishCurrent) {
            activity.finish();
        }
    }

    /**
     * Performs copy string extra.
     *
     * @param source the source
     * @param target the target
     * @param key the key
     */
    private static void copyStringExtra(Intent source, Intent target, String key) {
        if (source.hasExtra(key)) {
            target.putExtra(key, source.getStringExtra(key));
        }
    }

    /**
     * Performs copy boolean extra.
     *
     * @param source the source
     * @param target the target
     * @param key the key
     */
    private static void copyBooleanExtra(Intent source, Intent target, String key) {
        if (source.hasExtra(key)) {
            target.putExtra(key, source.getBooleanExtra(key, false));
        }
    }
}
