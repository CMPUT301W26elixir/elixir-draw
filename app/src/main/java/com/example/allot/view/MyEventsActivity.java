package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.allot.R;
import com.example.allot.controller.EventController;
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity that displays the current user's events.
 *
 * <p>This screen is divided into two top-level tabs:
 * <ul>
 *     <li><b>Registered</b> - events the user has registered for, grouped by status</li>
 *     <li><b>Hosting</b> - events the user is organizing, grouped by progress</li>
 * </ul>
 *
 * <p>The activity loads event data, classifies events into sections, and updates the UI
 * accordingly. It also provides navigation to other app screens such as explore, saved,
 * profile, scan, event details, and event creation.
 */
public class MyEventsActivity extends AppCompatActivity {

    /**
     * Intent extra key used to specify which top tab should be shown first.
     */
    public static final String EXTRA_INITIAL_TAB = "initial_tab";

    /**
     * Intent extra value indicating that the hosting tab should be selected initially.
     */
    public static final String INITIAL_TAB_HOSTING = "hosting";

    private BottomNavBarView bottomNavBar;
    private TextView registeredTabText;
    private TextView hostingTabText;
    private ProgressBar loadingIndicator;
    private TextView stateText;
    private LinearLayout registeredSectionsContainer;
    private LinearLayout selectedContainer;
    private LinearLayout waitingContainer;
    private LinearLayout notSelectedContainer;
    private LinearLayout pastContainer;
    private LinearLayout hostingSectionsContainer;
    private LinearLayout ongoingContainer;
    private LinearLayout completedContainer;
    private TextView createEventButton;

    private UserController userController;
    private EventController eventController;
    private LayoutInflater layoutInflater;
    private TopTab currentTab = TopTab.REGISTERED;

