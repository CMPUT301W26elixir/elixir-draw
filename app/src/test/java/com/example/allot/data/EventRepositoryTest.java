package com.example.allot.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.model.event.WaitlistJoinLocation;
import java.util.Date;
import java.util.Map;
import org.junit.Test;

/**
 * Tests the event repository.
 */
public class EventRepositoryTest {
    /**
     * Performs build join waiting list updates adds waitlist and join location entries.
     */
    @Test
    public void buildJoinWaitingListUpdates_addsWaitlistAndJoinLocationEntries() {
        EventRepository repository = new EventRepository(null);
        Date joinedAt = new Date();

        Map<String, Object> updates = repository.buildJoinWaitingListUpdates("device-1", 53.5232, -113.5263, joinedAt);

        assertTrue(updates.containsKey("waitingList.list"));
        assertTrue(updates.containsKey("waitingList.joinLocations.device-1"));
        WaitlistJoinLocation joinLocation = (WaitlistJoinLocation) updates.get("waitingList.joinLocations.device-1");
        assertNotNull(joinLocation);
        assertEquals(Double.valueOf(53.5232), joinLocation.getLatitude());
        assertEquals(Double.valueOf(-113.5263), joinLocation.getLongitude());
        assertEquals(joinedAt, joinLocation.getJoinedAt());
    }

    /**
     * Performs build leave waiting list updates removes waitlist and join location entries.
     */
    @Test
    public void buildLeaveWaitingListUpdates_removesWaitlistAndJoinLocationEntries() {
        EventRepository repository = new EventRepository(null);

        Map<String, Object> updates = repository.buildLeaveWaitingListUpdates("device-1");

        assertTrue(updates.containsKey("waitingList.list"));
        assertTrue(updates.containsKey("waitingList.joinLocations.device-1"));
        assertNotNull(updates.get("waitingList.list"));
        assertNotNull(updates.get("waitingList.joinLocations.device-1"));
    }

    /**
     * Performs build join waiting list updates skips join location when coordinates missing.
     */
    @Test
    public void buildJoinWaitingListUpdates_skipsJoinLocationWhenCoordinatesMissing() {
        EventRepository repository = new EventRepository(null);

        Map<String, Object> updates = repository.buildJoinWaitingListUpdates("device-1", null, null, new Date());

        assertTrue(updates.containsKey("waitingList.list"));
        assertTrue(!updates.containsKey("waitingList.joinLocations.device-1"));
    }
}
