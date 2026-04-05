package com.example.allot.view.explore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.controller.explore.ExploreController;
import com.example.allot.controller.shared.NotificationController;
import com.example.allot.controller.notification.NotificationService;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.event.SearchEventActivity;
import com.example.allot.view.events.EventListModeFragment;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.EventListAdapter;
import com.example.allot.view.shared.EventListItem;
import com.example.allot.view.shared.UiHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shows the explore screen where users can browse, search, and save events.
 */
public class ExploreActivity extends AppCompatActivity {
    private static final String TAG = "ExploreActivity";

    private ExploreController browseController;
    private NotificationController notificationController;
    private NotificationService notificationService;
    private EventListAdapter eventListAdapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private ProgressBar loadingIndicator;
    private TextView stateText;
    private BottomNavBarView bottomNavBar;
    private BottomNavBarView.Tab currentHomeTab = BottomNavBarView.Tab.EXPLORE;

    private FrameLayout fragmentContainer;
    private LinearLayout exploreContainer;

    // These chips help filter the event list
    private TextView chipFortnite, chipSports, chipArts, chipScience;
    private String selectedChipFilter = "";

    private List<String> userSavedEvents = new ArrayList<>();

    /**
     * Initializes the activity, binds views, configures filters and navigation,
     * and starts the notification background listeners.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        browseController = new ExploreController(this);

        // Start background notification listening (Sub-collection logic)
        notificationController = new NotificationController(this);
        notificationController.startListening(browseController.getCurrentDeviceId());

        // Start background notification listening (Top-level collection logic)
        notificationService = new NotificationService(this);
        notificationService.startListening();

        bindViews();
        setupSearch();
        setupFilterChips();
        setupBottomNavigation();

        currentHomeTab = resolveInitialTab(getIntent());
        refreshSavedEventsAndVisibleContent();
    }

    /**
     * Binds all layout views to their corresponding fields.
     */
    private void bindViews() {
        recyclerView = findViewById(R.id.eventsRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        stateText = findViewById(R.id.stateText);
        bottomNavBar = findViewById(R.id.bottomNavBar);
        fragmentContainer = findViewById(R.id.fragment_container);
        exploreContainer = findViewById(R.id.exploreContainer);

        chipFortnite = findViewById(R.id.chipFortnite);
        chipSports = findViewById(R.id.chipSports);
        chipArts = findViewById(R.id.chipArts);
        chipScience = findViewById(R.id.chipScience);

        eventListAdapter = new EventListAdapter(new ArrayList<>(), new EventListAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(EventListItem event) {
                openEventDetailScreen(event);
            }

            @Override
            public void onHeartClick(EventListItem event, int position) {
                boolean isSaving = event.isSaved();
                browseController.toggleSavedEvent(userSavedEvents, event.getEventId(), !isSaving, (savedEventIds, success) -> {
                    if (success) {
                        userSavedEvents = new ArrayList<>(savedEventIds == null ? new ArrayList<>() : savedEventIds);
                        event.isSaved = !isSaving;
                        eventListAdapter.notifyItemChanged(position);
                    }
                });
            }
        });
        recyclerView.setAdapter(eventListAdapter);
    }

    /**
     * Sets up the search input field to open the search activity.
     */
    private void setupSearch() {
        searchInput.setFocusable(false);
        searchInput.setClickable(true);
        searchInput.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchEventActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Sets up click listeners for all filter chips and updates the chip UI.
     * Clicking an already-selected chip removes the filter.
     */
    private void setupFilterChips() {
        View.OnClickListener chipClickListener = view -> {
            TextView clickedChip = (TextView) view;
            String clickedText = clickedChip.getText().toString();
            selectedChipFilter = toggleChipFilter(selectedChipFilter, clickedText);

            updateChipUI();
            loadBrowseEvents("");
        };

        if (chipFortnite != null) chipFortnite.setOnClickListener(chipClickListener);
        if (chipSports != null) chipSports.setOnClickListener(chipClickListener);
        if (chipArts != null) chipArts.setOnClickListener(chipClickListener);
        if (chipScience != null) chipScience.setOnClickListener(chipClickListener);

        updateChipUI();
    }

    /**
     * Updates the visual state of each filter chip based on the selected filter.
     */
    private void updateChipUI() {
        if (chipFortnite != null) chipFortnite.setBackgroundResource(getChipBackground(selectedChipFilter, "Fortnite"));
        if (chipSports != null) chipSports.setBackgroundResource(getChipBackground(selectedChipFilter, getString(R.string.chip_sports)));
        if (chipArts != null) chipArts.setBackgroundResource(getChipBackground(selectedChipFilter, getString(R.string.chip_arts)));
        if (chipScience != null) chipScience.setBackgroundResource(getChipBackground(selectedChipFilter, getString(R.string.chip_science)));
    }

    /**
     * Handles new intents delivered to the activity and switches to the saved tab
     * when requested.
     *
     * @param intent the new intent delivered to the activity
     */
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if ("saved".equals(intent.getStringExtra("navigate_to"))) {
            currentHomeTab = BottomNavBarView.Tab.SAVED;
        }
        refreshSavedEventsAndVisibleContent();
    }

    /**
     * Refreshes the saved event list and currently visible tab on resume.
     */
    @Override
    protected void onResume() {
        super.onResume();
        refreshSavedEventsAndVisibleContent();
    }

