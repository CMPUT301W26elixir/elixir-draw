package com.example.allot.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.allot.R;
import com.example.allot.controller.UserController;
import com.example.allot.controller.EventController;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.eventsRecyclerView);
        searchInput = findViewById(R.id.searchInput);
        loadingIndicator = findViewById(R.id.loadingIndicator);
        stateText = findViewById(R.id.stateText);
        bottomNavBar = findViewById(R.id.bottomNavBar);

        eventListAdapter = new EventListAdapter(new ArrayList<>());
        recyclerView.setAdapter(eventListAdapter);
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.EXPLORE);

        eventController = new EventController();
        setupSearchInput();
        loadBrowseEvents("");

        // Keep the existing user setup while we build the search screen UI.
        userController = new UserController(this);
        userController.loadOrCreateUser((user, success) -> {
            if (success && user != null) {
                Log.d(TAG, "Welcome, " + user.getName());
                Log.d(TAG, "Role: " + user.getRole());
            } else {
                Log.e(TAG, "Failed to load user.");
            }
        });

    }

    private void setupSearchInput() {
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                loadBrowseEvents(editable.toString());
            }
        });
    }

    private void loadBrowseEvents(String searchTerm) {
        setLoadingState();

        eventController.searchOpenEvents(searchTerm, new EventController.EventListCallback() {
            @Override
            public void onCallback(List<Event> events) {
                List<EventListItem> browseItems = new ArrayList<>();

                for (Event event : events) {
                    browseItems.add(EventListItem.fromEvent(event));
                }

                eventListAdapter.updateEvents(browseItems);

                if (browseItems.isEmpty()) {
                    setEmptyState(searchTerm);
                } else {
                    setContentState();
                }

                Log.d(TAG, "Loaded " + browseItems.size() + " browse events.");
            }

            @Override
            public void onError(Exception exception) {
                eventListAdapter.updateEvents(new ArrayList<>());
                setErrorState();
                Log.e(TAG, "Failed to load browse events", exception);
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

        String trimmedSearch = searchTerm == null ? "" : searchTerm.trim();
        if (trimmedSearch.isEmpty()) {
            stateText.setText(R.string.browse_state_empty);
            return;
        }

        stateText.setText(String.format(
                Locale.getDefault(),
                "No events match \"%s\".",
                trimmedSearch
        ));
    }

    private void setErrorState() {
        recyclerView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);
        stateText.setVisibility(View.VISIBLE);
        stateText.setText(R.string.browse_state_error);
    }
}
