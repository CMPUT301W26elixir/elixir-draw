package com.example.allot.model.profile;

import java.util.ArrayList;
/**
 * Holds a user profile and the event lists tied to that user.
 */
public class User {
    private String deviceId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String profilePhotoUrl;
    private boolean notiEnabled;
    private String role;
    private String fcmToken;

    private ArrayList<String> history;
    private ArrayList<String> myEvents;
    private ArrayList<String> savedEvents;

    /**
     * Creates an empty User object with initialized event lists.
     */
    public User(){
        this.history = new ArrayList<>();
        this.myEvents = new ArrayList<>();
        this.savedEvents = new ArrayList<>();
    }

    /**
     * Creates a user with the provided profile information.
     *
     * @param deviceId the unique device ID for the user
     * @param firstname the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param phone the user's phone number
     * @param role the user's role in the application
     */
    public User(String deviceId, String firstname, String lastName, String email, String phone, String role) {
        this.deviceId = deviceId;
        this.firstName = firstname;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.notiEnabled = true;
        this.role = role;
        this.fcmToken = null;
        this.history = new ArrayList<>();
        this.myEvents = new ArrayList<>();
        this.savedEvents = new ArrayList<>();
    }

    /**
     * Returns the device ID of the user.
     *
     * @return the user's device ID
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the device ID of the user.
     *
     * @param deviceId the device ID to assign
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the first name of the user.
     *
     * @return the user's first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name of the user.
     *
     * @param firstName the first name to assign
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name of the user.
     *
     * @return the user's last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name of the user.
     *
     * @param lastName the last name to assign
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the full display name of the user.
     *
     * @return the combined first and last name, or whichever one is available
     */
    public String getName() {
        String safeFirstName = firstName == null ? "" : firstName.trim();
        String safeLastName = lastName == null ? "" : lastName.trim();

        if (safeFirstName.isEmpty()) {
            return safeLastName;
        }

        if (safeLastName.isEmpty()) {
            return safeFirstName;
        }

        return safeFirstName + " " + safeLastName;
    }

    /**
     * Returns the email address of the user.
     *
     * @return the user's email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email the email address to assign
     */
    @SuppressWarnings("unused")
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the phone number of the user.
     *
     * @return the user's phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of the user.
     *
     * @param phone the phone number to assign
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the profile photo URL for this user.
     *
     * @return the profile photo URL
     */
    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    /**
     * Sets the profile photo URL for this user.
     *
     * @param profilePhotoUrl the profile photo URL to assign
     */
    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    /**
     * Returns whether notifications are enabled for the user.
     *
     * @return true if notifications are enabled, otherwise false
     */
    public boolean isNotiEnabled() {
        return notiEnabled;
    }

    /**
     * Sets whether notifications are enabled for the user.
     *
     * @param notiEnabled true to enable notifications, false otherwise
     */
    @SuppressWarnings("unused")
    public void setNotiEnabled(boolean notiEnabled) {
        this.notiEnabled = notiEnabled;
    }

    /**
     * Returns the role of the user.
     *
     * @return the user's role
     */
    @SuppressWarnings("unused")
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the user.
     *
     * @param role the role to assign
     */
    @SuppressWarnings("unused")
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the user's FCM token.
     *
     * @return the user's FCM token
     */
    public String getFcmToken() {
        return fcmToken;
    }

    /**
     * Sets the user's FCM token.
     *
     * @param fcmToken the token to assign
     */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * Returns the user's event history.
     *
     * @return the list of event IDs in the user's history
     */
    public ArrayList<String> getHistory(){
        if (history == null) {
            history = new ArrayList<>();
        }
        return history;
    }

    @SuppressWarnings("unused")
    public void setHistory(ArrayList<String> history) {
        this.history = history;
    }

    public ArrayList<String> getMyEvents() {
        if (myEvents == null) {
            myEvents = new ArrayList<>();
        }
        return myEvents;
    }

    @SuppressWarnings("unused")
    public void setMyEvents(ArrayList<String> myEvents) {
        this.myEvents = myEvents;
    }

    /**
     * Returns the list of saved event IDs.
     *
     * @return the user's saved events
     */
    public ArrayList<String> getSavedEvents() {
        if (savedEvents == null) {
            savedEvents = new ArrayList<>();
        }
        return savedEvents;
    }

    @SuppressWarnings("unused")
    public void setSavedEvents(ArrayList<String> savedEvents) {
        this.savedEvents = savedEvents;
    }
}
