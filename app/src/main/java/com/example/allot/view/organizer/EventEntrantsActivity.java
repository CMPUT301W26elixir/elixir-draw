package com.example.allot.view.organizer;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import com.example.allot.R;
import com.example.allot.controller.organizer.EventEntrantsController;
import com.example.allot.model.event.Event;
import com.example.allot.model.lottery.LotteryEntrantItem;
import com.example.allot.model.organizer.EntrantsExportResult;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
public class EventEntrantsActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    private static final int EXPORT_STORAGE_PERMISSION_REQUEST = 1001;

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
    private MaterialButton exportFinalListButton;

    private String currentEventId;
    private Event currentEvent;
    private Tab selectedTab = Tab.SELECTED;
    private boolean isExporting;
    private String pendingExportFileName;
    private String pendingExportContent;

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
     * Sets up tab click behavior and export button behavior.
     */
    private void setupTabs() {
        selectedTabText.setOnClickListener(view -> showTab(Tab.SELECTED));
        cancelledTabText.setOnClickListener(view -> showTab(Tab.CANCELLED));
        notEnrolledTabText.setOnClickListener(view -> showTab(Tab.NOT_ENROLLED));
        enrolledTabText.setOnClickListener(view -> showTab(Tab.ENROLLED));
        allEntrantsTabText.setOnClickListener(view -> showTab(Tab.ALL));
        exportFinalListButton.setOnClickListener(view -> exportFinalList());
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
                updateExportButtonState();
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
        updateExportButtonState();
    }

    /**
     * Applies the selected or unselected style to a tab view.
     *
     * @param tabView the tab text view to style
     * @param isSelected true if the tab is selected
     */
    private void applyTabStyle(TextView tabView, boolean isSelected) {
        tabView.setBackgroundResource(isSelected ? R.drawable.bg_manage_entrant_tab_selected : R.drawable.bg_manage_entrant_tab_unselected);
        tabView.setTextColor(isSelected ? Color.parseColor("#1D1D1D") : getResources().getColor(R.color.text_secondary));
    }

    /**
     * Binds a list of entrants into the entrant container.
     *
     * @param entrantIds the entrant IDs to display
     * @param emptyMessageRes the message to show if the list is empty
     * @param subtitleRes the subtitle resource shown under each entrant
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

            nameText.setText(entrantItem.getDisplayName());
            timeText.setText(entrantItem.getSubtitleRes());
            entrantsContainer.addView(itemView);
        }
    }

    /**
     * Updates the visibility and enabled state of the export button.
     */
    private void updateExportButtonState() {
        boolean shouldShow = selectedTab == Tab.ENROLLED;
        exportFinalListButton.setVisibility(shouldShow ? View.VISIBLE : View.GONE);

        if (!shouldShow) {
            return;
        }

        boolean hasEnrolledEntrants = entrantsController.hasEnrolledEntrants(currentEvent);
        exportFinalListButton.setEnabled(hasEnrolledEntrants && !isExporting);
        exportFinalListButton.setAlpha(hasEnrolledEntrants && !isExporting ? 1f : 0.6f);
    }

    /**
     * Starts exporting the enrolled entrant list as a CSV file.
     */
    private void exportFinalList() {
        if (currentEvent == null || isExporting) {
            return;
        }

        isExporting = true;
        updateExportButtonState();
        exportFinalListButton.setText(R.string.manage_entrants_exporting);
        entrantsController.buildExportData(currentEvent, (EntrantsExportResult result, boolean success) -> {
            if (result == null || !result.isSuccess()) {
                isExporting = false;
                updateExportButtonState();
                exportFinalListButton.setText(R.string.manage_entrants_export_final_list);
                Toast.makeText(this, result == null ? getString(R.string.manage_entrants_export_failure) : getString(result.getMessageResId()), Toast.LENGTH_SHORT).show();
                return;
            }
            writeCsvFile(result.getFileName(), result.getCsvContent());
        });
    }

    /**
     * Writes the CSV file using the prepared export data.
     *
     * @param fileName the file name to create
     * @param csvContent the CSV content to write
     */
    private void writeCsvFile(String fileName, String csvContent) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                pendingExportFileName = fileName;
                pendingExportContent = csvContent;
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXPORT_STORAGE_PERMISSION_REQUEST
                );
                return;
            }

            writeCsvFileLegacy(fileName, csvContent);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri fileUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (fileUri == null) {
            finishExport(false, null, null);
            return;
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(fileUri)) {
            if (outputStream == null) {
                finishExport(false, null, null);
                return;
            }

            outputStream.write(csvContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            finishExport(true, fileName, fileUri);
        } catch (IOException exception) {
            finishExport(false, null, null);
        }
    }

    /**
     * Writes the CSV file using legacy external storage APIs.
     *
     * @param fileName the file name to create
     * @param csvContent the CSV content to write
     */
    private void writeCsvFileLegacy(String fileName, String csvContent) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir == null) {
            finishExport(false, null, null);
            return;
        }
        if (!downloadsDir.exists() && !downloadsDir.mkdirs()) {
            finishExport(false, null, null);
            return;
        }

        File exportFile = new File(downloadsDir, fileName);
        try (FileOutputStream outputStream = new FileOutputStream(exportFile)) {
            outputStream.write(csvContent.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    exportFile
            );
            finishExport(true, fileName, fileUri);
        } catch (IOException exception) {
            finishExport(false, null, null);
        }
    }

    /**
     * Finalizes the export flow, restores the UI, and optionally opens the exported file.
     *
     * @param success true if the export succeeded
     * @param fileName the exported file name
     * @param fileUri the URI of the exported file
     */
    private void finishExport(boolean success, String fileName, Uri fileUri) {
        isExporting = false;
        pendingExportFileName = null;
        pendingExportContent = null;
        exportFinalListButton.setText(R.string.manage_entrants_export_final_list);
        updateExportButtonState();

        int messageRes = success ? R.string.manage_entrants_export_success : R.string.manage_entrants_export_failure;
        String message = success
                ? getString(messageRes, fileName)
                : getString(messageRes);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        if (success && fileUri != null) {
            openExportedCsv(fileUri);
        }
    }

    /**
     * Opens or shares the exported CSV file.
     *
     * @param fileUri the URI of the exported CSV file
     */
    private void openExportedCsv(Uri fileUri) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(fileUri, "text/csv")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent shareIntent = new Intent(Intent.ACTION_SEND)
                .setType("text/csv")
                .putExtra(Intent.EXTRA_STREAM, fileUri)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (viewIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(viewIntent, getString(R.string.manage_entrants_open_csv)));
            return;
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.manage_entrants_open_csv)));
    }

    /**
     * Handles the permission result for legacy export storage access.
     *
     * @param requestCode the request code originally supplied
     * @param permissions the requested permissions
     * @param grantResults the grant results for the requested permissions
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != EXPORT_STORAGE_PERMISSION_REQUEST) {
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && !TextUtils.isEmpty(pendingExportFileName)
                && pendingExportContent != null) {
            writeCsvFileLegacy(pendingExportFileName, pendingExportContent);
            return;
        }

        finishExport(false, null, null);
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
}









