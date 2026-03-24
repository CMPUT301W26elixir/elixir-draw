package com.example.allot.controller.admin;

import android.content.Context;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.UserRepository;
import com.example.allot.model.profile.User;
import java.util.List;

/**
 * Controller for admin profile operations.
 * Handles loading all profiles and deleting profiles with admin privileges.
 */
public class AdminProfileController {
    private final UserRepository userRepository;
    private final UserController userController;

    /**
     * Creates an AdminProfileController with default dependencies.
     *
     * @param context the context used to access shared preferences
     */
    public AdminProfileController(Context context) {
        this(new UserRepository(), new UserController(context));
    }

    /**
     * Creates an AdminProfileController with provided dependencies.
     *
     * @param userRepository the user repository
     * @param userController the user controller
     */
    public AdminProfileController(UserRepository userRepository, UserController userController) {
        this.userRepository = userRepository;
        this.userController = userController;
    }

    /**
     * Loads all profiles for admin browsing.
     * Only admin users can access this operation.
     *
     * @param listener the listener that receives the profiles list and success result
     */
    public void loadAllProfiles(OnCompleteListener<List<User>> listener) {
        userController.isCurrentUserAdmin((isAdmin, success) -> {
            if (!success || !isAdmin) {
                listener.onComplete(null, false);
                return;
            }
            userRepository.getAllUsers(listener);
        });
    }

    /**
     * Deletes a profile with admin privileges.
     * Removes the user document and all references from event documents.
     * Only admin users can access this operation.
     *
     * @param deviceId the device ID of the user to delete
     * @param listener the listener that receives the deletion success result
     */
    public void deleteProfile(String deviceId, OnCompleteListener<Boolean> listener) {
        userController.isCurrentUserAdmin((isAdmin, success) -> {
            if (!success || !isAdmin) {
                listener.onComplete(false, false);
                return;
            }
            userRepository.deleteUserAsAdmin(deviceId, listener);
        });
    }
}
