package com.example.allot.model.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EventQrCodePayloadBuilderTest {
    /**
     * Builds event payload_trims event id.
     */
    @Test
    public void buildEventPayload_trimsEventId() {
        assertEquals("event-1", EventQrCodePayloadBuilder.buildEventPayload("  event-1  "));
    }

    /**
     * Builds event payload_rejects blank event id.
     */
    @Test(expected = IllegalArgumentException.class)
    public void buildEventPayload_rejectsBlankEventId() {
        EventQrCodePayloadBuilder.buildEventPayload("   ");
    }
}
