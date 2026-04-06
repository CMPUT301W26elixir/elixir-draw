package com.example.allot.view.profile;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
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
public class EditProfileFlowTest {

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
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ProfileActivity.class);
        intent.putExtra(ProfileActivity.EXTRA_UI_TEST_PROFILE_MODE, true);
        intent.putExtra(ProfileActivity.EXTRA_UI_TEST_FIRST_NAME, "Jordan");
        intent.putExtra(ProfileActivity.EXTRA_UI_TEST_LAST_NAME, "Tester");
        intent.putExtra(ProfileActivity.EXTRA_UI_TEST_EMAIL, "jordan@example.com");
        intent.putExtra(ProfileActivity.EXTRA_UI_TEST_PHONE, "555-0100");
        intent.putExtra(ProfileActivity.EXTRA_UI_TEST_NOTIFICATIONS_ENABLED, false);
        return intent;
    }

    /**
     * Handles edit Profile_updates Form And Clears Dirty State After Save.
     */
    @Test
    public void editProfile_updatesFormAndClearsDirtyStateAfterSave() {
        try (ActivityScenario<ProfileActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withId(R.id.profileTitle)).check(matches(isDisplayed()));
            onView(withId(R.id.firstNameInput)).check(matches(withText("Jordan")));
            onView(withId(R.id.emailInput)).check(matches(withText("jordan@example.com")));

            onView(withId(R.id.firstNameInput)).perform(replaceText("Jamie"), closeSoftKeyboard());
            onView(withId(R.id.phoneInput)).perform(replaceText("555-0199"), closeSoftKeyboard());
            onView(withId(R.id.eventUpdatesCheckbox)).perform(click());

            onView(withId(R.id.saveChangesButton)).perform(scrollTo(), closeSoftKeyboard(), click());

            onView(withId(R.id.firstNameInput)).check(matches(withText("Jamie")));
            onView(withId(R.id.phoneInput)).check(matches(withText("555-0199")));
            onView(withId(R.id.eventUpdatesCheckbox)).check(matches(isChecked()));
            onView(withId(R.id.emailInput)).check(matches(withText("jordan@example.com")));
        }
    }
}
