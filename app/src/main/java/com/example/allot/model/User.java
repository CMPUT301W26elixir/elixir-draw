package com.example.allot.model;

import java.util.ArrayList;

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
     * Default constructor required for Firestore deserialization.
     * Initializes list fields to avoid null references.
     */
    public User() {
        this.history = new ArrayList<>();
        this.myEvents = new ArrayList<>();
        this.savedEvents = new ArrayList<>();
    }

    /**
     * Constructs a new User object with the provided profile data.
     * Notification enabled by default.
     *
     * @param deviceId unique device identifier
     * @param firstname user's first name
     * @param lastName user's last name
     * @param email user's email
     * @param phone user's phone number
     * @param role user's role (e.g., "participant", "organizer")
     */
    public User(String deviceId, String firstname, String lastName, String email, String phone, String role) {
        this.deviceId = deviceId;
        this.firstName = firstname;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.notiEnabled = true;
        this.role = role;
        this.history = new ArrayList<>();
        this.myEvents = new ArrayList<>();
        this.savedEvents = new ArrayList<>();
    }

    /** Returns the device ID for this user. */
    public String getDeviceId() {
        return deviceId;
    }

    /** Sets the device ID for this user. */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /** Returns the first name of the user. */
    public String getFirstName() {
        return firstName;
    }

    /** Sets the first name of the user. */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** Returns the last name of the user. */
    public String getLastName() {
        return lastName;
    }

    /** Sets the last name of the user. */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the full name of the user.
     * Safely handles missing first or last name.
     */
    public String getName() {
        String safeFirstName = firstName == null ? "" : firstName.trim();
        String safeLastName = lastName == null ? "" : lastName.trim();

        if (safeFirstName.isEmpty()) return safeLastName;
        if (safeLastName.isEmpty()) return safeFirstName;

        return safeFirstName + " " + safeLastName;
    }

    /** Returns the user's email. */
    public String getEmail() {
        return email;
    }

    /** Sets the user's email. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Returns the user's phone number. */
    public String getPhone() {
        return phone;
    }

    /** Sets the user's phone number. */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** Returns whether notifications are enabled. */
    public boolean isNotiEnabled() {
        return notiEnabled;
    }

    /** Sets the notification enabled state. */
    public void setNotiEnabled(boolean notiEnabled) {
        this.notiEnabled = notiEnabled;
    }

    /** Returns the role of the user. */
    public String getRole() {
        return role;
    }

    /** Sets the role of the user. */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Adds an event ID to the user's history of joined events.
     *
     * @param eventID the event ID to add
     */
    public void addHistory(String eventID) {
        this.history.add(eventID);
    }

    /** Returns the user's event history. */
    public ArrayList<String> getHistory() {
        return this.history;
    }

    /**
     * Adds an event ID to the user's list of organized events.
     * Automatically sets the role to "organizer" if this is the first event.
     *
     * @param eventID the event ID to add
     */
    public void addEvents(String eventID) {
        this.myEvents.add(eventID);
        if (myEvents.size() == 1) {
            this.role = "organizer";
        }
    }

    /** Returns the user's list of organized events. */
    public ArrayList<String> getEvents() {
        return this.myEvents;
    }

    /**
     * Adds an event to the user's saved events list, avoiding duplicates.
     *
     * @param eventID the event ID to save
     */
    public void addSavedEvent(String eventID) {
        if (!this.savedEvents.contains(eventID)) {
            this.savedEvents.add(eventID);
        }
    }

    /**
     * Removes an event from the user's saved events list.
     *
     * @param eventID the event ID to remove
     */
    public void removeSavedEvent(String eventID) {
        this.savedEvents.remove(eventID);
    }

    /** Returns the user's list of saved events. */
    public ArrayList<String> getSavedEvents() {
        return this.savedEvents;
    }
}