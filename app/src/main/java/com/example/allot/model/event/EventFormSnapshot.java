package com.example.allot.model.event;

import java.util.Objects;
public class EventFormSnapshot {
    private final String title;
    private final String location;
    private final String price;
    private final String description;
    private final String participants;
    private final String eventDate;
    private final String registrationStart;
    private final String registrationEnd;
    private final boolean geolocationEnabled;

    public EventFormSnapshot(String title,
                             String location,
                             String price,
                             String description,
                             String participants,
                             String eventDate,
                             String registrationStart,
                             String registrationEnd,
                             boolean geolocationEnabled) {
        this.title = title;
        this.location = location;
        this.price = price;
        this.description = description;
        this.participants = participants;
        this.eventDate = eventDate;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.geolocationEnabled = geolocationEnabled;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof EventFormSnapshot)) {
            return false;
        }
        EventFormSnapshot that = (EventFormSnapshot) other;
        return geolocationEnabled == that.geolocationEnabled
                && Objects.equals(title, that.title)
                && Objects.equals(location, that.location)
                && Objects.equals(price, that.price)
                && Objects.equals(description, that.description)
                && Objects.equals(participants, that.participants)
                && Objects.equals(eventDate, that.eventDate)
                && Objects.equals(registrationStart, that.registrationStart)
                && Objects.equals(registrationEnd, that.registrationEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, location, price, description, participants, eventDate,
                registrationStart, registrationEnd, geolocationEnabled);
    }
}









