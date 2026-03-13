package com.example.allot.qr;

public final class QrCodePayloadBuilder {
    private static final String EVENT_URI_PREFIX = "allot://event/";

    private QrCodePayloadBuilder() {
    }

    public static String buildEventPayload(String eventId) {
        if (eventId == null || eventId.trim().isEmpty()) {
            throw new IllegalArgumentException("eventId must not be blank");
        }
        return EVENT_URI_PREFIX + eventId.trim();
    }
}
