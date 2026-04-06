package com.example.allot.controller.event;

/**
 * Holds resolved latitude and longitude for an event address.
 */
public class EventLocationCoordinates {
    private final double latitude;
    private final double longitude;

    /**
     * Creates a new EventLocationCoordinates instance.
     */
    public EventLocationCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns whether g.et Latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns whether g.et Longitude
     */
    public double getLongitude() {
        return longitude;
    }
}
