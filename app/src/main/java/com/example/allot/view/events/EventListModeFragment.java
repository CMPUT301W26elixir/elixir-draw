package com.example.allot.view.events;

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
import com.example.allot.controller.events.UserEventsController;
import com.example.allot.view.shared.EventListAdapter;
import com.example.allot.view.shared.EventListAdapter.OnEventClickListener;
import com.example.allot.view.shared.EventListItem;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows either the user's own events or saved events in one fragment layout.
 */
public class EventListModeFragment extends Fragment {
    private static final String ARG_MODE = "mode";
    private static final String ARG_SAVED_IDS = "saved_ids";

    public static final String MODE_MY_EVENTS = "my_events";
    public static final String MODE_SAVED_EVENTS = "saved_events";

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private EventListAdapter adapter;
    private UserEventsController userEventsController;

    /**
     * Creates a fragment configured to show the user's own events.
     *
     * @return a fragment instance for the My Events mode
     */
    public static EventListModeFragment newMyEventsInstance() {
        EventListModeFragment fragment = new EventListModeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, MODE_MY_EVENTS);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Creates a fragment configured to show saved events.
     *
     * @param savedIds the saved event IDs to load
     * @return a fragment instance for the Saved Events mode
     */
    public static EventListModeFragment newSavedEventsInstance(ArrayList<String> savedIds) {
        EventListModeFragment fragment = new EventListModeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, MODE_SAVED_EVENTS);
        args.putStringArrayList(ARG_SAVED_IDS, savedIds);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Inflates the fragment layout and loads the requested event list.
     *
     * @param inflater the inflater used to create the view
     * @param container the optional parent view group
     * @param savedInstanceState the previously saved state bundle
     * @return the fragment root view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventListAdapter(new ArrayList<>(), new EventListAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(EventListItem event) {
            }

            @Override
            public void onHeartClick(EventListItem event, int position) {
            }
        });
        recyclerView.setAdapter(adapter);

        userEventsController = new UserEventsController(requireContext());
        emptyStateText.setText(getEmptyMessage());
        loadItems();
        return view;
    }

    /**
     * Loads either the hosted events list or the saved events list based on the current mode.
     */
    private void loadItems() {
        if (MODE_MY_EVENTS.equals(getMode())) {
            userEventsController.loadMyEventsList((items, success) -> updateUi(items));
            return;
        }

        ArrayList<String> savedIds = new ArrayList<>();
        Bundle args = getArguments();
        if (args != null) {
            ArrayList<String> argSavedIds = args.getStringArrayList(ARG_SAVED_IDS);
            if (argSavedIds != null) {
                savedIds = argSavedIds;
            }
        }
        userEventsController.loadSavedEvents(savedIds, (items, success) -> updateUi(items));
    }

    /**
     * Updates the empty state and list visibility after items are loaded.
     *
     * @param items the items returned by the controller
     */
    private void updateUi(List<EventListItem> items) {
        if (items == null || items.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
            emptyStateText.setText(getEmptyMessage());
            return;
        }

        recyclerView.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);
        adapter.updateEvents(items);
    }

    /**
     * Reads the current fragment mode from the arguments bundle.
     *
     * @return the active list mode
     */
    private String getMode() {
        Bundle args = getArguments();
        return args == null ? MODE_MY_EVENTS : args.getString(ARG_MODE, MODE_MY_EVENTS);
    }

    /**
     * Builds the empty-state message for the current list mode.
     *
     * @return the empty-state text shown when no events are available
     */
    private String getEmptyMessage() {
        return MODE_SAVED_EVENTS.equals(getMode())
                ? "You haven't saved any events yet."
                : "You have no events.";
    }
}
