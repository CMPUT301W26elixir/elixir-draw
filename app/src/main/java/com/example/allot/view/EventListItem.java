package com.example.allot.view;

import com.example.allot.model.Event;

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

    public EventListItem(String title, String street, String date, String price, String daysLeft) {
        this(null, title, street, date, price, daysLeft, null, null);
    }

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
