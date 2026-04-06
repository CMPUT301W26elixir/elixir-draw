package com.example.allot.model.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EventScanResultTest {
    /**
     * Performs open event should open when event present.
     */
    @Test
    public void openEvent_shouldOpenWhenEventPresent() {
        Event event = new Event();
        event.setEventId("event-1");

        EventScanResult result = EventScanResult.openEvent(event);

        assertEquals(EventScanResult.Status.OPEN_EVENT, result.getStatus());
        assertEquals("event-1", result.getEventId());
        assertEquals(event, result.getEvent());
        assertTrue(result.shouldOpenEvent());
    }

    /**
     * Performs invalid and error results do not open event.
     */
    @Test
    public void invalidAndErrorResults_doNotOpenEvent() {
        EventScanResult invalid = EventScanResult.invalidPayload("bad", 1);
        EventScanResult notFound = EventScanResult.eventNotFound("missing", 2);
        EventScanResult loadError = EventScanResult.loadError("event-1", 3);

        assertEquals(EventScanResult.Status.INVALID_PAYLOAD, invalid.getStatus());
        assertEquals(Integer.valueOf(1), invalid.getMessageResId());
        assertFalse(invalid.shouldOpenEvent());

        assertEquals(EventScanResult.Status.EVENT_NOT_FOUND, notFound.getStatus());
        assertFalse(notFound.shouldOpenEvent());

        assertEquals(EventScanResult.Status.LOAD_ERROR, loadError.getStatus());
        assertNull(loadError.getEvent());
        assertFalse(loadError.shouldOpenEvent());
    }
}
