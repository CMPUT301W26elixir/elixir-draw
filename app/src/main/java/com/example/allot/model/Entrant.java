package com.example.allot.model;

public class Entrant {
    private String deviceId;
    private String name;
    private String email;
    private String phone;
    private String role;

    public Entrant() {}

    public Entrant(String deviceId, String name, String email, String phone, String role) {
        this.deviceId = deviceId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }

    public Entrant(String deviceId, String newUser, String s, String s1, String entrant, String s2) {
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
}
