package com.example.allot.model.event;

/**
 * Builds QR payloads for event features.
 */
public final class EventQrCodePayloadBuilder {

    /**
     * Creates a new EventQrCodePayloadBuilder instance.
     */
    private EventQrCodePayloadBuilder() {
    }

    /**
     * Builds the payload used for event QR codes.
     *
     * @param eventId the event identifier to encode
     * @return the trimmed event identifier
     * @throws IllegalArgumentException if the event ID is blank
     */
    public static String buildEventPayload(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
        return eventId.trim();
    }
}
