package com.example.allot.view.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.controller.admin.AdminEventController;
import com.example.allot.model.event.Event;
import com.example.allot.view.shared.AppDialogHelper;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.UiHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for admin event management.
 * Allows admins to browse all events and delete them.
 */
public class AdminActivity extends AppCompatActivity {
    private AdminEventController adminEventController;
    private BottomNavBarView bottomNavBar;
    private RecyclerView eventsRecyclerView;
    private View loadingLayout;
    private TextView emptyStateText;
    private ImageView backButton;
    private AdminEventListAdapter adapter;
    private List<Event> eventsList;

    /**
     * Initializes the activity, binds views, sets up navigation,
     * and loads all events.
     *
     * @param savedInstanceState the previously saved activity state, if one exists
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        adminEventController = new AdminEventController(this);
        eventsList = new ArrayList<>();

        bindViews();
        setupBottomNav();
        setupRecyclerView();
        setupBackButton();
        loadEvents();
    }

    /**
     * Finishes the activity without any transition animation.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Binds all layout views to their corresponding fields.
     */
    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        loadingLayout = findViewById(R.id.loadingLayout);
        emptyStateText = findViewById(R.id.emptyStateText);
        backButton = findViewById(R.id.backButton);
    }

    /**
     * Configures the bottom navigation bar.
     * No tab is selected since Admin is a separate screen.
     */
    private void setupBottomNav() {
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> AppNavigator.openExplore(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> AppNavigator.openSaved(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> AppNavigator.openMyEvents(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> AppNavigator.openScan(this, false));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> AppNavigator.openProfile(this, true));
    }

    /**
     * Sets up the RecyclerView with an adapter.
     */
    private void setupRecyclerView() {
        adapter = new AdminEventListAdapter(eventsList, this::onDeleteClick);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(adapter);
    }

    /**
     * Sets up the back button to finish the activity.
     */
    private void setupBackButton() {
        backButton.setOnClickListener(view -> finish());
    }

    /**
     * Loads all events from the database.
     * Shows loading state while loading, then displays events or empty state.
     */
    private void loadEvents() {
        showLoadingState();
        adminEventController.loadAllEvents((events, success) -> {
            if (!success || events == null) {
                showEmptyState(getString(R.string.admin_error_loading_events));
                Toast.makeText(this, R.string.admin_error_loading_events, Toast.LENGTH_SHORT).show();
                return;
            }

            if (events.isEmpty()) {
                showEmptyState(getString(R.string.admin_no_events));
                return;
            }

            eventsList.clear();
            eventsList.addAll(events);
            showEventsState();
            adapter.notifyDataSetChanged();
        });
    }

    /**
     * Handles delete button click for an event.
     * Shows confirmation dialog before deleting.
     *
     * @param event the event to delete
     * @param position the position in the list
     */
    private void onDeleteClick(Event event, int position) {
        showDeleteConfirmationDialog(event, position);
    }

    /**
     * Shows the delete confirmation dialog.
     *
     * @param event the event to delete
     * @param position the position in the list
     */
    private void showDeleteConfirmationDialog(Event event, int position) {
        Dialog dialog = AppDialogHelper.createDialog(this, R.layout.dialog_admin_delete_event, true);
        View dialogView = dialog.findViewById(android.R.id.content);

        TextView eventTitleText = dialogView.findViewById(R.id.eventTitleText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteEventButton);

        eventTitleText.setText(event.getTitle());

        cancelButton.setOnClickListener(view -> dialog.dismiss());
        deleteButton.setOnClickListener(view -> deleteEvent(event, position, dialog, cancelButton, deleteButton));

        AppDialogHelper.show(dialog, UiHelper.dpToPx(this, 300), UiHelper.dpToPx(this, 200));
    }

    /**
     * Deletes an event after confirmation.
     *
     * @param event the event to delete
     * @param position the position in the list
     * @param dialog the confirmation dialog
     * @param cancelButton the cancel button
     * @param deleteButton the delete button
     */
    private void deleteEvent(Event event, int position, Dialog dialog, Button cancelButton, Button deleteButton) {
        cancelButton.setEnabled(false);
        deleteButton.setEnabled(false);

        adminEventController.deleteEvent(event.getEventId(), (success, result) -> {
            if (!success || !result) {
                cancelButton.setEnabled(true);
                deleteButton.setEnabled(true);
                Toast.makeText(this, R.string.admin_error_deleting_event, Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(this, R.string.admin_event_deleted, Toast.LENGTH_SHORT).show();

            // Remove from list
            eventsList.remove(position);
            adapter.notifyItemRemoved(position);

            // Show empty state if no events left
            if (eventsList.isEmpty()) {
                showEmptyState(getString(R.string.admin_no_events));
            }
        });
    }

    /**
     * Shows the loading state.
     */
    private void showLoadingState() {
        loadingLayout.setVisibility(View.VISIBLE);
        eventsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the events list state.
     */
    private void showEventsState() {
        loadingLayout.setVisibility(View.GONE);
        eventsRecyclerView.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the empty state with a message.
     *
     * @param message the message to display
     */
    private void showEmptyState(String message) {
        loadingLayout.setVisibility(View.GONE);
        eventsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
        emptyStateText.setText(message);
    }
}
