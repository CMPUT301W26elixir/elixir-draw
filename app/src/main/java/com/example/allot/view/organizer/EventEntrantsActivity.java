package com.example.allot.view.organizer;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import com.example.allot.R;
import com.example.allot.controller.organizer.EventEntrantsController;
import com.example.allot.model.event.Event;
import com.example.allot.model.lottery.LotteryEntrantItem;
import com.example.allot.view.explore.MapViewActivity;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Shows entrant lists and organizer actions for a specific event.
 */
public class EventEntrantsActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";

    /**
     * Represents the available entrant tabs in the manage entrants screen.
     */
    private enum Tab {
        SELECTED,
        CANCELLED,
        NOT_ENROLLED,
        ENROLLED,
        ALL
    }

    private EventEntrantsController entrantsController;
    private TextView drawDateValueText;
    private TextView attendeesValueText;
    private TextView selectedTabText;
    private TextView cancelledTabText;
    private TextView notEnrolledTabText;
    private TextView enrolledTabText;
    private TextView allEntrantsTabText;
    private TextView stateText;
    private LinearLayout entrantsContainer;
    private MaterialButton viewEntrantMapButton;
    private MaterialButton exportFinalListButton;
    private MaterialButton drawReplacementButton;

    private String currentEventId;
    private Event currentEvent;
    private Tab selectedTab = Tab.SELECTED;

    /**
     * Initializes the activity, binds views, sets up the header and tabs,
     * and loads the event data.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_entrants);

        entrantsController = new EventEntrantsController(this);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        setupHeader();
        setupTabs();
        loadEvent();
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
        drawDateValueText = findViewById(R.id.drawDateValueText);
        attendeesValueText = findViewById(R.id.attendeesValueText);
        selectedTabText = findViewById(R.id.selectedTabText);
        cancelledTabText = findViewById(R.id.cancelledTabText);
        notEnrolledTabText = findViewById(R.id.notEnrolledTabText);
        enrolledTabText = findViewById(R.id.enrolledTabText);
        allEntrantsTabText = findViewById(R.id.allEntrantsTabText);
        stateText = findViewById(R.id.stateText);
        entrantsContainer = findViewById(R.id.entrantsContainer);
        viewEntrantMapButton = findViewById(R.id.viewEntrantMapButton);
        exportFinalListButton = findViewById(R.id.exportFinalListButton);
        drawReplacementButton = findViewById(R.id.drawReplacementButton);
    }

    /**
     * Sets up the header back button behavior.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Sets up tab click behavior and action buttons.
     */
    private void setupTabs() {
        selectedTabText.setOnClickListener(view -> showTab(Tab.SELECTED));
        cancelledTabText.setOnClickListener(view -> showTab(Tab.CANCELLED));
        notEnrolledTabText.setOnClickListener(view -> showTab(Tab.NOT_ENROLLED));
        enrolledTabText.setOnClickListener(view -> showTab(Tab.ENROLLED));
        allEntrantsTabText.setOnClickListener(view -> showTab(Tab.ALL));
        viewEntrantMapButton.setOnClickListener(view -> openEntrantMap());
        exportFinalListButton.setOnClickListener(view ->
                Toast.makeText(this, R.string.manage_entrants_export_unavailable, Toast.LENGTH_SHORT).show()
        );
        drawReplacementButton.setOnClickListener(view -> showDrawReplacementDialog());
        updateTabState();
    }

    /**
     * Loads the current event from Firestore.
     * Shows an error and finishes if the event ID is missing.
     */
    private void loadEvent() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_entrants_load_failure, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewEntrantMapButton.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.manage_entrants_loading);
        entrantsContainer.removeAllViews();

        entrantsController.loadEvent(currentEventId, (event, success) -> {
            if (!success || event == null) {
                stateText.setVisibility(View.VISIBLE);
                stateText.setText(R.string.manage_entrants_load_failure);
                Toast.makeText(this, R.string.manage_entrants_load_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            currentEvent = event;
            viewEntrantMapButton.setVisibility(View.VISIBLE);
            drawDateValueText.setText(buildDrawDateText(event));
            attendeesValueText.setText(buildAttendeesText(event));

            entrantsController.loadEntrantItems(event, mapTab(selectedTab), (entrantItems, itemsLoaded) -> {
                if (!itemsLoaded) {
                    stateText.setVisibility(View.VISIBLE);
                    stateText.setText(R.string.manage_entrants_load_failure);
                    Toast.makeText(this, R.string.manage_entrants_load_failure, Toast.LENGTH_SHORT).show();
                    return;
                }

                bindEntrants(entrantItems, getEmptyMessageRes(selectedTab));
            });
        });
    }

    /**
     * Switches the currently selected entrant tab and refreshes the displayed content.
     *
     * @param tab the tab to show
     */
    private void showTab(Tab tab) {
        selectedTab = tab;
        updateTabState();
        loadEvent();
    }

    /**
     * Updates the visual selected state of the tabs and action buttons.
     */
    private void updateTabState() {
        applyTabStyle(selectedTabText, selectedTab == Tab.SELECTED);
        applyTabStyle(cancelledTabText, selectedTab == Tab.CANCELLED);
        applyTabStyle(notEnrolledTabText, selectedTab == Tab.NOT_ENROLLED);
        applyTabStyle(enrolledTabText, selectedTab == Tab.ENROLLED);
        applyTabStyle(allEntrantsTabText, selectedTab == Tab.ALL);
        exportFinalListButton.setVisibility(selectedTab == Tab.ENROLLED ? View.VISIBLE : View.GONE);
        drawReplacementButton.setVisibility(selectedTab == Tab.CANCELLED ? View.VISIBLE : View.GONE);
    }

    /**
     * Applies the selected or unselected style to a tab view.
     *
     * @param tabView the tab text view to style
     * @param isSelected true if the tab is selected
     */
    private void applyTabStyle(TextView tabView, boolean isSelected) {
        tabView.setBackgroundResource(isSelected ? R.drawable.bg_manage_entrant_tab_selected : R.drawable.bg_manage_entrant_tab_unselected);
        tabView.setTextColor(isSelected ? Color.parseColor("#1D1D1D") : ContextCompat.getColor(this, R.color.text_secondary));
    }

    /**
     * Binds a list of entrants into the entrant container.
     *
     * @param entrantItems the entrant items to display
     * @param emptyMessageRes the message to show if the list is empty
     */
    private void bindEntrants(List<LotteryEntrantItem> entrantItems, int emptyMessageRes) {
        entrantsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        if (entrantItems == null || entrantItems.isEmpty()) {
            stateText.setVisibility(View.VISIBLE);
            stateText.setText(emptyMessageRes);
            return;
        }

        stateText.setVisibility(View.GONE);
        for (LotteryEntrantItem entrantItem : entrantItems) {
            View itemView = inflater.inflate(R.layout.item_lottery_entrant, entrantsContainer, false);
            TextView nameText = itemView.findViewById(R.id.entrantNameText);
            TextView timeText = itemView.findViewById(R.id.entrantTimeText);
            ImageButton removeButton = itemView.findViewById(R.id.removeEntrantButton);

            nameText.setText(entrantItem.getDisplayName());
            timeText.setText(entrantItem.getSubtitleRes());

            // Only show remove button in Selected tab
            if (selectedTab == Tab.SELECTED) {
                removeButton.setVisibility(View.VISIBLE);
                removeButton.setOnClickListener(v -> showRemoveEntrantDialog(entrantItem));
            } else {
                removeButton.setVisibility(View.GONE);
            }

            entrantsContainer.addView(itemView);
        }
    }

    private void showRemoveEntrantDialog(LotteryEntrantItem entrantItem) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.manage_entrants_cancel_dialog_title)
                .setMessage(R.string.manage_entrants_cancel_dialog_message)
                .setPositiveButton(R.string.admin_delete, (dialog, which) -> {
                    entrantsController.cancelEntrant(currentEventId, entrantItem.getEntrantId(), (result, success) -> {
                        if (success) {
                            Toast.makeText(this, R.string.manage_entrants_cancel_success, Toast.LENGTH_SHORT).show();
                            loadEvent();
                        } else {
                            Toast.makeText(this, R.string.manage_entrants_cancel_failure, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.admin_cancel, null)
                .show();
    }

    private void showDrawReplacementDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.manage_entrants_replacement_dialog_title)
                .setMessage(R.string.manage_entrants_replacement_dialog_message)
                .setPositiveButton(R.string.manage_lottery_force_start, (dialog, which) -> {
                    entrantsController.drawReplacement(currentEventId, (result, success) -> {
                        if (success) {
                            Toast.makeText(this, R.string.manage_entrants_replacement_success, Toast.LENGTH_SHORT).show();
                            loadEvent();
                        } else {
                            Toast.makeText(this, R.string.manage_entrants_replacement_failure, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.admin_cancel, null)
                .show();
    }

    private EventEntrantsController.Tab mapTab(Tab tab) {
        switch (tab) {
            case CANCELLED:
                return EventEntrantsController.Tab.CANCELLED;
            case NOT_ENROLLED:
                return EventEntrantsController.Tab.NOT_ENROLLED;
            case ENROLLED:
                return EventEntrantsController.Tab.ENROLLED;
            case ALL:
                return EventEntrantsController.Tab.ALL;
            case SELECTED:
            default:
                return EventEntrantsController.Tab.SELECTED;
        }
    }

    private int getEmptyMessageRes(Tab tab) {
        switch (tab) {
            case CANCELLED:
                return R.string.manage_entrants_empty_cancelled;
            case NOT_ENROLLED:
                return R.string.manage_entrants_empty_not_enrolled;
            case ENROLLED:
                return R.string.manage_entrants_empty_enrolled;
            case ALL:
                return R.string.manage_entrants_empty_all;
            case SELECTED:
            default:
                return R.string.manage_entrants_empty_selected;
        }
    }

    private String buildDrawDateText(Event event) {
        if (event == null) {
            return "";
        }

        java.util.Date effectiveDrawDate = event.getDrawDate() != null
                ? event.getDrawDate()
                : event.getRegistrationDeadline() != null ? event.getRegistrationDeadline() : new java.util.Date();
        return new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(effectiveDrawDate);
    }

    private String buildAttendeesText(Event event) {
        if (event == null) {
            return "20";
        }

        int attendeesToSelect = event.getLimit() > 0 ? event.getLimit() : event.getCapacity();
        return attendeesToSelect > 0 ? String.valueOf(attendeesToSelect) : "20";
    }

    private void openEntrantMap() {
        if (TextUtils.isEmpty(currentEventId) || currentEvent == null) {
            Toast.makeText(this, R.string.manage_entrants_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new android.content.Intent(this, MapViewActivity.class)
                .putExtra(MapViewActivity.EXTRA_EVENT_ID, currentEventId));
        overridePendingTransition(0, 0);
    }
}
