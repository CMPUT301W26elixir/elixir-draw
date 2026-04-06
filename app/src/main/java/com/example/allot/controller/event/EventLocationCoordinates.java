package com.example.allot.controller.event;

/**
 * Holds resolved latitude and longitude for an event address.
 */
public class EventLocationCoordinates {
    private final double latitude;
    private final double longitude;

    /**
     * Creates a new EventLocationCoordinates instance.
     *
     * @param latitude the latitude
     * @param longitude the longitude
     */
    public EventLocationCoordinates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Returns the latitude.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude.
     *
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }
}
