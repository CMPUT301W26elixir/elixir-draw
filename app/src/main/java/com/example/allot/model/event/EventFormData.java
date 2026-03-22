package com.example.allot.model.event;
public class EventFormData {
    private final String title;
    private final String location;
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

    public EventFormData(String title,
                         String location,
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

    public static EventFormData forBinding(String title,
                                           String location,
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

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public boolean isGeolocationEnabled() {
        return geolocationEnabled;
    }

    public String getEventMonth() {
        return eventMonth;
    }

    public String getEventDay() {
        return eventDay;
    }

    public String getEventYear() {
        return eventYear;
    }

    public String getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public String getParticipants() {
        return participants;
    }

    public String getRegistrationStartMonth() {
        return registrationStartMonth;
    }

    public String getRegistrationStartDay() {
        return registrationStartDay;
    }

    public String getRegistrationStartYear() {
        return registrationStartYear;
    }

    public String getRegistrationEndMonth() {
        return registrationEndMonth;
    }

    public String getRegistrationEndDay() {
        return registrationEndDay;
    }

    public String getRegistrationEndYear() {
        return registrationEndYear;
    }

    private static String safeValue(String value) {
        return value == null ? "" : value;
    }
}









