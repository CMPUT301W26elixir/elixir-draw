package com.example.allot.controller;

import com.example.allot.model.Event;
import com.example.allot.model.EventDetailState;
import com.example.allot.model.WaitingList;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EventDetailStateFactoryTest {
    private EventDetailStateFactory factory;

    @Before
    public void setUp() {
        factory = new EventDetailStateFactory();
    }

    @Test
    public void create_returnsManageStateForOrganizer() {
        Event event = buildEvent();
        event.organizerId = "user1";

        EventDetailState state = factory.create(event, "user1");

        assertEquals(EventDetailState.ActionType.MANAGE, state.getActionType());
    }

    @Test
    public void create_returnsEnrolledState() {
        Event event = buildEvent();
        event.enrolled.add("user1");

        EventDetailState state = factory.create(event, "user1");

        assertEquals(EventDetailState.ActionType.ENROLLED, state.getActionType());
    }

    @Test
    public void create_returnsOfferState() {
        Event event = buildEvent();
        event.chosen.add("user1");

        EventDetailState state = factory.create(event, "user1");

        assertEquals(EventDetailState.ActionType.OFFER, state.getActionType());
    }

    @Test
    public void create_returnsReplacementState() {
        Event event = buildEvent();
        event.waitingList.list.add("user1");
        event.waitingList.chosen.add("other");
        event.status = "open";

        EventDetailState state = factory.create(event, "user1");

        assertEquals(EventDetailState.ActionType.NOT_SELECTED_REPLACEMENT, state.getActionType());
    }

    @Test
    public void create_returnsFinalizedNotSelectedState() {
        Event event = buildEvent();
        event.waitingList.list.add("user1");
        event.waitingList.chosen.add("other");
        event.status = "finalized";

        EventDetailState state = factory.create(event, "user1");

        assertEquals(EventDetailState.ActionType.NOT_SELECTED_FINAL, state.getActionType());
    }

    @Test
    public void create_returnsJoinAndLeaveWaitlistStates() {
        Event event = buildEvent();

        EventDetailState joinState = factory.create(event, "user1");
        assertEquals(EventDetailState.ActionType.JOIN_WAITLIST, joinState.getActionType());

        event.waitingList.list.add("user1");
        EventDetailState leaveState = factory.create(event, "user1");
        assertEquals(EventDetailState.ActionType.LEAVE_WAITLIST, leaveState.getActionType());
    }

    private Event buildEvent() {
        Event event = new Event();
        event.waitingList = new WaitingList();
        event.chosen = new java.util.ArrayList<>();
        event.enrolled = new java.util.ArrayList<>();
        event.cancelled = new java.util.ArrayList<>();
        event.notEnrolled = new java.util.ArrayList<>();
        return event;
    }
}
