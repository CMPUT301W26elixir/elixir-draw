package com.example.allot.model.event;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Date;
import org.junit.Test;

public class WaitingListTest {
    @Test
    public void joinLocations_defaultsToEmptyMap() {
        WaitingList waitingList = new WaitingList();

        assertNotNull(waitingList.getJoinLocations());
    }

    @Test
    public void joinLocations_canStoreAndReadEntriesByDeviceId() {
        WaitingList waitingList = new WaitingList();
        WaitlistJoinLocation joinLocation = new WaitlistJoinLocation(53.5232, -113.5263, new Date());

        waitingList.getJoinLocations().put("device-1", joinLocation);

        assertSame(joinLocation, waitingList.getJoinLocations().get("device-1"));
    }
}
