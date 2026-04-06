package com.example.allot.controller.event;

/**
 * Resolves an event address string into map coordinates.
 */
public interface EventLocationGeocodingService {
    /**
     * Handles geocode.
     */
    EventLocationCoordinates geocode(String location);
}
