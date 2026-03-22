package com.example.allot.model.event;

import java.util.Date;

/**
 * Stores parsed event values that are ready to be submitted or saved.
 */
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

    /**
     * Creates a parsed event submission model.
     *
     * @param title the event title
     * @param location the event location
     * @param geolocationEnabled whether geolocation is enabled
     * @param eventDate the parsed event date
     * @param price the parsed price value
     * @param description the event description
     * @param participants the parsed participant limit
     * @param registrationStart the parsed registration start date
     * @param registrationEnd the parsed registration end date
     * @param category the selected event category
     */
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

    /**
     * @return the event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the event location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return true when geolocation is enabled
     */
    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    /**
     * @return the parsed event date
     */
    public Date getEventDate() {
        return eventDate;
    }

    /**
     * @return the parsed price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * @return the event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the participant limit
     */
    public Integer getParticipants() {
        return participants;
    }

    /**
     * @return the parsed registration start date
     */
    public Date getRegistrationStart() {
        return registrationStart;
    }

    /**
     * @return the parsed registration end date
     */
    public Date getRegistrationEnd() {
        return registrationEnd;
    }

    /**
     * @return the selected event category
     */
    public String getCategory() {
        return category;
    }
}
