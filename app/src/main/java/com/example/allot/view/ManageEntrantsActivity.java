package com.example.allot.view;

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
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;
import com.example.allot.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.OutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.nio.charset.StandardCharsets;

public class ManageEntrantsActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    private static final int EXPORT_STORAGE_PERMISSION_REQUEST = 1001;

    private enum Tab {
        SELECTED,
        CANCELLED,
        NOT_ENROLLED,
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
    private TextView notEnrolledTabText;
    private TextView enrolledTabText;
    private TextView allEntrantsTabText;
    private TextView stateText;
    private LinearLayout entrantsContainer;
    private MaterialButton exportFinalListButton;

    private String currentEventId;
    private Event currentEvent;
    private Date currentDrawDate;
    private Tab selectedTab = Tab.SELECTED;
    private boolean isExporting;
    private String pendingExportFileName;
    private String pendingExportContent;

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
        notEnrolledTabText = findViewById(R.id.notEnrolledTabText);
        enrolledTabText = findViewById(R.id.enrolledTabText);
        allEntrantsTabText = findViewById(R.id.allEntrantsTabText);
        stateText = findViewById(R.id.stateText);
        entrantsContainer = findViewById(R.id.entrantsContainer);
        exportFinalListButton = findViewById(R.id.exportFinalListButton);
    }

    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupTabs() {
        selectedTabText.setOnClickListener(view -> showTab(Tab.SELECTED));
        cancelledTabText.setOnClickListener(view -> showTab(Tab.CANCELLED));
        notEnrolledTabText.setOnClickListener(view -> showTab(Tab.NOT_ENROLLED));
        enrolledTabText.setOnClickListener(view -> showTab(Tab.ENROLLED));
        allEntrantsTabText.setOnClickListener(view -> showTab(Tab.ALL));
        exportFinalListButton.setOnClickListener(view -> exportFinalList());
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
        applyTabStyle(notEnrolledTabText, selectedTab == Tab.NOT_ENROLLED);
        applyTabStyle(enrolledTabText, selectedTab == Tab.ENROLLED);
        applyTabStyle(allEntrantsTabText, selectedTab == Tab.ALL);
        updateExportButtonState();
    }

    private void applyTabStyle(TextView tabView, boolean isSelected) {
        tabView.setBackgroundResource(isSelected ? R.drawable.bg_manage_entrant_tab_selected : R.drawable.bg_manage_entrant_tab_unselected);
        tabView.setTextColor(isSelected ? Color.parseColor("#1D1D1D") : getResources().getColor(R.color.text_secondary));
    }

    private void bindCurrentTab() {
        if (currentEvent == null) {
            updateExportButtonState();
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
            case NOT_ENROLLED:
                entrantIds = getNotEnrolledEntrants(currentEvent);
                emptyMessageRes = R.string.manage_entrants_empty_not_enrolled;
                subtitleRes = R.string.manage_entrants_not_enrolled_subtitle;
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
        updateExportButtonState();
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
        if (event.cancelled != null) {
            return new ArrayList<>(event.cancelled);
        }
        return new ArrayList<>();
    }

    private List<String> getNotEnrolledEntrants(Event event) {
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

    private void updateExportButtonState() {
        boolean shouldShow = selectedTab == Tab.ENROLLED;
        exportFinalListButton.setVisibility(shouldShow ? View.VISIBLE : View.GONE);

        if (!shouldShow) {
            return;
        }

        boolean hasEnrolledEntrants = currentEvent != null && !getEnrolledEntrants(currentEvent).isEmpty();
        exportFinalListButton.setEnabled(hasEnrolledEntrants && !isExporting);
        exportFinalListButton.setAlpha(hasEnrolledEntrants && !isExporting ? 1f : 0.6f);
    }

    private void exportFinalList() {
        if (currentEvent == null || isExporting) {
            return;
        }

        List<String> enrolledEntrantIds = getEnrolledEntrants(currentEvent);
        if (enrolledEntrantIds.isEmpty()) {
            Toast.makeText(this, R.string.manage_entrants_export_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        isExporting = true;
        updateExportButtonState();
        exportFinalListButton.setText(R.string.manage_entrants_exporting);

        List<String> exportedNames = new ArrayList<>();
        loadExportNames(enrolledEntrantIds, 0, exportedNames);
    }

    private void loadExportNames(List<String> entrantIds, int index, List<String> exportedNames) {
        if (index >= entrantIds.size()) {
            writeCsvFile(exportedNames);
            return;
        }

        String entrantId = entrantIds.get(index);
        userController.getUserByDeviceId(entrantId, (User user, boolean success) -> {
            exportedNames.add(buildExportName(entrantId, success ? user : null));
            loadExportNames(entrantIds, index + 1, exportedNames);
        });
    }

    private String buildExportName(String entrantId, User user) {
        if (user == null) {
            return safeCsvValue(entrantId, entrantId);
        }
        return safeCsvValue(user.getName(), entrantId);
    }

    private void writeCsvFile(List<String> exportedNames) {
        String fileName = buildExportFileName();
        String csvContent = buildCsvContent(exportedNames);

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

    private String buildCsvContent(List<String> exportedNames) {
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("Name\n");
        for (String exportedName : exportedNames) {
            csvBuilder.append(escapeCsv(exportedName)).append('\n');
        }
        return csvBuilder.toString();
    }

    private String buildExportFileName() {
        String eventTitle = currentEvent == null || TextUtils.isEmpty(currentEvent.title)
                ? "event"
                : currentEvent.title;
        String safeTitle = eventTitle.replaceAll("[^a-zA-Z0-9_-]+", "_").replaceAll("_+", "_");
        if (safeTitle.startsWith("_")) {
            safeTitle = safeTitle.substring(1);
        }
        if (safeTitle.endsWith("_")) {
            safeTitle = safeTitle.substring(0, safeTitle.length() - 1);
        }
        if (TextUtils.isEmpty(safeTitle)) {
            safeTitle = "event";
        }
        return safeTitle + "_enrolled_entrants.csv";
    }

    private String escapeCsv(String value) {
        String safeValue = value == null ? "" : value;
        String escapedValue = safeValue.replace("\"", "\"\"");
        return "\"" + escapedValue + "\"";
    }

    private String safeCsvValue(String value, String fallback) {
        if (!TextUtils.isEmpty(value)) {
            return value.trim();
        }
        return fallback == null ? "" : fallback;
    }

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
}
