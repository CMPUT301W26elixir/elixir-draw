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
        PROFILE_PIC,
        NOTIFICATIONS,
        POSTERS
    }

    public static final String EXTRA_UI_TEST_MODE = "ui_test_mode";

    private BottomNavBarView bottomNavBar;
    private ImageView backButton;
    private TextView eventsTabText;
    private TextView profilesTabText;
    private TextView profilePicTabText;
    private TextView notificationsTabText;
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

    // Profile picture management
    private RecyclerView profilePicsRecyclerView;
    private AdminProfilePictureListAdapter profilePicsAdapter;
    private List<User> profilePicsList;
    private View profilePicsContainer;
    private View profilePicsLoadingLayout;
    private TextView profilePicsEmptyStateText;

    // Notification management
    private AdminNotificationController adminNotificationController;
    private RecyclerView notificationsRecyclerView;
    private AdminNotificationListAdapter notificationsAdapter;
    private List<NotificationItem> notificationsList;
    private final Map<String, String> notificationUserNamesById = new HashMap<>();
    private View notificationsContainer;
    private View notificationsLoadingLayout;
    private TextView notificationsEmptyStateText;

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
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
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
        profilePicsList = new ArrayList<>();
        notificationsList = new ArrayList<>();
        posterEventsList = new ArrayList<>();

        bindViews();
        setupBottomNav();
        setupTabButtons();
        setupRecyclerViews();
        setupBackButton();
        if (isUiTestMode()) {
            seedUiTestData();
            showEventsTab();
            return;
        }
        loadEvents();
    }

    /**
     * Performs finish.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Performs bind views.
     */
    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        backButton = findViewById(R.id.backButton);
        eventsTabText = findViewById(R.id.eventsTabText);
        profilesTabText = findViewById(R.id.profilesTabText);
        profilePicTabText = findViewById(R.id.profilePicTabText);
        notificationsTabText = findViewById(R.id.notificationsTabText);
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

        // Profile pictures
        profilePicsContainer = findViewById(R.id.profilePicsContainer);
        profilePicsRecyclerView = findViewById(R.id.profilePicsRecyclerView);
        profilePicsLoadingLayout = findViewById(R.id.profilePicsLoadingLayout);
        profilePicsEmptyStateText = findViewById(R.id.profilePicsEmptyStateText);

        // Notifications
        notificationsContainer = findViewById(R.id.notificationsContainer);
        notificationsRecyclerView = findViewById(R.id.notificationsRecyclerView);
        notificationsLoadingLayout = findViewById(R.id.notificationsLoadingLayout);
        notificationsEmptyStateText = findViewById(R.id.notificationsEmptyStateText);

        // Posters
        postersContainer = findViewById(R.id.postersContainer);
        postersRecyclerView = findViewById(R.id.postersRecyclerView);
        postersLoadingLayout = findViewById(R.id.postersLoadingLayout);
        postersEmptyStateText = findViewById(R.id.postersEmptyStateText);
    }

    /**
     * Updates the up bottom nav.
     */
    private void setupBottomNav() {
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, view -> AppNavigator.openExplore(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, view -> AppNavigator.openSaved(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, view -> AppNavigator.openMyEvents(this, true));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> AppNavigator.openScan(this, false));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, view -> AppNavigator.openProfile(this, true));
    }

    /**
     * Updates the up tab buttons.
     */
    private void setupTabButtons() {
        eventsTabText.setOnClickListener(view -> showEventsTab());
        profilesTabText.setOnClickListener(view -> showProfilesTab());
        profilePicTabText.setOnClickListener(view -> showProfilePicsTab());
        notificationsTabText.setOnClickListener(view -> showNotificationsTab());
        postersTabText.setOnClickListener(view -> showPostersTab());
        updateTabStyles();
    }

    /**
     * Performs update tab styles.
     */
    private void updateTabStyles() {
        int activeColor = ContextCompat.getColor(this, R.color.text_primary);
        int inactiveColor = ContextCompat.getColor(this, R.color.my_events_tab_inactive);

        eventsTabText.setTextColor(currentTab == AdminTab.EVENTS ? activeColor : inactiveColor);
        profilesTabText.setTextColor(currentTab == AdminTab.PROFILES ? activeColor : inactiveColor);
        profilePicTabText.setTextColor(currentTab == AdminTab.PROFILE_PIC ? activeColor : inactiveColor);
        notificationsTabText.setTextColor(currentTab == AdminTab.NOTIFICATIONS ? activeColor : inactiveColor);
        postersTabText.setTextColor(currentTab == AdminTab.POSTERS ? activeColor : inactiveColor);
    }

    /**
     * Updates the up recycler views.
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

        // Profile pictures RecyclerView
        profilePicsAdapter = new AdminProfilePictureListAdapter(profilePicsList);
        profilePicsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        profilePicsRecyclerView.setAdapter(profilePicsAdapter);

        // Notifications RecyclerView
        notificationsAdapter = new AdminNotificationListAdapter(notificationsList);
        notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        notificationsRecyclerView.setAdapter(notificationsAdapter);

        // Posters RecyclerView
        postersAdapter = new AdminPosterListAdapter(posterEventsList, this::onPosterDeleteClick);
        postersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        postersRecyclerView.setAdapter(postersAdapter);
    }

    /**
     * Updates the up back button.
     */
    private void setupBackButton() {
        backButton.setOnClickListener(view -> finish());
    }

    /**
     * Returns whether ui test mode.
     *
     * @return whether ui test mode
     */
    private boolean isUiTestMode() {
        return getIntent().getBooleanExtra(EXTRA_UI_TEST_MODE, false);
    }

    /**
     * Performs seed ui test data.
     */
    private void seedUiTestData() {
        eventsList.clear();
        profilesList.clear();
        profilePicsList.clear();
        notificationsList.clear();
        posterEventsList.clear();

        Event event = new Event();
        event.setEventId("admin-event-1");
        event.setOrganizerId("organizer-1");
        event.setTitle("Admin Event One");
        event.setEventDate(new java.util.Date());
        event.setPosterUrl("https://example.com/poster.jpg");
        eventsList.add(event);

        Event posterEvent = new Event();
        posterEvent.setEventId("poster-event-1");
        posterEvent.setOrganizerId("organizer-2");
        posterEvent.setTitle("Poster Event");
        posterEvent.setPosterUrl("https://example.com/poster2.jpg");
        posterEventsList.add(posterEvent);

        User user = new User();
        user.setDeviceId("device-1");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setEmail("admin@example.com");
        user.setRole("admin");
        profilesList.add(user);

        User profilePicUser = new User();
        profilePicUser.setDeviceId("device-2");
        profilePicUser.setFirstName("Photo");
        profilePicUser.setLastName("User");
        profilePicUser.setProfilePhotoUrl("https://example.com/profile.jpg");
        profilePicsList.add(profilePicUser);

        NotificationItem notification = new NotificationItem(
                "device-1",
                "admin-event-1",
                "System Notice",
                "Admin notification body"
        );
        notificationsList.add(notification);

        notificationUserNamesById.clear();
        notificationUserNamesById.put("device-1", "Admin User");
        notificationsAdapter.setUserNamesById(notificationUserNamesById);

        setEventsVisibleState();
        eventsAdapter.notifyDataSetChanged();
        setProfilesVisibleState();
        profilesAdapter.notifyDataSetChanged();
        setProfilePicsVisibleState();
        profilePicsAdapter.notifyDataSetChanged();
        setNotificationsVisibleState();
        notificationsAdapter.notifyDataSetChanged();
        setPostersVisibleState();
        postersAdapter.notifyDataSetChanged();
    }

    /**
     * Performs show events tab.
     */
    private void showEventsTab() {
        currentTab = AdminTab.EVENTS;
        updateTabStyles();
        eventsContainer.setVisibility(View.VISIBLE);
        profilesContainer.setVisibility(View.GONE);
        profilePicsContainer.setVisibility(View.GONE);
        notificationsContainer.setVisibility(View.GONE);
        postersContainer.setVisibility(View.GONE);

        if (eventsList.isEmpty()) {
            loadEvents();
        }
    }

    /**
     * Performs show profiles tab.
     */
    private void showProfilesTab() {
        currentTab = AdminTab.PROFILES;
        updateTabStyles();
        eventsContainer.setVisibility(View.GONE);
        profilesContainer.setVisibility(View.VISIBLE);
        profilePicsContainer.setVisibility(View.GONE);
        notificationsContainer.setVisibility(View.GONE);
        postersContainer.setVisibility(View.GONE);

        if (profilesList.isEmpty()) {
            loadProfiles();
        }
    }

    /**
     * Performs show profile pics tab.
     */
    private void showProfilePicsTab() {
        currentTab = AdminTab.PROFILE_PIC;
        updateTabStyles();
        eventsContainer.setVisibility(View.GONE);
        profilesContainer.setVisibility(View.GONE);
        profilePicsContainer.setVisibility(View.VISIBLE);
        notificationsContainer.setVisibility(View.GONE);
        postersContainer.setVisibility(View.GONE);

        if (profilePicsList.isEmpty()) {
            loadProfilePics();
        }
    }

    /**
     * Performs show notifications tab.
     */
    private void showNotificationsTab() {
        currentTab = AdminTab.NOTIFICATIONS;
        updateTabStyles();
        eventsContainer.setVisibility(View.GONE);
        profilesContainer.setVisibility(View.GONE);
        profilePicsContainer.setVisibility(View.GONE);
        notificationsContainer.setVisibility(View.VISIBLE);
        postersContainer.setVisibility(View.GONE);

        if (notificationsList.isEmpty()) {
            loadNotifications();
        }
    }

    /**
     * Performs show posters tab.
     */
    private void showPostersTab() {
        currentTab = AdminTab.POSTERS;
        updateTabStyles();
        eventsContainer.setVisibility(View.GONE);
        profilesContainer.setVisibility(View.GONE);
        profilePicsContainer.setVisibility(View.GONE);
        notificationsContainer.setVisibility(View.GONE);
        postersContainer.setVisibility(View.VISIBLE);

        if (posterEventsList.isEmpty()) {
            loadPosters();
        }
    }

    /**
     * Performs load events.
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
     * Performs load profiles.
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
     * Performs load profile pics.
     */
    private void loadProfilePics() {
        setProfilePicsLoadingState();
        adminProfileController.loadAllProfiles((profiles, success) -> {
            if (!success || profiles == null) {
                setProfilePicsEmptyState(getString(R.string.admin_error_loading_profilepic));
                Toast.makeText(this, R.string.admin_error_loading_profilepic, Toast.LENGTH_SHORT).show();
                return;
            }

            profilePicsList.clear();
            for (User user : profiles) {
                if (user == null || UiHelper.isBlank(user.getProfilePhotoUrl())) {
                    continue;
                }
                profilePicsList.add(user);
            }

            if (profilePicsList.isEmpty()) {
                setProfilePicsEmptyState(getString(R.string.admin_no_profilepic));
                return;
            }

            setProfilePicsVisibleState();
            profilePicsAdapter.notifyDataSetChanged();
        });
    }

    /**
     * Performs load notifications.
     */
    private void loadNotifications() {
        setNotificationsLoadingState();
        adminNotificationController.loadAllNotifications((notifications, success) -> {
            if (!success || notifications == null) {
                setNotificationsEmptyState(getString(R.string.admin_error_loading_notifications));
                Toast.makeText(this, R.string.admin_error_loading_notifications, Toast.LENGTH_SHORT).show();
                return;
            }

            if (notifications.isEmpty()) {
                setNotificationsEmptyState(getString(R.string.admin_no_notifications));
                return;
            }

            adminProfileController.loadAllProfiles((profiles, profilesSuccess) -> {
                notificationUserNamesById.clear();
                if (profilesSuccess && profiles != null) {
                    for (User user : profiles) {
                        if (user == null || UiHelper.isBlank(user.getDeviceId())) {
                            continue;
                        }

                        String fullName = user.getName();
                        if (UiHelper.isBlank(fullName)) {
                            fullName = "Unknown User";
                        }
                        notificationUserNamesById.put(user.getDeviceId(), fullName);
                    }
                }

                notificationsAdapter.setUserNamesById(notificationUserNamesById);
                notificationsList.clear();
                notificationsList.addAll(notifications);
                setNotificationsVisibleState();
                notificationsAdapter.notifyDataSetChanged();
            });
        });
    }

    /**
     * Performs load posters.
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
     * Updates the events loading state.
     */
    private void setEventsLoadingState() {
        eventsRecyclerView.setVisibility(View.GONE);
        eventsLoadingLayout.setVisibility(View.VISIBLE);
        eventsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the events visible state.
     */
    private void setEventsVisibleState() {
        eventsRecyclerView.setVisibility(View.VISIBLE);
        eventsLoadingLayout.setVisibility(View.GONE);
        eventsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the events empty state.
     *
     * @param message the message
     */
    private void setEventsEmptyState(String message) {
        eventsRecyclerView.setVisibility(View.GONE);
        eventsLoadingLayout.setVisibility(View.GONE);
        eventsEmptyStateText.setVisibility(View.VISIBLE);
        eventsEmptyStateText.setText(message);
    }

    // ============== Profile state management ==============

    /**
     * Updates the profiles loading state.
     */
    private void setProfilesLoadingState() {
        profilesRecyclerView.setVisibility(View.GONE);
        profilesLoadingLayout.setVisibility(View.VISIBLE);
        profilesEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the profiles visible state.
     */
    private void setProfilesVisibleState() {
        profilesRecyclerView.setVisibility(View.VISIBLE);
        profilesLoadingLayout.setVisibility(View.GONE);
        profilesEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the profiles empty state.
     *
     * @param message the message
     */
    private void setProfilesEmptyState(String message) {
        profilesRecyclerView.setVisibility(View.GONE);
        profilesLoadingLayout.setVisibility(View.GONE);
        profilesEmptyStateText.setVisibility(View.VISIBLE);
        profilesEmptyStateText.setText(message);
    }

    // ============== Profile picture state management ==============

    /**
     * Updates the profile pics loading state.
     */
    private void setProfilePicsLoadingState() {
        profilePicsRecyclerView.setVisibility(View.GONE);
        profilePicsLoadingLayout.setVisibility(View.VISIBLE);
        profilePicsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the profile pics visible state.
     */
    private void setProfilePicsVisibleState() {
        profilePicsRecyclerView.setVisibility(View.VISIBLE);
        profilePicsLoadingLayout.setVisibility(View.GONE);
        profilePicsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the profile pics empty state.
     *
     * @param message the message
     */
    private void setProfilePicsEmptyState(String message) {
        profilePicsRecyclerView.setVisibility(View.GONE);
        profilePicsLoadingLayout.setVisibility(View.GONE);
        profilePicsEmptyStateText.setVisibility(View.VISIBLE);
        profilePicsEmptyStateText.setText(message);
    }

    // ============== Notification state management ==============

    /**
     * Updates the notifications loading state.
     */
    private void setNotificationsLoadingState() {
        notificationsRecyclerView.setVisibility(View.GONE);
        notificationsLoadingLayout.setVisibility(View.VISIBLE);
        notificationsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the notifications visible state.
     */
    private void setNotificationsVisibleState() {
        notificationsRecyclerView.setVisibility(View.VISIBLE);
        notificationsLoadingLayout.setVisibility(View.GONE);
        notificationsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the notifications empty state.
     *
     * @param message the message
     */
    private void setNotificationsEmptyState(String message) {
        notificationsRecyclerView.setVisibility(View.GONE);
        notificationsLoadingLayout.setVisibility(View.GONE);
        notificationsEmptyStateText.setVisibility(View.VISIBLE);
        notificationsEmptyStateText.setText(message);
    }

    // ============== Poster state management ==============

    /**
     * Updates the posters loading state.
     */
    private void setPostersLoadingState() {
        postersRecyclerView.setVisibility(View.GONE);
        postersLoadingLayout.setVisibility(View.VISIBLE);
        postersEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the posters visible state.
     */
    private void setPostersVisibleState() {
        postersRecyclerView.setVisibility(View.VISIBLE);
        postersLoadingLayout.setVisibility(View.GONE);
        postersEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Updates the posters empty state.
     *
     * @param message the message
     */
    private void setPostersEmptyState(String message) {
        postersRecyclerView.setVisibility(View.GONE);
        postersLoadingLayout.setVisibility(View.GONE);
        postersEmptyStateText.setVisibility(View.VISIBLE);
        postersEmptyStateText.setText(message);
    }

    // ============== Delete handlers ==============

    /**
     * Handles the event delete click callback.
     *
     * @param event the event
     * @param position the position
     */
    private void onEventDeleteClick(Event event, int position) {
        showEventDeleteConfirmationDialog(event, position);
    }

    /**
     * Handles the profile delete click callback.
     *
     * @param user the user
     * @param position the position
     */
    private void onProfileDeleteClick(User user, int position) {
        showProfileDeleteConfirmationDialog(user, position);
    }

    /**
     * Handles the poster delete click callback.
     *
     * @param event the event
     * @param position the position
     */
    private void onPosterDeleteClick(Event event, int position) {
        if (event == null) {
            return;
        }

        if (isUiTestMode()) {
            posterEventsList.remove(position);
            postersAdapter.notifyItemRemoved(position);
            if (posterEventsList.isEmpty()) {
                setPostersEmptyState(getString(R.string.admin_no_posters));
            }
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
     * Performs show event delete confirmation dialog.
     *
     * @param event the event
     * @param position the position
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
     * Performs show profile delete confirmation dialog.
     *
     * @param user the user
     * @param position the position
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
     * Performs delete event.
     *
     * @param event the event
     * @param position the position
     * @param dialog the dialog
     * @param cancelButton the cancel button
     * @param deleteButton the delete button
     */
    private void deleteEvent(Event event, int position, Dialog dialog, Button cancelButton, Button deleteButton) {
        cancelButton.setEnabled(false);
        deleteButton.setEnabled(false);

        if (isUiTestMode()) {
            dialog.dismiss();
            removeEventFromList(position);
            return;
        }

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
     * Performs delete profile.
     *
     * @param user the user
     * @param position the position
     * @param dialog the dialog
     * @param cancelButton the cancel button
     * @param deleteButton the delete button
     */
    private void deleteProfile(User user, int position, Dialog dialog, Button cancelButton, Button deleteButton) {
        cancelButton.setEnabled(false);
        deleteButton.setEnabled(false);

        if (isUiTestMode()) {
            dialog.dismiss();
            removeProfileFromList(user, position);
            return;
        }

        adminProfileController.deleteProfile(user.getDeviceId(), (success, result) -> {
            if (!success || !result) {
                cancelButton.setEnabled(true);
                deleteButton.setEnabled(true);
                Toast.makeText(this, R.string.admin_error_deleting_profile, Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(this, R.string.admin_profile_deleted, Toast.LENGTH_SHORT).show();

            removeProfileFromList(user, position);
        });
    }

    /**
     * Performs remove event from list.
     *
     * @param position the position
     */
    private void removeEventFromList(int position) {
        if (position < 0 || position >= eventsList.size()) {
            return;
        }

        eventsList.remove(position);
        eventsAdapter.notifyItemRemoved(position);

        if (eventsList.isEmpty()) {
            setEventsEmptyState(getString(R.string.admin_no_events));
        }
    }

    /**
     * Performs remove profile from list.
     *
     * @param user the user
     * @param position the position
     */
    private void removeProfileFromList(User user, int position) {
        if (position >= 0 && position < profilesList.size()) {
            profilesList.remove(position);
            profilesAdapter.notifyItemRemoved(position);
        }

        profilePicsList.removeIf(profileUser -> profileUser != null
                && user.getDeviceId() != null
                && user.getDeviceId().equals(profileUser.getDeviceId()));
        profilePicsAdapter.notifyDataSetChanged();

        if (profilesList.isEmpty()) {
            setProfilesEmptyState(getString(R.string.admin_no_profiles));
        }

        if (profilePicsList.isEmpty()) {
            setProfilePicsEmptyState(getString(R.string.admin_no_profilepic));
        }
    }
}
