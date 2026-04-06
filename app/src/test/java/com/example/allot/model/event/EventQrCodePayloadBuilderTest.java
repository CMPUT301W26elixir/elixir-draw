package com.example.allot.model.event;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EventQrCodePayloadBuilderTest {
    @Test
    public void buildEventPayload_trimsEventId() {
        assertEquals("event-1", EventQrCodePayloadBuilder.buildEventPayload("  event-1  "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildEventPayload_rejectsBlankEventId() {
        EventQrCodePayloadBuilder.buildEventPayload("   ");
    }
}
