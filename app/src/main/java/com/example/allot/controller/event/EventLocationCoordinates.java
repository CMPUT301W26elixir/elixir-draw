package com.example.allot.controller.event;

/**
 * Holds resolved latitude and longitude for an event address.
 */
public class EventLocationCoordinates {
    private final double latitude;
    private final double longitude;

    public EventLocationCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
