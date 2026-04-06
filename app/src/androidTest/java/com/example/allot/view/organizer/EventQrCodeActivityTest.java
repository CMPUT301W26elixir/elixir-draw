package com.example.allot.view.organizer;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.example.allot.R;
import com.example.allot.testutil.SystemAnimations;
import com.example.allot.view.event.EventCreatedActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class EventQrCodeActivityTest {

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
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventQrCodeActivity.class);
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_ID, "ui-test-event-qr-1");
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_TITLE, "UI Test Jazz Night");
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_LOCATION, "Edmonton");
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_DATE, "Apr 12");
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_PRICE, "$10");
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_DEADLINE, "2 days left");
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_CATEGORY, "Music");
        return intent;
    }

    /**
     * Handles event Qr Screen_renders Generated Qr Code.
     */
    @Test
    public void eventQrScreen_rendersGeneratedQrCode() {
        try (ActivityScenario<EventQrCodeActivity> scenario = ActivityScenario.launch(buildIntent())) {
            onView(withText(R.string.event_qr_title)).check(matches(isDisplayed()));
            onView(withId(R.id.qrImageView)).check(matches(isDisplayed()));
            onView(withContentDescription(R.string.event_qr_image_description)).check(matches(isDisplayed()));
            onView(withId(R.id.qrErrorText)).check(matches(withEffectiveVisibility(GONE)));
            onView(withId(R.id.saveQrButton))
                    .check(matches(isDisplayed()))
                    .check(matches(withText(R.string.event_qr_save_button)));
            onView(withId(R.id.viewEventPageButton)).check(matches(isDisplayed()));
        }
    }
}
