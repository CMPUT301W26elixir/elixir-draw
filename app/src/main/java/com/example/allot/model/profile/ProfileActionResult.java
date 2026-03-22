package com.example.allot.model.profile;
public class ProfileActionResult {
    private final boolean success;
    private final String message;
    private final ProfileFormSnapshot formSnapshot;

    public ProfileActionResult(boolean success, String message, ProfileFormSnapshot formSnapshot) {
        this.success = success;
        this.message = message;
        this.formSnapshot = formSnapshot;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public ProfileFormSnapshot getFormSnapshot() {
        return formSnapshot;
    }
}









