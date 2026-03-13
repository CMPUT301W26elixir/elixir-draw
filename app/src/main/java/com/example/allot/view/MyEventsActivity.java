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

public class MyEventsActivity extends AppCompatActivity {
    public static final String EXTRA_INITIAL_TAB = "initial_tab";
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

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

    private void setupTopTabs() {
        registeredTabText.setOnClickListener(view -> showRegisteredTab());
        hostingTabText.setOnClickListener(view -> showHostingTab());
        createEventButton.setOnClickListener(view -> openCreateEventScreen());
    }

    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> openSavedScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> openProfileScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> openScanScreen());
    }

    private void openSavedScreen() {
        Intent intent = new Intent(MyEventsActivity.this, MainActivity.class);
        intent.putExtra("navigate_to", "saved");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void showRegisteredTab() {
        currentTab = TopTab.REGISTERED;
        updateTopTabStyles();
        createEventButton.setVisibility(View.GONE);
        loadRegisteredEvents();
    }

    private void showHostingTab() {
        currentTab = TopTab.HOSTING;
        updateTopTabStyles();
        createEventButton.setVisibility(View.VISIBLE);
        loadHostedEvents();
    }

    private void updateTopTabStyles() {
        registeredTabText.setTextColor(ContextCompat.getColor(this,
                currentTab == TopTab.REGISTERED ? R.color.text_primary : R.color.my_events_tab_inactive));
        hostingTabText.setTextColor(ContextCompat.getColor(this,
                currentTab == TopTab.HOSTING ? R.color.text_primary : R.color.my_events_tab_inactive));
    }

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

    private View createEmptyTextView(int textRes) {
        TextView emptyView = new TextView(this);
        emptyView.setText(textRes);
        emptyView.setTextColor(ContextCompat.getColor(this, R.color.my_events_section_empty));
        emptyView.setTextSize(14);
        emptyView.setPadding(0, 0, 0, dpToPx(12));
        return emptyView;
    }

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

    private boolean isPastEvent(Event event) {
        return event != null
                && event.eventDate != null
                && event.eventDate.getTime() < System.currentTimeMillis();
    }

    private boolean isSelected(Event event) {
        String deviceId = userController.getCurrentDeviceId();
        return containsUser(event == null ? null : event.enrolled, deviceId)
                || containsUser(event == null ? null : event.chosen, deviceId)
                || containsUser(event != null && event.waitingList != null ? event.waitingList.chosen : null, deviceId);
    }

    private boolean isWaiting(Event event) {
        if (event == null) {
            return false;
        }

        if (!isDeadlinePassed(event)) {
            return true;
        }

        return !hasPublishedSelectionResults(event);
    }

    private boolean isDeadlinePassed(Event event) {
        return event != null
                && event.registrationDeadline != null
                && event.registrationDeadline.getTime() <= System.currentTimeMillis();
    }

    private boolean hasPublishedSelectionResults(Event event) {
        return (event != null && event.chosen != null && !event.chosen.isEmpty())
                || (event != null && event.enrolled != null && !event.enrolled.isEmpty())
                || (event != null && event.cancelled != null && !event.cancelled.isEmpty())
                || (event != null && event.notEnrolled != null && !event.notEnrolled.isEmpty())
                || (event != null && event.waitingList != null && event.waitingList.chosen != null && !event.waitingList.chosen.isEmpty());
    }

    private boolean containsUser(List<String> users, String deviceId) {
        return users != null && users.contains(deviceId);
    }

    private boolean shouldUsePrimaryImage(Event event) {
        String category = event == null || event.category == null ? "" : event.category.trim();
        return Math.abs(category.hashCode()) % 2 == 0;
    }

    private void setLoadingState() {
        loadingIndicator.setVisibility(View.VISIBLE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.my_events_state_loading);
        registeredSectionsContainer.setVisibility(View.GONE);
        hostingSectionsContainer.setVisibility(View.GONE);
    }

    private void setErrorState() {
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.my_events_state_error);
        registeredSectionsContainer.setVisibility(View.GONE);
        hostingSectionsContainer.setVisibility(View.GONE);
    }

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

    private void openExploreScreen() {
        Intent intent = new Intent(MyEventsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void openProfileScreen() {
        Intent intent = new Intent(MyEventsActivity.this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();
    }

    private void openCreateEventScreen() {
        startActivity(new Intent(this, CreateEventActivity.class));
        overridePendingTransition(0, 0);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private enum Section {
        SELECTED,
        WAITING,
        NOT_SELECTED,
        PAST
    }

    private enum TopTab {
        REGISTERED,
        HOSTING
    }
    private void openScanScreen() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
        // Note: Do NOT call finish() here if pasting this into MainActivity.java!
        // You CAN call finish() here if pasting into MyEventsActivity or ProfileActivity.
    }
}










