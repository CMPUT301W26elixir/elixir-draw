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
     * Creates a new UserController instance.
     *
     * @param context the context
     */
    public UserController(Context context) {
        this(new UserRepository(), new DeviceSessionManager(context));
    }

    /**
     * Creates a new UserController instance.
     *
     * @param userRepository the user repository
     * @param deviceSessionManager the device session manager
     */
    public UserController(UserRepository userRepository, DeviceSessionManager deviceSessionManager) {
        this.userRepository = userRepository;
        this.deviceSessionManager = deviceSessionManager;
    }

    /**
     * Performs get user by device id.
     *
     * @param deviceId the device id
     * @param listener the listener
     */
    public void getUserByDeviceId(String deviceId, OnCompleteListener<User> listener) {
        userRepository.getUserByDeviceId(deviceId, listener);
    }

    /**
     * Performs load current user.
     *
     * @param listener the listener
     */
    public void loadCurrentUser(OnCompleteListener<User> listener) {
        String deviceId = getCurrentDeviceId();
        userRepository.findUserByDeviceId(deviceId, (user, success) -> {
            if (!success) {
                listener.onComplete(null, false);
                return;
            }

            if (user != null && isBlank(user.getDeviceId())) {
                user.setDeviceId(deviceId);
            }
            listener.onComplete(user, true);
        });
    }

    /**
     * Performs search users.
     *
     * @param query the query
     * @param listener the listener
     */
    public void searchUsers(String query, OnCompleteListener<java.util.List<User>> listener) {
        userRepository.searchUsers(query, listener);
    }

    /**
     * Performs load or create user.
     *
     * @param listener the listener
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
     * Performs create new user.
     *
     * @param deviceId the device id
     * @param listener the listener
     */
    private void createNewUser(String deviceId, OnCompleteListener<User> listener) {
        userRepository.createNewUser(deviceId, listener);
    }


    /**
     * Performs update user profile.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email
     * @param phone the phone
     * @param notiEnabled the noti enabled
     * @param listener the listener
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
     * Returns whether this instance has completed profile.
     *
     * @param user the user
     * @return whether this instance has completed profile
     */
    public boolean hasCompletedProfile(User user) {
        if (user == null) {
            return false;
        }

        return !isBlank(user.getFirstName())
                && !isBlank(user.getLastName())
                && !isBlank(user.getEmail());
    }

    /**
     * Performs update current fcm token.
     *
     * @param token the token
     */
    public void updateCurrentFcmToken(String token) {
        userRepository.updateFcmToken(getCurrentDeviceId(), token);
    }
    /**
     * Performs toggle saved event.
     *
     * @param eventId the event id
     * @param isSaving whether saving
     * @param listener the listener
     */
    public void toggleSavedEvent(String eventId, boolean isSaving, OnCompleteListener<Boolean> listener) {
        userRepository.toggleSavedEvent(getCurrentDeviceId(), eventId, isSaving, listener);
    }
    /**
     * Performs delete current user.
     *
     * @param listener the listener
     */
    public void deleteCurrentUser(OnCompleteListener<Boolean> listener) {
        userRepository.deleteCurrentUser(getCurrentDeviceId(), listener);
    }

    /**
     * Performs is current user admin.
     *
     * @param listener the listener
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


    /**
     * Returns the current device id.
     *
     * @return the current device id
     */
    public String getCurrentDeviceId() {
        return deviceSessionManager.getCurrentDeviceId();
    }
    /**
     * Returns whether new device id.
     *
     * @return whether new device id
     */
    public boolean isNewDeviceId() {
        return deviceSessionManager.isNewDeviceId();
    }

    /**
     * Returns the result of validate profile fields.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email
     * @return the result of this call
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
     * Returns whether blank.
     *
     * @param value the value
     * @return whether blank
     */
    private boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }

}
