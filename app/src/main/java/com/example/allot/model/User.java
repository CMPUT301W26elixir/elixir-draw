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
    public User(){
        this.history = new ArrayList<String>();
        this.myEvents = new ArrayList<String>();
        this.savedEvents = new ArrayList<String>();
    }
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

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isNotiEnabled() {
        return notiEnabled;
    }

    public void setNotiEnabled(boolean notiEnabled) {
        this.notiEnabled = notiEnabled;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }

    public void addHistory(String eventID){
        this.history.add(eventID);
    }
    public ArrayList<String> getHistory(){
        return this.history;
    }
    public void addEvents(String eventID){
        this.myEvents.add(eventID);
        if (myEvents.size() == 1){
            this.role = "organizer";
        }
    }
    public ArrayList<String> getEvents(){
        return this.myEvents;
    }
    public void addSavedEvent(String eventID) {
        if (!this.savedEvents.contains(eventID)) {
            this.savedEvents.add(eventID);
        }
    }

    public void removeSavedEvent(String eventID) {
        this.savedEvents.remove(eventID);
    }

    public ArrayList<String> getSavedEvents() {
        return this.savedEvents;
    }


}