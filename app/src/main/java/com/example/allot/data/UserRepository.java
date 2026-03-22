package com.example.allot.data;

import android.util.Log;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.common.TextHelper;
import com.example.allot.model.profile.User;
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
/**
 * Handles Firestore reads and writes for users.
 */
public class UserRepository {
    private static final String TAG = "UserRepository";

    private final CollectionReference usersCollection;

    /**
     * Creates a UserRepository and connects it to Firestore.
     */
    public UserRepository() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Creates a UserRepository with a provided Firestore instance.
     *
     * @param database the Firestore instance to use
     */
    public UserRepository(FirebaseFirestore database) {
        this.usersCollection = database.collection("users");
    }

    /**
     * Loads a user by device ID.
     *
     * @param deviceId the device ID to look up
     * @param listener the listener that receives the user
     */
    public void getUserByDeviceId(String deviceId, OnCompleteListener<User> listener) {
        // Get the Firestore user doc for this device
        DocumentReference userRef = usersCollection.document(deviceId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();

                // Turn the doc into a User object
                if (document != null && document.exists()) {
                    User user = document.toObject(User.class);
                    if (user != null && isBlank(user.getDeviceId())) {
                        user.setDeviceId(deviceId);
                    }
                    listener.onComplete(user, user != null);
                } else {
                    // This device does not have a user yet
                    listener.onComplete(null, false);
                }
            } else {
                // Let the caller know the Firestore call failed
                Log.d(TAG, "Failed to get user", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Creates a new user with the provided device ID.
     *
     * @param deviceId the device ID to assign
     * @param listener the listener that receives the created user
     */
    public void createNewUser(String deviceId, OnCompleteListener<User> listener) {
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
     * Updates the current user's profile information.
     *
     * @param deviceId the current user device ID
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param phone the user's phone number
     * @param notiEnabled whether notifications are enabled
     * @param listener the listener that receives the updated user
     */
    public void updateUserProfile(String deviceId,
                                  String firstName,
                                  String lastName,
                                  String email,
                                  String phone,
                                  boolean notiEnabled,
                                  OnCompleteListener<User> listener) {
        // Only update the profile fields the user can change
        DocumentReference userRef = usersCollection.document(deviceId);
        userRef.update(
                "firstName", firstName.trim(),
                "lastName", lastName.trim(),
                "email", email.trim(),
                "phone", normalizePhone(phone),
                "notiEnabled", notiEnabled
        ).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Load the user again so the caller gets fresh data
                getUserByDeviceId(deviceId, listener);
            } else {
                // Let the UI know the save failed
                Log.d(TAG, "Failed to update user", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Adds or removes an event ID from the user's saved events.
     *
     * @param deviceId the current user device ID
     * @param eventId the event ID to toggle
     * @param isSaving true to save, false to remove
     * @param listener the listener that receives the result
     */
    public void toggleSavedEvent(String deviceId, String eventId, boolean isSaving, OnCompleteListener<Boolean> listener) {
        DocumentReference userRef = usersCollection.document(deviceId);

        // FieldValue lets Firestore add or remove one saved event
        FieldValue updateAction = isSaving ?
                FieldValue.arrayUnion(eventId) :
                FieldValue.arrayRemove(eventId);

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
     * Deletes the current user's profile and removes related event references.
     *
     * @param deviceId the current user device ID
     * @param listener the listener that receives the result
     */
    public void deleteCurrentUser(String deviceId, OnCompleteListener<Boolean> listener) {
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
                                "cancelled", FieldValue.arrayRemove(deviceId),
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
     * Updates a user's device ID if it is missing.
     *
     * @param deviceId the device ID to backfill
     */
    public void backfillDeviceId(String deviceId) {
        usersCollection.document(deviceId)
                .update("deviceId", deviceId)
                .addOnFailureListener(e -> Log.d(TAG, "Failed to backfill device ID", e));
    }

    /**
     * Checks if a string is null or empty after trimming spaces.
     *
     * @param value the string to check
     * @return true if the string is blank, otherwise false
     */
    private boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }

    /**
     * Cleans up the phone number before storing it.
     *
     * @param phone the phone number entered by the user
     * @return a trimmed phone number, or an empty string if null
     */
    private String normalizePhone(String phone) {
        // Keep the phone value safe for Firestore
        return phone == null ? "" : phone.trim();
    }
}








