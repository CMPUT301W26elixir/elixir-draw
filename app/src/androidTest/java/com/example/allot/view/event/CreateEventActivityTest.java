package com.example.allot.view.event;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.allot.R;
import com.example.allot.view.events.UserEventsActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CreateEventActivityTest {

    @Test
    public void launchCreateEventActivity_displaysFormWithoutCrash() {
        try (ActivityScenario<CreateEventActivity> scenario =
                     ActivityScenario.launch(CreateEventActivity.class)) {
            onView(withId(R.id.createEventTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.locationInput)).check(matches(isDisplayed()));
            onView(withId(R.id.createEventNextButton)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void createEventButtonFromHostingTab_opensCreateEventActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), UserEventsActivity.class);
        intent.putExtra(UserEventsActivity.EXTRA_INITIAL_TAB, UserEventsActivity.INITIAL_TAB_HOSTING);

        try (ActivityScenario<UserEventsActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.createEventButton)).check(matches(isDisplayed()));
            onView(withId(R.id.createEventButton)).perform(click());

            onView(withId(R.id.createEventTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.locationInput)).check(matches(isDisplayed()));
        }
    }
}
