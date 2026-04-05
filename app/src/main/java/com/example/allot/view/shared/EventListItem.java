package com.example.allot.view.shared;

import com.example.allot.model.event.Event;
/**
 * Holds the values shown in one event list row.
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
                event.getEventId(),
                event.getTitle(),
                EventDisplayFormatter.location(event),
                EventDisplayFormatter.date(event),
                EventDisplayFormatter.price(event),
                EventDisplayFormatter.deadline(event),
                event.getCategory(),
                event.getPosterUrl()
        );
    }

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getStreet() {
        return street;
    }

    public String getDate() {
        return date;
    }

    public String getPrice() {
        return price;
    }

    public String getDaysLeft() {
        return daysLeft;
    }

    public String getCategory() {
        return category;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public boolean isSaved() {
        return isSaved;
    }
}








