package com.example.allot.view;

import com.example.allot.model.Event;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for formatting event information for display in the UI.
 */
public final class EventDisplayFormatter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private EventDisplayFormatter() {
    }

    /**
     * Returns the trimmed title of the given event.
     *
     * @param event the event whose title should be formatted
     * @return the trimmed event title, or an empty string if unavailable
     */
    public static String title(Event event) {
        return event == null || event.title == null ? "" : event.title.trim();
    }

    /**
     * Returns the formatted location text for the given event.
     *
     * @param event the event whose location should be formatted
     * @return the event location, or "Location TBA" if unavailable
     */
    public static String location(Event event) {
        if (event == null || isBlank(event.location)) {
            return "Location TBA";
        }
        return event.location;
    }

    /**
     * Returns the formatted event date for the given event.
     *
     * @param event the event whose date should be formatted
     * @return the formatted event date, or "Date TBA" if unavailable
     */
    public static String date(Event event) {
        if (event == null || event.eventDate == null) {
            return "Date TBA";
        }
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(event.eventDate);
    }

    /**
     * Returns the formatted price text for the given event.
     *
     * @param event the event whose price should be formatted
     * @return the formatted price, or "Free" if the event has no price
     */
    public static String price(Event event) {
        if (event == null || event.price == null || event.price <= 0) {
            return "Free";
        }
        if (Math.rint(event.price) == event.price) {
            return String.format(Locale.getDefault(), "$%.0f", event.price);
        }
        return String.format(Locale.getDefault(), "$%.2f", event.price);
    }

    /**
     * Returns the formatted registration deadline text for the given event.
     *
     * @param event the event whose registration deadline should be formatted
     * @return the formatted deadline text, or "Deadline TBA" if unavailable
     */
    public static String deadline(Event event) {
        if (event == null || event.registrationDeadline == null) {
            return "Deadline TBA";
        }

        long millisRemaining = event.registrationDeadline.getTime() - System.currentTimeMillis();
        if (millisRemaining <= 0) {
            return "Closed";
        }

        long daysLeft = TimeUnit.MILLISECONDS.toDays(millisRemaining);
        if (daysLeft == 0) {
            return "Ends Today";
        }
        if (daysLeft == 1) {
            return "1 Day Left";
        }
        return daysLeft + " Days Left";
    }

    /**
     * Checks whether a string is null or blank after trimming.
     *
     * @param value the string to check
     * @return true if the string is blank, otherwise false
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}