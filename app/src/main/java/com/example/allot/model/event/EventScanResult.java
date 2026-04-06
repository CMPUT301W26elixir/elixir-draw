package com.example.allot.model.event;

/**
 * Represents the outcome of scanning an event QR code.
 */
public class EventScanResult {
    /**
     * Enumerates the available status values.
     */
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

    /**
     * Creates a new EventScanResult instance.
     *
     * @param status the status
     * @param event the event
     * @param eventId the event id
     * @param messageResId the message res id
     */
    private EventScanResult(Status status, Event event, String eventId, Integer messageResId) {
        this.status = status;
        this.event = event;
        this.eventId = eventId;
        this.messageResId = messageResId;
    }

    /**
     * Returns the result of open event.
     *
     * @param event the event
     * @return the result of this call
     */
    public static EventScanResult openEvent(Event event) {
        String eventId = event == null ? "" : event.getEventId();
        return new EventScanResult(Status.OPEN_EVENT, event, eventId, null);
    }

    /**
     * Returns the result of invalid payload.
     *
     * @param eventId the event id
     * @param messageResId the message res id
     * @return the result of this call
     */
    public static EventScanResult invalidPayload(String eventId, int messageResId) {
        return new EventScanResult(Status.INVALID_PAYLOAD, null, eventId, messageResId);
    }

    /**
     * Returns the result of event not found.
     *
     * @param eventId the event id
     * @param messageResId the message res id
     * @return the result of this call
     */
    public static EventScanResult eventNotFound(String eventId, int messageResId) {
        return new EventScanResult(Status.EVENT_NOT_FOUND, null, eventId, messageResId);
    }

    /**
     * Returns the result of load error.
     *
     * @param eventId the event id
     * @param messageResId the message res id
     * @return the result of this call
     */
    public static EventScanResult loadError(String eventId, int messageResId) {
        return new EventScanResult(Status.LOAD_ERROR, null, eventId, messageResId);
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the event.
     *
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Returns the event id.
     *
     * @return the event id
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Returns the message res id.
     *
     * @return the message res id
     */
    public Integer getMessageResId() {
        return messageResId;
    }

    /**
     * Returns whether this instance should open event.
     *
     * @return whether this instance should open event
     */
    public boolean shouldOpenEvent() {
        return status == Status.OPEN_EVENT && event != null;
    }
}
