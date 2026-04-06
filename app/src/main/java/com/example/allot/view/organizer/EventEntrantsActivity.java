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
import com.example.allot.controller.organizer.EventEntrantsCsvFormatter;
import com.example.allot.controller.organizer.EventEntrantsCsvSaveService;
import com.example.allot.model.event.Event;
import com.example.allot.model.lottery.LotteryEntrantItem;
import com.example.allot.model.organizer.EntrantExportRow;
import com.example.allot.view.explore.MapViewActivity;
import com.google.android.material.button.MaterialButton;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
/**
 * Shows entrant lists and organizer actions for a specific event.
 */
public class EventEntrantsActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_UI_TEST_MODE = "ui_test_mode";
    public static final String EXTRA_UI_TEST_EVENT_ID = "ui_test_event_id";
    public static final String EXTRA_UI_TEST_EVENT_TITLE = "ui_test_event_title";
    public static final String EXTRA_UI_TEST_SELECTED = "ui_test_selected";
    public static final String EXTRA_UI_TEST_CANCELLED = "ui_test_cancelled";
    public static final String EXTRA_UI_TEST_NOT_ENROLLED = "ui_test_not_enrolled";
    public static final String EXTRA_UI_TEST_ENROLLED = "ui_test_enrolled";
    public static final String EXTRA_UI_TEST_ALL = "ui_test_all";

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
    private EventEntrantsCsvFormatter csvFormatter;
    private EventEntrantsCsvSaveService csvSaveService;
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

    private String currentEventId;
    private Event currentEvent;
    private Tab selectedTab = Tab.SELECTED;
    private boolean isExporting;
    private boolean isCancellingEntrant;
    private java.util.ArrayList<String> uiTestSelected;
    private java.util.ArrayList<String> uiTestCancelled;
    private java.util.ArrayList<String> uiTestNotEnrolled;
    private java.util.ArrayList<String> uiTestEnrolled;
    private java.util.ArrayList<String> uiTestAll;

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
        csvFormatter = new EventEntrantsCsvFormatter();
        csvSaveService = new EventEntrantsCsvSaveService();
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (isUiTestMode()) {
            loadUiTestLists();
            currentEventId = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_ID), "ui-test-event");
        }

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
    }

    /**
     * Sets up the header back button behavior.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Sets up tab click behavior and the export action.
     */
    private void setupTabs() {
        selectedTabText.setOnClickListener(view -> showTab(Tab.SELECTED));
        cancelledTabText.setOnClickListener(view -> showTab(Tab.CANCELLED));
        notEnrolledTabText.setOnClickListener(view -> showTab(Tab.NOT_ENROLLED));
        enrolledTabText.setOnClickListener(view -> showTab(Tab.ENROLLED));
        allEntrantsTabText.setOnClickListener(view -> showTab(Tab.ALL));
        viewEntrantMapButton.setOnClickListener(view -> openEntrantMap());
        exportFinalListButton.setOnClickListener(view -> exportFinalList());
        updateTabState();
    }

    /**
     * Loads the current event from Firestore.
     * Shows an error and finishes if the event ID is missing.
     */
    private void loadEvent() {
        if (isUiTestMode()) {
            bindUiTestEvent();
            return;
        }

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
     * Updates the visual selected state of the tabs and export button.
     */
    private void updateTabState() {
        applyTabStyle(selectedTabText, selectedTab == Tab.SELECTED);
        applyTabStyle(cancelledTabText, selectedTab == Tab.CANCELLED);
        applyTabStyle(notEnrolledTabText, selectedTab == Tab.NOT_ENROLLED);
        applyTabStyle(enrolledTabText, selectedTab == Tab.ENROLLED);
        applyTabStyle(allEntrantsTabText, selectedTab == Tab.ALL);
        exportFinalListButton.setVisibility(selectedTab == Tab.ENROLLED ? View.VISIBLE : View.GONE);
        exportFinalListButton.setEnabled(!isExporting);
        exportFinalListButton.setText(isExporting
                ? R.string.manage_entrants_export_saving
                : R.string.manage_entrants_export_final_list);
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
            TextView cancelButton = itemView.findViewById(R.id.cancelEntrantButton);

            nameText.setText(entrantItem.getDisplayName());
            timeText.setText(entrantItem.getSubtitleRes());
            if (cancelButton != null) {
                if (selectedTab == Tab.SELECTED) {
                    cancelButton.setVisibility(View.VISIBLE);
                    cancelButton.setOnClickListener(view -> promptCancelEntrant(entrantItem));
                } else {
                    cancelButton.setVisibility(View.GONE);
                }
            }
            entrantsContainer.addView(itemView);
        }
    }

    private boolean isUiTestMode() {
        return getIntent().getBooleanExtra(EXTRA_UI_TEST_MODE, false);
    }

    private void loadUiTestLists() {
        uiTestSelected = getIntent().getStringArrayListExtra(EXTRA_UI_TEST_SELECTED);
        uiTestCancelled = getIntent().getStringArrayListExtra(EXTRA_UI_TEST_CANCELLED);
        uiTestNotEnrolled = getIntent().getStringArrayListExtra(EXTRA_UI_TEST_NOT_ENROLLED);
        uiTestEnrolled = getIntent().getStringArrayListExtra(EXTRA_UI_TEST_ENROLLED);
        uiTestAll = getIntent().getStringArrayListExtra(EXTRA_UI_TEST_ALL);

        if (uiTestSelected == null) uiTestSelected = new java.util.ArrayList<>();
        if (uiTestCancelled == null) uiTestCancelled = new java.util.ArrayList<>();
        if (uiTestNotEnrolled == null) uiTestNotEnrolled = new java.util.ArrayList<>();
        if (uiTestEnrolled == null) uiTestEnrolled = new java.util.ArrayList<>();
        if (uiTestAll == null) {
            uiTestAll = new java.util.ArrayList<>();
            uiTestAll.addAll(uiTestSelected);
            uiTestAll.addAll(uiTestCancelled);
            uiTestAll.addAll(uiTestNotEnrolled);
            uiTestAll.addAll(uiTestEnrolled);
        }
    }

    private void bindUiTestEvent() {
        currentEvent = new Event();
        currentEvent.setEventId(currentEventId);
        currentEvent.setTitle(safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_TITLE), "UI Test Event"));

        viewEntrantMapButton.setVisibility(View.VISIBLE);
        drawDateValueText.setText("April 12, 2026");
        attendeesValueText.setText("20");

        bindEntrants(buildUiTestEntrants(selectedTab), getEmptyMessageRes(selectedTab));
    }

    private List<LotteryEntrantItem> buildUiTestEntrants(Tab tab) {
        List<String> names = getUiTestNamesForTab(tab);
        List<LotteryEntrantItem> items = new java.util.ArrayList<>();
        int subtitleRes = getSubtitleResForTab(tab);
        for (String name : names) {
            String safeName = safeString(name, "Entrant");
            items.add(new LotteryEntrantItem(safeName, safeName, subtitleRes));
        }
        return items;
    }

    private List<String> getUiTestNamesForTab(Tab tab) {
        switch (tab) {
            case CANCELLED:
                return uiTestCancelled;
            case NOT_ENROLLED:
                return uiTestNotEnrolled;
            case ENROLLED:
                return uiTestEnrolled;
            case ALL:
                return uiTestAll;
            case SELECTED:
            default:
                return uiTestSelected;
        }
    }

    private int getSubtitleResForTab(Tab tab) {
        switch (tab) {
            case CANCELLED:
                return R.string.manage_entrants_cancelled_subtitle;
            case NOT_ENROLLED:
                return R.string.manage_entrants_not_enrolled_subtitle;
            case ENROLLED:
                return R.string.manage_entrants_enrolled_subtitle;
            case ALL:
                return R.string.manage_entrants_all_subtitle;
            case SELECTED:
            default:
                return R.string.manage_entrants_selected_subtitle;
        }
    }

    private String safeString(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private void promptCancelEntrant(LotteryEntrantItem entrantItem) {
        if (entrantItem == null || isCancellingEntrant) {
            return;
        }

        String displayName = entrantItem.getDisplayName();
        new AlertDialog.Builder(this)
                .setTitle(R.string.manage_entrants_cancel_title)
                .setMessage(getString(R.string.manage_entrants_cancel_message, displayName))
                .setNegativeButton(R.string.manage_entrants_cancel_stay, null)
                .setPositiveButton(R.string.manage_entrants_cancel_confirm, (dialog, which) -> cancelEntrant(entrantItem))
                .show();
    }

    private void cancelEntrant(LotteryEntrantItem entrantItem) {
        if (entrantItem == null || isCancellingEntrant) {
            return;
        }

        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_entrants_cancel_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        isCancellingEntrant = true;
        if (isUiTestMode()) {
            String name = entrantItem.getDisplayName();
            uiTestSelected.remove(name);
            if (!uiTestCancelled.contains(name)) {
                uiTestCancelled.add(name);
            }
            if (!uiTestAll.contains(name)) {
                uiTestAll.add(name);
            }
            isCancellingEntrant = false;
            Toast.makeText(this, R.string.manage_entrants_cancel_success, Toast.LENGTH_SHORT).show();
            loadEvent();
            return;
        }

        entrantsController.cancelSelectedEntrant(currentEventId, entrantItem.getEntrantId(), (result, success) -> {
            isCancellingEntrant = false;
            if (!success || result == null || !result) {
                Toast.makeText(this, R.string.manage_entrants_cancel_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, R.string.manage_entrants_cancel_success, Toast.LENGTH_SHORT).show();
            loadEvent();
        });
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

    private void exportFinalList() {
        if (isExporting) {
            return;
        }
        if (isUiTestMode()) {
            Toast.makeText(this, R.string.manage_entrants_export_success, Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedTab != Tab.ENROLLED || currentEvent == null || TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_entrants_export_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        setExporting(true);
        entrantsController.loadEnrolledExportRows(currentEvent, (rows, success) -> {
            if (!success) {
                setExporting(false);
                Toast.makeText(this, R.string.manage_entrants_export_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            saveExportRows(rows);
        });
    }

    private void saveExportRows(List<EntrantExportRow> rows) {
        try {
            String csvContent = csvFormatter.format(rows);
            csvSaveService.saveToDownloads(this, csvContent, currentEvent.getTitle(), currentEventId);
            Toast.makeText(this, R.string.manage_entrants_export_success, Toast.LENGTH_SHORT).show();
        } catch (IOException | SecurityException | IllegalArgumentException exception) {
            Toast.makeText(this, R.string.manage_entrants_export_failure, Toast.LENGTH_SHORT).show();
        } finally {
            setExporting(false);
        }
    }

    private void setExporting(boolean exporting) {
        isExporting = exporting;
        updateTabState();
    }
}









