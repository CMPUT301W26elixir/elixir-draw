package com.example.allot.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Patterns;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.data.UserRepository;
import com.example.allot.model.User;

import java.util.UUID;

public class UserController {
    private static final String PREFS_NAME = "allot_prefs";
    private static final String DEVICE_ID_KEY = "device_id";

    private final UserRepository userRepository;
    private final String deviceId;
    private final boolean newDeviceId;

    /**
     * Creates an UserController and sets up Firestore and the device ID.
     *
     * @param context the context used to access shared preferences
     */
    public UserController(Context context) {
        this.userRepository = new UserRepository();
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
        userRepository.getUserByDeviceId(deviceId, listener);
    }

    /**
     * Loads the current userfor this device, or creates one if none exists.
     *
     * @param listener the listener that receives the userand success result
     */
    public void loadOrCreateUser(OnCompleteListener<User> listener) {
        userRepository.getUserByDeviceId(deviceId, (user, success) -> {
            if (success && user != null) {
                if (isBlank(user.getDeviceId())) {
                    user.setDeviceId(deviceId);
                    userRepository.backfillDeviceId(deviceId);
                }
                listener.onComplete(user, true);
                return;
            }

            createNewUser(deviceId, listener);
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
        userRepository.createNewUser(deviceId, listener);
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

        userRepository.updateUserProfile(deviceId, firstName, lastName, email, phone, notiEnabled, listener);
    }
    /**
     * Adds or removes an event ID from the user's savedEvents array.
     *
     * @param eventId The ID of the event to save/unsave
     * @param isSaving True to save, false to remove
     * @param listener Callback with success result
     */
    public void toggleSavedEvent(String eventId, boolean isSaving, OnCompleteListener<Boolean> listener) {
        userRepository.toggleSavedEvent(deviceId, eventId, isSaving, listener);
    }
    /**
     * Deletes the current user's profile and removes related event references.
     *
     * @param listener the listener that receives the deletion success result
     */
    public void deleteCurrentUser(OnCompleteListener<Boolean> listener) {
        userRepository.deleteCurrentUser(deviceId, listener);
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

    public String getCurrentDeviceId() {
        return deviceId;
    }
    public boolean isNewDeviceId() {
        return newDeviceId;
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
        this.userRepository.removeUser(deviceId);
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
