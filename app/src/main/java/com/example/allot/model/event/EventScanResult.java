package com.example.allot.model.event;

/**
 * Represents the outcome of scanning an event QR code.
 */
public class EventScanResult {
    public enum Status {
        OPEN_EVENT,
        INVALID_PAYLOAD,
        EVENT_NOT_FOUND,
        LOAD_ERROR
    }

    private final Status status;
    private final Event event;
    private final String eventId;
    private final Integer messageResId;

    private EventScanResult(Status status, Event event, String eventId, Integer messageResId) {
        this.status = status;
        this.event = event;
        this.eventId = eventId;
        this.messageResId = messageResId;
    }

    public static EventScanResult openEvent(Event event) {
        String eventId = event == null ? "" : event.getEventId();
        return new EventScanResult(Status.OPEN_EVENT, event, eventId, null);
    }

    public static EventScanResult invalidPayload(String eventId, int messageResId) {
        return new EventScanResult(Status.INVALID_PAYLOAD, null, eventId, messageResId);
    }

    public static EventScanResult eventNotFound(String eventId, int messageResId) {
        return new EventScanResult(Status.EVENT_NOT_FOUND, null, eventId, messageResId);
    }

    public static EventScanResult loadError(String eventId, int messageResId) {
        return new EventScanResult(Status.LOAD_ERROR, null, eventId, messageResId);
    }

    public Status getStatus() {
        return status;
    }

    public Event getEvent() {
        return event;
    }

    public String getEventId() {
        return eventId;
    }

    public Integer getMessageResId() {
        return messageResId;
    }

    public boolean shouldOpenEvent() {
        return status == Status.OPEN_EVENT && event != null;
    }
}
