package com.example.allot.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.allot.R;
import com.example.allot.controller.EventController;
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;

import java.util.ArrayList;
import java.util.List;

public class MyEventsActivity extends AppCompatActivity {
    private BottomNavBarView bottomNavBar;
    private TextView registeredTabText;
    private TextView hostingTabText;
    private ProgressBar loadingIndicator;
    private TextView stateText;
    private LinearLayout sectionsContainer;
    private LinearLayout selectedContainer;
    private LinearLayout waitingContainer;
    private LinearLayout notSelectedContainer;
    private LinearLayout pastContainer;

    private UserController userController;
    private EventController eventController;
    private LayoutInflater layoutInflater;

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
        loadRegisteredEvents();
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
        sectionsContainer = findViewById(R.id.sectionsContainer);
        selectedContainer = findViewById(R.id.selectedContainer);
        waitingContainer = findViewById(R.id.waitingContainer);
        notSelectedContainer = findViewById(R.id.notSelectedContainer);
        pastContainer = findViewById(R.id.pastContainer);
    }

    private void setupTopTabs() {
        registeredTabText.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        hostingTabText.setTextColor(ContextCompat.getColor(this, R.color.my_events_tab_inactive));
    }

    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> openExploreScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> openProfileScreen());
    }

    private void loadRegisteredEvents() {
        setLoadingState();
        eventController.getRegisteredEventsForUser(userController.getCurrentDeviceId(), new EventController.EventListCallback() {
            @Override
            public void onCallback(List<Event> events) {
                bindRegisteredSections(events == null ? new ArrayList<>() : events);
            }

            @Override
            public void onError(Exception exception) {
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
            Section section = classifyEvent(event);
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

        bindSection(selectedContainer, selectedEvents, Section.SELECTED, R.string.my_events_empty_selected);
        bindSection(waitingContainer, waitingEvents, Section.WAITING, R.string.my_events_empty_waiting);
        bindSection(notSelectedContainer, notSelectedEvents, Section.NOT_SELECTED, R.string.my_events_empty_not_selected);
        bindSection(pastContainer, pastEvents, Section.PAST, R.string.my_events_empty_past);

        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
        if (events.isEmpty()) {
            stateText.setText(R.string.my_events_state_empty);
        }
        sectionsContainer.setVisibility(View.VISIBLE);
    }

    private void bindSection(LinearLayout container, List<Event> events, Section section, int emptyMessageRes) {
        container.removeAllViews();

        if (events.isEmpty()) {
            container.addView(createEmptyTextView(emptyMessageRes));
            return;
        }

        for (Event event : events) {
            View cardView = layoutInflater.inflate(R.layout.item_my_event_status_card, container, false);
            bindCard(cardView, event, section);
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

    private void bindCard(View cardView, Event event, Section section) {
        View imageBackground = cardView.findViewById(R.id.imageBackground);
        ImageView previewIcon = cardView.findViewById(R.id.previewIcon);
        TextView titleText = cardView.findViewById(R.id.titleText);
        TextView locationText = cardView.findViewById(R.id.locationText);
        TextView dateText = cardView.findViewById(R.id.dateText);

        titleText.setText(event.getBrowseTitleText());
        locationText.setText(event.getBrowseLocationText());
        dateText.setText(event.getBrowseDateText());

        imageBackground.setBackgroundResource(shouldUsePrimaryImage(event)
                ? R.drawable.bg_event_image_one
                : R.drawable.bg_event_image_two);
        previewIcon.setImageResource(section == Section.PAST ? R.drawable.article : R.drawable.event);

        cardView.setOnClickListener(view -> openEventDetailScreen(event));
    }

    private Section classifyEvent(Event event) {
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
        sectionsContainer.setVisibility(View.GONE);
    }

    private void setErrorState() {
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.my_events_state_error);
        sectionsContainer.setVisibility(View.GONE);
    }

    private void openEventDetailScreen(Event event) {
        if (event == null || TextUtils.isEmpty(event.eventId)) {
            return;
        }

        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.eventId);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, event.getBrowseTitleText());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, event.getBrowseLocationText());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE, event.getBrowseDateText());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE, event.getBrowsePriceText());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE, event.getBrowseDeadlineText());
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
}
