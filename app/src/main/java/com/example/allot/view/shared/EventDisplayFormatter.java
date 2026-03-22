package com.example.allot.view.shared;

import com.example.allot.common.TextHelper;
import com.example.allot.model.event.Event;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
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
        return event == null || event.getTitle() == null ? "" : event.getTitle().trim();
    }

    /**
     * Returns the formatted location text for the given event.
     *
     * @param event the event whose location should be formatted
     * @return the event location, or "Location TBA" if unavailable
     */
    public static String location(Event event) {
        if (event == null || TextHelper.isBlank(event.getLocation())) {
            return "Location TBA";
        }
        return event.getLocation();
    }

    /**
     * Returns the formatted event date for the given event.
     *
     * @param event the event whose date should be formatted
     * @return the formatted event date, or "Date TBA" if unavailable
     */
    public static String date(Event event) {
        if (event == null || event.getEventDate() == null) {
            return "Date TBA";
        }
        return shortDate(event.getEventDate());
    }

    /**
     * Returns the formatted price text for the given event.
     *
     * @param event the event whose price should be formatted
     * @return the formatted price, or "Free" if the event has no price
     */
    public static String price(Event event) {
        if (event == null || event.getPrice() == null || event.getPrice() <= 0) {
            return "Free";
        }
        if (Math.rint(event.getPrice()) == event.getPrice()) {
            return String.format(Locale.getDefault(), "$%.0f", event.getPrice());
        }
        return String.format(Locale.getDefault(), "$%.2f", event.getPrice());
    }

    /**
     * Returns the formatted registration deadline text for the given event.
     *
     * @param event the event whose registration deadline should be formatted
     * @return the formatted deadline text, or "Deadline TBA" if unavailable
     */
    public static String deadline(Event event) {
        if (event == null || event.getRegistrationDeadline() == null) {
            return "Deadline TBA";
        }

        long millisRemaining = event.getRegistrationDeadline().getTime() - System.currentTimeMillis();
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

    public static String detailLocation(String location) {
        return TextHelper.defaultText(location, "Address TBA");
    }

    public static String detailDate(String value) {
        return TextHelper.defaultText(value, "Date TBA");
    }

    public static String shortDate(Date date) {
        return formatDate(date, "MMM d, yyyy");
    }

    public static String longDate(Date date) {
        return formatDate(date, "MMMM d, yyyy");
    }

    public static String labeledShortDate(String label, Date date) {
        String value = date == null ? "TBA" : shortDate(date);
        return String.format(Locale.getDefault(), "%s: %s", label, value);
    }

    private static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }
}









