package com.example.allot.view.explore;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.controller.explore.ExploreController;
import com.example.allot.controller.notification.NotificationService;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.event.SearchEventActivity;
import com.example.allot.view.events.EventListModeFragment;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.EventListAdapter;
import com.example.allot.view.shared.EventListItem;
import com.example.allot.view.shared.UiHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shows the explore screen where users can browse, search, and save events.
 */
public class ExploreActivity extends AppCompatActivity {
    private static final String TAG = "ExploreActivity";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final double DEFAULT_DISTANCE_KM = 50.0;

    private ExploreController browseController;
    private EventListAdapter eventListAdapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private ProgressBar loadingIndicator;
    private TextView stateText;
    private BottomNavBarView bottomNavBar;
    private BottomNavBarView.Tab currentHomeTab = BottomNavBarView.Tab.EXPLORE;

    private FrameLayout fragmentContainer;
    private LinearLayout exploreContainer;
    private LinearLayout filterPillsContainer;
    private HorizontalScrollView filterPillsScrollView;

    private NotificationService notificationService;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private String filterKeywords;
    private String filterAddress;
    private Double filterLatitude;
    private Double filterLongitude;
    private Double filterDistanceKm;
    private String selectedChipFilter = "";

    private List<String> userSavedEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        browseController = new ExploreController(this);
        notificationService = new NotificationService(this);
        notificationService.startListening();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        bindViews();
        setupSearch();
        setupFilterChips();
        setupBottomNavigation();

        currentHomeTab = resolveInitialTab(getIntent());
        refreshSavedEventsAndVisibleContent();
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.eventsRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        stateText = findViewById(R.id.stateText);
        bottomNavBar = findViewById(R.id.bottomNavBar);
        fragmentContainer = findViewById(R.id.fragment_container);
        exploreContainer = findViewById(R.id.exploreContainer);
        
        // These IDs must exist in activity_main.xml or be removed if unused
        // Restoring standard chip IDs
        View chipFortnite = findViewById(R.id.chipFortnite);
        View chipSports = findViewById(R.id.chipSports);
        View chipArts = findViewById(R.id.chipArts);
        View chipScience = findViewById(R.id.chipScience);

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

    private void setupSearch() {
        searchInput.setFocusable(false);
        searchInput.setClickable(true);
        searchInput.setOnClickListener(v -> {
            Intent intent = new Intent(this, SearchEventActivity.class);
            startActivity(intent);
        });
    }

    private void setupFilterChips() {
        View.OnClickListener chipClickListener = view -> {
            TextView clickedChip = (TextView) view;
            String clickedText = clickedChip.getText().toString();
            selectedChipFilter = toggleChipFilter(selectedChipFilter, clickedText);
            updateChipUI();
            loadBrowseEvents("");
        };

        findViewById(R.id.chipFortnite).setOnClickListener(chipClickListener);
        findViewById(R.id.chipSports).setOnClickListener(chipClickListener);
        findViewById(R.id.chipArts).setOnClickListener(chipClickListener);
        findViewById(R.id.chipScience).setOnClickListener(chipClickListener);

        updateChipUI();
    }

    private void updateChipUI() {
        TextView chipFortnite = findViewById(R.id.chipFortnite);
        TextView chipSports = findViewById(R.id.chipSports);
        TextView chipArts = findViewById(R.id.chipArts);
        TextView chipScience = findViewById(R.id.chipScience);

        chipFortnite.setBackgroundResource(getChipBackground(selectedChipFilter, "Fortnite"));
        chipSports.setBackgroundResource(getChipBackground(selectedChipFilter, getString(R.string.chip_sports)));
        chipArts.setBackgroundResource(getChipBackground(selectedChipFilter, getString(R.string.chip_arts)));
        chipScience.setBackgroundResource(getChipBackground(selectedChipFilter, getString(R.string.chip_science)));
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if ("saved".equals(intent.getStringExtra("navigate_to"))) {
            currentHomeTab = BottomNavBarView.Tab.SAVED;
        }
        refreshSavedEventsAndVisibleContent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshSavedEventsAndVisibleContent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationService != null) {
            notificationService.stopListening();
        }
    }

    private void setupBottomNavigation() {
        bottomNavBar.setSelectedTab(currentHomeTab);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, v -> showExploreTab());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, v -> openSavedTab());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, v -> AppNavigator.openMyEvents(this, false));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, v -> AppNavigator.openProfile(this, false));
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> AppNavigator.openScan(this, false));
    }

    private void showExploreTab() {
        currentHomeTab = BottomNavBarView.Tab.EXPLORE;
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
        if (exploreContainer != null) exploreContainer.setVisibility(View.VISIBLE);
        loadBrowseEvents("");
    }

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

    private BottomNavBarView.Tab resolveInitialTab(Intent intent) {
        return intent != null && "saved".equals(intent.getStringExtra("navigate_to"))
                ? BottomNavBarView.Tab.SAVED
                : BottomNavBarView.Tab.EXPLORE;
    }

    private void openEventDetailScreen(EventListItem eventItem) {
        if (eventItem == null || eventItem.getEventId() == null) return;
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, eventItem.getEventId());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, eventItem.getTitle());
        startActivity(intent);
    }

    private void loadBrowseEvents(String searchTerm) {
        showBrowseLoadingState();
        browseController.loadBrowseEvents(
                searchTerm,
                selectedChipFilter,
                filterKeywords,
                null, // startDate
                filterLatitude,
                filterLongitude,
                filterDistanceKm,
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
        String clicked = clickedChipText.trim();
        return currentFilter.equals(clicked) ? "" : clicked;
    }

    private int getChipBackground(String currentFilter, String chipText) {
        return currentFilter.equals(chipText.trim())
                ? R.drawable.bg_chip_selected
                : R.drawable.bg_chip_unselected;
    }

    private String buildEmptyStateMessage(String searchTerm, String chipFilter) {
        if (!UiHelper.isBlank(searchTerm)) {
            return String.format(Locale.getDefault(), "No events match \"%s\".", searchTerm);
        }
        if (!UiHelper.isBlank(chipFilter)) {
            return String.format(Locale.getDefault(), "No %s events found.", chipFilter);
        }
        return getString(R.string.browse_state_empty);
    }
}
