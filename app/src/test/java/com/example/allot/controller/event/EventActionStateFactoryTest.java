package com.example.allot.controller.event;

import static org.junit.Assert.assertEquals;

import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventActionState;
import com.example.allot.model.event.WaitingList;
import org.junit.Before;
import org.junit.Test;
public class EventActionStateFactoryTest {
    private EventActionStateFactory factory;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        factory = new EventActionStateFactory();
    }

    /**
     * Performs create returns manage state for organizer.
     */
    @Test
    public void create_returnsManageStateForOrganizer() {
        Event event = buildEvent();
        event.setOrganizerId("user1");

        EventActionState state = factory.create(event, "user1");

        assertEquals(EventActionState.ActionType.MANAGE, state.getActionType());
    }

    /**
     * Performs create returns enrolled state.
     */
    @Test
    public void create_returnsEnrolledState() {
        Event event = buildEvent();
        event.getEnrolled().add("user1");

        EventActionState state = factory.create(event, "user1");

        assertEquals(EventActionState.ActionType.ENROLLED, state.getActionType());
    }

    /**
     * Performs create returns offer state.
     */
    @Test
    public void create_returnsOfferState() {
        Event event = buildEvent();
        event.getChosen().add("user1");

        EventActionState state = factory.create(event, "user1");

        assertEquals(EventActionState.ActionType.OFFER, state.getActionType());
    }

    /**
     * Performs create returns replacement state.
     */
    @Test
    public void create_returnsReplacementState() {
        Event event = buildEvent();
        event.getWaitingList().list.add("user1");
        event.getWaitingList().chosen.add("other");
        event.setStatus("open");

        EventActionState state = factory.create(event, "user1");

        assertEquals(EventActionState.ActionType.NOT_SELECTED_REPLACEMENT, state.getActionType());
    }

    /**
     * Performs create returns finalized not selected state.
     */
    @Test
    public void create_returnsFinalizedNotSelectedState() {
        Event event = buildEvent();
        event.getWaitingList().list.add("user1");
        event.getWaitingList().chosen.add("other");
        event.setStatus("finalized");

        EventActionState state = factory.create(event, "user1");

        assertEquals(EventActionState.ActionType.NOT_SELECTED_FINAL, state.getActionType());
    }

    /**
     * Performs create returns join and leave waitlist states.
     */
    @Test
    public void create_returnsJoinAndLeaveWaitlistStates() {
        Event event = buildEvent();

        EventActionState joinState = factory.create(event, "user1");
        assertEquals(EventActionState.ActionType.JOIN_WAITLIST, joinState.getActionType());

        event.getWaitingList().list.add("user1");
        EventActionState leaveState = factory.create(event, "user1");
        assertEquals(EventActionState.ActionType.LEAVE_WAITLIST, leaveState.getActionType());
    }

    /**
     * Returns the result of build event.
     *
     * @return the result of this call
     */
    private Event buildEvent() {
        Event event = new Event();
        event.setWaitingList(new WaitingList());
        event.setChosen(new java.util.ArrayList<>());
        event.setEnrolled(new java.util.ArrayList<>());
        event.setCancelled(new java.util.ArrayList<>());
        event.setNotEnrolled(new java.util.ArrayList<>());
        return event;
    }
}









