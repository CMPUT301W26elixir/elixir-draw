package com.example.allot.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.allot.R;
import com.example.allot.controller.EventController;
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Admin-only screen that lists all events (any status) and allows deletion.
 * US 03.01.01 — Administrator can remove events and all associated lists.
 *
 * Access is granted automatically when the device's user document in Firestore
 * has role == "admin". That field is set manually in the Firebase Console.
 * There is no in-app flow to grant or revoke admin access.
 */
public class AdminEventListActivity extends AppCompatActivity {

    private static final String TAG = "AdminEventList";

    private UserController   userController;
    private EventController  eventController;
    private EventListAdapter eventListAdapter;

    private RecyclerView  recyclerView;
    private EditText      searchInput;
    private ProgressBar   loadingIndicator;
    private TextView      stateText;

    // Full unfiltered list — search filters client-side without re-fetching
    private final List<Event> allEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_event_list);

        userController  = new UserController(this);
        eventController = new EventController();

        bindViews();
        setupListeners();
        setupSearch();
        verifyAdminThenLoad();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    // -------------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------------

    private void bindViews() {
        recyclerView     = findViewById(R.id.adminEventsRecyclerView);
        searchInput      = findViewById(R.id.adminSearchInput);
        loadingIndicator = findViewById(R.id.adminLoadingIndicator);
        stateText        = findViewById(R.id.adminStateText);
    }

    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.adminBackButton);
        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupSearch() {
        eventListAdapter = new EventListAdapter(new ArrayList<>(), this::onEventClicked);
        recyclerView.setAdapter(eventListAdapter);

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable editable) {
                applyFilter(editable.toString());
            }
        });
    }

    // -------------------------------------------------------------------------
    // Access control
    // -------------------------------------------------------------------------

    /**
     * Re-loads the current user from Firestore and confirms role == "admin"
     * before showing any data. This re-check on every onCreate means that
     * revoking admin access in the Firebase Console takes effect the next
     * time this screen is opened.
     */
    private void verifyAdminThenLoad() {
        setLoadingState();

        userController.loadOrCreateUser((user, success) -> {
            if (!success || user == null || !user.isAdmin()) {
                // Not an admin — send back to the normal app
                Toast.makeText(this, R.string.admin_access_denied, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return;
            }

            loadAllEvents();
        });
    }

    // -------------------------------------------------------------------------
    // Data loading
    // -------------------------------------------------------------------------

    private void loadAllEvents() {
        setLoadingState();

        eventController.getAllEvents(new EventController.EventListCallback() {
            @Override
            public void onCallback(List<Event> events) {
                allEvents.clear();
                allEvents.addAll(events);
                applyFilter(searchInput.getText() == null
                        ? "" : searchInput.getText().toString());
                Log.d(TAG, "Loaded " + events.size() + " events for admin.");
            }

            @Override
            public void onError(Exception exception) {
                setErrorState();
                Log.e(TAG, "Failed to load events for admin", exception);
            }
        });
    }

    // -------------------------------------------------------------------------
    // Search / filter (client-side)
    // -------------------------------------------------------------------------

    private void applyFilter(String rawTerm) {
        String term = rawTerm == null ? "" : rawTerm.trim().toLowerCase(Locale.getDefault());
        List<EventListItem> filtered = new ArrayList<>();

        for (Event event : allEvents) {
            if (matchesSearch(event, term)) {
                filtered.add(EventListItem.fromEvent(event));
            }
        }

        eventListAdapter.updateEvents(filtered);

        if (filtered.isEmpty()) {
            setEmptyState(rawTerm);
        } else {
            setContentState();
        }
    }

    private boolean matchesSearch(Event event, String term) {
        if (term.isEmpty()) return true;
        return containsNormalized(event.title, term)
                || containsNormalized(event.location, term)
                || containsNormalized(event.category, term);
    }

    private boolean containsNormalized(String value, String term) {
        return value != null && value.toLowerCase(Locale.getDefault()).contains(term);
    }

    // -------------------------------------------------------------------------
    // Navigation / actions
    // -------------------------------------------------------------------------

    private void onEventClicked(EventListItem item) {
        if (item == null || item.eventId == null) return;

        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID,       item.eventId);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE,    item.title);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, item.street);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE,     item.date);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE,    item.price);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE, item.daysLeft);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, item.category);
        startActivity(intent);
    }

    /**
     * Shows a confirmation dialog before permanently deleting an event.
     * US 03.01.01
     */
    public void showDeleteEventDialog(String eventId, String eventTitle) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_admin_delete_event, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);

        TextView  titleText     = dialogView.findViewById(R.id.deleteEventTitleText);
        ImageView closeButton   = dialogView.findViewById(R.id.closeDeleteEventDialogButton);
        Button    cancelButton  = dialogView.findViewById(R.id.cancelDeleteEventButton);
        Button    confirmButton = dialogView.findViewById(R.id.confirmDeleteEventButton);

        titleText.setText(eventTitle != null
                ? eventTitle
                : getString(R.string.admin_delete_event_title_fallback));

        closeButton.setOnClickListener(v -> dialog.dismiss());
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        confirmButton.setOnClickListener(v ->
                deleteEvent(dialog, eventId, cancelButton, confirmButton));

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(dpToPx(342), dpToPx(342));
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    private void deleteEvent(Dialog dialog, String eventId,
                             Button cancelButton, Button confirmButton) {
        cancelButton.setEnabled(false);
        confirmButton.setEnabled(false);

        eventController.adminDeleteEvent(eventId, (result, success) -> {
            if (!success || result == null || !result) {
                cancelButton.setEnabled(true);
                confirmButton.setEnabled(true);
                Toast.makeText(this,
                        R.string.admin_delete_event_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(this,
                    R.string.admin_delete_event_success, Toast.LENGTH_SHORT).show();

            // Remove from local list and refresh without re-fetching
            allEvents.removeIf(e -> eventId.equals(e.eventId));
            applyFilter(searchInput.getText() == null
                    ? "" : searchInput.getText().toString());
        });
    }

    // -------------------------------------------------------------------------
    // UI state helpers
    // -------------------------------------------------------------------------

    private void setLoadingState() {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.browse_state_loading);
    }

    private void setContentState() {
        recyclerView.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.GONE);
    }

    private void setEmptyState(String searchTerm) {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        String trimmed = searchTerm == null ? "" : searchTerm.trim();
        stateText.setText(trimmed.isEmpty()
                ? getString(R.string.admin_no_events)
                : String.format(Locale.getDefault(), "No events match \"%s\".", trimmed));
    }

    private void setErrorState() {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.browse_state_error);
    }
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}