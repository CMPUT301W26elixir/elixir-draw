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
     * Returns the result of build event payload.
     *
     * @param eventId the event id
     * @return the result of this call
     */
    public static String buildEventPayload(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
        return eventId.trim();
    }
}
