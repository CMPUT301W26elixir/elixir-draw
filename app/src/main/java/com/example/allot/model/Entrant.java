package com.example.allot.model;

import java.util.ArrayList;

public class Entrant {
    private String deviceId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private boolean notiEnabled;
    private String role;

    public ArrayList<Event> history;

    public Entrant(){}
    public Entrant(String deviceId, String firstname, String lastName, String email, String phone, String role) {
        this.deviceId = deviceId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.notiEnabled = notiEnabled;
        this.role = role;
        this.history = new ArrayList<Event>();
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

    public void addHistory(Event event){
        this.history.add(event);
    }
    public ArrayList<Event> getHistory(){
        return this.history;
    }
}
