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
    private final String visibility;

    /**
     * Creates a new EventSubmissionInput instance.
     *
     * @param title the title
     * @param location the location
     * @param geolocationEnabled the geolocation enabled
     * @param eventDate the event date
     * @param price the price
     * @param description the description
     * @param participants the participants
     * @param registrationStart the registration start
     * @param registrationEnd the registration end
     * @param category the category
     * @param visibility the visibility
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
                                String category,
                                String visibility) {
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
        this.visibility = visibility;
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
     * Returns the location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns whether geolocation enabled.
     *
     * @return whether geolocation enabled
     */
    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    /**
     * Returns the event date.
     *
     * @return the event date
     */
    public Date getEventDate() {
        return eventDate;
    }

    /**
     * Returns the price.
     *
     * @return the price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Returns the description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the participants.
     *
     * @return the participants
     */
    public Integer getParticipants() {
        return participants;
    }

    /**
     * Returns the registration start.
     *
     * @return the registration start
     */
    public Date getRegistrationStart() {
        return registrationStart;
    }

    /**
     * Returns the registration end.
     *
     * @return the registration end
     */
    public Date getRegistrationEnd() {
        return registrationEnd;
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
     * Returns the visibility.
     *
     * @return the visibility
     */
    public String getVisibility() {
        return visibility;
    }
}
