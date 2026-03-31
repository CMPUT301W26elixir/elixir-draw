package com.example.allot;

import static org.junit.Assert.assertEquals;

import com.example.allot.model.event.EventQrCodePayloadBuilder;
import org.junit.Test;
public class ExampleUnitTest {
    @Test
    public void buildEventPayload_returnsTrimmedEventId() {
        assertEquals("event-123", EventQrCodePayloadBuilder.buildEventPayload("  event-123  "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildEventPayload_rejectsBlankEventId() {
        EventQrCodePayloadBuilder.buildEventPayload("   ");
    }
}







