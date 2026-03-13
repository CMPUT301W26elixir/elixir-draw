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
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays the current user's events in a scrollable list.
 *
 * <p>If the user has no events, an empty state message is shown instead.
 * This fragment loads the user's saved and registered event data, converts
 * the events into displayable list items, and updates the recycler view.
 */
public class MyEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private EventListAdapter adapter;
    private UserController userController;
    private EventController eventController;

    /**
     * Inflates the fragment layout, initializes views, sets up the recycler view,
     * creates the adapter, and starts loading the current user's events.
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

        emptyStateText.setText("You have no events.");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventListAdapter(new ArrayList<>(), new EventListAdapter.OnEventClickListener() {
            /**
             * Handles clicks on an event item.
             *
             * @param event the clicked event list item
             */
            @Override
            public void onEventClick(EventListItem event) {
                // Future click logic
            }

            /**
             * Handles clicks on the heart icon for an event item.
             *
             * <p>Fragments do not handle database save logic directly.
             * That responsibility belongs to MainActivity.
             *
             * @param event the event list item whose heart icon was clicked
             * @param position the adapter position of the clicked item
             */
            @Override
            public void onHeartClick(EventListItem event, int position) {
                // Fragments don't need to handle the DB save, MainActivity does it
            }
        });
        recyclerView.setAdapter(adapter);

        userController = new UserController(requireContext());
        eventController = new EventController();

        loadMyEvents();

        return view;
    }

    /**
     * Loads the current user's events.
     *
     * <p>If the user has registered events, those event IDs are used to fetch the
     * corresponding event objects. If no events exist, the UI is updated to show
     * the empty state.
     */
    private void loadMyEvents() {
        userController.loadOrCreateUser((user, success) -> {
            if (success && user != null && user.myEvents != null && !user.myEvents.isEmpty()) {
                eventController.getEventsByIds(user.myEvents, new EventController.EventListCallback() {
                    /**
                     * Updates the UI once the user's event list has been retrieved.
                     *
                     * @param events the list of retrieved events
                     */
                    @Override
                    public void onCallback(List<Event> events) {
                        updateUI(events, user.getSavedEvents() != null ? user.getSavedEvents() : new ArrayList<>());
                    }
                });
            } else {
                updateUI(new ArrayList<>(), new ArrayList<>()); // Show empty state
            }
        });
    }

    /**
     * Updates the fragment UI based on the provided event data.
     *
     * <p>If no events are available, the recycler view is hidden and the empty
     * state text is shown. Otherwise, the events are converted into
     * {@link EventListItem} objects, marked as saved when applicable,
     * and passed to the adapter.
     *
     * @param events the list of events to display
     * @param savedEvents the list of saved event IDs for the current user
     */
    private void updateUI(List<Event> events, List<String> savedEvents) {
        if (events.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);

            List<EventListItem> listItems = new ArrayList<>();
            for (Event event : events) {
                EventListItem item = EventListItem.fromEvent(event);
                item.isSaved = savedEvents.contains(event.eventId);
                listItems.add(item);
            }

            adapter.updateEvents(listItems);
        }
    }
}