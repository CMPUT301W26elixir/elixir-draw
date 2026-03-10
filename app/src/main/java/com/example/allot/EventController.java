package com.example.allot;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;

public class EventController {

    public FirebaseFirestore database;

    public EventController() {
        // Connect to the database tools
        this.database = FirebaseFirestore.getInstance();
    }

    /**
     * This takes an Event and saves it in the "events" folder in the cloud.
     */
    public void createNewEvent(Event event) {

        // Go to the "events" collection
        // Make a document named after the eventId
        // Put the event data inside it
        database.collection("events").document(event.eventId)
                .set(event)
                .addOnSuccessListener(aVoid -> {
                    Log.d("EventLogic", "Sweet! Event " + event.title + " is now live.");
                })
                .addOnFailureListener(e -> {
                    Log.e("EventLogic", "Darn, it failed: " + e.getMessage());
                });
    }
}