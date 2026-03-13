package com.example.allot.view;

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

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.allot.R;
import com.example.allot.controller.EventController;
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Allot_Logic";

    private UserController userController;
    private EventController eventController;
    private EventListAdapter eventListAdapter;
    private RecyclerView recyclerView;
    private EditText searchInput;
    private ProgressBar loadingIndicator;
    private TextView stateText;
    private BottomNavBarView bottomNavBar;

    private FrameLayout fragmentContainer;
    private LinearLayout exploreContainer;

    // Added Variables for the Filters
    private TextView chipFortnite, chipSports, chipArts, chipScience;
    private String selectedChipFilter = "";

    private List<String> userSavedEvents = new ArrayList<>();

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

        // Bind the chips
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

                if (isSaving && !userSavedEvents.contains(event.eventId)) {
                    userSavedEvents.add(event.eventId);
                } else if (!isSaving) {
                    userSavedEvents.remove(event.eventId);
                }

                userController.toggleSavedEvent(event.eventId, isSaving, (success, result) -> {
                    if (!success) {
                        event.isSaved = !isSaving;
                        eventListAdapter.notifyItemChanged(position);
                        if (isSaving) {
                            userSavedEvents.remove(event.eventId);
                        } else if (!userSavedEvents.contains(event.eventId)) {
                            userSavedEvents.add(event.eventId);
                        }
                    }
                });
            }
        });

        recyclerView.setAdapter(eventListAdapter);
        setupBottomNavigation();

        eventController = new EventController();
        setupSearchInput();

        userController = new UserController(this);
        userController.loadOrCreateUser((user, success) -> {
            if (success && user != null) {
                userSavedEvents = user.getSavedEvents() != null ? user.getSavedEvents() : new ArrayList<>();

                if ("saved".equals(getIntent().getStringExtra("navigate_to"))) {
                    openSavedTab();
                } else {
                    loadBrowseEvents(searchInput.getText().toString());
                }
            } else {
                Log.e(TAG, "Failed to load user.");
            }
        });
    }

    // --- FILTER CHIP LOGIC ---
    private void setupFilterChips() {
        View.OnClickListener chipClickListener = view -> {
            TextView clickedChip = (TextView) view;
            String clickedText = clickedChip.getText().toString();

            // Toggle logic: If they click the active chip, turn it off. Otherwise, activate it.
            if (selectedChipFilter.equals(clickedText)) {
                selectedChipFilter = "";
            } else {
                selectedChipFilter = clickedText;
            }

            updateChipUI();
            loadBrowseEvents(searchInput.getText().toString()); // Refresh the list
        };

        chipFortnite.setOnClickListener(chipClickListener);
        chipSports.setOnClickListener(chipClickListener);
        chipArts.setOnClickListener(chipClickListener);
        chipScience.setOnClickListener(chipClickListener);

        updateChipUI(); // Set initial backgrounds
    }

    private void updateChipUI() {
        chipFortnite.setBackgroundResource(selectedChipFilter.equals(chipFortnite.getText().toString()) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipSports.setBackgroundResource(selectedChipFilter.equals(chipSports.getText().toString()) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipArts.setBackgroundResource(selectedChipFilter.equals(chipArts.getText().toString()) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipScience.setBackgroundResource(selectedChipFilter.equals(chipScience.getText().toString()) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
    }
    // -------------------------

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if ("saved".equals(intent.getStringExtra("navigate_to"))) {
            openSavedTab();
        } else {
            showExploreTab();
        }
    }

    private void setupBottomNavigation() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.EXPLORE, v -> showExploreTab());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SAVED, v -> openSavedTab());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.MY_EVENTS, v -> openMyEventsScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.PROFILE, v -> openProfileScreen());
        bottomNavBar.setOnTabClickListener(BottomNavBarView.Tab.SCAN, view -> openScanScreen());
    }

    private void showExploreTab() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
        if (exploreContainer != null) exploreContainer.setVisibility(View.VISIBLE);
        loadBrowseEvents(searchInput.getText().toString());
    }

    private void openSavedTab() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.SAVED);
        SavedEventsFragment fragment = new SavedEventsFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("saved_ids", new ArrayList<>(userSavedEvents));
        fragment.setArguments(args);

        if (exploreContainer != null) exploreContainer.setVisibility(View.GONE);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    private void openMyEventsScreen() {
        Intent intent = new Intent(this, MyEventsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void openProfileScreen() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void openScanScreen() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

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

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable editable) { loadBrowseEvents(editable.toString()); }
        });
    }

    private void loadBrowseEvents(String searchTerm) {
        setLoadingState();
        eventController.searchOpenEvents(searchTerm, new EventController.EventListCallback() {
            @Override
            public void onCallback(List<Event> events) {
                List<EventListItem> browseItems = new ArrayList<>();
                for (Event event : events) {

                    // --- APPLY THE SELECTED CHIP FILTER ---
                    if (!selectedChipFilter.isEmpty()) {
                        String title = event.title != null ? event.title.toLowerCase() : "";
                        String category = event.category != null ? event.category.toLowerCase() : "";
                        String desc = event.description != null ? event.description.toLowerCase() : "";
                        String filter = selectedChipFilter.toLowerCase();

                        // If the chip text isn't anywhere in the title, category, or description, skip it!
                        if (!title.contains(filter) && !category.contains(filter) && !desc.contains(filter)) {
                            continue;
                        }
                    }
                    // ---------------------------------------

                    EventListItem item = EventListItem.fromEvent(event);
                    item.isSaved = userSavedEvents.contains(event.eventId);
                    browseItems.add(item);
                }
                eventListAdapter.updateEvents(browseItems);

                // Handle the text displaying depending on filters/searches
                if (browseItems.isEmpty()) {
                    setEmptyState(searchTerm);
                } else {
                    setContentState();
                }
            }
            @Override
            public void onError(Exception exception) {
                eventListAdapter.updateEvents(new ArrayList<>());
                setErrorState();
            }
        });
    }

    private void setLoadingState() {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.browse_state_loading);
    }

    private void setContentState() {
        recyclerView.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.GONE);
    }

    private void setEmptyState(String searchTerm) {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);

        String displayMsg = "No events match";
        String trimmedSearch = searchTerm == null ? "" : searchTerm.trim();

        if (!trimmedSearch.isEmpty() && !selectedChipFilter.isEmpty()) {
            displayMsg = String.format(Locale.getDefault(), "No '%s' events match \"%s\".", selectedChipFilter, trimmedSearch);
        } else if (!trimmedSearch.isEmpty()) {
            displayMsg = String.format(Locale.getDefault(), "No events match \"%s\".", trimmedSearch);
        } else if (!selectedChipFilter.isEmpty()) {
            displayMsg = String.format(Locale.getDefault(), "No %s events found.", selectedChipFilter);
        } else {
            displayMsg = getString(R.string.browse_state_empty);
        }
        stateText.setText(displayMsg);
    }

    private void setErrorState() {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.browse_state_error);
    }
}