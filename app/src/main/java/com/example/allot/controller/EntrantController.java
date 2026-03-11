package com.example.allot.controller;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.example.allot.model.Entrant;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantController {

    public FirebaseFirestore database;
    public String deviceId;

    public EntrantController(Context context) {
        this.database = FirebaseFirestore.getInstance();
        // Get the phone's unique ID
        this.deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * This checks if the Entrant exists.
     * If they do, it fetches them. If not, it makes a new one.
     */
    public void getOrCreateEntrant(EntrantCallback callback) {

        database.collection("users").document(deviceId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();

                        if (document.exists()) {
                            // Turn the cloud data into our Entrant object
                            Entrant existingEntrant = document.toObject(Entrant.class);
                            callback.onCallback(existingEntrant);
                        } else {
                            // Create a brand new Entrant
                            Entrant newEntrant = new Entrant();
                            newEntrant.setDeviceId(deviceId);
                            newEntrant.setName("New Entrant");
                            newEntrant.setRole("entrant");

                            // Save to the "users" collection
                            database.collection("users").document(deviceId).set(newEntrant);
                            callback.onCallback(newEntrant);
                        }
                    } else {
                        Log.e("EntrantController", "Error: " + task.getException());
                    }
                });
    }

    public interface EntrantCallback {
        void onCallback(Entrant entrant);
    }
}
