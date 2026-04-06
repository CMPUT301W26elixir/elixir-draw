package com.example.allot.model.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import org.junit.Test;

/**
 * Tests the waitlist join location.
 */
public class WaitlistJoinLocationTest {
    /**
     * Performs waitlist join location defaults to null.
     */
    @Test
    public void waitlistJoinLocation_defaultsToNull() {
        WaitlistJoinLocation joinLocation = new WaitlistJoinLocation();

        assertNull(joinLocation.getLatitude());
        assertNull(joinLocation.getLongitude());
        assertNull(joinLocation.getJoinedAt());
    }

    /**
     * Performs waitlist join location can be set and read back.
     */
    @Test
    public void waitlistJoinLocation_canBeSetAndReadBack() {
        WaitlistJoinLocation joinLocation = new WaitlistJoinLocation();
        Date joinedAt = new Date();

        joinLocation.setLatitude(53.5232);
        joinLocation.setLongitude(-113.5263);
        joinLocation.setJoinedAt(joinedAt);

        assertEquals(Double.valueOf(53.5232), joinLocation.getLatitude());
        assertEquals(Double.valueOf(-113.5263), joinLocation.getLongitude());
        assertEquals(joinedAt, joinLocation.getJoinedAt());
    }
}
