package com.example.allot.controller.events;

import static org.junit.Assert.assertEquals;

import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
import java.util.Arrays;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
public class UserEventsSectionServiceTest {
    private UserEventsSectionService service;

    @Before
    public void setUp() {
        service = new UserEventsSectionService();
    }

    @Test
    public void classifyRegisteredEvent_returnsSelectedWhenUserWasChosen() {
        Event event = buildEvent(System.currentTimeMillis() + 20_000L, System.currentTimeMillis() + 10_000L);
        event.getChosen().add("user1");

        assertEquals(UserEventsSectionService.RegisteredSection.SELECTED,
                service.classifyRegisteredEvent(event, "user1"));
    }

    @Test
    public void classifyRegisteredEvent_returnsWaitingBeforeDeadline() {
        Event event = buildEvent(System.currentTimeMillis() + 20_000L, System.currentTimeMillis() + 10_000L);

        assertEquals(UserEventsSectionService.RegisteredSection.WAITING,
                service.classifyRegisteredEvent(event, "user1"));
    }

    @Test
    public void classifyRegisteredEvent_returnsWaitingAfterDeadlineWithoutPublishedResults() {
        Event event = buildEvent(System.currentTimeMillis() + 20_000L, System.currentTimeMillis() - 10_000L);

        assertEquals(UserEventsSectionService.RegisteredSection.WAITING,
                service.classifyRegisteredEvent(event, "user1"));
    }

    @Test
    public void classifyRegisteredEvent_returnsNotSelectedAfterDeadlineWithPublishedResults() {
        Event event = buildEvent(System.currentTimeMillis() + 20_000L, System.currentTimeMillis() - 10_000L);
        event.getNotEnrolled().add("otherUser");

        assertEquals(UserEventsSectionService.RegisteredSection.NOT_SELECTED,
                service.classifyRegisteredEvent(event, "user1"));
    }

    @Test
    public void classifyRegisteredEvent_returnsPastWhenEventAlreadyOccurred() {
        Event event = buildEvent(System.currentTimeMillis() - 10_000L, System.currentTimeMillis() - 20_000L);

        assertEquals(UserEventsSectionService.RegisteredSection.PAST,
                service.classifyRegisteredEvent(event, "user1"));
    }

    @Test
    public void groupRegisteredEvents_splitsEventsIntoExpectedSections() {
        Event selected = buildEvent(System.currentTimeMillis() + 30_000L, System.currentTimeMillis() + 20_000L);
        selected.getEnrolled().add("user1");

        Event waiting = buildEvent(System.currentTimeMillis() + 40_000L, System.currentTimeMillis() + 30_000L);

        Event notSelected = buildEvent(System.currentTimeMillis() + 50_000L, System.currentTimeMillis() - 30_000L);
        notSelected.getChosen().add("otherUser");

        Event past = buildEvent(System.currentTimeMillis() - 30_000L, System.currentTimeMillis() - 40_000L);

        UserEventsSectionService.RegisteredSections sections = service.groupRegisteredEvents(
                Arrays.asList(selected, waiting, notSelected, past),
                "user1"
        );

        assertEquals(1, sections.getSelectedEvents().size());
        assertEquals(1, sections.getWaitingEvents().size());
        assertEquals(1, sections.getNotSelectedEvents().size());
        assertEquals(1, sections.getPastEvents().size());
    }

    @Test
    public void groupHostedEvents_splitsEventsIntoOngoingAndCompleted() {
        Event ongoing = buildEvent(System.currentTimeMillis() + 30_000L, System.currentTimeMillis() + 20_000L);
        Event completed = buildEvent(System.currentTimeMillis() - 30_000L, System.currentTimeMillis() - 40_000L);

        UserEventsSectionService.HostedSections sections = service.groupHostedEvents(Arrays.asList(ongoing, completed));

        assertEquals(1, sections.getOngoingEvents().size());
        assertEquals(1, sections.getCompletedEvents().size());
    }

    private Event buildEvent(long eventTime, long deadlineTime) {
        Event event = new Event();
        event.setEventDate(new Date(eventTime));
        event.setRegistrationDeadline(new Date(deadlineTime));
        event.setWaitingList(new WaitingList());
        event.setChosen(new java.util.ArrayList<>());
        event.setEnrolled(new java.util.ArrayList<>());
        event.setCancelled(new java.util.ArrayList<>());
        event.setNotEnrolled(new java.util.ArrayList<>());
        return event;
    }
}









