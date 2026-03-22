package com.example.allot.view.lottery;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.controller.lottery.LotteryController;
import com.example.allot.model.event.Event;
import com.example.allot.model.lottery.LotteryEntrantItem;
import com.example.allot.model.lottery.RunLotteryData;
import com.example.allot.view.organizer.EventEntrantsActivity;
import com.example.allot.view.shared.SimpleTextWatcher;
import com.example.allot.view.shared.UiHelper;
import com.google.android.material.button.MaterialButton;
public class RunLotteryActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";

    private LotteryController lotteryController;
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

        lotteryController = new LotteryController(this);
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
        SimpleTextWatcher dirtyWatcher = new SimpleTextWatcher() {
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

        lotteryController.loadLotteryState(currentEventId, (RunLotteryData state, boolean success) -> {
            isLoading = false;
            if (state == null) {
                updateActionState();
                stateText.setVisibility(View.VISIBLE);
                stateText.setText(R.string.manage_lottery_load_failure);
                Toast.makeText(this, R.string.manage_lottery_load_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            renderState(state);
        });
    }

    /**
     * Binds the retrieved event snapshot to the UI.
     *
     * @param event the loaded event for the screen
     */
    private void bindEventSnapshot(Event event) {
        currentEvent = event;
    }

    /**
     * Binds the event draw form values to the UI.
     *
     * @param event the event being displayed
     * @param storedDrawDate the saved draw date from Firestore
     */
    private void bindForm(Event event, java.util.Date storedDrawDate) {
        if (event == null) {
            return;
        }
    }

    /**
     * Binds the current entrant list to the screen.
     *
     * @param entrantIds the list of entrant IDs to display
     */
    private void bindEntrants(java.util.List<LotteryEntrantItem> entrantItems) {
        entrantsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (entrantItems == null || entrantItems.isEmpty()) {
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(R.string.manage_lottery_empty);
            return;
        }

        stateText.setVisibility(View.GONE);
        for (LotteryEntrantItem entrantItem : entrantItems) {
            View itemView = inflater.inflate(R.layout.item_lottery_entrant, entrantsContainer, false);
            TextView nameText = itemView.findViewById(R.id.entrantNameText);
            TextView timeText = itemView.findViewById(R.id.entrantTimeText);

            nameText.setText(entrantItem.getDisplayName());
            timeText.setText(entrantItem.getSubtitleRes());

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

        isStartingDraw = true;
        updateActionState();
        lotteryController.startLotteryDraw(
                currentEventId,
                currentEvent,
                UiHelper.readText(drawDateInput),
                UiHelper.readText(attendeesToSelectInput),
                (AppResult<Event> result, boolean success) -> {
            isStartingDraw = false;
            updateActionState();

            if (result == null) {
                Toast.makeText(this, R.string.manage_lottery_draw_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!result.isSuccess()) {
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                return;
            }

            currentEvent = result.getData();
            setResult(RESULT_OK);
            Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
            finish();
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
     * Parses the attendee count entered by the organizer.
     *
     * @param value the text value to parse
     * @return the parsed integer, or null if the value is invalid
     */
    private Integer safeParseAttendees(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Renders the provided lottery screen state.
     *
     * @param state the state to render
     */
    private void renderState(RunLotteryData state) {
        if (state == null) {
            return;
        }

        if (state.shouldRedirectToEntrants()) {
            startActivity(new android.content.Intent(this, EventEntrantsActivity.class)
                    .putExtra(EventEntrantsActivity.EXTRA_EVENT_ID, currentEventId));
            overridePendingTransition(0, 0);
            finish();
            return;
        }

        currentEvent = state.getCurrentEvent();
        drawDateInput.setText(state.getDrawDateValue());
        attendeesToSelectInput.setText(state.getAttendeesValue());
        bindEntrants(state.getEntrantItems());
        if (state.getStateMessageRes() != 0 && state.getEntrantItems().isEmpty()) {
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(state.getStateMessageRes());
        }
        updateActionState();
    }
}









