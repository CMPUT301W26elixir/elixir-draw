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
     * Creates a new AdminProfileController instance.
     *
     * @param context the context
     */
    public AdminProfileController(Context context) {
        this(new UserRepository(), new UserController(context));
    }

    /**
     * Creates a new AdminProfileController instance.
     *
     * @param userRepository the user repository
     * @param userController the user controller
     */
    public AdminProfileController(UserRepository userRepository, UserController userController) {
        this.userRepository = userRepository;
        this.userController = userController;
    }

    /**
     * Performs load all profiles.
     *
     * @param listener the listener
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
     * Performs delete profile.
     *
     * @param deviceId the device id
     * @param listener the listener
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
