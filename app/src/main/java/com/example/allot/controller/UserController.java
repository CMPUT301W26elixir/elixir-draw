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
     * Initializes the controller and connects to the Firestore users collection.
     * Also retrieves or generates a unique device ID for the current device.
     */
    public UserController(Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.usersCollection = db.collection("users");

        DeviceIdResult deviceIdResult = getOrCreateDeviceId(context);
        this.deviceId = deviceIdResult.deviceId;
        this.newDeviceId = deviceIdResult.wasCreated;
    }

    /**
     * Retrieves a user document from Firestore using the provided device ID.
     * Returns the user through the provided listener.
     */
    public void getUserByDeviceId(String deviceId, OnCompleteListener<User> listener) {

        DocumentReference userRef = usersCollection.document(deviceId);

        userRef.get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                DocumentSnapshot document = task.getResult();

                if (document != null && document.exists()) {

                    User user = document.toObject(User.class);

                    if (user != null && isBlank(user.getDeviceId())) {
                        user.setDeviceId(deviceId);
                    }

                    listener.onComplete(user, user != null);

                } else {

                    listener.onComplete(null, false);
                }

            } else {

                Log.d(TAG, "Failed to get user", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Loads the current user's profile using the device ID.
     * If the user does not exist in Firestore, a new user document is created.
     *
     * AI assistance used to structure asynchronous Firestore retrieval
     * and conditional user creation logic.
     * Tool: ChatGPT (OpenAI), 2026.
     */
    public void loadOrCreateUser(OnCompleteListener<User> listener) {

        DocumentReference userRef = usersCollection.document(deviceId);

        userRef.get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {

                Log.d(TAG, "Failed to get user", task.getException());
                listener.onComplete(null, false);
                return;
            }

            DocumentSnapshot document = task.getResult();

            if (document == null || !document.exists()) {

                createNewUser(deviceId, listener);
                return;
            }

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
     * Creates a new user document using the provided device ID.
     */
    public void createNewUser(String deviceId) {
        createNewUser(deviceId, null);
    }

    /**
     * Creates a new user document and optionally returns the result
     * through a completion listener.
     */
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
     * Updates the current user's profile fields in Firestore.
     * The updated user object is returned through the listener.
     *
     * AI assistance used for validation flow and Firestore update pattern.
     * Tool: ChatGPT (OpenAI), 2026.
     */
    public void updateUserProfile(String firstName, String lastName, String email,
                                  String phone, boolean notiEnabled, OnCompleteListener<User> listener) {

        if (!validateProfileFields(firstName, lastName, email)) {

            listener.onComplete(null, false);
            return;
        }

        DocumentReference userRef = usersCollection.document(deviceId);

        userRef.update(
                "firstName", firstName.trim(),
                "lastName", lastName.trim(),
                "email", email.trim(),
                "phone", normalizePhone(phone),
                "notiEnabled", notiEnabled
        ).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                getUserByDeviceId(deviceId, listener);

            } else {

                Log.d(TAG, "Failed to update user", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Adds or removes an event ID from the user's saved events list.
     */
    public void toggleSavedEvent(String eventId, boolean isSaving, OnCompleteListener<Boolean> listener) {

        DocumentReference userRef = usersCollection.document(deviceId);

        FieldValue updateAction = isSaving
                ? FieldValue.arrayUnion(eventId)
                : FieldValue.arrayRemove(eventId);

        userRef.update("savedEvents", updateAction)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        listener.onComplete(true, true);

                    } else {

                        Log.e(TAG, "Failed to toggle saved event", task.getException());
                        listener.onComplete(false, false);
                    }
                });
    }

    /**
     * Deletes the current user and removes all references to that user
     * from event documents in Firestore.
     *
     * AI assistance used to design the Firestore batch cleanup logic.
     * Tool: ChatGPT (OpenAI), 2026.
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

                        batch.update(
                                document.getReference(),
                                "waitingList.list", FieldValue.arrayRemove(deviceId),
                                "waitingList.chosen", FieldValue.arrayRemove(deviceId),
                                "chosen", FieldValue.arrayRemove(deviceId),
                                "enrolled", FieldValue.arrayRemove(deviceId),
                                "cancelled", FieldValue.arrayRemove(deviceId),
                                "notEnrolled", FieldValue.arrayRemove(deviceId),
                                FieldPath.of("waitingList", "status", deviceId), FieldValue.delete()
                        );
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
     * Retrieves the stored device ID from SharedPreferences,
     * or generates a new unique ID if one does not exist.
     */
    private DeviceIdResult getOrCreateDeviceId(Context context) {

        SharedPreferences prefs =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String savedDeviceId = prefs.getString(DEVICE_ID_KEY, null);

        if (savedDeviceId != null && !savedDeviceId.trim().isEmpty()) {
            return new DeviceIdResult(savedDeviceId, false);
        }

        String newDeviceId = UUID.randomUUID().toString();

        prefs.edit().putString(DEVICE_ID_KEY, newDeviceId).apply();

        return new DeviceIdResult(newDeviceId, true);
    }

    /**
     * Returns the device ID used as the current user's identifier.
     */
    public String getCurrentDeviceId() {
        return deviceId;
    }

    /**
     * Returns whether the device ID was newly created.
     */
    public boolean isNewDeviceId() {
        return newDeviceId;
    }

    /**
     * Updates the Firestore document to include the device ID if it was missing.
     */
    private void backfillDeviceId(DocumentReference userRef, String deviceId) {

        userRef.update("deviceId", deviceId)
                .addOnFailureListener(e ->
                        Log.d(TAG, "Failed to backfill device ID", e));
    }

    /**
     * Validates that required user profile fields are present and valid.
     */
    private boolean validateProfileFields(String firstName, String lastName, String email) {

        if (isBlank(firstName)) return false;
        if (isBlank(lastName)) return false;
        if (isBlank(email)) return false;

        return Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches();
    }

    /**
     * Normalizes the phone number before storing it in the database.
     */
    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim();
    }

    /**
     * Checks if a string is null or empty after trimming whitespace.
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Deletes a user document directly using a provided device ID.
     */
    public void removeUser(String deviceId) {

        this.usersCollection.document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d(TAG, "Event " + deviceId + " removed successfully."))
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to remove event: " + e.getMessage()));
    }

    /**
     * Helper class used to store the result of device ID retrieval.
     */
    private static class DeviceIdResult {

        private final String deviceId;
        private final boolean wasCreated;

        private DeviceIdResult(String deviceId, boolean wasCreated) {
            this.deviceId = deviceId;
            this.wasCreated = wasCreated;
        }
    }
}