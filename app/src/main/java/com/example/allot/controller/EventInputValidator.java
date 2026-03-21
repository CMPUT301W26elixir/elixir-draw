package com.example.allot.controller;

import com.example.allot.model.CreateEventInput;
import com.example.allot.model.UpdateEventInput;

import java.util.Date;

/**
 * Validates the common input used when creating or updating events.
 */
public class EventInputValidator {
    /**
     * Validates create-event input.
     *
     * @param input the input to validate
     * @return true if the input is valid, otherwise false
     */
    public boolean isValid(CreateEventInput input) {
        if (input == null) {
            return false;
        }

        return validateEventInput(
                input.getTitle(),
                input.getLocation(),
                input.getPrice(),
                input.getDescription(),
                input.getParticipants(),
                input.getEventDate(),
                input.getRegistrationStart(),
                input.getRegistrationEnd()
        );
    }

    /**
     * Validates update-event input.
     *
     * @param input the input to validate
     * @return true if the input is valid, otherwise false
     */
    public boolean isValid(UpdateEventInput input) {
        if (input == null) {
            return false;
        }

        return validateEventInput(
                input.getTitle(),
                input.getLocation(),
                input.getPrice(),
                input.getDescription(),
                input.getParticipants(),
                input.getEventDate(),
                input.getRegistrationStart(),
                input.getRegistrationEnd()
        );
    }

    /**
     * Validates the common input used when creating or updating events.
     *
     * @param title the event title
     * @param location the event location
     * @param price the event price
     * @param description the event description
     * @param participants the participant count
     * @param eventDate the event date
     * @param registrationStart the registration opening date
     * @param registrationEnd the registration closing date
     * @return true if the input is valid, otherwise false
     */
    private boolean validateEventInput(String title,
                                       String location,
                                       Double price,
                                       String description,
                                       Integer participants,
                                       Date eventDate,
                                       Date registrationStart,
                                       Date registrationEnd) {
        return !isBlank(title)
                && !isBlank(location)
                && !isBlank(description)
                && price != null
                && price >= 0
                && participants != null
                && participants > 0
                && eventDate != null
                && registrationStart != null
                && registrationEnd != null
                && !registrationEnd.before(registrationStart)
                && !eventDate.before(registrationEnd);
    }

    /**
     * Checks whether a string is blank after trimming whitespace.
     *
     * @param value the string to check
     * @return true if the string is blank, otherwise false
     */
    private boolean isBlank(String value) {
        return safeString(value).trim().isEmpty();
    }

    /**
     * Returns a safe string value, replacing null with an empty string.
     *
     * @param value the string to sanitize
     * @return the original string, or an empty string if null
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
