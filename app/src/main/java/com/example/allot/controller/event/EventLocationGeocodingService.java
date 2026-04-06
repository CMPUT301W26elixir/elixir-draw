package com.example.allot.controller.event;

/**
 * Resolves an event address string into map coordinates.
 */
public interface EventLocationGeocodingService {
    /**
     * Returns the result of geocode.
     *
     * @param location the location
     * @return the result of this call
     */
    EventLocationCoordinates geocode(String location);
}
