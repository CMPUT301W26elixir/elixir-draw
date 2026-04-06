package com.example.allot.view.explore;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.allot.R;
import com.example.allot.controller.event.AndroidEventLocationGeocodingService;
import com.example.allot.controller.explore.ExploreController;
import com.example.allot.controller.notification.NotificationService;
import com.example.allot.controller.shared.UserController;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.events.EventListModeFragment;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.DeferredOnboardingNavigator;
import com.example.allot.view.shared.EventListAdapter;
import com.example.allot.view.shared.EventListItem;
import com.example.allot.view.shared.UiHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Shows the explore screen where users can browse, search, and save events.
 */
public class ExploreActivity extends AppCompatActivity {
    private static final String TAG = "ExploreActivity";
    private static final int FILTER_REQUEST_CODE = 4102;
    private static final int LOCATION_PERMISSION_REQUEST = 4103;
    private static final int SEARCH_DEBOUNCE_MS = 150;
    private static final double DEFAULT_DISTANCE_KM = 50.0;

    private ExploreController browseController;
    private NotificationService notificationService;
    private EventListAdapter eventListAdapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private ImageButton filterMenuButton;
    private ProgressBar loadingIndicator;
    private TextView stateText;
    private BottomNavBarView bottomNavBar;
    private BottomNavBarView.Tab currentHomeTab = BottomNavBarView.Tab.EXPLORE;
    private HorizontalScrollView filterPillsScrollView;
    private LinearLayout filterPillsContainer;
    private FrameLayout fragmentContainer;
    private LinearLayout exploreContainer;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private UserController userController;

    private String filterDateText = "";
    private String filterAddress = "";
    private Double filterLatitude;
    private Double filterLongitude;
    private Double filterDistanceKm;
    private String filterKeywords = "";
    private boolean filterOnlyOpenSpots;
    private Integer filterMinimumCapacity;

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private final SimpleDateFormat pillDateParser = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat pillDateFormatter = new SimpleDateFormat("MMM d", Locale.getDefault());

    private List<String> userSavedEvents = new ArrayList<>();
    private boolean isInitialBrowseLoadComplete;
    private boolean hasInitializedDefaultLocationFilter;
    private boolean isInitializingDefaultLocationFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        browseController = new ExploreController(this);
        userController = new UserController(this);
        notificationService = new NotificationService(this);
        notificationService.startListening();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        pillDateParser.setLenient(false);

        bindViews();
        setupSearchInput();
        setupFilterMenu();
        setupBottomNavigation();

        currentHomeTab = resolveInitialTab(getIntent());

