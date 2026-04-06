package com.example.allot.view.explore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.allot.R;
import com.example.allot.testutil.SystemAnimations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExploreLeaveEventFlowTest {

    /**
     * Handles disable Animations.
     */
    @Before
    public void disableAnimations() {
        SystemAnimations.disableAll();
    }

    /**
     * Builds intent.
     */
    private Intent buildIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ExploreActivity.class);
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_ID, "ui-test-event-1");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_TITLE, "UI Test Jazz Night");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_LOCATION, "Edmonton");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_DATE, "Apr 12");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_PRICE, "$10");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_DEADLINE, "2 days left");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_CATEGORY, "Music");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_BYPASS_PROFILE_GATE, true);
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_SKIP_DETAIL_NETWORK_LOAD, true);
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_START_ON_WAITLIST, true);
        return intent;
    }

    /**
     * Handles explore To Event Detail To Leave Waitlist_flow Works.
     */
    @Test
    public void exploreToEventDetailToLeaveWaitlist_flowWorks() {
        try (ActivityScenario<ExploreActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withText("UI Test Jazz Night")).check(matches(isDisplayed())).perform(click());

            onView(withId(R.id.eventTitleText)).check(matches(withText("UI Test Jazz Night")));
            onView(withId(R.id.waitlistStatusText)).check(matches(isDisplayed()));
            onView(withId(R.id.joinWaitingListButton))
                    .check(matches(withText(R.string.event_detail_leave_waiting_list)))
                    .perform(click());

            onView(withId(R.id.waitlistStatusText))
                    .check(matches(withEffectiveVisibility(GONE)));
            onView(withId(R.id.joinWaitingListButton))
                    .check(matches(withText(R.string.event_detail_join_waiting_list)))
                    .check(matches(isDisplayed()));
        }
    }
}
