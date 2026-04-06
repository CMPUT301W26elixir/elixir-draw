package com.example.allot.controller.explore;

import com.example.allot.model.BrowseFilter;
import com.example.allot.model.event.Event;
import android.location.Location;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Filters and sorts events before they are shown on the explore screen.
 */
public class ExploreFilterService {
    private static final String OPEN_STATUS = "open";

    /**
     * Returns the result of build browsable event list.
     *
     * @param events the events
     * @param filter the filter
     * @return the result of this call
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

            if (!matchesKeywords(event, filter == null ? null : filter.getKeywords())) {
                continue;
            }

            if (!matchesStartDate(event, filter == null ? null : filter.getStartDate())) {
                continue;
            }

            /**
             * Returns whether get Distance Km.
             */
            if (!matchesDistance(event,
                    filter == null ? null : filter.getLatitude(),
                    filter == null ? null : filter.getLongitude(),
                    filter == null ? null : filter.getDistanceKm())) {
                continue;
            }

            if (!matchesOpenSpots(event, filter == null ? null : filter.getOnlyOpenSpots())) {
                continue;
            }

            if (!matchesMinimumCapacity(event, filter == null ? null : filter.getMinimumCapacity())) {
                continue;
            }

            openEvents.add(event);
        }

        sortBrowsableEvents(openEvents);
        return openEvents;
    }

    /**
     * Returns whether browsable.
     *
     * @param event the event
     * @return whether browsable
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
     * Returns the result of matches category.
     *
     * @param event the event
     * @param normalizedCategory the normalized category
     * @return the result of this call
     */
    private boolean matchesCategory(Event event, String normalizedCategory) {
        if (normalizedCategory.isEmpty()) {
            return true;
        }

        /**
         * Returns whether get Description.
         */
        // Let the chip match the category or other event text
        if (normalize(event.getCategory()).equals(normalizedCategory)
                || containsNormalized(event.getTitle(), normalizedCategory)
                || containsNormalized(event.getDescription(), normalizedCategory)) {
            return true;
        }

        /**
         * Returns whether get Category.
         */
        for (String keyword : categoryKeywords(normalizedCategory)) {
            if (containsNormalized(event.getTitle(), keyword)
                    || containsNormalized(event.getDescription(), keyword)
                    || containsNormalized(event.getCategory(), keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the result of matches search.
     *
     * @param event the event
     * @param normalizedSearchTerm the normalized search term
     * @return the result of this call
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
     * Returns the result of contains normalized.
     *
     * @param value the value
     * @param normalizedSearchTerm the normalized search term
     * @return the result of this call
     */
    private boolean containsNormalized(String value, String normalizedSearchTerm) {
        return normalize(value).contains(normalizedSearchTerm);
    }

    /**
     * Returns the result of matches keywords.
     *
     * @param event the event
     * @param keywords the keywords
     * @return the result of this call
     */
    private boolean matchesKeywords(Event event, String keywords) {
        String normalizedKeywords = normalize(keywords);
        if (normalizedKeywords.isEmpty()) {
            return true;
        }

        List<String> tokens = splitKeywords(normalizedKeywords);
        if (tokens.isEmpty()) {
            return true;
        }

        /**
         * Returns whether get Description.
         */
        for (String token : tokens) {
            if (!containsNormalized(event.getTitle(), token)
                    && !containsNormalized(event.getDescription(), token)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the result of split keywords.
     *
     * @param normalizedKeywords the normalized keywords
     * @return the result of this call
     */
    private List<String> splitKeywords(String normalizedKeywords) {
        List<String> tokens = new ArrayList<>();
        if (normalizedKeywords.isEmpty()) {
            return tokens;
        }

        String[] parts = normalizedKeywords.split("[,\\s]+");
        for (String part : parts) {
            if (!part.trim().isEmpty()) {
                tokens.add(part.trim());
            }
        }
        return tokens;
    }

    /**
     * Returns the result of matches start date.
     *
     * @param event the event
     * @param startDate the start date
     * @return the result of this call
     */
    private boolean matchesStartDate(Event event, java.util.Date startDate) {
        if (startDate == null) {
            return true;
        }

        if (event == null || event.getEventDate() == null) {
            return false;
        }

        return !event.getEventDate().before(startDate);
    }

    /**
     * Returns the result of matches distance.
     *
     * @param event the event
     * @param latitude the latitude
     * @param longitude the longitude
     * @param distanceKm the distance km
     * @return the result of this call
     */
    private boolean matchesDistance(Event event, Double latitude, Double longitude, Double distanceKm) {
        if (latitude == null || longitude == null || distanceKm == null || distanceKm <= 0) {
            return true;
        }

        if (event == null || event.getEventLatitude() == null || event.getEventLongitude() == null) {
            return false;
        }

        float[] results = new float[1];
        Location.distanceBetween(
                latitude,
                longitude,
                event.getEventLatitude(),
                event.getEventLongitude(),
                results
        );
        float distanceMeters = results[0];
        return distanceMeters <= (distanceKm * 1000.0);
    }

    /**
     * Returns the result of matches open spots.
     *
     * @param event the event
     * @param onlyOpenSpots the only open spots
     * @return the result of this call
     */
    private boolean matchesOpenSpots(Event event, Boolean onlyOpenSpots) {
        if (!Boolean.TRUE.equals(onlyOpenSpots)) {
            return true;
        }

        Integer effectiveCapacity = getEffectiveCapacity(event);
        if (effectiveCapacity == null) {
            return true;
        }

        int currentEntrantCount = event != null
                && event.getWaitingList() != null
                && event.getWaitingList().list != null
                ? event.getWaitingList().list.size()
                : 0;
        return currentEntrantCount < effectiveCapacity;
    }

    /**
     * Returns the result of matches minimum capacity.
     *
     * @param event the event
     * @param minimumCapacity the minimum capacity
     * @return the result of this call
     */
    private boolean matchesMinimumCapacity(Event event, Integer minimumCapacity) {
        if (minimumCapacity == null || minimumCapacity <= 0) {
            return true;
        }

        Integer effectiveCapacity = getEffectiveCapacity(event);
        return effectiveCapacity != null && effectiveCapacity >= minimumCapacity;
    }

    /**
     * Returns the effective capacity.
     *
     * @param event the event
     * @return the effective capacity
     */
    private Integer getEffectiveCapacity(Event event) {
        if (event == null) {
            return null;
        }
        if (event.getLimit() > 0) {
            return event.getLimit();
        }
        if (event.getCapacity() > 0) {
            return event.getCapacity();
        }
        return null;
    }

    /**
     * Returns the result of category keywords.
     *
     * @param normalizedCategory the normalized category
     * @return the result of this call
     */
    private List<String> categoryKeywords(String normalizedCategory) {
        if (normalizedCategory.isEmpty()) {
            return new ArrayList<>();
        }

        switch (normalizedCategory) {
            case "fortnite":
                return Arrays.asList("fortnite", "battle royale", "epic");
            case "sports":
                return Arrays.asList("sports", "soccer", "football", "basketball", "tennis",
                        "hockey", "baseball", "golf", "running", "marathon");
            case "arts & crafts":
            case "arts and crafts":
            case "arts":
                return Arrays.asList("art", "arts", "craft", "crafts", "painting", "drawing",
                        "pottery", "sculpture", "illustration", "ceramics");
            case "science":
                return Arrays.asList("science", "stem", "robotics", "chemistry", "physics",
                        "biology", "astronomy", "engineering");
            default:
                return new ArrayList<>();
        }
    }

    /**
     * Performs sort browsable events.
     *
     * @param events the events
     */
    private void sortBrowsableEvents(List<Event> events) {
        events.sort(Comparator
                .comparingLong(this::getDeadlineSortValue)
                .thenComparingLong(this::getEventDateSortValue)
                .thenComparing(event -> safeString(event.getTitle()), String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Returns the deadline sort value.
     *
     * @param event the event
     * @return the deadline sort value
     */
    private long getDeadlineSortValue(Event event) {
        if (event == null || event.getRegistrationDeadline() == null) {
            return Long.MAX_VALUE;
        }

        return event.getRegistrationDeadline().getTime();
    }

    /**
     * Returns the event date sort value.
     *
     * @param event the event
     * @return the event date sort value
     */
    private long getEventDateSortValue(Event event) {
        if (event == null || event.getEventDate() == null) {
            return Long.MAX_VALUE;
        }

        return event.getEventDate().getTime();
    }

    /**
     * Returns the result of normalize.
     *
     * @param value the value
     * @return the result of this call
     */
    private String normalize(String value) {
        return safeString(value).trim().toLowerCase(Locale.getDefault());
    }

    /**
     * Returns the result of safe string.
     *
     * @param value the value
     * @return the result of this call
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }
}









