package com.example.allot.model.event;

import java.util.Objects;

/**
 * Captures the visible event form state so changes can be compared later.
 */
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
    private final boolean privateEvent;

    /**
     * Creates a new EventFormSnapshot instance.
     *
     * @param title the title
     * @param location the location
     * @param price the price
     * @param description the description
     * @param participants the participants
     * @param eventDate the event date
     * @param registrationStart the registration start
     * @param registrationEnd the registration end
     * @param geolocationEnabled the geolocation enabled
     * @param privateEvent the private event
     */
    public EventFormSnapshot(String title,
                             String location,
                             String price,
                             String description,
                             String participants,
                             String eventDate,
                             String registrationStart,
                             String registrationEnd,
                             boolean geolocationEnabled,
                             boolean privateEvent) {
        this.title = title;
        this.location = location;
        this.price = price;
        this.description = description;
        this.participants = participants;
        this.eventDate = eventDate;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.geolocationEnabled = geolocationEnabled;
        this.privateEvent = privateEvent;
    }

    /**
     * Compares this event form snapshot with another object.
     *
     * @param other the other
     * @return whether the supplied object matches this instance
     */
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
                && privateEvent == that.privateEvent
                && Objects.equals(title, that.title)
                && Objects.equals(location, that.location)
                && Objects.equals(price, that.price)
                && Objects.equals(description, that.description)
                && Objects.equals(participants, that.participants)
                && Objects.equals(eventDate, that.eventDate)
                && Objects.equals(registrationStart, that.registrationStart)
                && Objects.equals(registrationEnd, that.registrationEnd);
    }

    /**
     * Returns the hash code for this event form snapshot.
     *
     * @return the hash code for this instance
     */
    @Override
    public int hashCode() {
        return Objects.hash(title, location, price, description, participants, eventDate,
                registrationStart, registrationEnd, geolocationEnabled, privateEvent);
    }
}









