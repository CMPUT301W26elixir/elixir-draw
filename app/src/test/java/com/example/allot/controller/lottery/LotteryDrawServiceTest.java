package com.example.allot.controller.lottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
public class LotteryDrawServiceTest {
    private LotteryDrawService service;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        service = new LotteryDrawService();
    }

    /**
     * Performs has draw results detects existing chosen entrants.
     */
    @Test
    public void hasDrawResults_detectsExistingChosenEntrants() {
        Event event = buildEventWithEntrants("user1");
        event.getChosen().add("user1");

        assertTrue(service.hasDrawResults(event));
    }

    /**
     * Performs build draw result builds chosen lists and resets processed state.
     */
    @Test
    public void buildDrawResult_buildsChosenListsAndResetsProcessedState() {
        Event event = buildEventWithEntrants("user1", "user2", "user3");
        event.getEnrolled().add("oldUser");
        event.getCancelled().add("oldCancelled");
        event.getNotEnrolled().add("oldNotEnrolled");

        Event result = service.buildDrawResult(event, 2);

        assertNotNull(result);
        assertEquals(2, result.getCapacity());
        assertEquals(2, result.getLimit());
        assertEquals(2, result.getWaitingList().limit);
        assertNotNull(result.getChosen());
        assertEquals(result.getChosen(), result.getWaitingList().chosen);
        assertTrue(result.getEnrolled().isEmpty());
        assertTrue(result.getCancelled().isEmpty());
        assertTrue(result.getNotEnrolled().isEmpty());
        assertEquals(result.getChosen().size(), result.getWaitingList().status.size());
    }

    /**
     * Performs build draw result returns null when no entrants exist.
     */
    @Test
    public void buildDrawResult_returnsNullWhenNoEntrantsExist() {
        Event event = buildEventWithEntrants();

        Event result = service.buildDrawResult(event, 2);

        assertFalse(service.hasDrawResults(event));
        assertEquals(0, event.getWaitingList().list.size());
        assertTrue(result == null);
    }

    /**
     * Returns the result of build event with entrants.
     *
     * @param entrants the entrants
     * @return the result of this call
     */
    private Event buildEventWithEntrants(String... entrants) {
        Event event = new Event();
        event.setWaitingList(new WaitingList());
        event.getWaitingList().list.addAll(Arrays.asList(entrants));
        event.setChosen(new java.util.ArrayList<>());
        event.setEnrolled(new java.util.ArrayList<>());
        event.setCancelled(new java.util.ArrayList<>());
        event.setNotEnrolled(new java.util.ArrayList<>());
        return event;
    }
}









