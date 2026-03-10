package com.example.allot;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;

public class WaitingListManager {

    public FirebaseFirestore database;

    public WaitingListManager() {
        // Connect to database
        this.database = FirebaseFirestore.getInstance();
    }

    public void addEntrantToList(WaitingList entry) {

        database.collection("events")
                .document(entry.eventId)
                .collection("waitlist")
                .document(entry.entrantId)
                .set(entry)
                .addOnSuccessListener(aVoid -> {
                    Log.d("WaitlistLogic", "Entrant added to the list!");
                })
                .addOnFailureListener(e -> {
                    Log.e("WaitlistLogic", "Failed to join: " + e.getMessage());
                });
    }
}