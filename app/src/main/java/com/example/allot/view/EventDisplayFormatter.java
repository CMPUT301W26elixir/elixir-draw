package com.example.allot.view;

import com.example.allot.model.Event;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public final class EventDisplayFormatter {
    private EventDisplayFormatter() {
    }

    public static String title(Event event) {
        return event == null || event.title == null ? "" : event.title.trim();
    }

    public static String location(Event event) {
        if (event == null || isBlank(event.location)) {
            return "Location TBA";
        }
        return event.location;
    }

    public static String date(Event event) {
        if (event == null || event.eventDate == null) {
            return "Date TBA";
        }
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(event.eventDate);
    }

    public static String price(Event event) {
        if (event == null || event.price == null || event.price <= 0) {
            return "Free";
        }
        if (Math.rint(event.price) == event.price) {
            return String.format(Locale.getDefault(), "$%.0f", event.price);
        }
        return String.format(Locale.getDefault(), "$%.2f", event.price);
    }

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

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
