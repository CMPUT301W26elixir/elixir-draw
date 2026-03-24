package com.example.allot.controller.event;

/**
 * Validates whether an entrant is close enough to an event to join.
 */
public class EventJoinDistanceValidator {

    // Can make this a field that can be edited when turning on geolocation?
    public static final double ALLOWED_RADIUS_KM = 50.0;
    // Used for converting to KM from long/lat degrees from google maps
    private static final double KM_PER_DEGREE = 111.0;

    /**
     * Returns true when the entrant is within the allowed distance of the event.
     */
    public boolean isWithinAllowedRadius(Double entrantLatitude,
                                         Double entrantLongitude,
                                         Double eventLatitude,
                                         Double eventLongitude) {
        if (entrantLatitude == null || entrantLongitude == null
                || eventLatitude == null || eventLongitude == null) {
            return false;
        }

        return calculateDistanceKm(
                entrantLatitude,
                entrantLongitude,
                eventLatitude,
                eventLongitude
        ) <= ALLOWED_RADIUS_KM;
    }
    // The following function is from OpenAI, ChatGPT, "How can I calculate distance between two locations through latitude and longitude?", 2026-03-23
    private double calculateDistanceKm(double startLatitude,
                                       double startLongitude,
                                       double endLatitude,
                                       double endLongitude) {
        double latDistanceKm = (endLatitude - startLatitude) * KM_PER_DEGREE;
        double averageLatitude = (startLatitude + endLatitude) / 2.0;
        double lngDistanceKm = (endLongitude - startLongitude)
                * KM_PER_DEGREE
                * Math.cos(Math.toRadians(averageLatitude));

        return Math.sqrt(latDistanceKm * latDistanceKm + lngDistanceKm * lngDistanceKm);
    }
}
