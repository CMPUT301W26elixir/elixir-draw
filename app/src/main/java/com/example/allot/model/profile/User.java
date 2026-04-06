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
     * Creates a new User instance.
     */
    public User(){
        this.history = new ArrayList<>();
        this.myEvents = new ArrayList<>();
        this.savedEvents = new ArrayList<>();
    }

    /**
     * Creates a new User instance.
     *
     * @param deviceId the device id
     * @param firstname the firstname
     * @param lastName the last name
     * @param email the email
     * @param phone the phone
     * @param role the role
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
     * Returns the device id.
     *
     * @return the device id
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Updates the device id.
     *
     * @param deviceId the device id
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Updates the first name.
     *
     * @param firstName the first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Updates the last name.
     *
     * @param lastName the last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the name.
     *
     * @return the name
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
     * Returns the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the email.
     *
     * @param email the email
     */
    @SuppressWarnings("unused")
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the phone.
     *
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Updates the phone.
     *
     * @param phone the phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the profile photo url.
     *
     * @return the profile photo url
     */
    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    /**
     * Updates the profile photo url.
     *
     * @param profilePhotoUrl the profile photo url
     */
    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    /**
     * Returns whether noti enabled.
     *
     * @return whether noti enabled
     */
    public boolean isNotiEnabled() {
        return notiEnabled;
    }

    /**
     * Updates the noti enabled.
     *
     * @param notiEnabled the noti enabled
     */
    @SuppressWarnings("unused")
    public void setNotiEnabled(boolean notiEnabled) {
        this.notiEnabled = notiEnabled;
    }

    /**
     * Returns the role.
     *
     * @return the role
     */
    @SuppressWarnings("unused")
    public String getRole() {
        return role;
    }

    /**
     * Updates the role.
     *
     * @param role the role
     */
    @SuppressWarnings("unused")
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns the fcm token.
     *
     * @return the fcm token
     */
    public String getFcmToken() {
        return fcmToken;
    }

    /**
     * Updates the fcm token.
     *
     * @param fcmToken the fcm token
     */
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    /**
     * Returns the history.
     *
     * @return the history
     */
    public ArrayList<String> getHistory(){
        if (history == null) {
            history = new ArrayList<>();
        }
        return history;
    }

    /**
     * Updates the history.
     *
     * @param history the history
     */
    @SuppressWarnings("unused")
    public void setHistory(ArrayList<String> history) {
        this.history = history;
    }

    /**
     * Returns the my events.
     *
     * @return the my events
     */
    public ArrayList<String> getMyEvents() {
        if (myEvents == null) {
            myEvents = new ArrayList<>();
        }
        return myEvents;
    }

    /**
     * Updates the my events.
     *
     * @param myEvents the my events
     */
    @SuppressWarnings("unused")
    public void setMyEvents(ArrayList<String> myEvents) {
        this.myEvents = myEvents;
    }

    /**
     * Returns the saved events.
     *
     * @return the saved events
     */
    public ArrayList<String> getSavedEvents() {
        if (savedEvents == null) {
            savedEvents = new ArrayList<>();
        }
        return savedEvents;
    }

    /**
     * Updates the saved events.
     *
     * @param savedEvents the saved events
     */
    @SuppressWarnings("unused")
    public void setSavedEvents(ArrayList<String> savedEvents) {
        this.savedEvents = savedEvents;
    }
}
