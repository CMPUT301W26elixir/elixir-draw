package com.example.allot.controller;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.util.Patterns;

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
                            if (existingEntrant == null) {
                                IllegalStateException exception = new IllegalStateException(
                                        "Entrant document could not be parsed."
                                );
                                Log.e("EntrantController", "Error loading entrant", exception);
                                callback.onError(exception);
                            } else {
                                callback.onCallback(existingEntrant);
                            }
                        } else {
                            createEntrant(deviceId, "First Name", "Last Name", "", "", true, callback);
                        }
                    } else {
                        Exception exception = task.getException();
                        Log.e("EntrantController", "Error: " + exception);
                        callback.onError(exception);
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
        Exception validationError = validateProfileFields(deviceId, firstName, lastName, email);
        if (validationError != null) {
            callback.onError(validationError);
            return;
        }

        Entrant newEntrant = new Entrant(
                deviceId,
                firstName.trim(),
                lastName.trim(),
                email.trim(),
                normalizePhone(phone),
                notiEnabled,
                "entrant"
        );

        database.collection("users").document(deviceId)
                .set(newEntrant)
                .addOnSuccessListener(unused -> callback.onCallback(newEntrant))
                .addOnFailureListener(e -> {
                    Log.e("EntrantController", "Failed to create entrant", e);
                    callback.onError(e);
                });
    }

    /**
     * Updates the profile information of the current entrant in the database.
     *
     * @param firstName The updated first name of the entrant.
     * @param lastName The updated last name of the entrant.
     * @param email The updated email address of the entrant.
     * @param phone The updated phone number of the entrant.
     * @param notiEnabled Indicates whether notifications are enabled for the entrant.
     * @param callback Callback used to return the updated entrant after the operation completes.
     */
    // If only updating specific fields, pass all existing fields in!!!!!!!!
    public void updateEntrantProfile(String firstName, String lastName, String email,
                                     String phone, boolean notiEnabled, EntrantCallback callback) {
        Exception validationError = validateProfileFields(deviceId, firstName, lastName, email);
        if (validationError != null) {
            callback.onError(validationError);
            return;
        }

        database.collection("users").document(deviceId)
                .update(
                        "firstName", firstName.trim(),
                        "lastName", lastName.trim(),
                        "email", email.trim(),
                        "phone", normalizePhone(phone),
                        "notiEnabled", notiEnabled
                )
                .addOnSuccessListener(unused -> getOrCreateEntrant(callback))
                .addOnFailureListener(e -> {
                    Log.e("EntrantController", "Failed to update entrant profile", e);
                    callback.onError(e);
                });
    }

    private Exception validateProfileFields(String deviceId, String firstName, String lastName, String email) {
        if (isBlank(deviceId)) {
            return new IllegalArgumentException("Must have a Device ID");
        }

        if (isBlank(firstName)) {
            return new IllegalArgumentException("First name is required.");
        }

        if (isBlank(lastName)) {
            return new IllegalArgumentException("Last name is required.");
        }

        if (isBlank(email)) {
            return new IllegalArgumentException("Email is required.");
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            return new IllegalArgumentException("Email format is invalid.");
        }

        return null;
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public interface EntrantCallback {
        void onCallback(Entrant entrant);

        default void onError(Exception exception) {
        }
    }
}
