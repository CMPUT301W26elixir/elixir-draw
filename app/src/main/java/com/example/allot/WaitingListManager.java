package com.example.allot;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;

public class WaitingListManager {

    public FirebaseFirestore database;

    public WaitingListManager() {
        // Just grabbing the database tool from Firebase
        this.database = FirebaseFirestore.getInstance();
    }

    public void joinEvent(String eventId, String entrantId) {

        // Create the Ticket
        WaitingList entry = new WaitingList(entrantId, eventId);

        // Save it to the cloud
        // Path: events -> [eventId] -> waitlist -> [entrantId]
        database.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(entrantId) // Using entrantId as the name prevents duplicates!
                .set(entry)
                .addOnSuccessListener(aVoid -> {
                    Log.d("WaitlistLogic", "Nice! You joined the list for " + eventId);
                })
                .addOnFailureListener(e -> {
                    Log.e("WaitlistLogic", "Oops, couldn't join: " + e.getMessage());
                });
    }

    public void leaveEvent(String eventId, String entrantId) {

        // Path: events -> [eventId] -> waitlist -> [entrantId]
        database.collection("events")
                .document(eventId)
                .collection("waitlist")
                .document(entrantId)
                .delete() // This is the "Magic" button that removes them
                .addOnSuccessListener(aVoid -> {
                    Log.d("WaitlistLogic", "Successfully left the event: " + eventId);
                })
                .addOnFailureListener(e -> {
                    Log.e("WaitlistLogic", "Error leaving event: " + e.getMessage());
                });
    }
}