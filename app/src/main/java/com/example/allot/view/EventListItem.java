package com.example.allot.view;

import com.example.allot.model.Event;

/**
 * Represents a formatted event item used for display in event lists.
 * Stores the event fields needed by list adapters and UI cards.
 */
public class EventListItem {
    public String eventId;
    public String title;
    public String street;
    public String date;
    public String price;
    public String daysLeft;
    public String category;
    public String posterUrl;
    public boolean isSaved;

    /**
     * Creates an EventListItem without an event ID, category, or poster URL.
     *
     * @param title the event title
     * @param street the event location text
     * @param date the formatted event date
     * @param price the formatted event price
     * @param daysLeft the formatted registration deadline text
     */
    public EventListItem(String title, String street, String date, String price, String daysLeft) {
        this(null, title, street, date, price, daysLeft, null, null);
    }

    /**
     * Creates a fully populated EventListItem.
     *
     * @param eventId the event ID
     * @param title the event title
     * @param street the event location text
     * @param date the formatted event date
     * @param price the formatted event price
     * @param daysLeft the formatted registration deadline text
     * @param category the event category
     * @param posterUrl the poster image URL for the event
     */
    public EventListItem(String eventId, String title, String street, String date, String price,
                         String daysLeft, String category, String posterUrl) {
        this.eventId = eventId;
        this.title = title;
        this.street = street;
        this.date = date;
        this.price = price;
        this.daysLeft = daysLeft;
        this.category = category;
        this.posterUrl = posterUrl;
    }

    /**
     * Creates an EventListItem from an Event model using display-formatted values.
     *
     * @param event the event model to convert
     * @return a formatted EventListItem for UI display
     */
    public static EventListItem fromEvent(Event event) {
        return new EventListItem(
                event.eventId,
                event.title,
                EventDisplayFormatter.location(event),
                EventDisplayFormatter.date(event),
                EventDisplayFormatter.price(event),
                EventDisplayFormatter.deadline(event),
                event.category,
                event.posterUrl
        );
    }
}