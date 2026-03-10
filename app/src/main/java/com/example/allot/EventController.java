package com.example.allot;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EventController {

    public FirebaseFirestore database;

    public EventController() {
        // Connect to the database tools
        this.database = FirebaseFirestore.getInstance();
    }

    /**
     * This takes an Event and saves it in the "events" folder in the cloud.
     * Use this for Organizer tasks! US 02.01.01
     */
    public void createNewEvent(Event event) {
        // Go to the "events" collection -> Make a document named after the eventId -> Put the event data inside it
        database.collection("events").document(event.eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventLogic", "Sweet! Event " + event.title + " is now live.");
                })
                .addOnFailureListener(e -> {
                    Log.e("EventLogic", "Darn, it failed: " + e.getMessage());
                });
    }

    /**
     * US 01.01.03: Get a list of events that are open for joining for entrant
     * Might make a different javaclass for this and seperate entrants and organizers
     */
    public void getAllOpenEvents(EventListCallback callback) {

        // Look in the events folder -> Only grab the ones where status is "open"
        database.collection("events")
                .whereEqualTo("status", "open")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> openEvents = new ArrayList<>();

                        // Loop through everything Firebase found
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Turn data into an Event
                            Event event = document.toObject(Event.class);
                            openEvents.add(event);
                        }

                        // Send the list back to the app
                        callback.onCallback(openEvents);
                    } else {
                        Log.e("EventLogic", "Error getting events: ", task.getException());
                    }
                });
    }

    // Waits for internet to finish
    public interface EventListCallback {
        void onCallback(List<Event> events);
    }
}