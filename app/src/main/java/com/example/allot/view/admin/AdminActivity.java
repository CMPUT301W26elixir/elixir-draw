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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.controller.admin.AdminEventController;
import com.example.allot.controller.admin.AdminNotificationController;
import com.example.allot.controller.admin.AdminProfileController;
import com.example.allot.controller.event.EventPosterController;
import com.example.allot.model.event.Event;
import com.example.allot.model.notification.NotificationItem;
import com.example.allot.model.profile.User;
import com.example.allot.view.shared.AppDialogHelper;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.UiHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activity for admin panel with tab-based navigation.
 * Allows admins to browse and delete events and profiles.
 */
public class AdminActivity extends AppCompatActivity {
    private enum AdminTab {
        EVENTS,
        PROFILES,
        PHOTOS,
        POSTERS
    }

    private BottomNavBarView bottomNavBar;
    private ImageView backButton;
    private TextView eventsTabText;
    private TextView profilesTabText;
    private TextView photosTabText;
    private TextView postersTabText;

    // Event management
    private AdminEventController adminEventController;
    private RecyclerView eventsRecyclerView;
    private AdminEventListAdapter eventsAdapter;
    private List<Event> eventsList;
    private View eventsContainer;
    private View eventsLoadingLayout;
    private TextView eventsEmptyStateText;

    // Profile management
    private AdminProfileController adminProfileController;
    private RecyclerView profilesRecyclerView;
    private AdminProfileListAdapter profilesAdapter;
    private List<User> profilesList;
    private View profilesContainer;
    private View profilesLoadingLayout;
    private TextView profilesEmptyStateText;

    // Profile photo management
    private RecyclerView photosRecyclerView;
    private AdminProfilePhotoListAdapter photosAdapter;
    private List<User> photosList;
    private View photosContainer;
    private View photosLoadingLayout;
    private TextView photosEmptyStateText;

    // Poster management
    private EventPosterController eventPosterController;
    private RecyclerView postersRecyclerView;
    private AdminPosterListAdapter postersAdapter;
    private List<Event> posterEventsList;
    private View postersContainer;
    private View postersLoadingLayout;
    private TextView postersEmptyStateText;

    private AdminTab currentTab = AdminTab.EVENTS;

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
        adminProfileController = new AdminProfileController(this);
        adminNotificationController = new AdminNotificationController(this);
        eventPosterController = new EventPosterController();
        eventsList = new ArrayList<>();
        profilesList = new ArrayList<>();
        photosList = new ArrayList<>();
        posterEventsList = new ArrayList<>();

        bindViews();
        setupBottomNav();
        setupTabButtons();
        setupRecyclerViews();
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
        backButton = findViewById(R.id.backButton);
        eventsTabText = findViewById(R.id.eventsTabText);
        profilesTabText = findViewById(R.id.profilesTabText);
        photosTabText = findViewById(R.id.photosTabText);
        postersTabText = findViewById(R.id.postersTabText);

        // Events
        eventsContainer = findViewById(R.id.eventsContainer);
        eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        eventsLoadingLayout = findViewById(R.id.eventsLoadingLayout);
        eventsEmptyStateText = findViewById(R.id.eventsEmptyStateText);

        // Profiles
        profilesContainer = findViewById(R.id.profilesContainer);
        profilesRecyclerView = findViewById(R.id.profilesRecyclerView);
        profilesLoadingLayout = findViewById(R.id.profilesLoadingLayout);
        profilesEmptyStateText = findViewById(R.id.profilesEmptyStateText);

        // Profile photos
        photosContainer = findViewById(R.id.photosContainer);
        photosRecyclerView = findViewById(R.id.photosRecyclerView);
        photosLoadingLayout = findViewById(R.id.photosLoadingLayout);
        photosEmptyStateText = findViewById(R.id.photosEmptyStateText);

        // Posters
        postersContainer = findViewById(R.id.postersContainer);
        postersRecyclerView = findViewById(R.id.postersRecyclerView);
        postersLoadingLayout = findViewById(R.id.postersLoadingLayout);
        postersEmptyStateText = findViewById(R.id.postersEmptyStateText);
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
     * Sets up the tab buttons for switching between events and profiles.
     */
    private void setupTabButtons() {
        eventsTabText.setOnClickListener(view -> showEventsTab());
        profilesTabText.setOnClickListener(view -> showProfilesTab());
        photosTabText.setOnClickListener(view -> showPhotosTab());
        postersTabText.setOnClickListener(view -> showPostersTab());
        updateTabStyles();
    }

