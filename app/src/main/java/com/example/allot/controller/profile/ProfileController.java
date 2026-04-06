package com.example.allot.controller.profile;

import android.content.Context;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.model.profile.ProfileActionResult;
import com.example.allot.model.profile.ProfileFormSnapshot;
import com.example.allot.model.profile.User;
import java.util.Objects;
/**
 * Handles profile loading, compare checks, and updates.
 */
public class ProfileController {
    private final UserController userController;

    /**
     * Creates a new ProfileController instance.
     *
     * @param context the context
     */
    public ProfileController(Context context) {
        this(new UserController(context));
    }

    /**
     * Creates a new ProfileController instance.
     *
     * @param userController the user controller
     */
    ProfileController(UserController userController) {
        this.userController = userController;
    }

    /**
     * Performs load profile.
     *
     * @param listener the listener
     */
    public void loadProfile(OnCompleteListener<ProfileFormSnapshot> listener) {
        userController.loadCurrentUser((User user, boolean success) -> {
            if (!success || user == null) {
                listener.onComplete(null, false);
                return;
            }

            ProfileFormSnapshot snapshot = ProfileFormSnapshot.fromUser(user);
            listener.onComplete(snapshot, true);
        });
    }

    /**
     * Returns whether save available.
     *
     * @param originalSnapshot the original snapshot
     * @param currentSnapshot the current snapshot
     * @param isSaving whether saving
     * @param isDeleting whether deleting
     * @return whether save available
     */
    public boolean isSaveAvailable(ProfileFormSnapshot originalSnapshot,
                                   ProfileFormSnapshot currentSnapshot,
                                   boolean isSaving,
                                   boolean isDeleting) {
        boolean hasUnsavedChanges = !Objects.equals(originalSnapshot, currentSnapshot);
        return hasUnsavedChanges && !isSaving && !isDeleting;
    }

    /**
     * Performs save profile.
     *
     * @param currentSnapshot the current snapshot
     * @param listener the listener
     */
    public void saveProfile(ProfileFormSnapshot currentSnapshot,
                            OnCompleteListener<ProfileActionResult> listener) {
        userController.updateUserProfile(
                currentSnapshot.getFirstName(),
                currentSnapshot.getLastName(),
                currentSnapshot.getEmail(),
                currentSnapshot.getPhone(),
                currentSnapshot.isNotificationsEnabled(),
                (user, success) -> {
                    if (!success || user == null) {
                        listener.onComplete(
                                new ProfileActionResult(false,
                                        "Could not save your profile. Please try again.",
                                        null),
                                false
                        );
                        return;
                    }

                    ProfileFormSnapshot savedSnapshot = ProfileFormSnapshot.fromUser(user);
                    listener.onComplete(
                            new ProfileActionResult(
                                    true,
                                    "Profile updated.",
                                    savedSnapshot
                            ),
                            true
                    );
                }
        );
    }

    /**
     * Performs delete profile.
     *
     * @param listener the listener
     */
    public void deleteProfile(OnCompleteListener<ProfileActionResult> listener) {
        userController.deleteCurrentUser((result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(
                        new ProfileActionResult(false, "Could not delete your profile. Please try again.", null),
                        false
                );
                return;
            }

            listener.onComplete(new ProfileActionResult(true, "Profile deleted.", null), true);
        });
    }
}









