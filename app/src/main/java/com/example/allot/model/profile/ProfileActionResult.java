package com.example.allot.model.profile;

/**
 * Holds the result of a profile action and the latest form data.
 */
public class ProfileActionResult {
    private final boolean success;
    private final String message;
    private final ProfileFormSnapshot formSnapshot;

    /**
     * Creates a profile action result for the view layer.
     *
     * @param success true when the action succeeded
     * @param message the message shown to the user
     * @param formSnapshot the latest form snapshot after the action
     */
    public ProfileActionResult(boolean success, String message, ProfileFormSnapshot formSnapshot) {
        this.success = success;
        this.message = message;
        this.formSnapshot = formSnapshot;
    }

    /**
     * @return true when the profile action succeeded
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * @return the user-facing result message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the latest profile form snapshot
     */
    public ProfileFormSnapshot getFormSnapshot() {
        return formSnapshot;
    }
}









