package com.example.allot.controller.explore;

import com.example.allot.model.BrowseFilter;
import com.example.allot.model.event.Event;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Filters and sorts events before they are shown on the explore screen.
 */
public class ExploreFilterService {
    private static final String OPEN_STATUS = "open";

    /**
     * Builds a list of open events that match the given normalized filters.
     *
     * @param events the loaded events
     * @param filter the active browse filter
     * @return a sorted list of browsable events
     */
    public List<Event> buildBrowsableEventList(List<Event> events, BrowseFilter filter) {
        String normalizedSearchTerm = normalize(filter == null ? null : filter.getSearchTerm());
        String normalizedCategory = normalize(filter == null ? null : filter.getSelectedCategory());
        List<Event> openEvents = new ArrayList<>();

        if (events == null) {
            return openEvents;
        }

        for (Event event : events) {
            if (!isBrowsable(event)) {
                continue;
            }

            if (!matchesCategory(event, normalizedCategory)) {
                continue;
            }

            if (!matchesSearch(event, normalizedSearchTerm)) {
                continue;
            }

            openEvents.add(event);
        }

        sortBrowsableEvents(openEvents);
        return openEvents;
    }

    /**
     * Checks whether an event should be shown in the browsable event list.
     *
     * @param event the event to check
     * @return true if the event is browsable, otherwise false
     */
    private boolean isBrowsable(Event event) {
        if (event == null) {
            return false;
        }

        if (!OPEN_STATUS.equalsIgnoreCase(safeString(event.getStatus()))) {
            return false;
        }

        if (!event.isPublic()) {
            return false;
        }

        return event.getRegistrationDeadline() == null
                || event.getRegistrationDeadline().getTime() > System.currentTimeMillis();
    }

    /**
     * Checks whether an event matches the given normalized category.
     *
     * @param event the event to check
     * @param normalizedCategory the normalized category filter
     * @return true if the event matches the category, otherwise false
     */
    private boolean matchesCategory(Event event, String normalizedCategory) {
        if (normalizedCategory.isEmpty()) {
            return true;
        }

        // Let the chip match the category or other event text
        return normalize(event.getCategory()).equals(normalizedCategory)
                || containsNormalized(event.getTitle(), normalizedCategory)
                || containsNormalized(event.getDescription(), normalizedCategory);
    }

    /**
     * Checks whether an event matches the given normalized search term.
     *
     * @param event the event to check
     * @param normalizedSearchTerm the normalized search term
     * @return true if the event matches the search term, otherwise false
     */
    private boolean matchesSearch(Event event, String normalizedSearchTerm) {
        if (normalizedSearchTerm.isEmpty()) {
            return true;
        }

        return containsNormalized(event.getTitle(), normalizedSearchTerm)
                || containsNormalized(event.getDescription(), normalizedSearchTerm)
                || containsNormalized(event.getLocation(), normalizedSearchTerm)
                || containsNormalized(event.getCategory(), normalizedSearchTerm);
    }

    /**
     * Checks whether a string contains the given normalized search term.
     *
     * @param value the string value to search
     * @param normalizedSearchTerm the normalized search term
     * @return true if the value contains the search term, otherwise false
     */
    private boolean containsNormalized(String value, String normalizedSearchTerm) {
        return normalize(value).contains(normalizedSearchTerm);
    }

    /**
     * Sorts browsable events by registration deadline, event date, and title.
     *
     * @param events the list of events to sort
     */
    private void sortBrowsableEvents(List<Event> events) {
        events.sort(Comparator
                .comparingLong(this::getDeadlineSortValue)
                .thenComparingLong(this::getEventDateSortValue)
                .thenComparing(event -> safeString(event.getTitle()), String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Gets the value used to sort an event by registration deadline.
     *
     * @param event the event to evaluate
     * @return the deadline time in milliseconds, or Long.MAX_VALUE if unavailable
     */
    private long getDeadlineSortValue(Event event) {
        if (event == null || event.getRegistrationDeadline() == null) {
            return Long.MAX_VALUE;
        }

        return event.getRegistrationDeadline().getTime();
    }

    /**
     * Gets the value used to sort an event by event date.
     *
     * @param event the event to evaluate
     * @return the event date time in milliseconds, or Long.MAX_VALUE if unavailable
     */
    private long getEventDateSortValue(Event event) {
        if (event == null || event.getEventDate() == null) {
            return Long.MAX_VALUE;
        }

        return event.getEventDate().getTime();
    }

    /**
     * Normalizes a string by trimming whitespace and converting it to lowercase.
     *
     * @param value the string to normalize
     * @return the normalized string
     */
    private String normalize(String value) {
        return safeString(value).trim().toLowerCase(Locale.getDefault());
    }

    /**
     * Returns a safe string value, replacing null with an empty string.
     *
     * @param value the string to sanitize
     * @return the original string, or an empty string if null
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }
}









