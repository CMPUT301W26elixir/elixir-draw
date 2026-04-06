package com.example.allot.model.profile;

/**
 * Holds the result of a profile action and the latest form data.
 */
public class ProfileActionResult {
    private final boolean success;
    private final String message;
    private final ProfileFormSnapshot formSnapshot;

    /**
     * Creates a new ProfileActionResult instance.
     *
     * @param success the success
     * @param message the message
     * @param formSnapshot the form snapshot
     */
    public ProfileActionResult(boolean success, String message, ProfileFormSnapshot formSnapshot) {
        this.success = success;
        this.message = message;
        this.formSnapshot = formSnapshot;
    }

    /**
     * Returns whether success.
     *
     * @return whether success
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the form snapshot.
     *
     * @return the form snapshot
     */
    public ProfileFormSnapshot getFormSnapshot() {
        return formSnapshot;
    }
}