    /**
     * Updates the tab text colors to show which tab is active.
     */
    private void updateTabStyles() {
        int activeColor = ContextCompat.getColor(this, R.color.text_primary);
        int inactiveColor = ContextCompat.getColor(this, R.color.my_events_tab_inactive);

        eventsTabText.setTextColor(currentTab == AdminTab.EVENTS ? activeColor : inactiveColor);
        profilesTabText.setTextColor(currentTab == AdminTab.PROFILES ? activeColor : inactiveColor);
        photosTabText.setTextColor(currentTab == AdminTab.PHOTOS ? activeColor : inactiveColor);
        postersTabText.setTextColor(currentTab == AdminTab.POSTERS ? activeColor : inactiveColor);
    }

    /**
     * Sets up both RecyclerViews with their respective adapters.
     */
    private void setupRecyclerViews() {
        // Events RecyclerView
        eventsAdapter = new AdminEventListAdapter(eventsList, this::onEventDeleteClick);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(eventsAdapter);

        // Profiles RecyclerView
        profilesAdapter = new AdminProfileListAdapter(profilesList, this::onProfileDeleteClick);
        profilesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        profilesRecyclerView.setAdapter(profilesAdapter);

        photosAdapter = new AdminProfilePhotoListAdapter(photosList);
        photosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        photosRecyclerView.setAdapter(photosAdapter);

        // Posters RecyclerView
        postersAdapter = new AdminPosterListAdapter(posterEventsList, this::onPosterDeleteClick);
        postersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postersRecyclerView.setAdapter(postersAdapter);
    }

    /**
     * Sets up the back button to finish the activity.
     */
    private void setupBackButton() {
        backButton.setOnClickListener(view -> finish());
    }

    /**
     * Shows the events tab and loads events if not already loaded.
     */
    private void showEventsTab() {
        currentTab = AdminTab.EVENTS;
        updateTabStyles();
        eventsContainer.setVisibility(View.VISIBLE);
        profilesContainer.setVisibility(View.GONE);
        photosContainer.setVisibility(View.GONE);
        postersContainer.setVisibility(View.GONE);

        if (eventsList.isEmpty()) {
            loadEvents();
        }
    }

    /**
     * Shows the profiles tab and loads profiles if not already loaded.
     */
    private void showProfilesTab() {
        currentTab = AdminTab.PROFILES;
        updateTabStyles();
        eventsContainer.setVisibility(View.GONE);
        profilesContainer.setVisibility(View.VISIBLE);
        photosContainer.setVisibility(View.GONE);
        postersContainer.setVisibility(View.GONE);

        if (profilesList.isEmpty()) {
            loadProfiles();
        }
    }

    /**
     * Shows the profile photos tab and loads photos if not already loaded.
     */
    private void showPhotosTab() {
        currentTab = AdminTab.PHOTOS;
        updateTabStyles();
        eventsContainer.setVisibility(View.GONE);
        profilesContainer.setVisibility(View.GONE);
        photosContainer.setVisibility(View.VISIBLE);
        postersContainer.setVisibility(View.GONE);

        if (photosList.isEmpty()) {
            loadProfilePhotos();
        }
    }

    /**
     * Shows the posters tab and loads poster list if not already loaded.
     */
    private void showPostersTab() {
        currentTab = AdminTab.POSTERS;
        updateTabStyles();
        eventsContainer.setVisibility(View.GONE);
        profilesContainer.setVisibility(View.GONE);
        photosContainer.setVisibility(View.GONE);
        postersContainer.setVisibility(View.VISIBLE);

        if (posterEventsList.isEmpty()) {
            loadPosters();
        }
    }