        eventListAdapter = new EventListAdapter(new ArrayList<>(), new EventListAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(EventListItem event) {
                openEventDetailScreen(event);
            }

            @Override
            public void onHeartClick(EventListItem event, int position) {
                if (event == null) return;
                requireCompletedProfile(
                        () -> toggleSavedEvent(event, position),
                        buildDeferredSaveIntent(event)
                );
            }
        });
        recyclerView.setAdapter(eventListAdapter);

        maybeInitializeDefaultLocationFilter();
        refreshSavedEventsAndVisibleContent();
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.eventsRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        filterMenuButton = findViewById(R.id.filterMenuButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        stateText = findViewById(R.id.stateText);
        bottomNavBar = findViewById(R.id.bottomNavBar);
        filterPillsScrollView = findViewById(R.id.filterPillsScrollView);
        filterPillsContainer = findViewById(R.id.filterPillsContainer);
        fragmentContainer = findViewById(R.id.fragment_container);
        exploreContainer = findViewById(R.id.exploreContainer);
    }

    private void setupSearchInput() {
        searchInput.setFocusable(true);
        searchInput.setFocusableInTouchMode(true);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim();
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> applyBrowseFilters(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterMenu() {
        if (filterMenuButton == null) return;
        filterMenuButton.setOnClickListener(view -> {
            // Intent to Filter Activity would go here
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        maybeInitializeDefaultLocationFilter();
        refreshSavedEventsAndVisibleContent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationService != null) notificationService.stopListening();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
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
        rebuildFilterPills();
        refreshBrowseEvents(false);
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
            if (!success) return;
            userSavedEvents = new ArrayList<>(savedEventIds == null ? new ArrayList<>() : savedEventIds);
            if (currentHomeTab == BottomNavBarView.Tab.SAVED) {
                openSavedTab();
            } else {
                rebuildFilterPills();
                refreshBrowseEvents(!browseController.hasCachedOpenEvents());
            }
        });
    }

    private void maybeInitializeDefaultLocationFilter() {
        if (hasInitializedDefaultLocationFilter || isInitializingDefaultLocationFilter) return;
        if (!UiHelper.isBlank(filterAddress) || filterLatitude != null || filterLongitude != null || filterDistanceKm != null) {
            hasInitializedDefaultLocationFilter = true;
            return;
        }
        if (!hasLocationPermission()) {
            isInitializingDefaultLocationFilter = true;
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }
        initializeDefaultLocationFilter();
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void initializeDefaultLocationFilter() {
        isInitializingDefaultLocationFilter = true;
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        finishDefaultLocationInitialization();
                        return;
                    }
                    reverseGeocodeDefaultLocation(location);
                })
                .addOnFailureListener(this, exception -> finishDefaultLocationInitialization());
    }

    private void reverseGeocodeDefaultLocation(Location location) {
        new Thread(() -> {
            AndroidEventLocationGeocodingService geocodingService = new AndroidEventLocationGeocodingService(this);
            String resolvedAddress = geocodingService.reverseGeocode(location.getLatitude(), location.getLongitude());
            runOnUiThread(() -> {
                if (!isFinishing() && !isDestroyed() && UiHelper.isBlank(filterAddress)) {
                    filterAddress = resolvedAddress;
                    filterLatitude = location.getLatitude();
                    filterLongitude = location.getLongitude();
                    filterDistanceKm = DEFAULT_DISTANCE_KM;
                    rebuildFilterPills();
                }
                finishDefaultLocationInitialization();
            });
        }).start();
    }

    private void finishDefaultLocationInitialization() {
        isInitializingDefaultLocationFilter = false;
        hasInitializedDefaultLocationFilter = true;
        if (currentHomeTab == BottomNavBarView.Tab.EXPLORE) {
            rebuildFilterPills();
            if (browseController.hasCachedOpenEvents()) {
                applyBrowseFilters(searchInput.getText().toString());
            }
        }
    }

    private void refreshBrowseEvents(boolean showLoadingState) {
        if (showLoadingState) showBrowseLoadingState();
        browseController.refreshOpenEvents((events, success) -> {
            if (!success) {
                if (!isInitialBrowseLoadComplete) showBrowseMessageState(getString(R.string.browse_state_error));
                return;
            }
            isInitialBrowseLoadComplete = true;
            applyBrowseFilters(searchInput.getText().toString());
        });
    }

    private void applyBrowseFilters(String searchTerm) {
        browseController.loadBrowseEvents(
                searchTerm,
                "",
                filterKeywords,
                parseFilterDate(filterDateText),
                filterLatitude,
                filterLongitude,
                filterDistanceKm,
                userSavedEvents,
                (items, success) -> {
                    loadingIndicator.setVisibility(View.GONE);
                    if (!success) {
                        if (!isInitialBrowseLoadComplete) showBrowseMessageState(getString(R.string.browse_state_error));
                        return;
                    }
                    if (items == null || items.isEmpty()) {
                        showBrowseMessageState(buildEmptyStateMessage(searchTerm));
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

    private String buildEmptyStateMessage(String searchTerm) {
        String trimmedSearch = normalize(searchTerm);
        if (!trimmedSearch.isEmpty()) {
            return String.format(Locale.getDefault(), "No events match \"%s\".", trimmedSearch);
        }
        return getString(R.string.browse_state_empty);
    }

    private void rebuildFilterPills() {
        if (filterPillsContainer == null) return;
        filterPillsContainer.removeAllViews();
        // Pills logic would go here
        if (filterPillsScrollView != null) {
            filterPillsScrollView.setVisibility(filterPillsContainer.getChildCount() > 0 ? View.VISIBLE : View.GONE);
        }
    }

    private java.util.Date parseFilterDate(String rawDate) {
        if (UiHelper.isBlank(rawDate)) return null;
        try {
            return pillDateParser.parse(rawDate.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private BottomNavBarView.Tab resolveInitialTab(Intent intent) {
        return intent != null && "saved".equals(intent.getStringExtra("navigate_to"))
                ? BottomNavBarView.Tab.SAVED : BottomNavBarView.Tab.EXPLORE;
    }

    private void openEventDetailScreen(EventListItem eventItem) {
        if (eventItem == null || UiHelper.isBlank(eventItem.getEventId())) return;
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, eventItem.getEventId());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, eventItem.getTitle());
        startActivity(intent);
    }

    private void toggleSavedEvent(EventListItem event, int position) {
        boolean nextState = !event.isSaved;
        browseController.toggleSavedEvent(userSavedEvents, event.getEventId(), nextState, (savedEventIds, success) -> {
            if (success) {
                userSavedEvents = new ArrayList<>(savedEventIds == null ? new ArrayList<>() : savedEventIds);
                event.isSaved = nextState;
                eventListAdapter.notifyItemChanged(position);
            }
        });
    }

    private void requireCompletedProfile(Runnable onReady, Intent onboardingIntent) {
        userController.loadOrCreateUser((user, success) -> {
            if (success) onReady.run();
            else DeferredOnboardingNavigator.openOnboarding(this, onboardingIntent, false);
        });
    }

    private Intent buildDeferredSaveIntent(EventListItem eventItem) {
        return DeferredOnboardingNavigator.createEventActionIntent(
                this, eventItem.getEventId(), eventItem.getTitle(), eventItem.getStreet(),
                eventItem.getDate(), eventItem.getPrice(), eventItem.getDaysLeft(),
                eventItem.getCategory(), DeferredOnboardingNavigator.ACTION_AUTO_SAVE
        );
    }
}
