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
     * Creates a snapshot of the current event form field values.
     *
     * @param title the event title field
     * @param location the event location field
     * @param privateEvent whether the event is private
     * @param geolocationEnabled whether geolocation is enabled
     * @param eventMonth the selected event month
     * @param eventDay the entered event day
     * @param eventYear the entered event year
     * @param price the entered event price
     * @param description the event description field
     * @param participants the participant limit field
     * @param registrationStartMonth the selected registration start month
     * @param registrationStartDay the entered registration start day
     * @param registrationStartYear the entered registration start year
     * @param registrationEndMonth the selected registration end month
     * @param registrationEndDay the entered registration end day
     * @param registrationEndYear the entered registration end year
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
     * Creates a form snapshot that is safe to bind back into the UI.
     *
     * @param title the event title field
     * @param location the event location field
     * @param privateEvent whether the event is private
     * @param geolocationEnabled whether geolocation is enabled
     * @param eventMonth the selected event month
     * @param eventDay the entered event day
     * @param eventYear the entered event year
     * @param price the entered event price
     * @param description the event description field
     * @param participants the participant limit field
     * @param registrationStartMonth the selected registration start month
     * @param registrationStartDay the entered registration start day
     * @param registrationStartYear the entered registration start year
     * @param registrationEndMonth the selected registration end month
     * @param registrationEndDay the entered registration end day
     * @param registrationEndYear the entered registration end year
     * @return a form data object with null values replaced by empty strings
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
     * @return the entered event title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the entered event location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @return true when the event is marked private
     */
    public boolean isPrivateEvent() {
        return privateEvent;
    }

    /**
     * @return true when geolocation is enabled
     */
    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    /**
     * @return the selected event month
     */
    public String getEventMonth() {
        return eventMonth;
    }

    /**
     * @return the entered event day
     */
    public String getEventDay() {
        return eventDay;
    }

    /**
     * @return the entered event year
     */
    public String getEventYear() {
        return eventYear;
    }

    /**
     * @return the entered event price
     */
    public String getPrice() {
        return price;
    }

    /**
     * @return the entered event description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the entered participant limit
     */
    public String getParticipants() {
        return participants;
    }

    /**
     * @return the selected registration start month
     */
    public String getRegistrationStartMonth() {
        return registrationStartMonth;
    }

    /**
     * @return the entered registration start day
     */
    public String getRegistrationStartDay() {
        return registrationStartDay;
    }

    /**
     * @return the entered registration start year
     */
    public String getRegistrationStartYear() {
        return registrationStartYear;
    }

    /**
     * @return the selected registration end month
     */
    public String getRegistrationEndMonth() {
        return registrationEndMonth;
    }

    /**
     * @return the entered registration end day
     */
    public String getRegistrationEndDay() {
        return registrationEndDay;
    }

    /**
     * @return the entered registration end year
     */
    public String getRegistrationEndYear() {
        return registrationEndYear;
    }

    /**
     * Replaces null form values so the UI never binds null text.
     *
     * @param value the raw field value
     * @return the original value, or an empty string when it is null
     */
    private static String safeValue(String value) {
        return value == null ? "" : value;
    }
}









