package com.example.allot.view.event;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.allot.R;
import com.example.allot.controller.event.SearchEventController;
import com.example.allot.model.event.Event;
import com.example.allot.view.shared.EventListAdapter;
import com.example.allot.view.shared.EventListItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays a search screen allowing entrants to find events by keyword.
 * Searches event titles and descriptions.
 */
public class SearchEventActivity extends AppCompatActivity {

    private static final int SEARCH_DEBOUNCE_MS = 400;

    private SearchEventController searchEventController;

    private EditText searchInput;
    private RecyclerView resultsRecyclerView;
    private ProgressBar loadingIndicator;
    private TextView emptyText;
    private TextView errorText;

    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_event);

        searchEventController = new SearchEventController();

        bindViews();
        setupListeners();
    }

    /**
     * Performs bind views.
     */
    private void bindViews() {
        searchInput = findViewById(R.id.searchInput);
        resultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        loadingIndicator = findViewById(R.id.searchLoadingIndicator);
        emptyText = findViewById(R.id.searchEmptyText);
        errorText = findViewById(R.id.searchErrorText);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Updates the up listeners.
     */
    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());

        /**
         * Documents text Watcher.
         */
        searchInput.addTextChangedListener(new TextWatcher() {
            /**
             * Performs before text changed.
             *
             * @param s the s
             * @param start the start
             * @param count the count
             * @param after the after
             */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            /**
             * Handles the text changed callback.
             *
             * @param s the s
             * @param start the start
             * @param before the before
             * @param count the count
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // Cancel any pending search
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }

                if (query.isEmpty()) {
                    clearResults();
                } else {
                    // Debounce: wait 400ms after user stops typing before searching
                    searchRunnable = () -> performSearch(query);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
                }
            }

            /**
             * Performs after text changed.
             *
             * @param s the s
             */
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Performs perform search.
     *
     * @param keyword the keyword
     */
    private void performSearch(String keyword) {
        runOnUiThread(() -> {
            setLoading(true);
            errorText.setVisibility(View.GONE);
            emptyText.setVisibility(View.GONE);
        });

        /**
         * Documents search Callback.
         */
        searchEventController.searchEvents(keyword, new SearchEventController.SearchCallback() {
            /**
             * Handles the results callback.
             *
             * @param results the results
             */
            @Override
            public void onResults(List<Event> results) {
                runOnUiThread(() -> {
                    setLoading(false);
                    if (results.isEmpty()) {
                        emptyText.setVisibility(View.VISIBLE);
                        resultsRecyclerView.setVisibility(View.GONE);
                    } else {
                        emptyText.setVisibility(View.GONE);
                        resultsRecyclerView.setVisibility(View.VISIBLE);
                        showResults(results);
                    }
                });
            }

            /**
             * Handles the error callback.
             *
             * @param e the e
             */
            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    errorText.setVisibility(View.VISIBLE);
                    resultsRecyclerView.setVisibility(View.GONE);
                });
            }
        });
    }

    /**
     * Performs show results.
     *
     * @param results the results
     */
    private void showResults(List<Event> results) {
        List<EventListItem> items = new ArrayList<>();
        for (Event event : results) {
            items.add(EventListItem.fromEvent(event));
        }

        /**
         * Handles on Event Click Listener.
         */
        resultsRecyclerView.setAdapter(new EventListAdapter(items, new EventListAdapter.OnEventClickListener() {
            /**
             * Handles the event click callback.
             *
             * @param event the event
             */
            @Override
            public void onEventClick(EventListItem event) {
                openEventDetail(event);
            }

            /**
             * Handles the heart click callback.
             *
             * @param event the event
             * @param position the position
             */
            @Override
            public void onHeartClick(EventListItem event, int position) {
                // not needed for search
            }
        }));
    }

    /**
     * Performs open event detail.
     *
     * @param event the event
     */
    private void openEventDetail(EventListItem event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_ID, event.getEventId());
        intent.putExtra(EventDetailActivity.EXTRA_EVENT_TITLE, event.getTitle());
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Performs clear results.
     */
    private void clearResults() {
        resultsRecyclerView.setVisibility(View.GONE);
        emptyText.setVisibility(View.GONE);
        errorText.setVisibility(View.GONE);
    }

    /**
     * Updates the loading.
     *
     * @param isLoading whether loading
     */
    private void setLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    /**
     * Handles the destroy callback.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up any pending search callbacks to avoid memory leaks
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}
