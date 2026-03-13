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

public class MyEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private EventListAdapter adapter;
    private UserController userController;
    private EventController eventController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);

        emptyStateText.setText("You have no events.");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventListAdapter(new ArrayList<>(), new EventListAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(EventListItem event) {
                // Future click logic
            }

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

    private void loadMyEvents() {
        userController.loadOrCreateUser((user, success) -> {
            if (success && user != null && user.myEvents != null && !user.myEvents.isEmpty()) {
                eventController.getEventsByIds(user.myEvents, new EventController.EventListCallback() {
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