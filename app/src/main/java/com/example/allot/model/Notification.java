package com.example.allot.model;

import java.util.Date;

/**
 * Represents a notification message sent to a user.
 */
public class Notification {
    private String id;
    private String title;
    private String body;
    private Date timestamp;
    private String eventId;
    private boolean isRead;

    /**
     * Required no-argument constructor for Firestore.
     */
    public Notification() {
    }

    /**
     * Creates a new Notification with the specified details.
     *
     * @param id        unique identifier for the notification
     * @param title     the subject line of the notification
     * @param body      the detailed message content
     * @param timestamp when the notification was sent
     * @param eventId   the ID of the event this notification is related to
     */
    public Notification(String id, String title, String body, Date timestamp, String eventId) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.eventId = eventId;
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
