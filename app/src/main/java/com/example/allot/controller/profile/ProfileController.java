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
     */
    public ProfileController(Context context) {
        this(new UserController(context));
    }

    /**
     * Creates a new ProfileController instance.
     */
    ProfileController(UserController userController) {
        this.userController = userController;
    }

    /**
     * Loads the current profile and builds the initial screen state.
     *
     * @param listener the listener that receives the profile state
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
     * Builds the current screen state from the values shown in the view.
     *
     * @param originalSnapshot the original loaded profile values
     * @param currentSnapshot the current form values
     * @param isSaving whether a save is in progress
     * @param isDeleting whether a delete is in progress
     * @return the profile screen state
     */
    public boolean isSaveAvailable(ProfileFormSnapshot originalSnapshot,
                                   ProfileFormSnapshot currentSnapshot,
                                   boolean isSaving,
                                   boolean isDeleting) {
        boolean hasUnsavedChanges = !Objects.equals(originalSnapshot, currentSnapshot);
        return hasUnsavedChanges && !isSaving && !isDeleting;
    }

    /**
     * Saves the current profile values.
     *
     * @param currentSnapshot the current form values
     * @param listener the listener that receives the save result
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
     * Deletes the current profile.
     *
     * @param listener the listener that receives the delete result
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