    /**
     * Loads all events from the database.
     * Shows loading state while loading, then displays events or empty state.
     */
    private void loadEvents() {
        setEventsLoadingState();
        adminEventController.loadAllEvents((events, success) -> {
            if (!success || events == null) {
                setEventsEmptyState(getString(R.string.admin_error_loading_events));
                Toast.makeText(this, R.string.admin_error_loading_events, Toast.LENGTH_SHORT).show();
                return;
            }

            if (events.isEmpty()) {
                setEventsEmptyState(getString(R.string.admin_no_events));
                return;
            }

            eventsList.clear();
            eventsList.addAll(events);
            setEventsVisibleState();
            eventsAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Loads all profiles from the database.
     * Shows loading state while loading, then displays profiles or empty state.
     */
    private void loadProfiles() {
        setProfilesLoadingState();
        adminProfileController.loadAllProfiles((profiles, success) -> {
            if (!success || profiles == null) {
                setProfilesEmptyState(getString(R.string.admin_error_loading_profiles));
                Toast.makeText(this, R.string.admin_error_loading_profiles, Toast.LENGTH_SHORT).show();
                return;
            }

            if (profiles.isEmpty()) {
                setProfilesEmptyState(getString(R.string.admin_no_profiles));
                return;
            }

            profilesList.clear();
            profilesList.addAll(profiles);
            setProfilesVisibleState();
            profilesAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Loads all profile photos from the database.
     * Only users with an uploaded profile photo are displayed.
     */
    private void loadProfilePhotos() {
        setPhotosLoadingState();
        adminProfileController.loadAllProfiles((profiles, success) -> {
            if (!success || profiles == null) {
                setPhotosEmptyState(getString(R.string.admin_error_loading_photos));
                Toast.makeText(this, R.string.admin_error_loading_photos, Toast.LENGTH_SHORT).show();
                return;
            }

            photosList.clear();
            for (User user : profiles) {
                if (user == null || UiHelper.isBlank(user.getProfilePhotoUrl())) {
                    continue;
                }
                photosList.add(user);
            }

            if (photosList.isEmpty()) {
                setPhotosEmptyState(getString(R.string.admin_no_photos));
                return;
            }

            setPhotosVisibleState();
            photosAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Loads all events with poster URLs for admin poster moderation.
     */
    private void loadPosters() {
        setPostersLoadingState();
        adminEventController.loadAllEvents((events, success) -> {
            if (!success || events == null) {
                setPostersEmptyState(getString(R.string.admin_error_loading_posters));
                Toast.makeText(this, R.string.admin_error_loading_posters, Toast.LENGTH_SHORT).show();
                return;
            }

            posterEventsList.clear();
            for (Event event : events) {
                if (event == null || UiHelper.isBlank(event.getPosterUrl())) {
                    continue;
                }
                posterEventsList.add(event);
            }

            if (posterEventsList.isEmpty()) {
                setPostersEmptyState(getString(R.string.admin_no_posters));
                return;
            }

            setPostersVisibleState();
            postersAdapter.notifyDataSetChanged();
        });
    }

    // ============== Event state management ==============

    /**
     * Shows the events loading state.
     */
    private void setEventsLoadingState() {
        eventsRecyclerView.setVisibility(View.GONE);
        eventsLoadingLayout.setVisibility(View.VISIBLE);
        eventsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the events visible state (RecyclerView is displayed).
     */
    private void setEventsVisibleState() {
        eventsRecyclerView.setVisibility(View.VISIBLE);
        eventsLoadingLayout.setVisibility(View.GONE);
        eventsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the events empty state.
     *
     * @param message the message to display
     */
    private void setEventsEmptyState(String message) {
        eventsRecyclerView.setVisibility(View.GONE);
        eventsLoadingLayout.setVisibility(View.GONE);
        eventsEmptyStateText.setVisibility(View.VISIBLE);
        eventsEmptyStateText.setText(message);
    }

    // ============== Profile state management ==============

    /**
     * Shows the profiles loading state.
     */
    private void setProfilesLoadingState() {
        profilesRecyclerView.setVisibility(View.GONE);
        profilesLoadingLayout.setVisibility(View.VISIBLE);
        profilesEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the profiles visible state (RecyclerView is displayed).
     */
    private void setProfilesVisibleState() {
        profilesRecyclerView.setVisibility(View.VISIBLE);
        profilesLoadingLayout.setVisibility(View.GONE);
        profilesEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the profiles empty state.
     *
     * @param message the message to display
     */
    private void setProfilesEmptyState(String message) {
        profilesRecyclerView.setVisibility(View.GONE);
        profilesLoadingLayout.setVisibility(View.GONE);
        profilesEmptyStateText.setVisibility(View.VISIBLE);
        profilesEmptyStateText.setText(message);
    }

    // ============== Profile photo state management ==============

    /**
     * Shows the profile photos loading state.
     */
    private void setPhotosLoadingState() {
        photosRecyclerView.setVisibility(View.GONE);
        photosLoadingLayout.setVisibility(View.VISIBLE);
        photosEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the profile photos visible state.
     */
    private void setPhotosVisibleState() {
        photosRecyclerView.setVisibility(View.VISIBLE);
        photosLoadingLayout.setVisibility(View.GONE);
        photosEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the profile photos empty state.
     *
     * @param message the message to display
     */
    private void setPhotosEmptyState(String message) {
        photosRecyclerView.setVisibility(View.GONE);
        photosLoadingLayout.setVisibility(View.GONE);
        photosEmptyStateText.setVisibility(View.VISIBLE);
        photosEmptyStateText.setText(message);
    }

    // ============== Poster state management ==============

    /**
     * Shows the posters loading state.
     */
    private void setPostersLoadingState() {
        postersRecyclerView.setVisibility(View.GONE);
        postersLoadingLayout.setVisibility(View.VISIBLE);
        postersEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the posters visible state.
     */
    private void setPostersVisibleState() {
        postersRecyclerView.setVisibility(View.VISIBLE);
        postersLoadingLayout.setVisibility(View.GONE);
        postersEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the posters empty state.
     */
    private void setPostersEmptyState(String message) {
        postersRecyclerView.setVisibility(View.GONE);
        postersLoadingLayout.setVisibility(View.GONE);
        postersEmptyStateText.setVisibility(View.VISIBLE);
        postersEmptyStateText.setText(message);
    }

    // ============== Delete handlers ==============

    /**
     * Handles delete button click for an event.
     * Shows confirmation dialog before deleting.
     *
     * @param event the event to delete
     * @param position the position in the list
     */
    private void onEventDeleteClick(Event event, int position) {
        showEventDeleteConfirmationDialog(event, position);
    }

    /**
     * Handles delete button click for a profile.
     * Shows confirmation dialog before deleting.
     *
     * @param user the user profile to delete
     * @param position the position in the list
     */
    private void onProfileDeleteClick(User user, int position) {
        showProfileDeleteConfirmationDialog(user, position);
    }

    /**
     * Handles delete button click for a poster.
     */
    private void onPosterDeleteClick(Event event, int position) {
        if (event == null) {
            return;
        }

        eventPosterController.deletePoster(event.getEventId(), event.getPosterUrl(), (result, success) -> {
            if (!success || result == null || !result) {
                Toast.makeText(this, R.string.admin_error_deleting_poster, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, R.string.admin_poster_deleted, Toast.LENGTH_SHORT).show();
            posterEventsList.remove(position);
            postersAdapter.notifyItemRemoved(position);

            if (posterEventsList.isEmpty()) {
                setPostersEmptyState(getString(R.string.admin_no_posters));
            }
        });
    }

    /**
     * Shows the delete confirmation dialog for an event.
     *
     * @param event the event to delete
     * @param position the position in the list
     */
    private void showEventDeleteConfirmationDialog(Event event, int position) {
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
     * Shows the delete confirmation dialog for a profile.
     *
     * @param user the user profile to delete
     * @param position the position in the list
     */
    private void showProfileDeleteConfirmationDialog(User user, int position) {
        Dialog dialog = AppDialogHelper.createDialog(this, R.layout.dialog_admin_delete_profile, true);
        View dialogView = dialog.findViewById(android.R.id.content);

        TextView userNameText = dialogView.findViewById(R.id.userNameText);
        Button cancelButton = dialogView.findViewById(R.id.cancelButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteProfileButton);

        String displayName = (user.getFirstName() != null && !user.getFirstName().isEmpty())
                ? user.getFirstName() + " " + (user.getLastName() != null ? user.getLastName() : "")
                : user.getDeviceId();
        userNameText.setText(displayName);

        cancelButton.setOnClickListener(view -> dialog.dismiss());
        deleteButton.setOnClickListener(view -> deleteProfile(user, position, dialog, cancelButton, deleteButton));

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
            eventsAdapter.notifyItemRemoved(position);

            // Show empty state if no events left
            if (eventsList.isEmpty()) {
                setEventsEmptyState(getString(R.string.admin_no_events));
            }
        });
    }

    /**
     * Deletes a profile after confirmation.
     *
     * @param user the user profile to delete
     * @param position the position in the list
     * @param dialog the confirmation dialog
     * @param cancelButton the cancel button
     * @param deleteButton the delete button
     */
    private void deleteProfile(User user, int position, Dialog dialog, Button cancelButton, Button deleteButton) {
        cancelButton.setEnabled(false);
        deleteButton.setEnabled(false);

        adminProfileController.deleteProfile(user.getDeviceId(), (success, result) -> {
            if (!success || !result) {
                cancelButton.setEnabled(true);
                deleteButton.setEnabled(true);
                Toast.makeText(this, R.string.admin_error_deleting_profile, Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(this, R.string.admin_profile_deleted, Toast.LENGTH_SHORT).show();

            // Remove from list
            profilesList.remove(position);
            profilesAdapter.notifyItemRemoved(position);

            photosList.removeIf(photoUser -> photoUser != null
                    && user.getDeviceId() != null
                    && user.getDeviceId().equals(photoUser.getDeviceId()));
            photosAdapter.notifyDataSetChanged();

            // Show empty state if no profiles left
            if (profilesList.isEmpty()) {
                setProfilesEmptyState(getString(R.string.admin_no_profiles));
            }

            if (photosList.isEmpty()) {
                setPhotosEmptyState(getString(R.string.admin_no_photos));
            }
        });
    }
}
