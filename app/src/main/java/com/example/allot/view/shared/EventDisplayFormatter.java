package com.example.allot.view.shared;

import com.example.allot.common.TextHelper;
import com.example.allot.model.event.Event;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
/**
 * Formats event model values into short strings for the UI.
 */
public final class EventDisplayFormatter {

    /**
     * Creates a new EventDisplayFormatter instance.
     */
    private EventDisplayFormatter() {
    }

    /**
     * Returns the result of title.
     *
     * @param event the event
     * @return the result of this call
     */
    public static String title(Event event) {
        return event == null || event.getTitle() == null ? "" : event.getTitle().trim();
    }

    /**
     * Returns the result of location.
     *
     * @param event the event
     * @return the result of this call
     */
    public static String location(Event event) {
        if (event == null || TextHelper.isBlank(event.getLocation())) {
            return "Location TBA";
        }
        return event.getLocation();
    }

    /**
     * Returns the result of date.
     *
     * @param event the event
     * @return the result of this call
     */
    public static String date(Event event) {
        if (event == null || event.getEventDate() == null) {
            return "Date TBA";
        }
        return shortDate(event.getEventDate());
    }

    /**
     * Returns the result of price.
     *
     * @param event the event
     * @return the result of this call
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
     * Returns the result of deadline.
     *
     * @param event the event
     * @return the result of this call
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

    /**
     * Returns the result of detail location.
     *
     * @param location the location
     * @return the result of this call
     */
    public static String detailLocation(String location) {
        return TextHelper.defaultText(location, "Address TBA");
    }

    /**
     * Returns the result of detail date.
     *
     * @param value the value
     * @return the result of this call
     */
    public static String detailDate(String value) {
        return TextHelper.defaultText(value, "Date TBA");
    }

    /**
     * Returns the result of short date.
     *
     * @param date the date
     * @return the result of this call
     */
    public static String shortDate(Date date) {
        return formatDate(date, "MMM d, yyyy");
    }

    /**
     * Returns the result of long date.
     *
     * @param date the date
     * @return the result of this call
     */
    public static String longDate(Date date) {
        return formatDate(date, "MMMM d, yyyy");
    }

    /**
     * Returns the result of labeled short date.
     *
     * @param label the label
     * @param date the date
     * @return the result of this call
     */
    public static String labeledShortDate(String label, Date date) {
        String value = date == null ? "TBA" : shortDate(date);
        return String.format(Locale.getDefault(), "%s: %s", label, value);
    }

    /**
     * Returns the result of format date.
     *
     * @param date the date
     * @param pattern the pattern
     * @return the result of this call
     */
    private static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }
}









