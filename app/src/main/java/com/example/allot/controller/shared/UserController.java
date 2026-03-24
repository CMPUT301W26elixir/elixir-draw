package com.example.allot.controller.shared;

import android.content.Context;
import android.util.Patterns;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.common.TextHelper;
import com.example.allot.common.UserRole;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.UserRepository;
import com.example.allot.model.profile.User;
/**
 * Holds shared user and session actions across the app.
 */
public class UserController {
    private final UserRepository userRepository;
    private final DeviceSessionManager deviceSessionManager;
    private User cachedCurrentUser;

    /**
     * Creates an UserController and sets up Firestore and the device ID.
     *
     * @param context the context used to access shared preferences
     */
    public UserController(Context context) {
        this(new UserRepository(), new DeviceSessionManager(context));
    }

    public UserController(UserRepository userRepository, DeviceSessionManager deviceSessionManager) {
        this.userRepository = userRepository;
        this.deviceSessionManager = deviceSessionManager;
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
     * Searches users by name, phone, or email.
     *
     * @param query the search query
     * @param listener the listener that receives matching users
     */
    public void searchUsers(String query, OnCompleteListener<java.util.List<User>> listener) {
        userRepository.searchUsers(query, listener);
    }

    /**
     * Loads the current userfor this device, or creates one if none exists.
     *
     * @param listener the listener that receives the userand success result
     */
    public void loadOrCreateUser(OnCompleteListener<User> listener) {
        String deviceId = getCurrentDeviceId();
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
     * Creates a new user with default values and saves it to Firestore.
     *
     * @param deviceId the device ID for the created user
     * @param listener the listener that receives the created user and success result
     */
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

        userRepository.updateUserProfile(getCurrentDeviceId(), firstName, lastName, email, phone, notiEnabled, listener);
    }
    /**
     * Adds or removes an event ID from the user's savedEvents array.
     *
     * @param eventId The ID of the event to save/unsave
     * @param isSaving True to save, false to remove
     * @param listener Callback with success result
     */
    public void toggleSavedEvent(String eventId, boolean isSaving, OnCompleteListener<Boolean> listener) {
        userRepository.toggleSavedEvent(getCurrentDeviceId(), eventId, isSaving, listener);
    }
    /**
     * Deletes the current user's profile and removes related event references.
     *
     * @param listener the listener that receives the deletion success result
     */
    public void deleteCurrentUser(OnCompleteListener<Boolean> listener) {
        userRepository.deleteCurrentUser(getCurrentDeviceId(), listener);
    }

    /**
     * Checks if the current user has admin role.
     *
     * @param listener the listener that receives true if user is admin, false otherwise
     */
    public void isCurrentUserAdmin(OnCompleteListener<Boolean> listener) {
        String deviceId = getCurrentDeviceId();
        userRepository.getUserByDeviceId(deviceId, (user, success) -> {
            if (success && user != null) {
                cachedCurrentUser = user;
                listener.onComplete(UserRole.isAdmin(user), true);
            } else {
                listener.onComplete(false, false);
            }
        });
    }

    public String getCurrentDeviceId() {
        return deviceSessionManager.getCurrentDeviceId();
    }
    public boolean isNewDeviceId() {
        return deviceSessionManager.isNewDeviceId();
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
        return TextHelper.isBlank(value);
    }

}
