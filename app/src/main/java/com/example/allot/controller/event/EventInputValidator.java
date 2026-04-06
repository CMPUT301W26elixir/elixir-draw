package com.example.allot.controller.event;

import com.example.allot.model.event.EventSubmissionInput;
import java.util.Date;
/**
 * Checks event input before it is saved.
 */
public class EventInputValidator {
    /**
     * Returns whether valid.
     *
     * @param input the input
     * @return whether valid
     */
    public boolean isValid(EventSubmissionInput input) {
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
     * Returns the result of validate event input.
     *
     * @param title the title
     * @param location the location
     * @param price the price
     * @param description the description
     * @param participants the participants
     * @param eventDate the event date
     * @param registrationStart the registration start
     * @param registrationEnd the registration end
     * @return the result of this call
     */
    private boolean validateEventInput(String title,
                                       String location,
                                       Double price,
                                       String description,
                                       Integer participants,
                                       Date eventDate,
                                       Date registrationStart,
                                       Date registrationEnd) {
        return hasText(title)
                && hasText(location)
                && hasText(description)
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
     * Returns whether this instance has text.
     *
     * @param value the value
     * @return whether this instance has text
     */
    private boolean hasText(String value) {
        return !safeString(value).trim().isEmpty();
    }

    /**
     * Returns the result of safe string.
     *
     * @param value the value
     * @return the result of this call
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }
}









