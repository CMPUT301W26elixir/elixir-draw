package com.example.allot.controller.explore;

import static org.junit.Assert.assertEquals;

import com.example.allot.model.BrowseFilter;
import com.example.allot.model.event.Event;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
/**
 * Tests the explore filter service.
 */
public class ExploreFilterServiceTest {
    private ExploreFilterService service;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        service = new ExploreFilterService();
    }

    /**
     * Performs build browsable event list filters by search term.
     */
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
        assertEquals("2", result.get(0).getEventId());
    }

    /**
     * Performs build browsable event list filters by category chip.
     */
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
        assertEquals("1", result.get(0).getEventId());
    }

    /**
     * Performs build browsable event list excludes non open events.
     */
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
        assertEquals("2", result.get(0).getEventId());
    }

    /**
     * Performs build browsable event list sorts by deadline date and title.
     */
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
                Arrays.asList(result.get(0).getEventId(), result.get(1).getEventId(), result.get(2).getEventId()));
    }

    /**
     * Returns the result of build event.
     *
     * @param id the id
     * @param title the title
     * @param category the category
     * @param description the description
     * @param eventTime the event time
     * @param deadlineTime the deadline time
     * @param status the status
     * @return the result of this call
     */
    private Event buildEvent(String id, String title, String category, String description, long eventTime, long deadlineTime, String status) {
        Event event = new Event();
        event.setEventId(id);
        event.setTitle(title);
        event.setCategory(category);
        event.setDescription(description);
        event.setLocation("Location");
        event.setEventDate(new Date(eventTime));
        event.setRegistrationDeadline(new Date(deadlineTime));
        event.setStatus(status);
        return event;
    }
}









