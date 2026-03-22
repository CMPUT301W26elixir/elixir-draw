package com.example.allot.model.event;

import java.util.Date;
public class EventSubmissionInput {
    private final String title;
    private final String location;
    private final boolean geolocationEnabled;
    private final Date eventDate;
    private final Double price;
    private final String description;
    private final Integer participants;
    private final Date registrationStart;
    private final Date registrationEnd;
    private final String category;

    public EventSubmissionInput(String title,
                                String location,
                                boolean geolocationEnabled,
                                Date eventDate,
                                Double price,
                                String description,
                                Integer participants,
                                Date registrationStart,
                                Date registrationEnd,
                                String category) {
        this.title = title;
        this.location = location;
        this.geolocationEnabled = geolocationEnabled;
        this.eventDate = eventDate;
        this.price = price;
        this.description = description;
        this.participants = participants;
        this.registrationStart = registrationStart;
        this.registrationEnd = registrationEnd;
        this.category = category;
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

    public String getCategory() {
        return category;
    }
}
