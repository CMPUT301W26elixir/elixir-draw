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
        profilePicTabText.setOnClickListener(view -> showProfilePicsTab());
        notificationsTabText.setOnClickListener(view -> showNotificationsTab());
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
        profilePicTabText.setTextColor(currentTab == AdminTab.PROFILE_PIC ? activeColor : inactiveColor);
        notificationsTabText.setTextColor(currentTab == AdminTab.NOTIFICATIONS ? activeColor : inactiveColor);
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
     * Sets up the back button to finish the activity.
     */
    private void setupBackButton() {
        backButton.setOnClickListener(view -> finish());
    }

    private boolean isUiTestMode() {
        return getIntent().getBooleanExtra(EXTRA_UI_TEST_MODE, false);
    }

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
     * Shows the events tab and loads events if not already loaded.
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
     * Shows the profiles tab and loads profiles if not already loaded.
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
     * Shows the profile pictures tab and loads profile pictures if not already loaded.
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
     * Shows the notifications tab and loads notifications if not already loaded.
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
     * Shows the posters tab and loads poster list if not already loaded.
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
     * Loads all users with uploaded profile pictures.
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
     * Loads all sent notifications for admin review.
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

    // ============== Profile picture state management ==============

    /**
     * Shows the profile picture loading state.
     */
    private void setProfilePicsLoadingState() {
        profilePicsRecyclerView.setVisibility(View.GONE);
        profilePicsLoadingLayout.setVisibility(View.VISIBLE);
        profilePicsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the profile picture visible state.
     */
    private void setProfilePicsVisibleState() {
        profilePicsRecyclerView.setVisibility(View.VISIBLE);
        profilePicsLoadingLayout.setVisibility(View.GONE);
        profilePicsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the profile picture empty state.
     */
    private void setProfilePicsEmptyState(String message) {
        profilePicsRecyclerView.setVisibility(View.GONE);
        profilePicsLoadingLayout.setVisibility(View.GONE);
        profilePicsEmptyStateText.setVisibility(View.VISIBLE);
        profilePicsEmptyStateText.setText(message);
    }

    // ============== Notification state management ==============

    /**
     * Shows the notifications loading state.
     */
    private void setNotificationsLoadingState() {
        notificationsRecyclerView.setVisibility(View.GONE);
        notificationsLoadingLayout.setVisibility(View.VISIBLE);
        notificationsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the notifications visible state.
     */
    private void setNotificationsVisibleState() {
        notificationsRecyclerView.setVisibility(View.VISIBLE);
        notificationsLoadingLayout.setVisibility(View.GONE);
        notificationsEmptyStateText.setVisibility(View.GONE);
    }

    /**
     * Shows the notifications empty state.
     */
    private void setNotificationsEmptyState(String message) {
        notificationsRecyclerView.setVisibility(View.GONE);
        notificationsLoadingLayout.setVisibility(View.GONE);
        notificationsEmptyStateText.setVisibility(View.VISIBLE);
        notificationsEmptyStateText.setText(message);
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
