package com.example.allot.controller;

import com.example.allot.model.BrowseFilter;
import com.example.allot.model.Event;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EventBrowseServiceTest {
    private EventBrowseService service;

    @Before
    public void setUp() {
        service = new EventBrowseService();
    }

    @Test
    public void buildBrowsableEventList_filtersBySearchTerm() {
        long now = System.currentTimeMillis();
        Event sports = buildEvent("1", "Sports Day", "Sports", "Big game", now + 5_000L, now + 9_000L, "open");
        Event arts = buildEvent("2", "Gallery Night", "Arts", "Paintings", now + 6_000L, now + 10_000L, "open");

        List<Event> result = service.buildBrowsableEventList(
                Arrays.asList(sports, arts),
                new BrowseFilter("gallery", "")
        );

        assertEquals(1, result.size());
        assertEquals("2", result.get(0).eventId);
    }

    @Test
    public void buildBrowsableEventList_filtersByCategoryChip() {
        long now = System.currentTimeMillis();
        Event sports = buildEvent("1", "Sports Day", "Sports", "Big game", now + 5_000L, now + 9_000L, "open");
        Event arts = buildEvent("2", "Gallery Night", "Arts", "Paintings", now + 6_000L, now + 10_000L, "open");

        List<Event> result = service.buildBrowsableEventList(
                Arrays.asList(sports, arts),
                new BrowseFilter("", "sports")
        );

        assertEquals(1, result.size());
        assertEquals("1", result.get(0).eventId);
    }

    @Test
    public void buildBrowsableEventList_excludesNonOpenEvents() {
        long now = System.currentTimeMillis();
        Event closed = buildEvent("1", "Closed Event", "Sports", "Closed", now + 5_000L, now + 9_000L, "closed");
        Event open = buildEvent("2", "Open Event", "Sports", "Open", now + 6_000L, now + 10_000L, "open");

        List<Event> result = service.buildBrowsableEventList(
                Arrays.asList(closed, open),
                new BrowseFilter("", "")
        );

        assertEquals(1, result.size());
        assertEquals("2", result.get(0).eventId);
    }

    @Test
    public void buildBrowsableEventList_sortsByDeadlineDateAndTitle() {
        long now = System.currentTimeMillis();
        Event laterDeadline = buildEvent("1", "Zoo Trip", "Trips", "Trip", now + 7_000L, now + 20_000L, "open");
        Event earlierDeadline = buildEvent("2", "Art Fair", "Arts", "Fair", now + 6_000L, now + 19_000L, "open");
        Event sameDeadlineEarlierDate = buildEvent("3", "Book Club", "Arts", "Books", now + 5_000L, now + 19_000L, "open");

        List<Event> result = service.buildBrowsableEventList(
                Arrays.asList(laterDeadline, earlierDeadline, sameDeadlineEarlierDate),
                new BrowseFilter("", "")
        );

        assertEquals(Arrays.asList("3", "2", "1"),
                Arrays.asList(result.get(0).eventId, result.get(1).eventId, result.get(2).eventId));
    }

    private Event buildEvent(String id, String title, String category, String description, long eventTime, long deadlineTime, String status) {
        Event event = new Event();
        event.eventId = id;
        event.title = title;
        event.category = category;
        event.description = description;
        event.location = "Location";
        event.eventDate = new Date(eventTime);
        event.registrationDeadline = new Date(deadlineTime);
        event.status = status;
        return event;
    }
}
