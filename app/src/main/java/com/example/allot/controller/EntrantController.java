package com.example.allot.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.model.Entrant;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.UUID;

public class EntrantController {
    private static final String TAG = "EntrantController";
    private static final String PREFS_NAME = "allot_prefs";
    private static final String DEVICE_ID_KEY = "device_id";

    private final CollectionReference usersCollection;
    private final String deviceId;

    /**
     * Creates an EntrantController and sets up Firestore and the device ID.
     *
     * @param context the context used to access shared preferences
     */
    public EntrantController(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Reference to the "users" collection in Firestore
        this.usersCollection = db.collection("users");
        // Get the saved device ID, or create one if it does not exist yet
        this.deviceId = getOrCreateDeviceId(context);
    }

    /**
     * Gets an entrant from Firestore using the given device ID.
     *
     * @param deviceId the device ID of the entrant
     * @param listener the listener that receives the entrant and success result
     */
    public void getEntrantByDeviceId(String deviceId, OnCompleteListener<Entrant> listener) {
        // Get the document in the users collection that matches the device ID
        DocumentReference entrantRef = usersCollection.document(deviceId);

        entrantRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                // If the document exists, convert it into an Entrant object
                if (document != null && document.exists()) {
                    Entrant entrant = document.toObject(Entrant.class);
                    listener.onComplete(entrant, entrant != null);
                } else {
                    // No matching entrant was found
                    listener.onComplete(null, false);
                }
            } else {
                // Firestore request failed to work
                Log.d(TAG, "Failed to get entrant", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Loads the current entrant for this device, or creates one if none exists.
     *
     * @param listener the listener that receives the entrant and success result
     */
    public void loadOrCreateEntrant(OnCompleteListener<Entrant> listener) {
        // Look for the current device's entrant document
        DocumentReference entrantRef = usersCollection.document(deviceId);
        entrantRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                // Stop if Firestore could not complete the request
                Log.d(TAG, "Failed to get entrant", task.getException());
                listener.onComplete(null, false);
                return;
            }

            DocumentSnapshot document = task.getResult();

            // If no entrant exists yet, create a new one
            if (document == null || !document.exists()) {
                createNewEntrant(listener);
                return;
            }

            // Convert the document into an Entrant object
            Entrant entrant = document.toObject(Entrant.class);
            if (entrant != null) {
                listener.onComplete(entrant, true);
            } else {
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Creates a new entrant with default values and saves it to Firestore.
     *
     * @param listener the listener that receives the created entrant and success result
     */
    public void createNewEntrant(OnCompleteListener<Entrant> listener) {
        // Create a new entrant with empty profile fields and default settings
        Entrant entrant = new Entrant(
                deviceId,
                "",
                "",
                "",
                "",
                true,
                "entrant"
        );

        // Save the new entrant under the current device ID
        DocumentReference entrantRef = usersCollection.document(deviceId);
        entrantRef.set(entrant).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                listener.onComplete(entrant, true);
            } else {
                // Entrant could not be saved
                Log.d(TAG, "Failed to create entrant", task.getException());
                listener.onComplete(null, false);
            }
        });
    }


    /**
     * Updates the current entrant's profile information in Firestore.
     *
     * @param firstName the entrant's first name
     * @param lastName the entrant's last name
     * @param email the entrant's email address
     * @param phone the entrant's phone number
     * @param notiEnabled whether notifications are enabled
     * @param listener the listener that receives the updated entrant and success result
     */
    public void updateEntrantProfile(String firstName, String lastName, String email,
                                     String phone, boolean notiEnabled, OnCompleteListener<Entrant> listener) {
        // Make sure required fields are valid before updating
        if (!validateProfileFields(firstName, lastName, email)) {
            listener.onComplete(null, false);
            return;
        }

        // Update the entrant document with the new profile values
        DocumentReference entrantRef = usersCollection.document(deviceId);
        entrantRef.update(
                "firstName", firstName.trim(),
                "lastName", lastName.trim(),
                "email", email.trim(),
                "phone", normalizePhone(phone),
                "notiEnabled", notiEnabled
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Reload the entrant so the listener gets the updated object
                getEntrantByDeviceId(deviceId, listener);
            } else {
                // Update failed
                Log.d(TAG, "Failed to update entrant", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Gets the saved device ID or creates a new one if needed.
     *
     * @param context the context used to access shared preferences
     * @return the existing or newly created device ID
     */
    private String getOrCreateDeviceId(Context context) {
        // Get the Device ID
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedDeviceId = prefs.getString(DEVICE_ID_KEY, null);

        // Return the saved device ID if it already exists
        if (savedDeviceId != null && !savedDeviceId.trim().isEmpty()) {
            return savedDeviceId;
        }

        // Otherwise create a new unique device ID and save it
        String newDeviceId = UUID.randomUUID().toString();
        prefs.edit().putString(DEVICE_ID_KEY, newDeviceId).apply();
        return newDeviceId;
    }

    /**
     * Checks if the required profile fields are valid.
     *
     * @param firstName the entrant's first name
     * @param lastName the entrant's last name
     * @param email the entrant's email address
     * @return true if the required fields are valid, otherwise false
     */
    private boolean validateProfileFields(String firstName, String lastName, String email) {
        // First name cannot be empty
        if (isBlank(firstName)) {
            return false;
        }

        // Last name cannot be empty
        if (isBlank(lastName)) {
            return false;
        }

        // Email cannot be empty
        if (isBlank(email)) {
            return false;
        }

        // Check if the email matches a valid email format
        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /**
     * Cleans up the phone number before storing it.
     *
     * @param phone the phone number entered by the user
     * @return a trimmed phone number, or an empty string if null
     */
    private String normalizePhone(String phone) {
        // Store an empty string if phone is null, otherwise trim extra spaces
        return phone == null ? "" : phone.trim();
    }

    /**
     * Checks if a string is null or empty after trimming spaces.
     *
     * @param value the string to check
     * @return true if the string is blank, otherwise false
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public void removeEntrant(EntrantCallback callback) {

        database.collection("users").document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    callback.onCallback(null);
                })
                .addOnFailureListener(e -> {
                    Log.e("EntrantController", "Failed to delete: " + e.getMessage());
                });
    }

}
