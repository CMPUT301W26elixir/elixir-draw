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
    public static final String EXTRA_UI_TEST_EVENT_ID = "ui_test_event_id";
    public static final String EXTRA_UI_TEST_EVENT_TITLE = "ui_test_event_title";
    public static final String EXTRA_UI_TEST_EVENT_LOCATION = "ui_test_event_location";
    public static final String EXTRA_UI_TEST_EVENT_DATE = "ui_test_event_date";
    public static final String EXTRA_UI_TEST_EVENT_PRICE = "ui_test_event_price";
    public static final String EXTRA_UI_TEST_EVENT_DEADLINE = "ui_test_event_deadline";
    public static final String EXTRA_UI_TEST_EVENT_CATEGORY = "ui_test_event_category";
    public static final String EXTRA_UI_TEST_EVENT_IDS = "ui_test_event_ids";
    public static final String EXTRA_UI_TEST_EVENT_TITLES = "ui_test_event_titles";
    public static final String EXTRA_UI_TEST_EVENT_LOCATIONS = "ui_test_event_locations";
    public static final String EXTRA_UI_TEST_EVENT_DATES = "ui_test_event_dates";
    public static final String EXTRA_UI_TEST_EVENT_PRICES = "ui_test_event_prices";
    public static final String EXTRA_UI_TEST_EVENT_DEADLINES = "ui_test_event_deadlines";
    public static final String EXTRA_UI_TEST_EVENT_CATEGORIES = "ui_test_event_categories";
    public static final String EXTRA_UI_TEST_BYPASS_PROFILE_GATE = "ui_test_bypass_profile_gate";
    public static final String EXTRA_UI_TEST_SKIP_DETAIL_NETWORK_LOAD = "ui_test_skip_detail_network_load";
    public static final String EXTRA_UI_TEST_START_ON_WAITLIST = "ui_test_start_on_waitlist";

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
    private boolean isUiTestInjectedExploreMode;
    private List<EventListItem> uiTestInjectedEvents = new ArrayList<>();

    /**
     * Initializes the explore screen, its listeners, and the first event-loading path.
     *
     * <p>AI usage note: This Javadoc was drafted with AI assistance and then reviewed
     * against the implementation by the development team.
     */
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
        currentHomeTab = resolveInitialTab(getIntent());
        setupBottomNavigation();

        eventListAdapter = new EventListAdapter(new ArrayList<>(), new EventListAdapter.OnEventClickListener() {
            /**
             * Handles on Event Click.
             */
            @Override
            public void onEventClick(EventListItem event) {
                openEventDetailScreen(event);
            }

            /**
             * Handles on Heart Click.
             */
            @Override
            public void onHeartClick(EventListItem event, int position) {
                if (event == null) {
                    return;
                }

                requireCompletedProfile(
                        () -> toggleSavedEvent(event, position),
                        buildDeferredSaveIntent(event)
                );
            }
        });
        recyclerView.setAdapter(eventListAdapter);

        isUiTestInjectedExploreMode = hasInjectedUiTestEvent(getIntent());
        if (isUiTestInjectedExploreMode) {
            showInjectedUiTestEvent();
            return;
        }

        maybeInitializeDefaultLocationFilter();
        refreshSavedEventsAndVisibleContent();
    }

    /**
     * Binds the layout views used by the explore screen.
     */
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

    /**
     * Sets up the debounced search box so filtering happens on the current screen.
     *
     * <p>AI usage note: This Javadoc was drafted with AI assistance and then reviewed
     * against the implementation by the development team.
     */
    private void setupSearchInput() {
        searchInput.setFocusable(true);
        searchInput.setFocusableInTouchMode(true);
        searchInput.setOnClickListener(null);
        searchInput.addTextChangedListener(new TextWatcher() {
            /**
             * Handles before Text Changed.
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            /**
             * Handles on Text Changed.
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim();
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> applyBrowseFilters(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }

            /**
             * Handles after Text Changed.
             */
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    /**
     * Updates up filter menu.
     */
    private void setupFilterMenu() {
        if (filterMenuButton == null) {
            return;
        }

        filterMenuButton.setOnClickListener(view -> {
            Intent intent = new Intent(this, EventFilterActivity.class);
            intent.putExtra(EventFilterActivity.EXTRA_DATE_BEGIN, filterDateText);
            intent.putExtra(EventFilterActivity.EXTRA_ADDRESS, filterAddress);
            if (filterDistanceKm != null) {
                intent.putExtra(EventFilterActivity.EXTRA_DISTANCE_KM, filterDistanceKm);
            }
            if (filterLatitude != null && filterLongitude != null) {
                intent.putExtra(EventFilterActivity.EXTRA_LATITUDE, filterLatitude);
                intent.putExtra(EventFilterActivity.EXTRA_LONGITUDE, filterLongitude);
            }
            intent.putExtra(EventFilterActivity.EXTRA_KEYWORDS, filterKeywords);
            intent.putExtra(EventFilterActivity.EXTRA_ONLY_OPEN_SPOTS, filterOnlyOpenSpots);
            if (filterMinimumCapacity != null) {
                intent.putExtra(EventFilterActivity.EXTRA_MINIMUM_CAPACITY, filterMinimumCapacity);
            }
            startActivityForResult(intent, FILTER_REQUEST_CODE);
        });
    }

    /**
     * Handles on New Intent.
     */
    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        isUiTestInjectedExploreMode = hasInjectedUiTestEvent(intent);
        if (isUiTestInjectedExploreMode) {
            showInjectedUiTestEvent();
            return;
        }
        if ("saved".equals(intent.getStringExtra("navigate_to"))) {
            currentHomeTab = BottomNavBarView.Tab.SAVED;
        }
        refreshSavedEventsAndVisibleContent();
    }

    /**
     * Handles on Resume.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (isUiTestInjectedExploreMode) {
            return;
        }
        maybeInitializeDefaultLocationFilter();
        refreshSavedEventsAndVisibleContent();
    }

    /**
     * Handles on Destroy.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationService != null) {
            notificationService.stopListening();
        }
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }

    /**
     * Updates up bottom navigation.
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
     * Returns whether h.as Injected Ui Test Event
     */
    private boolean hasInjectedUiTestEvent(Intent intent) {
        return intent != null
                && (!UiHelper.isBlank(intent.getStringExtra(EXTRA_UI_TEST_EVENT_ID))
                || hasInjectedUiTestEventList(intent));
    }

    /**
     * Shows the injected UI-test event list instead of loading live explore data.
     *
     * <p>AI usage note: This Javadoc was drafted with AI assistance and then reviewed
     * against the implementation by the development team.
     */
    private void showInjectedUiTestEvent() {
        currentHomeTab = BottomNavBarView.Tab.EXPLORE;
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }
        if (exploreContainer != null) {
            exploreContainer.setVisibility(View.VISIBLE);
        }
        if (filterPillsScrollView != null) {
            filterPillsScrollView.setVisibility(View.GONE);
        }
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        userSavedEvents = new ArrayList<>();
        isInitialBrowseLoadComplete = true;

        uiTestInjectedEvents = buildInjectedUiTestEvents(getIntent());
        renderInjectedUiTestItems(uiTestInjectedEvents, "");
    }

    /**
     * Shows explore tab.
     */
    private void showExploreTab() {
        currentHomeTab = BottomNavBarView.Tab.EXPLORE;
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }
        if (exploreContainer != null) {
            exploreContainer.setVisibility(View.VISIBLE);
        }
        rebuildFilterPills();
        refreshBrowseEvents(false);
    }

    /**
     * Opens saved tab.
     */
    private void openSavedTab() {
        currentHomeTab = BottomNavBarView.Tab.SAVED;
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.SAVED);
        EventListModeFragment fragment = EventListModeFragment.newSavedEventsInstance(new ArrayList<>(userSavedEvents));

        if (exploreContainer != null) {
            exploreContainer.setVisibility(View.GONE);
        }
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * Refreshes the saved-event state and then reloads whichever explore content is visible.
     *
     * <p>AI usage note: This Javadoc was drafted with AI assistance and then reviewed
     * against the implementation by the development team.
     */
    private void refreshSavedEventsAndVisibleContent() {
        browseController.loadSavedEventIds((savedEventIds, success) -> {
            if (!success) {
                Log.e(TAG, "Failed to refresh saved events.");
                return;
            }

            userSavedEvents = new ArrayList<>(savedEventIds == null ? new ArrayList<>() : savedEventIds);
            if (currentHomeTab == BottomNavBarView.Tab.SAVED) {
                openSavedTab();
                return;
            }

            rebuildFilterPills();
            refreshBrowseEvents(!browseController.hasCachedOpenEvents());
        });
    }

    /**
     * Handles resolve Initial Tab.
     */
    private BottomNavBarView.Tab resolveInitialTab(Intent intent) {
        return intent != null && "saved".equals(intent.getStringExtra("navigate_to"))
                ? BottomNavBarView.Tab.SAVED
                : BottomNavBarView.Tab.EXPLORE;
    }

    /**
     * Handles maybe Initialize Default Location Filter.
     */
    private void maybeInitializeDefaultLocationFilter() {
        if (hasInitializedDefaultLocationFilter || isInitializingDefaultLocationFilter) {
            return;
        }
        if (!UiHelper.isBlank(filterAddress) || filterLatitude != null || filterLongitude != null || filterDistanceKm != null) {
            hasInitializedDefaultLocationFilter = true;
            return;
        }
        if (!hasLocationPermission()) {
            isInitializingDefaultLocationFilter = true;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }

        initializeDefaultLocationFilter();
    }

    /**
     * Returns whether h.as Location Permission
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Handles initialize Default Location Filter.
     */
    private void initializeDefaultLocationFilter() {
        isInitializingDefaultLocationFilter = true;
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationProviderClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        finishDefaultLocationInitialization();
                        return;
                    }
                    reverseGeocodeDefaultLocation(location);
                })
                .addOnFailureListener(this, exception -> finishDefaultLocationInitialization());
    }

    /**
     * Handles reverse Geocode Default Location.
     */
    private void reverseGeocodeDefaultLocation(Location location) {
        new Thread(() -> {
            AndroidEventLocationGeocodingService geocodingService = new AndroidEventLocationGeocodingService(this);
            String resolvedAddress = geocodingService.reverseGeocode(
                    location.getLatitude(),
                    location.getLongitude()
            );
            runOnUiThread(() -> {
                if (!isFinishing()
                        && !isDestroyed()
                        && UiHelper.isBlank(filterAddress)
                        && filterLatitude == null
                        && filterLongitude == null
                        && !UiHelper.isBlank(resolvedAddress)) {
                    filterAddress = resolvedAddress;
                    filterLatitude = location.getLatitude();
                    filterLongitude = location.getLongitude();
                    rebuildFilterPills();
                }
                finishDefaultLocationInitialization();
            });
        }).start();
    }

    /**
     * Handles finish Default Location Initialization.
     */
    private void finishDefaultLocationInitialization() {
        isInitializingDefaultLocationFilter = false;
        hasInitializedDefaultLocationFilter = true;
        if (currentHomeTab == BottomNavBarView.Tab.EXPLORE) {
            rebuildFilterPills();
            if (browseController.hasCachedOpenEvents()) {
                applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
            }
        }
    }

    /**
     * Handles refresh Browse Events.
     */
    private void refreshBrowseEvents(boolean showLoadingState) {
        if (showLoadingState) {
            showBrowseLoadingState();
        }

        browseController.refreshOpenEvents((events, success) -> {
            if (!success) {
                if (!isInitialBrowseLoadComplete) {
                    showBrowseMessageState(getString(R.string.browse_state_error));
                }
                return;
            }

            isInitialBrowseLoadComplete = true;
            applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
        });
    }

    /**
     * Handles apply Browse Filters.
     */
    private void applyBrowseFilters(String searchTerm) {
        if (isUiTestInjectedExploreMode) {
            renderInjectedUiTestItems(uiTestInjectedEvents, searchTerm);
            return;
        }

        browseController.filterCachedBrowseEvents(
                searchTerm,
                "",
                filterKeywords,
                parseFilterDate(filterDateText),
                filterLatitude,
                filterLongitude,
                filterDistanceKm,
                filterOnlyOpenSpots,
                filterMinimumCapacity,
                userSavedEvents,
                (items, success) -> {
                    List<EventListItem> safeItems = items == null ? new ArrayList<>() : items;
                    if (!success) {
                        if (!isInitialBrowseLoadComplete) {
                            showBrowseMessageState(getString(R.string.browse_state_error));
                        }
                        return;
                    }

                    if (safeItems.isEmpty()) {
                        showBrowseMessageState(buildEmptyStateMessage(searchTerm));
                        return;
                    }

                    eventListAdapter.updateEvents(safeItems);
                    recyclerView.setVisibility(View.VISIBLE);
                    loadingIndicator.setVisibility(View.GONE);
                    stateText.setVisibility(View.GONE);
                }
        );
    }

    /**
     * Shows browse loading state.
     */
    private void showBrowseLoadingState() {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.browse_state_loading);
    }

    /**
     * Shows browse message state.
     */
    private void showBrowseMessageState(String message) {
        eventListAdapter.updateEvents(new ArrayList<>());
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(message);
    }

    /**
     * Builds empty state message.
     */
    private String buildEmptyStateMessage(String searchTerm) {
        String trimmedSearch = normalize(searchTerm);
        if (!trimmedSearch.isEmpty()) {
            return String.format(Locale.getDefault(), "No events match \"%s\".", trimmedSearch);
        }
        return getString(R.string.browse_state_empty);
    }

    /**
     * Handles on Activity Result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILTER_REQUEST_CODE || resultCode != RESULT_OK || data == null) {
            return;
        }

        filterDateText = safeString(data.getStringExtra(EventFilterActivity.EXTRA_DATE_BEGIN));
        filterAddress = safeString(data.getStringExtra(EventFilterActivity.EXTRA_ADDRESS));
        filterKeywords = safeString(data.getStringExtra(EventFilterActivity.EXTRA_KEYWORDS));
        filterLatitude = data.hasExtra(EventFilterActivity.EXTRA_LATITUDE)
                ? data.getDoubleExtra(EventFilterActivity.EXTRA_LATITUDE, 0)
                : null;
        filterLongitude = data.hasExtra(EventFilterActivity.EXTRA_LONGITUDE)
                ? data.getDoubleExtra(EventFilterActivity.EXTRA_LONGITUDE, 0)
                : null;
        filterDistanceKm = data.hasExtra(EventFilterActivity.EXTRA_DISTANCE_KM)
                ? data.getDoubleExtra(EventFilterActivity.EXTRA_DISTANCE_KM, 0)
                : null;
        filterOnlyOpenSpots = data.getBooleanExtra(EventFilterActivity.EXTRA_ONLY_OPEN_SPOTS, false);
        filterMinimumCapacity = data.hasExtra(EventFilterActivity.EXTRA_MINIMUM_CAPACITY)
                ? data.getIntExtra(EventFilterActivity.EXTRA_MINIMUM_CAPACITY, 0)
                : null;
        if (filterMinimumCapacity != null && filterMinimumCapacity <= 0) {
            filterMinimumCapacity = null;
        }

        rebuildFilterPills();
        applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    /**
     * Handles on Request Permissions Result.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST) {
            return;
        }

        isInitializingDefaultLocationFilter = false;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeDefaultLocationFilter();
            return;
        }

        hasInitializedDefaultLocationFilter = true;
        refreshSavedEventsAndVisibleContent();
    }

    /**
     * Handles rebuild Filter Pills.
     */
    private void rebuildFilterPills() {
        if (filterPillsContainer == null || filterPillsScrollView == null) {
            return;
        }

        filterPillsContainer.removeAllViews();
        addFilterPill(buildDatePillLabel(), this::clearDateFilter);
        addFilterPill(buildDistancePillLabel(), this::clearDistanceFilter);
        addFilterPill(buildOpenSpotsPillLabel(), this::clearOpenSpotsFilter);
        addFilterPill(buildMinimumCapacityPillLabel(), this::clearMinimumCapacityFilter);
        for (String keyword : splitKeywords(filterKeywords)) {
            addFilterPill(keyword, () -> removeKeywordFilter(keyword));
        }

        filterPillsScrollView.setVisibility(filterPillsContainer.getChildCount() > 0 ? View.VISIBLE : View.GONE);
    }

    /**
     * Handles add Filter Pill.
     */
    private void addFilterPill(String label, Runnable onClick) {
        if (UiHelper.isBlank(label) || filterPillsContainer == null) {
            return;
        }

        TextView pillView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                UiHelper.dpToPx(this, 40)
        );
        params.setMarginEnd(UiHelper.dpToPx(this, 10));
        pillView.setLayoutParams(params);
        pillView.setBackgroundResource(R.drawable.bg_chip_selected);
        pillView.setClickable(true);
        pillView.setFocusable(true);
        pillView.setGravity(android.view.Gravity.CENTER);
        pillView.setPadding(
                UiHelper.dpToPx(this, 18),
                0,
                UiHelper.dpToPx(this, 18),
                0
        );
        pillView.setText(label);
        pillView.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        pillView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        pillView.setTypeface(ResourcesCompat.getFont(this, R.font.varela_round_regular));
        pillView.setOnClickListener(view -> onClick.run());
        filterPillsContainer.addView(pillView);
    }

    /**
     * Clears date filter.
     */
    private void clearDateFilter() {
        filterDateText = "";
        rebuildFilterPills();
        applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    /**
     * Clears distance filter.
     */
    private void clearDistanceFilter() {
        filterDistanceKm = null;
        rebuildFilterPills();
        applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    /**
     * Handles remove Keyword Filter.
     */
    private void removeKeywordFilter(String keywordToRemove) {
        List<String> remainingKeywords = new ArrayList<>();
        for (String keyword : splitKeywords(filterKeywords)) {
            if (!keyword.equalsIgnoreCase(keywordToRemove)) {
                remainingKeywords.add(keyword);
            }
        }
        filterKeywords = String.join(" ", remainingKeywords);
        rebuildFilterPills();
        applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    /**
     * Clears open spots filter.
     */
    private void clearOpenSpotsFilter() {
        filterOnlyOpenSpots = false;
        rebuildFilterPills();
        applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    /**
     * Clears minimum capacity filter.
     */
    private void clearMinimumCapacityFilter() {
        filterMinimumCapacity = null;
        rebuildFilterPills();
        applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    /**
     * Builds date pill label.
     */
    private String buildDatePillLabel() {
        if (UiHelper.isBlank(filterDateText)) {
            return "";
        }
        try {
            return pillDateFormatter.format(pillDateParser.parse(filterDateText.trim()));
        } catch (Exception e) {
            return filterDateText.trim();
        }
    }

    /**
     * Builds distance pill label.
     */
    private String buildDistancePillLabel() {
        if (filterDistanceKm == null || filterDistanceKm <= 0) {
            return "";
        }
        if (Math.abs(filterDistanceKm - Math.rint(filterDistanceKm)) < 0.0001d) {
            return String.format(Locale.getDefault(), "%.0f km", filterDistanceKm);
        }
        return String.format(Locale.getDefault(), "%.1f km", filterDistanceKm);
    }

    /**
     * Builds open spots pill label.
     */
    private String buildOpenSpotsPillLabel() {
        return filterOnlyOpenSpots ? getString(R.string.filter_open_spots_pill) : "";
    }

    /**
     * Builds minimum capacity pill label.
     */
    private String buildMinimumCapacityPillLabel() {
        if (filterMinimumCapacity == null || filterMinimumCapacity <= 0) {
            return "";
        }
        return getString(R.string.filter_min_capacity_pill, filterMinimumCapacity);
    }

    /**
     * Handles split Keywords.
     */
    private List<String> splitKeywords(String rawKeywords) {
        String normalizedKeywords = normalize(rawKeywords);
        if (normalizedKeywords.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> tokens = new ArrayList<>();
        for (String token : Arrays.asList(normalizedKeywords.split("[,\\s]+"))) {
            String trimmed = normalize(token);
            if (!trimmed.isEmpty()) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    /**
     * Handles parse Filter Date.
     */
    private java.util.Date parseFilterDate(String rawDate) {
        if (UiHelper.isBlank(rawDate)) {
            return null;
        }
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            formatter.setLenient(false);
            return formatter.parse(rawDate.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Handles normalize.
     */
    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Handles safe String.
     */
    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Opens event detail screen.
     */
    private void openEventDetailScreen(EventListItem eventItem) {
        if (eventItem == null || UiHelper.isBlank(eventItem.getEventId())) {
            return;
        }

        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, eventItem.getEventId());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, eventItem.getTitle());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_LOCATION, eventItem.getStreet());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DATE, eventItem.getDate());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_PRICE, eventItem.getPrice());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_DEADLINE, eventItem.getDaysLeft());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_CATEGORY, eventItem.getCategory());
        if (getIntent().getBooleanExtra(EXTRA_UI_TEST_BYPASS_PROFILE_GATE, false)) {
            intent.putExtra(EventDetailActivity.EXTRA_UI_TEST_BYPASS_PROFILE_GATE, true);
        }
        if (getIntent().getBooleanExtra(EXTRA_UI_TEST_SKIP_DETAIL_NETWORK_LOAD, false)) {
            intent.putExtra(EventDetailActivity.EXTRA_UI_TEST_SKIP_NETWORK_LOAD, true);
        }
        if (getIntent().getBooleanExtra(EXTRA_UI_TEST_START_ON_WAITLIST, false)) {
            intent.putExtra(EventDetailActivity.EXTRA_UI_TEST_START_ON_WAITLIST, true);
        }
        startActivity(intent);
    }

    /**
     * Handles toggle Saved Event.
     */
    private void toggleSavedEvent(EventListItem event, int position) {
        boolean nextSavedState = event.isSaved;
        browseController.toggleSavedEvent(userSavedEvents, event.getEventId(), nextSavedState, (savedEventIds, success) -> {
            userSavedEvents = new ArrayList<>(savedEventIds == null ? new ArrayList<>() : savedEventIds);
            event.isSaved = success ? nextSavedState : !nextSavedState;
            eventListAdapter.notifyItemChanged(position);
        });
    }

    /**
     * Returns whether h.as Injected Ui Test Event List
     */
    private boolean hasInjectedUiTestEventList(Intent intent) {
        ArrayList<String> eventIds = intent.getStringArrayListExtra(EXTRA_UI_TEST_EVENT_IDS);
        return eventIds != null && !eventIds.isEmpty();
    }

    /**
     * Builds injected ui test events.
     */
    private List<EventListItem> buildInjectedUiTestEvents(Intent intent) {
        List<EventListItem> injectedItems = new ArrayList<>();
        ArrayList<String> eventIds = intent.getStringArrayListExtra(EXTRA_UI_TEST_EVENT_IDS);
        if (eventIds != null && !eventIds.isEmpty()) {
            ArrayList<String> titles = intent.getStringArrayListExtra(EXTRA_UI_TEST_EVENT_TITLES);
            ArrayList<String> locations = intent.getStringArrayListExtra(EXTRA_UI_TEST_EVENT_LOCATIONS);
            ArrayList<String> dates = intent.getStringArrayListExtra(EXTRA_UI_TEST_EVENT_DATES);
            ArrayList<String> prices = intent.getStringArrayListExtra(EXTRA_UI_TEST_EVENT_PRICES);
            ArrayList<String> deadlines = intent.getStringArrayListExtra(EXTRA_UI_TEST_EVENT_DEADLINES);
            ArrayList<String> categories = intent.getStringArrayListExtra(EXTRA_UI_TEST_EVENT_CATEGORIES);

            for (int index = 0; index < eventIds.size(); index++) {
                injectedItems.add(new EventListItem(
                        safeString(valueAt(eventIds, index)),
                        safeString(valueAt(titles, index)),
                        safeString(valueAt(locations, index)),
                        safeString(valueAt(dates, index)),
                        safeString(valueAt(prices, index)),
                        safeString(valueAt(deadlines, index)),
                        safeString(valueAt(categories, index)),
                        null
                ));
            }
            return injectedItems;
        }

        injectedItems.add(new EventListItem(
                safeString(intent.getStringExtra(EXTRA_UI_TEST_EVENT_ID)),
                safeString(intent.getStringExtra(EXTRA_UI_TEST_EVENT_TITLE)),
                safeString(intent.getStringExtra(EXTRA_UI_TEST_EVENT_LOCATION)),
                safeString(intent.getStringExtra(EXTRA_UI_TEST_EVENT_DATE)),
                safeString(intent.getStringExtra(EXTRA_UI_TEST_EVENT_PRICE)),
                safeString(intent.getStringExtra(EXTRA_UI_TEST_EVENT_DEADLINE)),
                safeString(intent.getStringExtra(EXTRA_UI_TEST_EVENT_CATEGORY)),
                null
        ));
        return injectedItems;
    }

    /**
     * Handles render Injected Ui Test Items.
     */
    private void renderInjectedUiTestItems(List<EventListItem> sourceItems, String searchTerm) {
        List<EventListItem> filteredItems = new ArrayList<>();
        String normalizedSearch = normalize(searchTerm).toLowerCase(Locale.getDefault());
        for (EventListItem item : sourceItems) {
            if (item == null) {
                continue;
            }
            if (normalizedSearch.isEmpty()
                    || item.getTitle().toLowerCase(Locale.getDefault()).contains(normalizedSearch)
                    || item.getStreet().toLowerCase(Locale.getDefault()).contains(normalizedSearch)
                    || item.getCategory().toLowerCase(Locale.getDefault()).contains(normalizedSearch)) {
                filteredItems.add(item);
            }
        }

        if (filteredItems.isEmpty()) {
            showBrowseMessageState(buildEmptyStateMessage(searchTerm));
            return;
        }

        eventListAdapter.updateEvents(filteredItems);
        recyclerView.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.GONE);
    }

    /**
     * Handles value At.
     */
    private String valueAt(ArrayList<String> values, int index) {
        if (values == null || index < 0 || index >= values.size()) {
            return "";
        }
        return values.get(index);
    }

    /**
     * Handles require Completed Profile.
     */
    private void requireCompletedProfile(Runnable onReady, Intent onboardingIntent) {
        if (getIntent().getBooleanExtra(EXTRA_UI_TEST_BYPASS_PROFILE_GATE, false)) {
            onReady.run();
            return;
        }

        userController.loadCurrentUser((user, success) -> {
            if (success && userController.hasCompletedProfile(user)) {
                onReady.run();
                return;
            }

            DeferredOnboardingNavigator.openOnboarding(this, onboardingIntent, false);
        });
    }

    /**
     * Builds deferred save intent.
     */
    private Intent buildDeferredSaveIntent(EventListItem eventItem) {
        return DeferredOnboardingNavigator.createEventActionIntent(
                this,
                eventItem.getEventId(),
                eventItem.getTitle(),
                eventItem.getStreet(),
                eventItem.getDate(),
                eventItem.getPrice(),
                eventItem.getDaysLeft(),
                eventItem.getCategory(),
                DeferredOnboardingNavigator.ACTION_AUTO_SAVE
        );
    }
}
