package com.example.allot.view.explore;

import android.content.Intent;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.controller.explore.ExploreController;
import com.example.allot.view.event.EventDetailActivity;
import com.example.allot.view.events.EventListModeFragment;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.BottomNavBarView;
import com.example.allot.view.shared.EventListAdapter;
import com.example.allot.view.shared.EventListItem;
import com.example.allot.view.shared.UiHelper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
/**
 * Shows the explore screen where users can browse, search, and save events.
 */
public class ExploreActivity extends AppCompatActivity {
    private static final String TAG = "Allot_Logic";
    private static final int FILTER_REQUEST_CODE = 4102;
    private static final int SEARCH_DEBOUNCE_MS = 150;

    private ExploreController browseController;
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

    private String filterDateText = "";
    private String filterAddress = "";
    private Double filterLatitude;
    private Double filterLongitude;
    private Double filterDistanceKm;
    private String filterKeywords = "";

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private final SimpleDateFormat pillDateParser = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private final SimpleDateFormat pillDateFormatter = new SimpleDateFormat("MMM d", Locale.getDefault());

    private List<String> userSavedEvents = new ArrayList<>();
    private boolean isInitialBrowseLoadComplete;

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
        filterMenuButton = findViewById(R.id.filterMenuButton);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        stateText = findViewById(R.id.stateText);
        bottomNavBar = findViewById(R.id.bottomNavBar);
        filterPillsScrollView = findViewById(R.id.filterPillsScrollView);
        filterPillsContainer = findViewById(R.id.filterPillsContainer);
        fragmentContainer = findViewById(R.id.fragment_container);
        exploreContainer = findViewById(R.id.exploreContainer);
        browseController = new ExploreController(this);
        pillDateParser.setLenient(false);

        setupSearchInput();
        setupFilterMenu();
        currentHomeTab = resolveInitialTab(getIntent());

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
        refreshSavedEventsAndVisibleContent();
    }

    /**
     * Sets up the search input to filter on this screen.
     */
    private void setupSearchInput() {
        searchInput.setFocusable(true);
        searchInput.setFocusableInTouchMode(true);
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s == null ? "" : s.toString().trim();
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> applyBrowseFilters(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

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
            startActivityForResult(intent, FILTER_REQUEST_CODE);
        });
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

    @Override
    protected void onResume() {
        super.onResume();
        refreshSavedEventsAndVisibleContent();
    }

    /**
     * Configures the bottom navigation bar and assigns tab actions.
     */
    private void setupBottomNavigation() {
        bottomNavBar.setSelectedTab(currentHomeTab);
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
        currentHomeTab = BottomNavBarView.Tab.EXPLORE;
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
        if (exploreContainer != null) exploreContainer.setVisibility(View.VISIBLE);
        rebuildFilterPills();
        refreshBrowseEvents(false);
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

    private BottomNavBarView.Tab resolveInitialTab(Intent intent) {
        return intent != null && "saved".equals(intent.getStringExtra("navigate_to"))
                ? BottomNavBarView.Tab.SAVED
                : BottomNavBarView.Tab.EXPLORE;
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

    private void applyBrowseFilters(String searchTerm) {
        browseController.filterCachedBrowseEvents(
                searchTerm,
                "",
                filterKeywords,
                parseFilterDate(filterDateText),
                filterLatitude,
                filterLongitude,
                filterDistanceKm,
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
                showBrowseMessageState(buildEmptyStateMessage(searchTerm, ""));
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

        rebuildFilterPills();
        applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    private void rebuildFilterPills() {
        if (filterPillsContainer == null || filterPillsScrollView == null) {
            return;
        }

        filterPillsContainer.removeAllViews();
        addFilterPill(buildDatePillLabel(), this::clearDateFilter);
        addFilterPill(buildDistancePillLabel(), this::clearDistanceFilter);

        for (String keyword : splitKeywords(filterKeywords)) {
            addFilterPill(keyword, () -> removeKeywordFilter(keyword));
        }

        filterPillsScrollView.setVisibility(filterPillsContainer.getChildCount() > 0 ? View.VISIBLE : View.GONE);
    }

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

    private void clearDateFilter() {
        filterDateText = "";
        rebuildFilterPills();
        applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

    private void clearDistanceFilter() {
        filterDistanceKm = null;
        rebuildFilterPills();
        applyBrowseFilters(searchInput.getText() == null ? "" : searchInput.getText().toString());
    }

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

    private String buildDistancePillLabel() {
        if (filterDistanceKm == null || filterDistanceKm <= 0) {
            return "";
        }
        if (Math.abs(filterDistanceKm - Math.rint(filterDistanceKm)) < 0.0001d) {
            return String.format(Locale.getDefault(), "%.0f km", filterDistanceKm);
        }
        return String.format(Locale.getDefault(), "%.1f km", filterDistanceKm);
    }

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

    private java.util.Date parseFilterDate(String rawDate) {
        if (rawDate == null || rawDate.trim().isEmpty()) {
            return null;
        }
        try {
            java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            formatter.setLenient(false);
            return formatter.parse(rawDate.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}
