package com.example.allot.view.admin;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.allOf;
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
public class AdminUiTest {

    @Before
    public void disableAnimations() {
        SystemAnimations.disableAll();
    }

    private Intent buildIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminActivity.class);
        intent.putExtra(AdminActivity.EXTRA_UI_TEST_MODE, true);
        return intent;
    }

    @Test
    public void adminEventsTab_showsEventRow() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withText("Admin Event One")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adminProfilesTab_showsProfileRow() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.profilesTabText)).perform(scrollTo(), click());
            onView(withText("Admin User")).check(matches(isDisplayed()));
            onView(withText("Email: admin@example.com")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adminProfilePicsTab_showsProfilePictureRow() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.profilePicTabText)).perform(scrollTo(), click());
            onView(withText("Photo User")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adminNotificationsTab_showsNotificationRow() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.notificationsTabText)).perform(scrollTo(), click());
            onView(withText("System Notice")).check(matches(isDisplayed()));
            onView(withText("User: Admin User")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adminPostersTab_showsPosterRow() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.postersTabText)).perform(scrollTo(), click());
            onView(withText("Poster Event")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adminTabs_switchVisibility() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.eventsContainer))
                    .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
            onView(withId(R.id.profilesTabText)).perform(scrollTo(), click());
            onView(withId(R.id.eventsContainer))
                    .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)));
            onView(withId(R.id.profilesContainer))
                    .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE)));
        }
    }

    @Test
    public void adminEventsTab_deleteEventShowsEmptyState() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(allOf(withId(R.id.deleteButton), isDisplayed())).perform(click());
            onView(withId(R.id.deleteEventButton)).perform(click());
            onView(withText(R.string.admin_no_events)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adminProfilesTab_deleteProfileShowsEmptyState() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.profilesTabText)).perform(scrollTo(), click());
            onView(allOf(withId(R.id.deleteButton), isDisplayed())).perform(click());
            onView(withId(R.id.deleteProfileButton)).perform(click());
            onView(withText(R.string.admin_no_profiles)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void adminPostersTab_deletePosterShowsEmptyState() {
        try (ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.postersTabText)).perform(scrollTo(), click());
            onView(allOf(withId(R.id.deleteButton), isDisplayed())).perform(click());
            onView(withText(R.string.admin_no_posters)).check(matches(isDisplayed()));
        }
    }
}
