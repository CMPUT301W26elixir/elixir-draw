package com.example.allot.qr;
public final class QrCodePayloadBuilder {
    private static final String EVENT_URI_PREFIX = "allot://event/";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private QrCodePayloadBuilder() {
    }

    /**
     * Builds the QR code payload for an event using its event ID.
     *
     * @param eventId the ID of the event
     * @return the formatted QR code payload string for the event
     * @throws IllegalArgumentException if the event ID is blank
     */
    public static String buildEventPayload(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
        return EVENT_URI_PREFIX + eventId.trim();
    }
}






