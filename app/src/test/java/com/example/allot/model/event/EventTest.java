package com.example.allot.model.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class EventTest {
    @Test
    public void eventCoordinates_defaultToNull() {
        Event event = new Event();

        assertNull(event.getEventLatitude());
        assertNull(event.getEventLongitude());
    }

    @Test
    public void eventCoordinates_canBeSetAndReadBack() {
        Event event = new Event();

        event.setEventLatitude(53.5232);
        event.setEventLongitude(-113.5263);

        assertEquals(Double.valueOf(53.5232), event.getEventLatitude());
        assertEquals(Double.valueOf(-113.5263), event.getEventLongitude());
    }
}
