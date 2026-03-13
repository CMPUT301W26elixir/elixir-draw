package com.example.allot.view;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;
import com.example.allot.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ManageEntrantsActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";

    private enum Tab {
        SELECTED,
        CANCELLED,
        ENROLLED,
        ALL
    }

    private final SimpleDateFormat drawDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
    private final Map<String, String> userNameCache = new HashMap<>();

    private FirebaseFirestore database;
    private UserController userController;
    private TextView drawDateValueText;
    private TextView attendeesValueText;
    private TextView selectedTabText;
    private TextView cancelledTabText;
    private TextView enrolledTabText;
    private TextView allEntrantsTabText;
    private TextView stateText;
    private LinearLayout entrantsContainer;

    private String currentEventId;
    private Event currentEvent;
    private Date currentDrawDate;
    private Tab selectedTab = Tab.SELECTED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_entrants);

        database = FirebaseFirestore.getInstance();
        userController = new UserController(this);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        setupHeader();
        setupTabs();
        loadEvent();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void bindViews() {
        drawDateValueText = findViewById(R.id.drawDateValueText);
        attendeesValueText = findViewById(R.id.attendeesValueText);
        selectedTabText = findViewById(R.id.selectedTabText);
        cancelledTabText = findViewById(R.id.cancelledTabText);
        enrolledTabText = findViewById(R.id.enrolledTabText);
        allEntrantsTabText = findViewById(R.id.allEntrantsTabText);
        stateText = findViewById(R.id.stateText);
        entrantsContainer = findViewById(R.id.entrantsContainer);
    }

    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupTabs() {
        selectedTabText.setOnClickListener(view -> showTab(Tab.SELECTED));
        cancelledTabText.setOnClickListener(view -> showTab(Tab.CANCELLED));
        enrolledTabText.setOnClickListener(view -> showTab(Tab.ENROLLED));
        allEntrantsTabText.setOnClickListener(view -> showTab(Tab.ALL));
        updateTabState();
    }

    private void loadEvent() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_entrants_load_failure, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.manage_entrants_loading);
        entrantsContainer.removeAllViews();

        database.collection("events")
                .document(currentEventId)
                .get()
                .addOnSuccessListener(this::bindEventSnapshot)
                .addOnFailureListener(exception -> {
                    stateText.setVisibility(View.VISIBLE);
                    stateText.setText(R.string.manage_entrants_load_failure);
                    Toast.makeText(this, R.string.manage_entrants_load_failure, Toast.LENGTH_SHORT).show();
                });
    }

    private void bindEventSnapshot(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot == null || !documentSnapshot.exists()) {
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(R.string.manage_entrants_not_found);
            Toast.makeText(this, R.string.manage_entrants_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = documentSnapshot.toObject(Event.class);
        if (event == null) {
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(R.string.manage_entrants_load_failure);
            Toast.makeText(this, R.string.manage_entrants_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(event.eventId)) {
            event.eventId = documentSnapshot.getId();
        }

        currentEvent = event;
        currentDrawDate = documentSnapshot.getDate("drawDate");
        bindSummary(event, currentDrawDate);
        bindCurrentTab();
    }

    private void bindSummary(Event event, Date drawDate) {
        Date effectiveDrawDate = drawDate != null
                ? drawDate
                : event.registrationDeadline != null ? event.registrationDeadline : new Date();
        drawDateValueText.setText(drawDateFormat.format(effectiveDrawDate));

        int attendeesToSelect = event.limit > 0 ? event.limit : event.capacity;
        attendeesValueText.setText(attendeesToSelect > 0
                ? String.valueOf(attendeesToSelect)
                : getString(R.string.manage_lottery_attendees_hint));
    }

    private void showTab(Tab tab) {
        selectedTab = tab;
        updateTabState();
        bindCurrentTab();
    }

    private void updateTabState() {
        applyTabStyle(selectedTabText, selectedTab == Tab.SELECTED);
        applyTabStyle(cancelledTabText, selectedTab == Tab.CANCELLED);
        applyTabStyle(enrolledTabText, selectedTab == Tab.ENROLLED);
        applyTabStyle(allEntrantsTabText, selectedTab == Tab.ALL);
    }

    private void applyTabStyle(TextView tabView, boolean isSelected) {
        tabView.setBackgroundResource(isSelected ? R.drawable.bg_manage_entrant_tab_selected : R.drawable.bg_manage_entrant_tab_unselected);
        tabView.setTextColor(isSelected ? Color.parseColor("#1D1D1D") : getResources().getColor(R.color.text_secondary));
    }

    private void bindCurrentTab() {
        if (currentEvent == null) {
            return;
        }

        List<String> entrantIds;
        int emptyMessageRes;
        int subtitleRes;

        switch (selectedTab) {
            case CANCELLED:
                entrantIds = getCancelledEntrants(currentEvent);
                emptyMessageRes = R.string.manage_entrants_empty_cancelled;
                subtitleRes = R.string.manage_entrants_cancelled_subtitle;
                break;
            case ENROLLED:
                entrantIds = getEnrolledEntrants(currentEvent);
                emptyMessageRes = R.string.manage_entrants_empty_enrolled;
                subtitleRes = R.string.manage_entrants_enrolled_subtitle;
                break;
            case ALL:
                entrantIds = getAllEntrants(currentEvent);
                emptyMessageRes = R.string.manage_entrants_empty_all;
                subtitleRes = R.string.manage_entrants_all_subtitle;
                break;
            case SELECTED:
            default:
                entrantIds = getSelectedEntrants(currentEvent);
                emptyMessageRes = R.string.manage_entrants_empty_selected;
                subtitleRes = R.string.manage_entrants_selected_subtitle;
                break;
        }

        bindEntrants(entrantIds, emptyMessageRes, subtitleRes);
    }

    private void bindEntrants(List<String> entrantIds, int emptyMessageRes, int subtitleRes) {
        entrantsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (entrantIds == null || entrantIds.isEmpty()) {
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(emptyMessageRes);
            return;
        }

        stateText.setVisibility(View.GONE);
        for (String entrantId : entrantIds) {
            View itemView = inflater.inflate(R.layout.item_lottery_entrant, entrantsContainer, false);
            TextView nameText = itemView.findViewById(R.id.entrantNameText);
            TextView timeText = itemView.findViewById(R.id.entrantTimeText);

            nameText.setText(entrantId);
            timeText.setText(subtitleRes);

            bindUserName(entrantId, nameText);
            entrantsContainer.addView(itemView);
        }
    }

    private void bindUserName(String entrantId, TextView nameText) {
        if (TextUtils.isEmpty(entrantId)) {
            return;
        }

        String cachedName = userNameCache.get(entrantId);
        if (!TextUtils.isEmpty(cachedName)) {
            nameText.setText(cachedName);
            return;
        }

        userController.getUserByDeviceId(entrantId, (User user, boolean success) -> {
            if (success && user != null && !TextUtils.isEmpty(user.getName())) {
                userNameCache.put(entrantId, user.getName());
                nameText.setText(user.getName());
            }
        });
    }

    private List<String> getSelectedEntrants(Event event) {
        if (event.chosen != null && !event.chosen.isEmpty()) {
            return new ArrayList<>(event.chosen);
        }
        if (event.waitingList != null && event.waitingList.chosen != null) {
            return new ArrayList<>(event.waitingList.chosen);
        }
        return new ArrayList<>();
    }

    private List<String> getCancelledEntrants(Event event) {
        if (event.notEnrolled != null) {
            return new ArrayList<>(event.notEnrolled);
        }
        return new ArrayList<>();
    }

    private List<String> getEnrolledEntrants(Event event) {
        if (event.enrolled != null && !event.enrolled.isEmpty()) {
            return new ArrayList<>(event.enrolled);
        }

        ArrayList<String> enrolledEntrants = new ArrayList<>();
        if (event.waitingList != null && event.waitingList.chosen != null && event.waitingList.status != null) {
            for (String entrantId : event.waitingList.chosen) {
                if (Boolean.TRUE.equals(event.waitingList.status.get(entrantId))) {
                    enrolledEntrants.add(entrantId);
                }
            }
        }
        return enrolledEntrants;
    }

    private List<String> getAllEntrants(Event event) {
        if (event.waitingList != null && event.waitingList.list != null) {
            return new ArrayList<>(event.waitingList.list);
        }
        return new ArrayList<>();
    }
}
