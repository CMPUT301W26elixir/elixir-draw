package com.example.allot.model.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NotificationItemTest {
    @Test
    public void constructor_setsDefaultUnreadStateAndTimestamp() {
        NotificationItem item = new NotificationItem("user-1", "event-1", "Title", "Message");

        assertEquals("user-1", item.getUserId());
        assertEquals("event-1", item.getEventId());
        assertEquals("Title", item.getTitle());
        assertEquals("Message", item.getMessage());
        assertFalse(item.isRead());
        assertNotNull(item.getCreatedAt());
    }

    @Test
    public void setters_updateFields() {
        NotificationItem item = new NotificationItem();
        item.setId("id-1");
        item.setUserId("user-2");
        item.setEventId("event-2");
        item.setTitle("Updated");
        item.setMessage("Updated Message");
        item.setRead(true);

        assertEquals("id-1", item.getId());
        assertEquals("user-2", item.getUserId());
        assertEquals("event-2", item.getEventId());
        assertEquals("Updated", item.getTitle());
        assertEquals("Updated Message", item.getMessage());
        assertTrue(item.isRead());
    }
}
