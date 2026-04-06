package com.example.allot.model.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EventQrCodePayloadBuilderTest {
    /**
     * Performs build event payload trims event id.
     */
    @Test
    public void buildEventPayload_trimsEventId() {
        assertEquals("event-1", EventQrCodePayloadBuilder.buildEventPayload("  event-1  "));
    }

    /**
     * Performs build event payload rejects blank event id.
     */
    @Test(expected = IllegalArgumentException.class)
    public void buildEventPayload_rejectsBlankEventId() {
        EventQrCodePayloadBuilder.buildEventPayload("   ");
    }
}
