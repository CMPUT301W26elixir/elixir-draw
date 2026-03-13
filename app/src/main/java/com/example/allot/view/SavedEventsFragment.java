package com.example.allot.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.allot.R;
import com.example.allot.controller.EventController;
import com.example.allot.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the user's saved events in a scrollable list.
 *
 * <p>If no saved events are available, an empty state message is shown instead.
 * This fragment receives saved event IDs through its arguments, loads the
 * corresponding events, and displays them in a recycler view.
 */
public class SavedEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private EventListAdapter adapter;
    private EventController eventController;

    /**
     * Inflates the fragment layout, binds views, sets up the recycler view
     * and adapter, initializes the controller, and begins loading saved events.
     *
     * @param inflater the layout inflater used to inflate the fragment view
     * @param container the parent view that the fragment UI should attach to
     * @param savedInstanceState the previously saved state of the fragment, if any
     * @return the root view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        emptyStateText.setText("You haven't saved any events yet.");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventListAdapter(new ArrayList<>(), new EventListAdapter.OnEventClickListener() {
            /**
             * Handles clicks on a saved event item.
             *
             * @param event the clicked saved event item
             */
            @Override
            public void onEventClick(EventListItem event) {
                // Future logic: open details from the saved tab
            }

            /**
             * Handles clicks on the heart icon for a saved event item.
             *
             * @param event the saved event item whose heart icon was clicked
             * @param position the adapter position of the clicked item
             */
            @Override
            public void onHeartClick(EventListItem event, int position) {
                // Future logic: allow unsaving directly from this tab
            }
        });
        recyclerView.setAdapter(adapter);

        eventController = new EventController();
        loadSavedEvents();

        return view;
    }

    /**
     * Loads saved events using the list of saved event IDs passed in through
     * the fragment arguments.
     *
     * <p>If saved IDs exist, the corresponding events are fetched through the
     * event controller. Otherwise, the UI is immediately updated to show the
     * empty state.
     */
    private void loadSavedEvents() {
        // Read the instantly updated list sent by MainActivity
        List<String> savedIds = new ArrayList<>();
        if (getArguments() != null) {
            savedIds = getArguments().getStringArrayList("saved_ids");
        }

        if (savedIds != null && !savedIds.isEmpty()) {
            eventController.getEventsByIds(savedIds, new EventController.EventListCallback() {
                /**
                 * Updates the UI after the saved events have been retrieved.
                 *
                 * @param events the list of saved events
                 */
                @Override
                public void onCallback(List<Event> events) {
                    updateUI(events);
                }
            });
        } else {
            updateUI(new ArrayList<>()); // Show empty state instantly
        }
    }

    /**
     * Updates the fragment UI with the provided saved events.
     *
     * <p>If the list is empty, the recycler view is hidden and the empty state
     * message is shown. Otherwise, the events are converted into
     * {@link EventListItem} objects, marked as saved, and displayed in the adapter.
     *
     * @param events the list of saved events to display
     */
    private void updateUI(List<Event> events) {
        if (events.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);

            List<EventListItem> listItems = new ArrayList<>();
            for (Event event : events) {
                EventListItem item = EventListItem.fromEvent(event);
                item.isSaved = true; // Everything here is saved
                listItems.add(item);
            }
            adapter.updateEvents(listItems);
        }
    }
}