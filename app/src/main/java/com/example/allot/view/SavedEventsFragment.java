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

public class SavedEventsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private EventListAdapter adapter;
    private EventController eventController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyStateText = view.findViewById(R.id.emptyStateText);
        emptyStateText.setText("You haven't saved any events yet.");

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EventListAdapter(new ArrayList<>(), new EventListAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(EventListItem event) {
                // Future logic: open details from the saved tab
            }
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

    private void loadSavedEvents() {
        // Read the instantly updated list sent by MainActivity
        List<String> savedIds = new ArrayList<>();
        if (getArguments() != null) {
            savedIds = getArguments().getStringArrayList("saved_ids");
        }

        if (savedIds != null && !savedIds.isEmpty()) {
            eventController.getEventsByIds(savedIds, new EventController.EventListCallback() {
                @Override
                public void onCallback(List<Event> events) {
                    updateUI(events);
                }
            });
        } else {
            updateUI(new ArrayList<>()); // Show empty state instantly
        }
    }

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