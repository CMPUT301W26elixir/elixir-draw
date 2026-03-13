package com.example.allot.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;
import com.example.allot.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for managing an event lottery before draw results are finalized.
 * Loads entrants, allows the organizer to set draw details,
 * and starts the draw process.
 */
public class ManageLotteryActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";

    private final SimpleDateFormat drawDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

    private FirebaseFirestore database;
    private UserController userController;
    private EditText drawDateInput;
    private EditText attendeesToSelectInput;
    private LinearLayout entrantsContainer;
    private TextView stateText;
    private MaterialButton forceStartDrawButton;

    private String currentEventId;
    private Event currentEvent;
    private boolean isLoading;
    private boolean isStartingDraw;

    /**
     * Initializes the activity, binds views, sets listeners,
     * and loads the lottery data for the selected event.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_lottery);

        drawDateFormat.setLenient(false);
        database = FirebaseFirestore.getInstance();
        userController = new UserController(this);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        setupHeader();
        setupListeners();
        loadLotteryData();
    }

    /**
     * Finishes the activity without transition animation.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Binds all view references used by the activity.
     */
    private void bindViews() {
        drawDateInput = findViewById(R.id.drawDateInput);
        attendeesToSelectInput = findViewById(R.id.attendeesToSelectInput);
        entrantsContainer = findViewById(R.id.entrantsContainer);
        stateText = findViewById(R.id.stateText);
        forceStartDrawButton = findViewById(R.id.forceStartDrawButton);
    }

    /**
     * Sets up the header back button behavior.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Sets up text watchers and button listeners for the lottery form.
     */
    private void setupListeners() {
        TextWatcher dirtyWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                updateActionState();
            }
        };

        drawDateInput.addTextChangedListener(dirtyWatcher);
        attendeesToSelectInput.addTextChangedListener(dirtyWatcher);
        forceStartDrawButton.setOnClickListener(view -> forceStartDraw());
    }

    /**
     * Loads the lottery data for the current event from Firestore.
     */
    private void loadLotteryData() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_lottery_load_failure, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        isLoading = true;
        updateActionState();
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.manage_lottery_loading);

        database.collection("events")
                .document(currentEventId)
                .get()
                .addOnSuccessListener(this::bindEventSnapshot)
                .addOnFailureListener(exception -> {
                    isLoading = false;
                    updateActionState();
                    stateText.setVisibility(View.VISIBLE);
                    stateText.setText(R.string.manage_lottery_load_failure);
                    Toast.makeText(this, R.string.manage_lottery_load_failure, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Binds the retrieved event snapshot to the UI.
     *
     * @param documentSnapshot the Firestore document snapshot for the event
     */
    private void bindEventSnapshot(DocumentSnapshot documentSnapshot) {
        isLoading = false;
        if (documentSnapshot == null || !documentSnapshot.exists()) {
            updateActionState();
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(R.string.manage_lottery_not_found);
            Toast.makeText(this, R.string.manage_lottery_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = documentSnapshot.toObject(Event.class);
        if (event == null) {
            updateActionState();
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(R.string.manage_lottery_load_failure);
            Toast.makeText(this, R.string.manage_lottery_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(event.eventId)) {
            event.eventId = documentSnapshot.getId();
        }

        if (hasDrawResults(event)) {
            startActivity(new android.content.Intent(this, ManageEntrantsActivity.class)
                    .putExtra(ManageEntrantsActivity.EXTRA_EVENT_ID, currentEventId));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        currentEvent = event;
        Date storedDrawDate = documentSnapshot.getDate("drawDate");
        bindForm(event, storedDrawDate);
        bindEntrants(event.waitingList == null ? null : event.waitingList.list);
        updateActionState();
    }

    /**
     * Binds the event draw form values to the UI.
     *
     * @param event the event being displayed
     * @param storedDrawDate the saved draw date from Firestore
     */
    private void bindForm(Event event, Date storedDrawDate) {
        Date effectiveDrawDate = storedDrawDate != null
                ? storedDrawDate
                : event.registrationDeadline != null ? event.registrationDeadline : new Date();
        drawDateInput.setText(drawDateFormat.format(effectiveDrawDate));

        int attendeesToSelect = event.limit > 0 ? event.limit : event.capacity;
        attendeesToSelectInput.setText(attendeesToSelect > 0 ? String.valueOf(attendeesToSelect) : "");
    }

    /**
     * Binds the current entrant list to the screen.
     *
     * @param entrantIds the list of entrant IDs to display
     */
    private void bindEntrants(List<String> entrantIds) {
        entrantsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (entrantIds == null || entrantIds.isEmpty()) {
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(R.string.manage_lottery_empty);
            return;
        }

        stateText.setVisibility(View.GONE);
        for (String entrantId : entrantIds) {
            View itemView = inflater.inflate(R.layout.item_lottery_entrant, entrantsContainer, false);
            TextView nameText = itemView.findViewById(R.id.entrantNameText);
            TextView timeText = itemView.findViewById(R.id.entrantTimeText);

            nameText.setText(entrantId);
            timeText.setText(R.string.manage_lottery_join_time_unavailable);

            if (!TextUtils.isEmpty(entrantId)) {
                userController.getUserByDeviceId(entrantId, (User user, boolean success) -> {
                    if (success && user != null && !TextUtils.isEmpty(user.getName())) {
                        nameText.setText(user.getName());
                    }
                });
            }

            entrantsContainer.addView(itemView);
        }
    }

    /**
     * Validates the form, runs the draw, and saves the results to Firestore.
     */
    private void forceStartDraw() {
        if (isLoading || isStartingDraw || currentEvent == null) {
            return;
        }

        String drawDateValue = currentText(drawDateInput);
        String attendeesValue = currentText(attendeesToSelectInput);
        if (TextUtils.isEmpty(drawDateValue) || TextUtils.isEmpty(attendeesValue)) {
            Toast.makeText(this, R.string.manage_lottery_validation_required, Toast.LENGTH_SHORT).show();
            return;
        }

        Date drawDate = parseDrawDate(drawDateValue);
        if (drawDate == null) {
            Toast.makeText(this, R.string.manage_lottery_validation_date, Toast.LENGTH_SHORT).show();
            return;
        }

        Integer attendees = parsePositiveInt(attendeesValue);
        if (attendees == null || attendees <= 0) {
            Toast.makeText(this, R.string.manage_lottery_validation_attendees, Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentEvent.waitingList == null) {
            currentEvent.getWaitingList();
        }
        if (currentEvent.waitingList == null || currentEvent.waitingList.list == null || currentEvent.waitingList.list.isEmpty()) {
            Toast.makeText(this, R.string.manage_lottery_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        currentEvent.capacity = attendees;
        currentEvent.limit = attendees;
        currentEvent.waitingList.limit = attendees;
        currentEvent.waitingList.chosen = new ArrayList<>();
        currentEvent.waitingList.status = new HashMap<>();
        currentEvent.chosen = new ArrayList<>();
        currentEvent.enrolled = new ArrayList<>();
        currentEvent.cancelled = new ArrayList<>();
        currentEvent.notEnrolled = new ArrayList<>();

        currentEvent.lottery();

        Map<String, Object> updates = new HashMap<>();
        updates.put("drawDate", drawDate);
        updates.put("capacity", attendees);
        updates.put("limit", attendees);
        updates.put("chosen", currentEvent.chosen);
        updates.put("enrolled", currentEvent.enrolled);
        updates.put("cancelled", currentEvent.cancelled);
        updates.put("notEnrolled", currentEvent.notEnrolled);
        updates.put("waitingList.limit", currentEvent.waitingList.limit);
        updates.put("waitingList.chosen", currentEvent.waitingList.chosen);
        updates.put("waitingList.status", currentEvent.waitingList.status);

        isStartingDraw = true;
        updateActionState();
        database.collection("events")
                .document(currentEventId)
                .update(updates)
                .addOnSuccessListener(unused -> {
                    isStartingDraw = false;
                    updateActionState();
                    setResult(RESULT_OK);
                    Toast.makeText(this, R.string.manage_lottery_draw_success, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(exception -> {
                    isStartingDraw = false;
                    updateActionState();
                    Toast.makeText(this, R.string.manage_lottery_draw_failure, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Updates the enabled state of the main action button.
     */
    private void updateActionState() {
        boolean enabled = !isLoading && !isStartingDraw;
        forceStartDrawButton.setEnabled(enabled);
        forceStartDrawButton.setAlpha(enabled ? 1f : 0.6f);
    }

    /**
     * Checks whether the event already has draw results.
     *
     * @param event the event to check
     * @return true if the event already has selected or processed entrants, false otherwise
     */
    private boolean hasDrawResults(Event event) {
        return (event.chosen != null && !event.chosen.isEmpty())
                || (event.enrolled != null && !event.enrolled.isEmpty())
                || (event.cancelled != null && !event.cancelled.isEmpty())
                || (event.notEnrolled != null && !event.notEnrolled.isEmpty())
                || (event.waitingList != null && event.waitingList.chosen != null && !event.waitingList.chosen.isEmpty());
    }

    /**
     * Parses the draw date entered by the user.
     *
     * @param value the date text to parse
     * @return the parsed Date, or null if parsing fails
     */
    private Date parseDrawDate(String value) {
        try {
            return drawDateFormat.parse(value);
        } catch (ParseException exception) {
            return null;
        }
    }

    /**
     * Parses the attendee count entered by the organizer.
     *
     * @param value the text value to parse
     * @return the parsed integer, or null if the value is invalid
     */
    private Integer parsePositiveInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Returns the trimmed text currently entered in the given EditText.
     *
     * @param editText the input field to read from
     * @return the trimmed text, or an empty string if no text exists
     */
    private String currentText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    /**
     * Simple abstract TextWatcher with empty beforeTextChanged and onTextChanged methods.
     */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}