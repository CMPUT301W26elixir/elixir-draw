package com.example.allot.controller;

import com.example.allot.model.Event;
import com.example.allot.model.WaitingList;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class EventOfferServiceTest {
    private EventOfferService service;

    @Before
    public void setUp() {
        service = new EventOfferService();
    }

    @Test
    public void buildDeclinedOfferState_removesDecliningUserFromChosenAndEnrolled() {
        Event event = buildOpenEvent();
        event.waitingList.list.addAll(Arrays.asList("user1", "user2", "user3"));
        event.waitingList.chosen.addAll(Arrays.asList("user1", "user2"));
        event.waitingList.status.put("user1", true);
        event.waitingList.status.put("user2", false);
        event.chosen.addAll(Arrays.asList("user1", "user2"));
        event.enrolled.add("user1");

        Event result = service.buildDeclinedOfferState(event, "user1");

        assertNotNull(result);
        assertFalse(result.chosen.contains("user1"));
        assertFalse(result.enrolled.contains("user1"));
        assertTrue(result.cancelled.contains("user1"));
    }

    @Test
    public void buildDeclinedOfferState_addsReplacementOnlyFromEligibleUsers() {
        Event event = buildOpenEvent();
        event.waitingList.list.addAll(Arrays.asList("user1", "user2", "user3"));
        event.waitingList.chosen.addAll(Arrays.asList("user1"));
        event.waitingList.status.put("user1", false);
        event.chosen.add("user1");
        event.cancelled.add("user2");

        Event result = service.buildDeclinedOfferState(event, "user1");

        assertNotNull(result);
        assertEquals(1, result.waitingList.chosen.size());
        assertEquals("user3", result.waitingList.chosen.get(0));
    }

    @Test
    public void buildDeclinedOfferState_doesNotAddReplacementWhenNoCandidatesExist() {
        Event event = buildOpenEvent();
        event.waitingList.list.add("user1");
        event.waitingList.chosen.add("user1");
        event.waitingList.status.put("user1", false);
        event.chosen.add("user1");

        Event result = service.buildDeclinedOfferState(event, "user1");

        assertNotNull(result);
        assertTrue(result.waitingList.chosen.isEmpty());
    }

    private Event buildOpenEvent() {
        Event event = new Event();
        event.status = "open";
        event.waitingList = new WaitingList();
        event.chosen = new java.util.ArrayList<>();
        event.enrolled = new java.util.ArrayList<>();
        event.cancelled = new java.util.ArrayList<>();
        event.notEnrolled = new java.util.ArrayList<>();
        return event;
    }
}
