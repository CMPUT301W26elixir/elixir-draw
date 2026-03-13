package com.example.allot.view;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

// These three imports make Spinner selections bulletproof!
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.allot.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class CreateEventActivityTest {

    @Rule
    public ActivityScenarioRule<CreateEventActivity> activityRule =
            new ActivityScenarioRule<>(CreateEventActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testEmptySubmission_DoesNotFireIntent() {
        onView(withId(R.id.createEventNextButton)).perform(click());
        assert(Intents.getIntents().isEmpty());
    }

    // --- HELPER METHOD TO EFFICIENTLY CREATE 10 EVENTS ---
    private void createFortniteEvent(String eventName) throws InterruptedException {
        // Standard Text Fields
        onView(withId(R.id.eventNameInput)).perform(scrollTo(), replaceText(eventName), closeSoftKeyboard());
        onView(withId(R.id.locationInput)).perform(scrollTo(), replaceText("Online"), closeSoftKeyboard());
        onView(withId(R.id.priceInput)).perform(scrollTo(), replaceText("15"), closeSoftKeyboard());
        onView(withId(R.id.descriptionInput)).perform(scrollTo(), replaceText("Competitive custom matchmaking."), closeSoftKeyboard());
        onView(withId(R.id.participantsInput)).perform(scrollTo(), replaceText("100"), closeSoftKeyboard());

        // 1. EVENT START DATE
        onView(withId(R.id.startMonthSpinner)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("MAY"))).perform(click());
        onView(withId(R.id.startDayInput)).perform(scrollTo(), replaceText("15"), closeSoftKeyboard());
        onView(withId(R.id.startYearInput)).perform(scrollTo(), replaceText("2026"), closeSoftKeyboard());

        // 2. REGISTRATION START DATE
        onView(withId(R.id.registrationStartMonthSpinner)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("MAR"))).perform(click());
        onView(withId(R.id.registrationStartDayInput)).perform(scrollTo(), replaceText("01"), closeSoftKeyboard());
        onView(withId(R.id.registrationStartYearInput)).perform(scrollTo(), replaceText("2026"), closeSoftKeyboard());

        // 3. REGISTRATION END DATE
        onView(withId(R.id.registrationEndMonthSpinner)).perform(scrollTo(), click());
        onData(allOf(is(instanceOf(String.class)), is("APR"))).perform(click());
        onView(withId(R.id.registrationEndDayInput)).perform(scrollTo(), replaceText("10"), closeSoftKeyboard());
        onView(withId(R.id.registrationEndYearInput)).perform(scrollTo(), replaceText("2026"), closeSoftKeyboard());

        // Next Button
        onView(withId(R.id.createEventNextButton)).perform(scrollTo(), click());

        // Wait for Firebase to process the save
        Thread.sleep(2000);

        // Assert that we successfully navigated to MyEventsActivity
        intended(hasComponent(MyEventsActivity.class.getName()));
    }

    // --- OUR 10 FORTNITE EVENT TESTS ---
    @Test
    public void testEvent1_SoloCashCup() throws InterruptedException { createFortniteEvent("Solo Cash Cup"); }

    @Test
    public void testEvent2_DuoFNCS() throws InterruptedException { createFortniteEvent("Duo FNCS"); }

    @Test
    public void testEvent3_BoxFights() throws InterruptedException { createFortniteEvent("Box Fights Tournament"); }

    @Test
    public void testEvent4_ZoneWars() throws InterruptedException { createFortniteEvent("Zone Wars Championship"); }

    @Test
    public void testEvent5_SquadScrims() throws InterruptedException { createFortniteEvent("Squad Scrims"); }

    @Test
    public void testEvent6_TiltedThrowdown() throws InterruptedException { createFortniteEvent("Tilted Towers Throwdown"); }

    @Test
    public void testEvent7_ZeroBuildTrios() throws InterruptedException { createFortniteEvent("Zero Build Trios"); }

    @Test
    public void testEvent8_LateGameArena() throws InterruptedException { createFortniteEvent("Late Game Arena"); }

    @Test
    public void testEvent9_CreativeShowcase() throws InterruptedException { createFortniteEvent("Creative Map Showcase"); }

    @Test
    public void testEvent10_WinterRoyale() throws InterruptedException { createFortniteEvent("Winter Royale"); }
}