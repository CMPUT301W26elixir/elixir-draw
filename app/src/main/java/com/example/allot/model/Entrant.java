package com.example.allot.model;

import java.util.ArrayList;

public class Entrant {
    private String deviceId;
    private String name;
    private String email;
    private String phone;
    private String role;

    public ArrayList<Event> history;

    public Entrant(){}
    public Entrant(String deviceId, String name, String email, String phone, String role) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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
