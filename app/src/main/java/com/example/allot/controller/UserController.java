package com.example.allot.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Patterns;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.model.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserController {
    private static final String TAG = "UserController";
    private static final String PREFS_NAME = "allot_prefs";
    private static final String DEVICE_ID_KEY = "device_id";

    private final CollectionReference usersCollection;
    private final String deviceId;
    private final boolean newDeviceId;

    /**
     * Creates an UserController and sets up Firestore and the device ID.
     *
     * @param context the context used to access shared preferences
     */
    public UserController(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Reference to the "users" collection in Firestore
        this.usersCollection = db.collection("users");
        // Get the saved device ID, or create one if it does not exist yet
        DeviceIdResult deviceIdResult = getOrCreateDeviceId(context);
        this.deviceId = deviceIdResult.deviceId;
        this.newDeviceId = deviceIdResult.wasCreated;
    }

    /**
     * Gets an user from Firestore using the given device ID.
     *
     * @param deviceId the device ID of the user
     * @param listener the listener that receives the userand success result
     */
    public void getUserByDeviceId(String deviceId, OnCompleteListener<User> listener) {
        // Get the document in the users collection that matches the device ID
        DocumentReference userRef = usersCollection.document(deviceId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                // If the document exists, convert it into an userobject
                if (document != null && document.exists()) {
                    User user = document.toObject(User.class);
                    if (user != null && isBlank(user.getDeviceId())) {
                        user.setDeviceId(deviceId);
                    }
                    listener.onComplete(user, user != null);
                } else {
                    // No matching userwas found
                    listener.onComplete(null, false);
                }
            } else {
                // Firestore request failed to work
                Log.d(TAG, "Failed to get user", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Loads the current userfor this device, or creates one if none exists.
     *
     * @param listener the listener that receives the userand success result
     */
    public void loadOrCreateUser(OnCompleteListener<User> listener) {
        // Look for the current device's userdocument
        DocumentReference userRef = usersCollection.document(deviceId);
        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                // Stop if Firestore could not complete the request
                Log.d(TAG, "Failed to get user", task.getException());
                listener.onComplete(null, false);
                return;
            }

            DocumentSnapshot document = task.getResult();

            // If no userexists yet, create a new one
            if (document == null || !document.exists()) {
                createNewUser(deviceId, listener);
                return;
            }

            // Convert the document into an userobject
            User user = document.toObject(User.class);
            if (user != null) {
                if (isBlank(user.getDeviceId())) {
                    user.setDeviceId(deviceId);
                    backfillDeviceId(userRef, deviceId);
                }
                listener.onComplete(user, true);
            } else {
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Creates a new userwith default values and saves it to Firestore.
     *
     * @paramlistener the listener that receives the created userand success result
     */
    public void createNewUser(String deviceId) {
        createNewUser(deviceId, null);
    }

    private void createNewUser(String deviceId, OnCompleteListener<User> listener) {
        User user = new User();
        user.setDeviceId(deviceId);
        DocumentReference userRef = usersCollection.document(deviceId);
        userRef.set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User created: " + deviceId);
                if (listener != null) {
                    listener.onComplete(user, true);
                }
            } else {
                Log.d(TAG, "Failed to create user", task.getException());
                if (listener != null) {
                    listener.onComplete(null, false);
                }
            }
        });
    }


    /**
     * Updates the current user's profile information in Firestore.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param phone the user's phone number
     * @param notiEnabled whether notifications are enabled
     * @param listener the listener that receives the updated userand success result
     */
    public void updateUserProfile(String firstName, String lastName, String email,
                                     String phone, boolean notiEnabled, OnCompleteListener<User> listener) {
        // Make sure required fields are valid before updating
        if (!validateProfileFields(firstName, lastName, email)) {
            listener.onComplete(null, false);
            return;
        }

        // Update the userdocument with the new profile values
        DocumentReference userRef = usersCollection.document(deviceId);
        userRef.update(
                "firstName", firstName.trim(),
                "lastName", lastName.trim(),
                "email", email.trim(),
                "phone", normalizePhone(phone),
                "notiEnabled", notiEnabled
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Reload the userso the listener gets the updated object
                getUserByDeviceId(deviceId, listener);
            } else {
                // Update failed
                Log.d(TAG, "Failed to update user", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Deletes the current user's profile and removes related event references.
     *
     * @param listener the listener that receives the deletion success result
     */
    public void deleteCurrentUser(OnCompleteListener<Boolean> listener) {
        FirebaseFirestore database = usersCollection.getFirestore();
        database.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Failed to load events for cleanup", task.getException());
                        listener.onComplete(false, false);
                        return;
                    }

                    WriteBatch batch = database.batch();
                    List<DocumentReference> organizerEvents = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String organizerId = document.getString("organizerId");
                        if (deviceId.equals(organizerId)) {
                            organizerEvents.add(document.getReference());
                            continue;
                        }

                        batch.update(document.getReference(),
                                "waitingList.list", FieldValue.arrayRemove(deviceId),
                                "waitingList.chosen", FieldValue.arrayRemove(deviceId),
                                "chosen", FieldValue.arrayRemove(deviceId),
                                "enrolled", FieldValue.arrayRemove(deviceId),
                                "notEnrolled", FieldValue.arrayRemove(deviceId),
                                FieldPath.of("waitingList", "status", deviceId), FieldValue.delete());
                    }

                    for (DocumentReference organizerEvent : organizerEvents) {
                        batch.delete(organizerEvent);
                    }

                    batch.delete(usersCollection.document(deviceId));

                    batch.commit().addOnCompleteListener(commitTask -> {
                        if (!commitTask.isSuccessful()) {
                            Log.d(TAG, "Failed to delete user profile", commitTask.getException());
                            listener.onComplete(false, false);
                            return;
                        }

                        listener.onComplete(true, true);
                    });
                });
    }

    /**
     * Gets the saved device ID or creates a new one if needed.
     *
     * @param context the context used to access shared preferences
     * @return the existing or newly created device ID
     */
    private DeviceIdResult getOrCreateDeviceId(Context context) {
        // Get the Device ID
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedDeviceId = prefs.getString(DEVICE_ID_KEY, null);

        // Return the saved device ID if it already exists
        if (savedDeviceId != null && !savedDeviceId.trim().isEmpty()) {
            return new DeviceIdResult(savedDeviceId, false);
        }

        // Otherwise create a new unique device ID and save it
        String newDeviceId = UUID.randomUUID().toString();
        prefs.edit().putString(DEVICE_ID_KEY, newDeviceId).apply();
        return new DeviceIdResult(newDeviceId, true);
    }

    public boolean isNewDeviceId() {
        return newDeviceId;
    }

    private void backfillDeviceId(DocumentReference userRef, String deviceId) {
        userRef.update("deviceId", deviceId)
                .addOnFailureListener(e -> Log.d(TAG, "Failed to backfill device ID", e));
    }

    /**
     * Checks if the required profile fields are valid.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
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

    /**
     * Removes the current userfrom Firestore.
     *
     * @paramlistener the listener that receives the result of the deletion
     */
    public void removeUser(String deviceId) {

        this.usersCollection.document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event " + deviceId + " removed successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to remove event: " + e.getMessage());
                });
    }
    private static class DeviceIdResult {
        private final String deviceId;
        private final boolean wasCreated;

        private DeviceIdResult(String deviceId, boolean wasCreated) {
            this.deviceId = deviceId;
            this.wasCreated = wasCreated;
        }
    }
}
