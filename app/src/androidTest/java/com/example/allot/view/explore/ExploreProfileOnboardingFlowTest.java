package com.example.allot.view.explore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.allot.R;
import com.example.allot.testutil.SystemAnimations;
import com.example.allot.view.shared.DeferredOnboardingNavigator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExploreProfileOnboardingFlowTest {

    @Before
    public void disableAnimations() {
        SystemAnimations.disableAll();
    }

    private Intent buildIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ExploreActivity.class);
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_ID, "ui-test-event-1");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_TITLE, "UI Test Jazz Night");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_LOCATION, "Edmonton");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_DATE, "Apr 12");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_PRICE, "$10");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_DEADLINE, "2 days left");
        intent.putExtra(ExploreActivity.EXTRA_UI_TEST_EVENT_CATEGORY, "Music");
        intent.putExtra(DeferredOnboardingNavigator.EXTRA_UI_TEST_COMPLETE_DEFERRED_ONBOARDING, true);
        return intent;
    }

    @Test
    public void exploreProfileTab_launchesDeferredOnboardingAndReturnsToProfile() {
        try (ActivityScenario<ExploreActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.searchInput)).check(matches(isDisplayed()));
            onView(withId(R.id.profileTab)).perform(click());

            onView(withId(R.id.nameTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.firstNameInput)).perform(replaceText("Jordan"), closeSoftKeyboard());
            onView(withId(R.id.lastNameInput)).perform(replaceText("Tester"), closeSoftKeyboard());
            onView(withId(R.id.nameNextButton)).perform(click());

            onView(withId(R.id.emailTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.emailInput)).perform(replaceText("jordan.tester@example.com"), closeSoftKeyboard());
            onView(withId(R.id.emailNextButton)).perform(click());

            onView(withId(R.id.phoneTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.notNowButton)).perform(click());

            onView(withId(R.id.notificationsTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.notificationsNotNow)).perform(click());

            onView(withId(R.id.profileTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.saveChangesButton)).check(matches(isDisplayed()));
        }
    }
}
