package com.example.allot.view.organizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.scrollTo;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.allot.R;
import com.example.allot.testutil.SystemAnimations;
import com.example.allot.view.event.CreateEventActivity;
import com.example.allot.view.event.EditEventActivity;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class OrganizerUiTest {

    @Before
    public void disableAnimations() {
        SystemAnimations.disableAll();
    }

    private Intent buildEditEventIntent(boolean isPrivate) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EditEventActivity.class);
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_MODE, true);
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_EVENT_ID, "ui-test-event-1");
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_EVENT_TITLE, "Organizer Gala");
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_EVENT_LOCATION, "Downtown Hall");
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_EVENT_DATE, "Apr 12");
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_EVENT_PRICE, "15");
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_EVENT_DESCRIPTION, "Dress code required");
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_EVENT_PARTICIPANTS, "25");
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_EVENT_CATEGORY, "Arts");
        intent.putExtra(EditEventActivity.EXTRA_UI_TEST_PRIVATE_EVENT, isPrivate);
        return intent;
    }

    private Intent buildEntrantsIntent() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventEntrantsActivity.class);
        intent.putExtra(EventEntrantsActivity.EXTRA_UI_TEST_MODE, true);
        intent.putExtra(EventEntrantsActivity.EXTRA_UI_TEST_EVENT_ID, "ui-test-event-2");
        intent.putExtra(EventEntrantsActivity.EXTRA_UI_TEST_EVENT_TITLE, "Organizer Gala");
        intent.putStringArrayListExtra(EventEntrantsActivity.EXTRA_UI_TEST_SELECTED,
                new ArrayList<>(Arrays.asList("Ava", "Blake")));
        intent.putStringArrayListExtra(EventEntrantsActivity.EXTRA_UI_TEST_CANCELLED,
                new ArrayList<>(Arrays.asList("Casey")));
        intent.putStringArrayListExtra(EventEntrantsActivity.EXTRA_UI_TEST_NOT_ENROLLED,
                new ArrayList<>(Arrays.asList("Dana")));
        intent.putStringArrayListExtra(EventEntrantsActivity.EXTRA_UI_TEST_ENROLLED,
                new ArrayList<>(Arrays.asList("Elliot")));
        return intent;
    }

    @Test
    public void editEvent_showsOrganizerControls() {
        try (ActivityScenario<EditEventActivity> scenario = ActivityScenario.launch(buildEditEventIntent(true))) {
            onView(withId(R.id.summaryTitleText)).check(matches(withText("Organizer Gala")));
            onView(withId(R.id.privateEventCheckbox)).check(matches(isChecked()));
            onView(withId(R.id.inviteEntrantsButton)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.inviteCoOrganizerButton)).perform(scrollTo()).check(matches(isDisplayed()));
            onView(withId(R.id.deleteEventButton)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test
    public void editEvent_publicEventHidesInviteEntrants() {
        try (ActivityScenario<EditEventActivity> scenario = ActivityScenario.launch(buildEditEventIntent(false))) {
            onView(withId(R.id.privateEventCheckbox)).check(matches(isDisplayed()));
            onView(withId(R.id.inviteEntrantsButton))
                    .check(matches(withEffectiveVisibility(androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE)));
        }
    }

    @Test
    public void editEvent_allowsEditingFields() {
        try (ActivityScenario<EditEventActivity> scenario = ActivityScenario.launch(buildEditEventIntent(true))) {
            onView(withId(R.id.eventNameInput)).perform(replaceText("Organizer Gala Updated"), closeSoftKeyboard());
            onView(withId(R.id.descriptionInput)).perform(scrollTo(), replaceText("New description"), closeSoftKeyboard());
            onView(withId(R.id.eventNameInput)).check(matches(withText("Organizer Gala Updated")));
            onView(withId(R.id.descriptionInput)).perform(scrollTo()).check(matches(withText("New description")));
        }
    }

    @Test
    public void editEvent_deleteButtonIsClickable() {
        try (ActivityScenario<EditEventActivity> scenario = ActivityScenario.launch(buildEditEventIntent(true))) {
            onView(withId(R.id.deleteEventButton)).perform(scrollTo(), click());
            onView(withId(R.id.deleteEventButton)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test
    public void entrants_selectedTabShowsCancel() {
        try (ActivityScenario<EventEntrantsActivity> scenario = ActivityScenario.launch(buildEntrantsIntent())) {
            onView(withText("Ava")).check(matches(isDisplayed()));
            onView(withText("Ava")).perform(assertCancelForEntrant());
        }
    }

    private static androidx.test.espresso.ViewAction assertCancelForEntrant() {
        return new androidx.test.espresso.ViewAction() {
            @Override
            public org.hamcrest.Matcher<android.view.View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "assert cancel button exists for entrant row";
            }

            @Override
            public void perform(androidx.test.espresso.UiController uiController, android.view.View view) {
                android.view.View parent = (android.view.View) view.getParent();
                if (parent == null) {
                    throw new AssertionError("Entrant name has no parent");
                }
                android.view.View row = (android.view.View) parent.getParent();
                if (row == null) {
                    throw new AssertionError("Entrant row not found");
                }
                android.view.View cancelButton = row.findViewById(R.id.cancelEntrantButton);
                if (cancelButton == null || cancelButton.getVisibility() != android.view.View.VISIBLE) {
                    throw new AssertionError("Cancel button not visible in entrant row");
                }
            }
        };
    }

    @Test
    public void entrants_cancelMovesToCancelledTab() {
        try (ActivityScenario<EventEntrantsActivity> scenario = ActivityScenario.launch(buildEntrantsIntent())) {
            onView(withText("Ava")).perform(clickCancelForEntrant());
            onView(withText(R.string.manage_entrants_cancel_confirm)).perform(click());
            onView(withId(R.id.cancelledTabText)).perform(scrollTo(), click());
            onView(withText("Ava")).check(matches(isDisplayed()));
        }
    }

    private static androidx.test.espresso.ViewAction clickCancelForEntrant() {
        return new androidx.test.espresso.ViewAction() {
            @Override
            public org.hamcrest.Matcher<android.view.View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "click cancel button for entrant row";
            }

            @Override
            public void perform(androidx.test.espresso.UiController uiController, android.view.View view) {
                android.view.View parent = (android.view.View) view.getParent();
                if (parent == null) {
                    throw new AssertionError("Entrant name has no parent");
                }
                android.view.View row = (android.view.View) parent.getParent();
                if (row == null) {
                    throw new AssertionError("Entrant row not found");
                }
                android.view.View cancelButton = row.findViewById(R.id.cancelEntrantButton);
                if (cancelButton == null) {
                    throw new AssertionError("Cancel button not found in entrant row");
                }
                cancelButton.performClick();
                uiController.loopMainThreadUntilIdle();
            }
        };
    }

    @Test
    public void entrants_enrolledTabShowsExportButton() {
        try (ActivityScenario<EventEntrantsActivity> scenario = ActivityScenario.launch(buildEntrantsIntent())) {
            onView(withId(R.id.enrolledTabText)).perform(scrollTo(), click());
            onView(withId(R.id.exportFinalListButton)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void inviteCoOrganizer_screenLoads() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), InviteCoOrganizerActivity.class);
        intent.putExtra(InviteCoOrganizerActivity.EXTRA_UI_TEST_MODE, true);
        try (ActivityScenario<InviteCoOrganizerActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.searchInput)).check(matches(isDisplayed()));
            onView(withId(R.id.searchButton)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void createEvent_screenLoadsForOrganizer() {
        try (ActivityScenario<CreateEventActivity> scenario = ActivityScenario.launch(
                new Intent(ApplicationProvider.getApplicationContext(), CreateEventActivity.class))) {
            onView(withId(R.id.createEventTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.eventNameInput)).check(matches(isDisplayed()));
            onView(withId(R.id.privateEventCheckbox)).check(matches(isDisplayed()));
            onView(withId(R.id.createEventNextButton)).check(matches(isDisplayed()));
        }
    }
}
