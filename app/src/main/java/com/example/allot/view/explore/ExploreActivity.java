package com.example.allot.view.explore;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
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
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.event.SearchEventActivity;
import com.example.allot.view.events.EventListModeFragment;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.EventListAdapter;
import com.example.allot.view.shared.EventListItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/**
 * Shows the explore screen where users can browse, search, and save events.
 */
public class ExploreActivity extends AppCompatActivity {
    private static final String TAG = "Allot_Logic";

    private ExploreController browseController;
    private EventListAdapter eventListAdapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private ProgressBar loadingIndicator;
    private TextView stateText;
    private BottomNavBarView bottomNavBar;

    private FrameLayout fragmentContainer;
    private LinearLayout exploreContainer;

    // These chips help filter the event list
    private TextView chipFortnite, chipSports, chipArts, chipScience;
    private String selectedChipFilter = "";

    private List<String> userSavedEvents = new ArrayList<>();

    /**
     * Initializes the activity, binds views, configures filters and navigation,
     * loads the current user, and displays either the explore or saved tab.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.eventsRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        stateText = findViewById(R.id.stateText);
        bottomNavBar = findViewById(R.id.bottomNavBar);
        fragmentContainer = findViewById(R.id.fragment_container);
        exploreContainer = findViewById(R.id.exploreContainer);
        browseController = new ExploreController(this);

        // Make search bar open SearchEventActivity
        searchInput.setFocusable(false);
        searchInput.setClickable(true);
        searchInput.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchEventActivity.class);
            startActivity(intent);
        });

        // Set up the chips before loading events
        chipFortnite = findViewById(R.id.chipFortnite);
        chipSports = findViewById(R.id.chipSports);
        chipArts = findViewById(R.id.chipArts);
        chipScience = findViewById(R.id.chipScience);
        setupFilterChips();

        eventListAdapter = new EventListAdapter(new ArrayList<>(), new EventListAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(EventListItem event) {
                openEventDetailScreen(event);
            }

            @Override
            public void onHeartClick(EventListItem event, int position) {
                boolean isSaving = event.isSaved;

                browseController.toggleSavedEvent(userSavedEvents, event.getEventId(), isSaving, (savedEventIds, success) -> {
                    userSavedEvents = new ArrayList<>(savedEventIds == null ? new ArrayList<>() : savedEventIds);
                    // Update the heart after the save call finishes
                    event.isSaved = success == isSaving;
                    eventListAdapter.notifyItemChanged(position);
                });
            }
        });

        recyclerView.setAdapter(eventListAdapter);
        setupBottomNavigation();

        browseController.loadSavedEventIds((savedEventIds, success) -> {
            if (success) {
                userSavedEvents = new ArrayList<>(savedEventIds);

                if ("saved".equals(getIntent().getStringExtra("navigate_to"))) {
                    openSavedTab();
                } else {
                    loadBrowseEvents("");
                }
            } else {
                Log.e(TAG, "Failed to load user.");
            }
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
            // Load the list again with the new chip filter
            loadBrowseEvents("");
        };

        chipFortnite.setOnClickListener(chipClickListener);
        chipSports.setOnClickListener(chipClickListener);
        chipArts.setOnClickListener(chipClickListener);
        chipScience.setOnClickListener(chipClickListener);

        // Show the starting chip look
        updateChipUI();
    }

    /**
     * Updates the visual state of each filter chip based on the selected filter.
     */
    private void updateChipUI() {
        chipFortnite.setBackgroundResource(getChipBackground(selectedChipFilter, chipFortnite.getText().toString()));
        chipSports.setBackgroundResource(getChipBackground(selectedChipFilter, chipSports.getText().toString()));
        chipArts.setBackgroundResource(getChipBackground(selectedChipFilter, chipArts.getText().toString()));
        chipScience.setBackgroundResource(getChipBackground(selectedChipFilter, chipScience.getText().toString()));
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
            openSavedTab();
        } else {
            showExploreTab();
        }
    }

    /**
     * Configures the bottom navigation bar and assigns tab actions.
     */
    private void setupBottomNavigation() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, v -> showExploreTab());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, v -> openSavedTab());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, v -> openMyEventsScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, v -> openProfileScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> openScanScreen());
    }

    /**
     * Shows the explore tab and reloads the browse event list.
     */
    private void showExploreTab() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
        if (exploreContainer != null) exploreContainer.setVisibility(View.VISIBLE);
        loadBrowseEvents("");
    }

    /**
     * Opens the saved events tab by displaying the shared saved-events fragment.
     */
    private void openSavedTab() {
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
     * Opens the My Events screen.
     */
    private void openMyEventsScreen() {
        AppNavigator.openMyEvents(this, false);
    }

    /**
     * Opens the Profile screen.
     */
    private void openProfileScreen() {
        AppNavigator.openProfile(this, false);
    }

    /**
     * Opens the Scan screen.
     */
    private void openScanScreen() {
        AppNavigator.openScan(this, false);
    }

    /**
     * Opens the event detail screen for the selected event list item.
     *
     * @param eventItem the selected event item
     */
    private void openEventDetailScreen(EventListItem eventItem) {
        if (eventItem == null || eventItem.eventId == null || eventItem.eventId.trim().isEmpty()) return;
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, eventItem.eventId);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, eventItem.title);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, eventItem.street);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE, eventItem.date);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE, eventItem.price);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE, eventItem.daysLeft);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, eventItem.category);
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
        browseController.loadBrowseEvents(searchTerm, selectedChipFilter, userSavedEvents, (items, success) -> {
            List<EventListItem> safeItems = items == null ? new ArrayList<>() : items;
            if (!success) {
                showBrowseMessageState(getString(R.string.browse_state_error));
                return;
            }

            if (safeItems.isEmpty()) {
                showBrowseMessageState(buildEmptyStateMessage(searchTerm, selectedChipFilter));
                return;
            }

            eventListAdapter.updateEvents(safeItems);
            recyclerView.setVisibility(View.VISIBLE);
            loadingIndicator.setVisibility(View.GONE);
            stateText.setVisibility(View.GONE);
        });
    }

    private void showBrowseLoadingState() {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.browse_state_loading);
    }

    private void showBrowseMessageState(String message) {
        eventListAdapter.updateEvents(new ArrayList<>());
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(message);
    }

    private String toggleChipFilter(String currentFilter, String clickedChipText) {
        String safeCurrentFilter = normalize(currentFilter);
        String safeClickedChip = normalize(clickedChipText);
        return safeCurrentFilter.equals(safeClickedChip) ? "" : safeClickedChip;
    }

    private int getChipBackground(String currentFilter, String chipText) {
        return normalize(currentFilter).equals(normalize(chipText))
                ? R.drawable.bg_chip_selected
                : R.drawable.bg_chip_unselected;
    }

    private String buildEmptyStateMessage(String searchTerm, String chipFilter) {
        String trimmedSearch = normalize(searchTerm);
        String trimmedFilter = normalize(chipFilter);

        if (!trimmedSearch.isEmpty() && !trimmedFilter.isEmpty()) {
            return String.format(Locale.getDefault(), "No '%s' events match \"%s\".", trimmedFilter, trimmedSearch);
        }

        if (!trimmedSearch.isEmpty()) {
            return String.format(Locale.getDefault(), "No events match \"%s\".", trimmedSearch);
        }

        if (!trimmedFilter.isEmpty()) {
            return String.format(Locale.getDefault(), "No %s events found.", trimmedFilter);
        }

        return getString(R.string.browse_state_empty);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}