    /**
     * Initializes the activity, controllers, layout inflater, views, and tab navigation.
     * Displays either the registered tab or hosting tab depending on the provided intent extra.
     *
     * @param savedInstanceState the previously saved instance state, if one exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        userController = new UserController(this);
        eventController = new EventController();
        layoutInflater = LayoutInflater.from(this);

        bindViews();
        setupTopTabs();
        setupBottomNav();
        if (INITIAL_TAB_HOSTING.equals(getIntent().getStringExtra(EXTRA_INITIAL_TAB))) {
            showHostingTab();
        } else {
            showRegisteredTab();
        }
    }

    /**
     * Finishes the activity without applying a transition animation.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Binds all required layout views to their corresponding fields.
     */
    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        registeredTabText = findViewById(R.id.registeredTabText);
        hostingTabText = findViewById(R.id.hostingTabText);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        stateText = findViewById(R.id.stateText);
        registeredSectionsContainer = findViewById(R.id.registeredSectionsContainer);
        selectedContainer = findViewById(R.id.selectedContainer);
        waitingContainer = findViewById(R.id.waitingContainer);
        notSelectedContainer = findViewById(R.id.notSelectedContainer);
        pastContainer = findViewById(R.id.pastContainer);
        hostingSectionsContainer = findViewById(R.id.hostingSectionsContainer);
        ongoingContainer = findViewById(R.id.ongoingContainer);
        completedContainer = findViewById(R.id.completedContainer);
        createEventButton = findViewById(R.id.createEventButton);
    }

    /**
     * Sets up click listeners for the top tab controls and the create event button.
     */
    private void setupTopTabs() {
        registeredTabText.setOnClickListener(view -> showRegisteredTab());
        hostingTabText.setOnClickListener(view -> showHostingTab());
        createEventButton.setOnClickListener(view -> openCreateEventScreen());
    }

    /**
     * Configures the bottom navigation bar and assigns screen navigation handlers.
     */
    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> openSavedScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> openProfileScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> openScanScreen());
    }

    /**
     * Opens the saved screen by launching {@link MainActivity} and requesting navigation
     * to the saved tab.
     */
    private void openSavedScreen() {
        Intent intent = new Intent(MyEventsActivity.this, MainActivity.class);
        intent.putExtra("navigate_to", "saved");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Displays the registered events tab, updates tab styling, hides the create event button,
     * and loads the user's registered events.
     */
    private void showRegisteredTab() {
        currentTab = TopTab.REGISTERED;
        updateTopTabStyles();
        createEventButton.setVisibility(View.GONE);
        loadRegisteredEvents();
    }

    /**
     * Displays the hosting events tab, updates tab styling, shows the create event button,
     * and loads the events hosted by the user.
     */
    private void showHostingTab() {
        currentTab = TopTab.HOSTING;
        updateTopTabStyles();
        createEventButton.setVisibility(View.VISIBLE);
        loadHostedEvents();
    }

    /**
     * Updates the visual styling of the top tabs so the active tab appears highlighted.
     */
    private void updateTopTabStyles() {
        registeredTabText.setTextColor(ContextCompat.getColor(this,
                currentTab == TopTab.REGISTERED ? R.color.text_primary : R.color.my_events_tab_inactive));
        hostingTabText.setTextColor(ContextCompat.getColor(this,
                currentTab == TopTab.HOSTING ? R.color.text_primary : R.color.my_events_tab_inactive));
    }

    /**
     * Loads all events the current user has registered for.
     *
     * <p>If the request succeeds, the events are bound to the registered sections.
     * If it fails, an error state is shown as long as the registered tab is still active.
     */
    private void loadRegisteredEvents() {
        setLoadingState();
        eventController.getRegisteredEventsForUser(userController.getCurrentDeviceId(), new EventController.EventListCallback() {
            @Override
            public void onCallback(List<Event> events) {
                if (currentTab != TopTab.REGISTERED) {
                    return;
                }
                bindRegisteredSections(events == null ? new ArrayList<>() : events);
            }

            @Override
            public void onError(Exception exception) {
                if (currentTab == TopTab.REGISTERED) {
                    setErrorState();
                }
            }
        });
    }

    /**
     * Loads all events hosted by the current user from Firestore.
     *
     * <p>If an event is missing its event ID, the Firestore document ID is assigned to it.
     * Results are only displayed if the hosting tab is still active.
     */
    private void loadHostedEvents() {
        setLoadingState();
        FirebaseFirestore.getInstance()
                .collection("events")
                .whereEqualTo("organizerId", userController.getCurrentDeviceId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (currentTab != TopTab.HOSTING) {
                        return;
                    }

                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        Event event = document.toObject(Event.class);
                        if (event != null && (event.eventId == null || event.eventId.trim().isEmpty())) {
                            event.eventId = document.getId();
                        }
                        if (event != null) {
                            events.add(event);
                        }
                    }

                    bindHostedSections(events);
                })
                .addOnFailureListener(exception -> {
                    if (currentTab == TopTab.HOSTING) {
                        setErrorState();
                    }
                });
    }

    /**
     * Classifies and binds registered events into their corresponding status sections:
     * selected, waiting, not selected, and past.
     *
     * @param events the list of registered events to categorize and display
     */
    private void bindRegisteredSections(List<Event> events) {
        List<Event> selectedEvents = new ArrayList<>();
        List<Event> waitingEvents = new ArrayList<>();
        List<Event> notSelectedEvents = new ArrayList<>();
        List<Event> pastEvents = new ArrayList<>();

        for (Event event : events) {
            Section section = classifyRegisteredEvent(event);
            switch (section) {
                case SELECTED:
                    selectedEvents.add(event);
                    break;
                case WAITING:
                    waitingEvents.add(event);
                    break;
                case NOT_SELECTED:
                    notSelectedEvents.add(event);
                    break;
                case PAST:
                default:
                    pastEvents.add(event);
                    break;
            }
        }

        bindSection(selectedContainer, selectedEvents, R.string.my_events_empty_selected);
        bindSection(waitingContainer, waitingEvents, R.string.my_events_empty_waiting);
        bindSection(notSelectedContainer, notSelectedEvents, R.string.my_events_empty_not_selected);
        bindSection(pastContainer, pastEvents, R.string.my_events_empty_past);

        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        if (events.isEmpty()) {
            stateText.setText(R.string.my_events_state_empty);
        }
        registeredSectionsContainer.setVisibility(View.VISIBLE);
        hostingSectionsContainer.setVisibility(View.GONE);
    }

    /**
     * Splits hosted events into ongoing and completed sections and displays them.
     *
     * @param events the list of hosted events to bind
     */
    private void bindHostedSections(List<Event> events) {
        List<Event> ongoingEvents = new ArrayList<>();
        List<Event> completedEvents = new ArrayList<>();

        for (Event event : events) {
            if (isPastEvent(event)) {
                completedEvents.add(event);
            } else {
                ongoingEvents.add(event);
            }
        }

        bindSection(ongoingContainer, ongoingEvents, R.string.my_events_empty_ongoing);
        bindSection(completedContainer, completedEvents, R.string.my_events_empty_completed);

        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        if (events.isEmpty()) {
            stateText.setText(R.string.my_events_hosting_empty);
        }
        registeredSectionsContainer.setVisibility(View.GONE);
        hostingSectionsContainer.setVisibility(View.VISIBLE);
    }

    /**
     * Binds a list of events to a section container.
     *
     * <p>If the list is empty, an empty-state message is shown instead.
     *
     * @param container the layout container that will hold the event cards
     * @param events the events to display in the section
     * @param emptyMessageRes the string resource to show when the section is empty
     */
    private void bindSection(LinearLayout container, List<Event> events, int emptyMessageRes) {
        container.removeAllViews();

        if (events.isEmpty()) {
            container.addView(createEmptyTextView(emptyMessageRes));
            return;
        }

        for (Event event : events) {
            View cardView = layoutInflater.inflate(R.layout.item_my_event_status_card, container, false);
            bindCard(cardView, event);
            container.addView(cardView);
        }
    }

    /**
     * Creates a styled text view used for section empty states.
     *
     * @param textRes the string resource to display
     * @return a configured empty-state text view
     */
    private View createEmptyTextView(int textRes) {
        TextView emptyView = new TextView(this);
        emptyView.setText(textRes);
        emptyView.setTextColor(ContextCompat.getColor(this, R.color.my_events_section_empty));
        emptyView.setTextSize(14);
        emptyView.setPadding(0, 0, 0, dpToPx(12));
        return emptyView;
    }

    /**
     * Populates an event card view with event information and assigns a click listener
     * to open the event detail screen.
     *
     * @param cardView the card view to populate
     * @param event the event whose information should be displayed
     */
    private void bindCard(View cardView, Event event) {
        View imageBackground = cardView.findViewById(R.id.imageBackground);
        TextView titleText = cardView.findViewById(R.id.titleText);
        TextView locationText = cardView.findViewById(R.id.locationText);
        TextView dateText = cardView.findViewById(R.id.dateText);

        titleText.setText(event == null ? null : event.title);
        locationText.setText(EventDisplayFormatter.location(event));
        dateText.setText(EventDisplayFormatter.date(event));

        imageBackground.setBackgroundResource(shouldUsePrimaryImage(event)
                ? R.drawable.bg_event_image_one
                : R.drawable.bg_event_image_two);

        cardView.setOnClickListener(view -> openEventDetailScreen(event));
    }

    /**
     * Determines which registered section an event belongs to.
     *
     * @param event the event to classify
     * @return the matching section for the event
     */
    private Section classifyRegisteredEvent(Event event) {
        if (isPastEvent(event)) {
            return Section.PAST;
        }

        if (isSelected(event)) {
            return Section.SELECTED;
        }

        if (isWaiting(event)) {
            return Section.WAITING;
        }

        return Section.NOT_SELECTED;
    }

    /**
     * Determines whether an event has already occurred.
     *
     * @param event the event to evaluate
     * @return true if the event date is in the past; false otherwise
     */
    private boolean isPastEvent(Event event) {
        return event != null
                && event.eventDate != null
                && event.eventDate.getTime() < System.currentTimeMillis();
    }

    /**
     * Determines whether the current user has been selected or enrolled in an event.
     *
     * <p>A user is considered selected if their device ID appears in the enrolled list,
     * chosen list, or waiting list chosen list.
     *
     * @param event the event to check
     * @return true if the current user is selected; false otherwise
     */
    private boolean isSelected(Event event) {
        String deviceId = userController.getCurrentDeviceId();
        return containsUser(event == null ? null : event.enrolled, deviceId)
                || containsUser(event == null ? null : event.chosen, deviceId)
                || containsUser(event != null && event.waitingList != null ? event.waitingList.chosen : null, deviceId);
    }

    /**
     * Determines whether the event should be shown in the waiting section.
     *
     * <p>An event is considered waiting if the registration deadline has not passed yet,
     * or if the deadline has passed but selection results have not yet been published.
     *
     * @param event the event to evaluate
     * @return true if the event is still waiting for selection results; false otherwise
     */
    private boolean isWaiting(Event event) {
        if (event == null) {
            return false;
        }

        if (!isDeadlinePassed(event)) {
            return true;
        }

        return !hasPublishedSelectionResults(event);
    }

    /**
     * Determines whether an event's registration deadline has passed.
     *
     * @param event the event to check
     * @return true if the registration deadline is in the past or exactly now; false otherwise
     */
    private boolean isDeadlinePassed(Event event) {
        return event != null
                && event.registrationDeadline != null
                && event.registrationDeadline.getTime() <= System.currentTimeMillis();
    }

    /**
     * Determines whether any selection-related result lists have been published for the event.
     *
     * @param event the event to inspect
     * @return true if at least one result list contains data; false otherwise
     */
    private boolean hasPublishedSelectionResults(Event event) {
        return (event != null && event.chosen != null && !event.chosen.isEmpty())
                || (event != null && event.enrolled != null && !event.enrolled.isEmpty())
                || (event != null && event.cancelled != null && !event.cancelled.isEmpty())
                || (event != null && event.notEnrolled != null && !event.notEnrolled.isEmpty())
                || (event != null && event.waitingList != null && event.waitingList.chosen != null && !event.waitingList.chosen.isEmpty());
    }

    /**
     * Checks whether a given device ID appears in a list of users.
     *
     * @param users the list of user device IDs
     * @param deviceId the device ID to search for
     * @return true if the device ID is in the list; false otherwise
     */
    private boolean containsUser(List<String> users, String deviceId) {
        return users != null && users.contains(deviceId);
    }

    /**
     * Chooses which placeholder image background to use for an event card.
     *
     * <p>The selection is based on the hash of the event category to provide a consistent
     * but varied appearance across cards.
     *
     * @param event the event whose category is used for image selection
     * @return true if the primary image should be used; false if the secondary image should be used
     */
    private boolean shouldUsePrimaryImage(Event event) {
        String category = event == null || event.category == null ? "" : event.category.trim();
        return Math.abs(category.hashCode()) % 2 == 0;
    }

    /**
     * Updates the UI to a loading state while event data is being fetched.
     */
    private void setLoadingState() {
        loadingIndicator.setVisibility(View.VISIBLE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.my_events_state_loading);
        registeredSectionsContainer.setVisibility(View.GONE);
        hostingSectionsContainer.setVisibility(View.GONE);
    }

    /**
     * Updates the UI to show an error state when event data cannot be loaded.
     */
    private void setErrorState() {
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.my_events_state_error);
        registeredSectionsContainer.setVisibility(View.GONE);
        hostingSectionsContainer.setVisibility(View.GONE);
    }

    /**
     * Opens the event detail screen for a given event.
     *
     * <p>If the event is null or does not contain a valid event ID, the method returns
     * without doing anything.
     *
     * @param event the event to display in detail
     */
    private void openEventDetailScreen(Event event) {
        if (event == null || TextUtils.isEmpty(event.eventId)) {
            return;
        }

        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.eventId);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, event.title);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, EventDisplayFormatter.location(event));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE, EventDisplayFormatter.date(event));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE, EventDisplayFormatter.price(event));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE, EventDisplayFormatter.deadline(event));
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, event.category);
        startActivity(intent);
    }

    /**
     * Opens the explore screen by launching {@link MainActivity}.
     */
    private void openExploreScreen() {
        Intent intent = new Intent(MyEventsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the profile screen.
     */
    private void openProfileScreen() {
        Intent intent = new Intent(MyEventsActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    /**
     * Opens the screen used to create a new event.
     */
    private void openCreateEventScreen() {
        startActivity(new Intent(this, CreateEventActivity.class));
        overridePendingTransition(0, 0);
    }

    /**
     * Converts a density-independent pixel (dp) value to pixels (px).
     *
     * @param dp the dp value to convert
     * @return the equivalent pixel value
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Represents the different event status sections shown in the registered tab.
     */
    private enum Section {
        SELECTED,
        WAITING,
        NOT_SELECTED,
        PAST
    }

    /**
     * Represents the two top tabs available in the activity.
     */
    private enum TopTab {
        REGISTERED,
        HOSTING
    }

    /**
     * Opens the scan screen.
     *
     * <p>Existing note preserved:
     * Do not call {@code finish()} here if this code is pasted into {@code MainActivity.java}.
     * You can call {@code finish()} here if it is used inside {@code MyEventsActivity} or
     * {@code ProfileActivity}.
     */
    private void openScanScreen() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        // Note: Do NOT call finish() here if pasting this into MainActivity.java!
        // You CAN call finish() here if pasting into MyEventsActivity or ProfileActivity.
    }
}