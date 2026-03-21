package com.example.allot.model;

import java.util.Date;

/**
 * Represents the values entered when updating an existing event.
 */
public class UpdateEventInput {
    private final String title;
    private final String location;
    private final boolean geolocationEnabled;
    private final Date eventDate;
    private final Double price;
    private final String description;
    private final Integer participants;
    private final Date registrationStart;
    private final Date registrationEnd;

    /**
     * Creates an update-event input payload.
     *
     * @param title the event title
     * @param location the event location
     * @param geolocationEnabled whether geolocation is enabled
     * @param eventDate the event date
     * @param price the event price
     * @param description the event description
     * @param participants the participant limit
     * @param registrationStart the registration opening date
     * @param registrationEnd the registration closing date
     */
    public UpdateEventInput(String title,
                            String location,
                            boolean geolocationEnabled,
                            Date eventDate,
                            Double price,
                            String description,
                            Integer participants,
                            Date registrationStart,
                            Date registrationEnd) {
        this.title = title;
        this.location = location;
        this.geolocationEnabled = geolocationEnabled;
        this.eventDate = eventDate;
        this.price = price;
        this.description = description;
        this.participants = participants;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public Double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public Integer getParticipants() {
        return participants;
    }

    public Date getRegistrationStart() {
        return registrationStart;
    }

    public Date getRegistrationEnd() {
        return registrationEnd;
    }
}
