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
                            createEntrant(deviceId, "First Name", "Last Name", "", "", true, callback);
                        }
                    } else {
                        Log.e("EntrantController", "Error: " + task.getException());
                    }
                });
    }

    /**
     * Creates a new entrant and stores it in the database.
     *
     * @param deviceId Unique identifier for the entrant's device.
     * @param firstName The entrant's first name.
     * @param lastName The entrant's last name.
     * @param email The entrant's email address.
     * @param phone The entrant's phone number.
     * @param notiEnabled Indicates whether notifications are enabled for the entrant.
     * @param callback Callback used to handle the result of the database operation.
     */
    public void createEntrant(String deviceId, String firstName, String lastName, String email,
                              String phone, boolean notiEnabled, EntrantCallback callback) {
        Entrant newEntrant = new Entrant(
                deviceId,
                firstName,
                lastName,
                email,
                phone,
                notiEnabled,
                "entrant"
        );

        database.collection("users").document(deviceId)
                .set(newEntrant)
                .addOnSuccessListener(unused -> callback.onCallback(newEntrant))
                .addOnFailureListener(e ->
                        Log.e("EntrantController", "Failed to create entrant", e)
                );
    }

    public interface EntrantCallback {
        void onCallback(Entrant entrant);
    }
}
