package com.example.allot.controller.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.R;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventActionState;
import com.example.allot.model.event.WaitingList;
import org.junit.Before;
import org.junit.Test;
public class EventDetailViewServiceTest {
    private EventDetailViewService service;

    @Before
    public void setUp() {
        service = new EventDetailViewService();
    }

    @Test
    public void buildFooterState_returnsManageFooterForOrganizer() {
        EventActionState state = new EventActionState(
                buildEvent(),
                EventActionState.ActionType.MANAGE,
                false,
                true,
                true,
                null
        );

        EventDetailViewService.FooterState footerState = service.buildFooterState(state);

        assertEquals(R.string.event_detail_manage_event, footerState.getButtonTextRes());
        assertEquals(R.drawable.bg_waitlist_button, footerState.getButtonBackgroundRes());
        assertEquals(R.color.black, footerState.getButtonTextColorRes());
        assertTrue(footerState.isButtonEnabled());
    }

    @Test
    public void buildFooterState_returnsOfferFooterForSelectedUser() {
        EventActionState state = new EventActionState(
                buildEvent(),
                EventActionState.ActionType.OFFER,
                false,
                true,
                true,
                null
        );

        EventDetailViewService.FooterState footerState = service.buildFooterState(state);

        assertEquals(R.string.event_detail_offer, footerState.getButtonTextRes());
        assertEquals(R.drawable.bg_event_detail_offer_green, footerState.getButtonBackgroundRes());
        assertEquals(R.color.white, footerState.getButtonTextColorRes());
        assertTrue(service.hasActiveOffer(state));
    }

    @Test
    public void buildFooterState_returnsReplacementFooterState() {
        EventActionState state = new EventActionState(
                buildEvent(),
                EventActionState.ActionType.NOT_SELECTED_REPLACEMENT,
                false,
                false,
                false,
                "replacement"
        );

        EventDetailViewService.FooterState footerState = service.buildFooterState(state);

        assertEquals(R.string.event_detail_not_selected_main_draw, footerState.getButtonTextRes());
        assertEquals("replacement", footerState.getSubtext());
        assertFalse(footerState.shouldShowEntrantCount());
    }

    @Test
    public void buildFooterState_returnsLeaveWaitlistFooterState() {
        EventActionState state = new EventActionState(
                buildEvent(),
                EventActionState.ActionType.LEAVE_WAITLIST,
                true,
                true,
                true,
                null
        );

        EventDetailViewService.FooterState footerState = service.buildFooterState(state);

        assertEquals(R.string.event_detail_leave_waiting_list, footerState.getButtonTextRes());
        assertEquals(R.drawable.bg_waitlist_button_inactive, footerState.getButtonBackgroundRes());
        assertTrue(service.isOnWaitingList(state));
    }

    @Test
    public void getEntrantCount_returnsWaitingListSize() {
        Event event = buildEvent();
        event.getWaitingList().list.add("user1");
        event.getWaitingList().list.add("user2");

        assertEquals(2, service.getEntrantCount(event));
    }

    @Test
    public void getSelectionCriteriaCount_prefersCapacityThenLimit() {
        Event event = buildEvent();
        event.setCapacity(5);
        event.setLimit(3);

        assertEquals(5, service.getSelectionCriteriaCount(event));

        event.setCapacity(0);
        assertEquals(3, service.getSelectionCriteriaCount(event));
    }

    @Test
    public void buildFooterState_returnsDisabledJoinDefaultsWhenStateMissing() {
        EventDetailViewService.FooterState footerState = service.buildFooterState(null);

        assertFalse(footerState.isButtonEnabled());
        assertEquals(R.string.event_detail_join_waiting_list, footerState.getButtonTextRes());
    }

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









