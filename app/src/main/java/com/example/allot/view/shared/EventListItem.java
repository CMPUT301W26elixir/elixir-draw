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
     * Creates a new EventListItem instance.
     *
     * @param title the title
     * @param street the street
     * @param date the date
     * @param price the price
     * @param daysLeft the days left
     */
    public EventListItem(String title, String street, String date, String price, String daysLeft) {
        this(null, title, street, date, price, daysLeft, null, null);
    }

    /**
     * Creates a new EventListItem instance.
     *
     * @param eventId the event id
     * @param title the title
     * @param street the street
     * @param date the date
     * @param price the price
     * @param daysLeft the days left
     * @param category the category
     * @param posterUrl the poster url
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
     * Returns the result of from event.
     *
     * @param event the event
     * @return the result of this call
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

    /**
     * Returns the event id.
     *
     * @return the event id
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the street.
     *
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * Returns the date.
     *
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * Returns the price.
     *
     * @return the price
     */
    public String getPrice() {
        return price;
    }

    /**
     * Returns the days left.
     *
     * @return the days left
     */
    public String getDaysLeft() {
        return daysLeft;
    }

    /**
     * Returns the category.
     *
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Returns the poster url.
     *
     * @return the poster url
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * Returns whether saved.
     *
     * @return whether saved
     */
    public boolean isSaved() {
        return isSaved;
    }
}








