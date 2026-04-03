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

    public NotificationItem() {}

    public NotificationItem(String userId, String eventId, String title, String message) {
        this.userId = userId;
        this.eventId = eventId;
        this.title = title;
        this.message = message;
        this.read = false;
        this.createdAt = com.google.firebase.Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public com.google.firebase.Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(com.google.firebase.Timestamp createdAt) { this.createdAt = createdAt; }
}