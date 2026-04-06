package com.example.allot.model.event;

/**
 * Holds raw event form values exactly as they appear in the UI.
 */
public class EventFormData {
    private final String title;
    private final String location;
    private final boolean privateEvent;
    private final boolean geolocationEnabled;
    private final String eventMonth;
    private final String eventDay;
    private final String eventYear;
    private final String price;
    private final String description;
    private final String participants;
    private final String registrationStartMonth;
    private final String registrationStartDay;
    private final String registrationStartYear;
    private final String registrationEndMonth;
    private final String registrationEndDay;
    private final String registrationEndYear;

    /**
     * Creates a new EventFormData instance.
     *
     * @param title the title
     * @param location the location
     * @param privateEvent the private event
     * @param geolocationEnabled the geolocation enabled
     * @param eventMonth the event month
     * @param eventDay the event day
     * @param eventYear the event year
     * @param price the price
     * @param description the description
     * @param participants the participants
     * @param registrationStartMonth the registration start month
     * @param registrationStartDay the registration start day
     * @param registrationStartYear the registration start year
     * @param registrationEndMonth the registration end month
     * @param registrationEndDay the registration end day
     * @param registrationEndYear the registration end year
     */
    public EventFormData(String title,
                         String location,
                         boolean privateEvent,
                         boolean geolocationEnabled,
                         String eventMonth,
                         String eventDay,
                         String eventYear,
                         String price,
                         String description,
                         String participants,
                         String registrationStartMonth,
                         String registrationStartDay,
                         String registrationStartYear,
                         String registrationEndMonth,
                         String registrationEndDay,
                         String registrationEndYear) {
        this.title = title;
        this.location = location;
        this.privateEvent = privateEvent;
        this.geolocationEnabled = geolocationEnabled;
        this.eventMonth = eventMonth;
        this.eventDay = eventDay;
        this.eventYear = eventYear;
        this.price = price;
        this.description = description;
        this.participants = participants;
        this.registrationStartMonth = registrationStartMonth;
        this.registrationStartDay = registrationStartDay;
        this.registrationStartYear = registrationStartYear;
        this.registrationEndMonth = registrationEndMonth;
        this.registrationEndDay = registrationEndDay;
        this.registrationEndYear = registrationEndYear;
    }

    /**
     * Returns the result of for binding.
     *
     * @param title the title
     * @param location the location
     * @param privateEvent the private event
     * @param geolocationEnabled the geolocation enabled
     * @param eventMonth the event month
     * @param eventDay the event day
     * @param eventYear the event year
     * @param price the price
     * @param description the description
     * @param participants the participants
     * @param registrationStartMonth the registration start month
     * @param registrationStartDay the registration start day
     * @param registrationStartYear the registration start year
     * @param registrationEndMonth the registration end month
     * @param registrationEndDay the registration end day
     * @param registrationEndYear the registration end year
     * @return the result of this call
     */
    public static EventFormData forBinding(String title,
                                           String location,
                                           boolean privateEvent,
                                           boolean geolocationEnabled,
                                           String eventMonth,
                                           String eventDay,
                                           String eventYear,
                                           String price,
                                           String description,
                                           String participants,
                                           String registrationStartMonth,
                                           String registrationStartDay,
                                           String registrationStartYear,
                                           String registrationEndMonth,
                                           String registrationEndDay,
                                           String registrationEndYear) {
        return new EventFormData(
                safeValue(title),
                safeValue(location),
                privateEvent,
                geolocationEnabled,
                safeValue(eventMonth),
                safeValue(eventDay),
                safeValue(eventYear),
                safeValue(price),
                safeValue(description),
                safeValue(participants),
                safeValue(registrationStartMonth),
                safeValue(registrationStartDay),
                safeValue(registrationStartYear),
                safeValue(registrationEndMonth),
                safeValue(registrationEndDay),
                safeValue(registrationEndYear)
        );
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
     * Returns whether private event.
     *
     * @return whether private event
     */
    public boolean isPrivateEvent() {
        return privateEvent;
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
     * Returns the event month.
     *
     * @return the event month
     */
    public String getEventMonth() {
        return eventMonth;
    }

    /**
     * Returns the event day.
     *
     * @return the event day
     */
    public String getEventDay() {
        return eventDay;
    }

    /**
     * Returns the event year.
     *
     * @return the event year
     */
    public String getEventYear() {
        return eventYear;
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
    public String getParticipants() {
        return participants;
    }

    /**
     * Returns the registration start month.
     *
     * @return the registration start month
     */
    public String getRegistrationStartMonth() {
        return registrationStartMonth;
    }

    /**
     * Returns the registration start day.
     *
     * @return the registration start day
     */
    public String getRegistrationStartDay() {
        return registrationStartDay;
    }

    /**
     * Returns the registration start year.
     *
     * @return the registration start year
     */
    public String getRegistrationStartYear() {
        return registrationStartYear;
    }

    /**
     * Returns the registration end month.
     *
     * @return the registration end month
     */
    public String getRegistrationEndMonth() {
        return registrationEndMonth;
    }

    /**
     * Returns the registration end day.
     *
     * @return the registration end day
     */
    public String getRegistrationEndDay() {
        return registrationEndDay;
    }

    /**
     * Returns the registration end year.
     *
     * @return the registration end year
     */
    public String getRegistrationEndYear() {
        return registrationEndYear;
    }

    /**
     * Returns the result of safe value.
     *
     * @param value the value
     * @return the result of this call
     */
    private static String safeValue(String value) {
        return value == null ? "" : value;
    }
}