    /**
     * Stops the background notification listeners when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationService != null) {
            notificationService.stopListening();
        }
        if (notificationController != null) {
            notificationController.stopListening();
        }
    }

    /**
     * Configures the bottom navigation bar and assigns tab actions.
     */
    private void setupBottomNavigation() {
        bottomNavBar.setSelectedTab(currentHomeTab);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, v -> showExploreTab());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, v -> openSavedTab());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, v -> AppNavigator.openMyEvents(this, false));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, v -> AppNavigator.openProfile(this, false));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> AppNavigator.openScan(this, false));
    }

    /**
     * Shows the explore tab and reloads the browse event list.
     */
    private void showExploreTab() {
        currentHomeTab = BottomNavBarView.Tab.EXPLORE;
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
        if (exploreContainer != null) exploreContainer.setVisibility(View.VISIBLE);
        loadBrowseEvents("");
    }

    /**
     * Opens the saved events tab by displaying the shared saved-events fragment.
     */
    private void openSavedTab() {
        currentHomeTab = BottomNavBarView.Tab.SAVED;
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.SAVED);
        EventListModeFragment fragment = EventListModeFragment.newSavedEventsInstance(new ArrayList<>(userSavedEvents));

        if (exploreContainer != null) exploreContainer.setVisibility(View.GONE);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * Reloads the user's saved events from Firestore and refreshes the current view.
     */
    private void refreshSavedEventsAndVisibleContent() {
        browseController.loadSavedEventIds((savedEventIds, success) -> {
            if (success) {
                userSavedEvents = new ArrayList<>(savedEventIds == null ? new ArrayList<>() : savedEventIds);
                if (currentHomeTab == BottomNavBarView.Tab.SAVED) {
                    openSavedTab();
                } else {
                    loadBrowseEvents("");
                }
            }
        });
    }

    /**
     * Resolves the starting tab based on navigation parameters in the intent.
     *
     * @param intent the intent that started the activity
     * @return the resolved starting tab
     */
    private BottomNavBarView.Tab resolveInitialTab(Intent intent) {
        return intent != null && "saved".equals(intent.getStringExtra("navigate_to"))
                ? BottomNavBarView.Tab.SAVED
                : BottomNavBarView.Tab.EXPLORE;
    }

    /**
     * Opens the event detail screen for the selected event list item.
     *
     * @param eventItem the selected event item
     */
    private void openEventDetailScreen(EventListItem eventItem) {
        if (eventItem == null || eventItem.getEventId() == null) return;
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, eventItem.getEventId());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, eventItem.getTitle());
        startActivity(intent);
    }

    /**
     * Loads browseable events using the provided search term and currently
     * selected chip filter.
     *
     * @param searchTerm the text used to search events
     */
    private void loadBrowseEvents(String searchTerm) {
        showBrowseLoadingState();
        // Updated call to match the team's new 9-parameter signature in ExploreController while passing your filters
        browseController.loadBrowseEvents(
                searchTerm,
                selectedChipFilter,
                null, // keywords
                null, // startDate
                null, // latitude
                null, // longitude
                null, // distanceKm
                userSavedEvents,
                (items, success) -> {
                    loadingIndicator.setVisibility(View.GONE);
                    if (!success) {
                        showBrowseMessageState(getString(R.string.browse_state_error));
                        return;
                    }

                    if (items == null || items.isEmpty()) {
                        showBrowseMessageState(buildEmptyStateMessage(searchTerm, selectedChipFilter));
                        return;
                    }

                    stateText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    eventListAdapter.updateEvents(items);
                }
        );
    }

    /**
     * Updates the UI to show the loading state for event browsing.
     */
    private void showBrowseLoadingState() {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.browse_state_loading);
    }

    /**
     * Updates the UI to show a specific message state, such as an empty list or error.
     *
     * @param message the message to display
     */
    private void showBrowseMessageState(String message) {
        eventListAdapter.updateEvents(new ArrayList<>());
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(message);
    }

    /**
     * Toggles a chip filter value. If the filter is already selected, it is removed.
     *
     * @param currentFilter the current active filter
     * @param clickedChipText the text of the chip that was clicked
     * @return the new active filter value
     */
    private String toggleChipFilter(String currentFilter, String clickedChipText) {
        String clicked = clickedChipText.trim();
        return currentFilter.equals(clicked) ? "" : clicked;
    }

    /**
     * Determines the correct background drawable for a filter chip based on its selection state.
     *
     * @param currentFilter the currently active filter
     * @param chipText the text of the chip being styled
     * @return the drawable resource ID for the chip background
     */
    private int getChipBackground(String currentFilter, String chipText) {
        return currentFilter.equals(chipText.trim())
                ? R.drawable.bg_chip_selected
                : R.drawable.bg_chip_unselected;
    }

    /**
     * Builds a user-friendly empty state message based on the current search and filters.
     *
     * @param searchTerm the search query entered by the user
     * @param chipFilter the active category filter
     * @return the empty state message string
     */
    private String buildEmptyStateMessage(String searchTerm, String chipFilter) {
        if (!searchTerm.isEmpty()) {
            return String.format(Locale.getDefault(), "No events match \"%s\".", searchTerm);
        }
        if (!chipFilter.isEmpty()) {
            return String.format(Locale.getDefault(), "No %s events found.", chipFilter);
        }
        return getString(R.string.browse_state_empty);
    }

    /**
     * Normalizes a string by trimming whitespace and handling null values.
     *
     * @param value the string to normalize
     * @return the normalized string, or an empty string if null
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}