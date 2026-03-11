package com.example.allot;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EntrantController entrantController;
    private EventController eventController;
    private EventListAdapter eventListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.eventsRecyclerView);
        eventListAdapter = new EventListAdapter(new ArrayList<>());
        recyclerView.setAdapter(eventListAdapter);

        eventController = new EventController();
        loadBrowseEvents();

        // Keep the existing entrant setup while we build the search screen UI.
        entrantController = new EntrantController(this);
        entrantController.getOrCreateEntrant(new EntrantController.EntrantCallback() {
            @Override
            public void onCallback(Entrant entrant) {
                Log.d("Allot_Logic", "Welcome, " + entrant.getName());
                Log.d("Allot_Logic", "Role: " + entrant.getRole());
            }
        });
    }

    private void loadBrowseEvents() {
        eventController.getAllOpenEvents(new EventController.EventListCallback() {
            @Override
            public void onCallback(List<Event> events) {
                List<EventListItem> browseItems = new ArrayList<>();

                for (Event event : events) {
                    browseItems.add(EventListItem.fromEvent(event));
                }

                eventListAdapter.updateEvents(browseItems);
                Log.d("Allot_Logic", "Loaded " + browseItems.size() + " browse events.");
            }

            @Override
            public void onError(Exception exception) {
                Log.e("Allot_Logic", "Failed to load browse events", exception);
            }
        });
    }
}
