package com.example.allot.controller.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
public class EventOfferServiceTest {
    private EventOfferService service;

    /**
     * Updates up.
     */
    @Before
    public void setUp() {
        service = new EventOfferService();
    }

    /**
     * Builds declined offer state_removes declining user from chosen and enrolled.
     */
    @Test
    public void buildDeclinedOfferState_removesDecliningUserFromChosenAndEnrolled() {
        Event event = buildOpenEvent();
        event.getWaitingList().list.addAll(Arrays.asList("user1", "user2", "user3"));
        event.getWaitingList().chosen.addAll(Arrays.asList("user1", "user2"));
        event.getWaitingList().status.put("user1", true);
        event.getWaitingList().status.put("user2", false);
        event.getChosen().addAll(Arrays.asList("user1", "user2"));
        event.getEnrolled().add("user1");

        Event result = service.buildDeclinedOfferState(event, "user1");

        assertNotNull(result);
        assertFalse(result.getChosen().contains("user1"));
        assertFalse(result.getEnrolled().contains("user1"));
        assertTrue(result.getCancelled().contains("user1"));
    }

    /**
     * Builds declined offer state_adds replacement only from eligible users.
     */
    @Test
    public void buildDeclinedOfferState_addsReplacementOnlyFromEligibleUsers() {
        Event event = buildOpenEvent();
        event.getWaitingList().list.addAll(Arrays.asList("user1", "user2", "user3"));
        event.getWaitingList().chosen.addAll(Arrays.asList("user1"));
        event.getWaitingList().status.put("user1", false);
        event.getChosen().add("user1");
        event.getCancelled().add("user2");

        Event result = service.buildDeclinedOfferState(event, "user1");

        assertNotNull(result);
        assertEquals(1, result.getWaitingList().chosen.size());
        assertEquals("user3", result.getWaitingList().chosen.get(0));
    }

    /**
     * Builds declined offer state_does not add replacement when no candidates exist.
     */
    @Test
    public void buildDeclinedOfferState_doesNotAddReplacementWhenNoCandidatesExist() {
        Event event = buildOpenEvent();
        event.getWaitingList().list.add("user1");
        event.getWaitingList().chosen.add("user1");
        event.getWaitingList().status.put("user1", false);
        event.getChosen().add("user1");

        Event result = service.buildDeclinedOfferState(event, "user1");

        assertNotNull(result);
        assertTrue(result.getWaitingList().chosen.isEmpty());
    }

    /**
     * Builds open event.
     */
    private Event buildOpenEvent() {
        Event event = new Event();
        event.setStatus("open");
        event.setWaitingList(new WaitingList());
        event.setChosen(new java.util.ArrayList<>());
        event.setEnrolled(new java.util.ArrayList<>());
        event.setCancelled(new java.util.ArrayList<>());
        event.setNotEnrolled(new java.util.ArrayList<>());
        return event;
    }
}









