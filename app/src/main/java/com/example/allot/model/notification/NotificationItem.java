package com.example.allot.model.notification;

/**
 * Represents a notification sent to an entrant.
 */
public class NotificationItem {
    private String id;
    private String userId;
    private String eventId;
    private String title;
    private String message;
    private boolean read;
    private com.google.firebase.Timestamp createdAt;

    /**
     * Creates a new NotificationItem instance.
     */
    public NotificationItem() {}

    /**
     * Creates a new NotificationItem instance.
     */
    public NotificationItem(String userId, String eventId, String title, String message) {
        this.userId = userId;
        this.eventId = eventId;
        this.title = title;
        this.message = message;
        this.read = false;
        this.createdAt = com.google.firebase.Timestamp.now();
    }

    /**
     * Returns whether get Id.
     */
    public String getId() { return id; }
    /**
     * Updates id.
     */
    public void setId(String id) { this.id = id; }
    /**
     * Returns whether get User Id.
     */
    public String getUserId() { return userId; }
    /**
     * Updates user id.
     */
    public void setUserId(String userId) { this.userId = userId; }
    /**
     * Returns whether get Event Id.
     */
    public String getEventId() { return eventId; }
    /**
     * Updates event id.
     */
    public void setEventId(String eventId) { this.eventId = eventId; }
    /**
     * Returns whether get Title.
     */
    public String getTitle() { return title; }
    /**
     * Updates title.
     */
    public void setTitle(String title) { this.title = title; }
    /**
     * Returns whether get Message.
     */
    public String getMessage() { return message; }
    /**
     * Updates message.
     */
    public void setMessage(String message) { this.message = message; }
    /**
     * Returns whether is Read.
     */
    public boolean isRead() { return read; }
    /**
     * Updates read.
     */
    public void setRead(boolean read) { this.read = read; }
    /**
     * Returns whether get Created At.
     */
    public com.google.firebase.Timestamp getCreatedAt() { return createdAt; }
    /**
     * Updates created at.
     */
    public void setCreatedAt(com.google.firebase.Timestamp createdAt) { this.createdAt = createdAt; }
}
