package com.example.allot.model;

import java.util.ArrayList;

/**
 * Represents a user in the application, including profile information,
 * event history, created events, and saved events.
 */
public class User {
    public String deviceId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean notiEnabled;
    private String role;

    public ArrayList<String> history;

    public ArrayList<String> myEvents;
    public ArrayList<String> savedEvents;

    /**
     * Creates an empty User object with initialized event lists.
     */
    public User(){
        this.history = new ArrayList<String>();
        this.myEvents = new ArrayList<String>();
        this.savedEvents = new ArrayList<String>();
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
        this.history = new ArrayList<String>();
        this.myEvents = new ArrayList<String>();
        this.savedEvents = new ArrayList<String>();
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
    public void setNotiEnabled(boolean notiEnabled) {
        this.notiEnabled = notiEnabled;
    }

    /**
     * Returns the role of the user.
     *
     * @return the user's role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the user.
     *
     * @param role the role to assign
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Adds an event ID to the user's history.
     *
     * @param eventID the event ID to add
     */
    public void addHistory(String eventID){
        this.history.add(eventID);
    }

    /**
     * Returns the user's event history.
     *
     * @return the list of event IDs in the user's history
     */
    public ArrayList<String> getHistory(){
        return this.history;
    }

    /**
     * Adds an event ID to the user's created events list.
     * Sets the user's role to organizer when the first event is added.
     *
     * @param eventID the event ID to add
     */
    public void addEvents(String eventID){
        this.myEvents.add(eventID);
        if (myEvents.size() == 1){
            this.role = "organizer";
        }
    }

    /**
     * Returns the list of events created by the user.
     *
     * @return the list of created event IDs
     */
    public ArrayList<String> getEvents(){
        return this.myEvents;
    }

    /**
     * Adds an event ID to the user's saved events list if it is not already saved.
     *
     * @param eventID the event ID to save
     */
    public void addSavedEvent(String eventID) {
        if (!this.savedEvents.contains(eventID)) {
            this.savedEvents.add(eventID);
        }
    }

    /**
     * Removes an event ID from the user's saved events list.
     *
     * @param eventID the event ID to remove
     */
    public void removeSavedEvent(String eventID) {
        this.savedEvents.remove(eventID);
    }

    /**
     * Returns the list of saved event IDs.
     *
     * @return the user's saved events
     */
    public ArrayList<String> getSavedEvents() {
        return this.savedEvents;
    }
}