package com.example.allot.model.event;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Date;
import org.junit.Test;

public class WaitingListTest {
    /**
     * Handles join Locations_defaults To Empty Map.
     */
    @Test
    public void joinLocations_defaultsToEmptyMap() {
        WaitingList waitingList = new WaitingList();

        assertNotNull(waitingList.getJoinLocations());
    }

    /**
     * Handles join Locations_can Store And Read Entries By Device Id.
     */
    @Test
    public void joinLocations_canStoreAndReadEntriesByDeviceId() {
        WaitingList waitingList = new WaitingList();
        WaitlistJoinLocation joinLocation = new WaitlistJoinLocation(53.5232, -113.5263, new Date());

        waitingList.getJoinLocations().put("device-1", joinLocation);

        assertSame(joinLocation, waitingList.getJoinLocations().get("device-1"));
    }
}
