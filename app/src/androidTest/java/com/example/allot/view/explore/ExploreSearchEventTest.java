package com.example.allot.view.explore;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.allot.R;
import com.example.allot.testutil.SystemAnimations;
import java.util.ArrayList;
import java.util.Arrays;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ExploreSearchEventTest {

    @Before
    public void disableAnimations() {
        SystemAnimations.disableAll();
    }

    private Intent buildIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ExploreActivity.class);
        intent.putStringArrayListExtra(
                ExploreActivity.EXTRA_UI_TEST_EVENT_IDS,
                new ArrayList<>(Arrays.asList("ui-test-event-1", "ui-test-event-2"))
        );
        intent.putStringArrayListExtra(
                ExploreActivity.EXTRA_UI_TEST_EVENT_TITLES,
                new ArrayList<>(Arrays.asList("UI Test Jazz Night", "Community Yoga Morning"))
        );
        intent.putStringArrayListExtra(
                ExploreActivity.EXTRA_UI_TEST_EVENT_LOCATIONS,
                new ArrayList<>(Arrays.asList("Edmonton", "Calgary"))
        );
        intent.putStringArrayListExtra(
                ExploreActivity.EXTRA_UI_TEST_EVENT_DATES,
                new ArrayList<>(Arrays.asList("Apr 12", "Apr 18"))
        );
        intent.putStringArrayListExtra(
                ExploreActivity.EXTRA_UI_TEST_EVENT_PRICES,
                new ArrayList<>(Arrays.asList("$10", "Free"))
        );
        intent.putStringArrayListExtra(
                ExploreActivity.EXTRA_UI_TEST_EVENT_DEADLINES,
                new ArrayList<>(Arrays.asList("2 days left", "5 days left"))
        );
        intent.putStringArrayListExtra(
                ExploreActivity.EXTRA_UI_TEST_EVENT_CATEGORIES,
                new ArrayList<>(Arrays.asList("Music", "Wellness"))
        );
        return intent;
    }

    @Test
    public void searchFiltersVisibleEventsAndShowsEmptyState() {
        try (ActivityScenario<ExploreActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withText("UI Test Jazz Night")).check(matches(withText("UI Test Jazz Night")));
            onView(withText("Community Yoga Morning")).check(matches(withText("Community Yoga Morning")));

            onView(withId(R.id.searchInput)).perform(click(), replaceText("Yoga"), closeSoftKeyboard());
            onView(isRoot()).perform(waitFor(250));

            onView(withText("Community Yoga Morning")).check(matches(withText("Community Yoga Morning")));

            onView(withId(R.id.searchInput)).perform(click(), replaceText("Zumba"), closeSoftKeyboard());
            onView(isRoot()).perform(waitFor(250));

            onView(withId(R.id.stateText)).check(matches(withText("No events match \"Zumba\".")));
        }
    }

    private ViewAction waitFor(long delayMs) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for " + delayMs + " milliseconds";
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (delayMs < 0) {
                    throw new PerformException.Builder()
                            .withActionDescription(getDescription())
                            .withViewDescription(String.valueOf(view))
                            .build();
                }
                SystemClock.sleep(delayMs);
                uiController.loopMainThreadUntilIdle();
            }
        };
    }
}
