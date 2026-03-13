package com.example.allot;

import com.example.allot.qr.QrCodePayloadBuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExampleUnitTest {
    @Test
    public void buildEventPayload_returnsCustomAppLink() {
        assertEquals("allot://event/event-123", QrCodePayloadBuilder.buildEventPayload("event-123"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void buildEventPayload_rejectsBlankIds() {
        QrCodePayloadBuilder.buildEventPayload("   ");
    }
}
