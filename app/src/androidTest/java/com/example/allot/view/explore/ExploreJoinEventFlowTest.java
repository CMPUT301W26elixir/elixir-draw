package com.example.allot.view.explore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
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
public class ExploreJoinEventFlowTest {

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
        return intent;
    }

    /**
     * Handles explore To Event Detail To Join Dialog_flow Works.
     */
    @Test
    public void exploreToEventDetailToJoinDialog_flowWorks() {
        try (ActivityScenario<ExploreActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.searchInput)).check(matches(isDisplayed()));
            onView(withText("UI Test Jazz Night")).check(matches(isDisplayed())).perform(click());

            onView(withId(R.id.eventTitleText)).check(matches(withText("UI Test Jazz Night")));
            onView(withId(R.id.commentInputText)).perform(closeSoftKeyboard());
            onView(withId(R.id.joinWaitingListButton)).check(matches(isDisplayed())).perform(click());

            onView(withId(R.id.eligibilityBodyText)).inRoot(isDialog()).check(matches(isDisplayed()));
            onView(withId(R.id.selectionBodyText)).inRoot(isDialog()).check(matches(isDisplayed()));
            onView(withId(R.id.confirmJoinWaitlistButton)).inRoot(isDialog()).check(matches(isDisplayed()));
        }
    }
}
