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
     *
     * @param userId the user id
     * @param eventId the event id
     * @param title the title
     * @param message the message
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
     * Returns the id.
     *
     * @return the id
     */
    public String getId() { return id; }
    /**
     * Updates the id.
     *
     * @param id the id
     */
    public void setId(String id) { this.id = id; }
    /**
     * Returns the user id.
     *
     * @return the user id
     */
    public String getUserId() { return userId; }
    /**
     * Updates the user id.
     *
     * @param userId the user id
     */
    public void setUserId(String userId) { this.userId = userId; }
    /**
     * Returns the event id.
     *
     * @return the event id
     */
    public String getEventId() { return eventId; }
    /**
     * Updates the event id.
     *
     * @param eventId the event id
     */
    public void setEventId(String eventId) { this.eventId = eventId; }
    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() { return title; }
    /**
     * Updates the title.
     *
     * @param title the title
     */
    public void setTitle(String title) { this.title = title; }
    /**
     * Returns the message.
     *
     * @return the message
     */
    public String getMessage() { return message; }
    /**
     * Updates the message.
     *
     * @param message the message
     */
    public void setMessage(String message) { this.message = message; }
    /**
     * Returns whether read.
     *
     * @return whether read
     */
    public boolean isRead() { return read; }
    /**
     * Updates the read.
     *
     * @param read the read
     */
    public void setRead(boolean read) { this.read = read; }
    /**
     * Returns the created at.
     *
     * @return the created at
     */
    public com.google.firebase.Timestamp getCreatedAt() { return createdAt; }
    /**
     * Updates the created at.
     *
     * @param createdAt the created at
     */
    public void setCreatedAt(com.google.firebase.Timestamp createdAt) { this.createdAt = createdAt; }
}
