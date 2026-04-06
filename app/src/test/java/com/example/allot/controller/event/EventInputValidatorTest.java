package com.example.allot.controller.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventSubmissionInput;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
public class EventInputValidatorTest {
    private EventInputValidator validator;
    private Date registrationStart;
    private Date registrationEnd;
    private Date eventDate;

    /**
     * Updates up.
     */
    @Before
    public void setUp() {
        validator = new EventInputValidator();
        registrationStart = new Date(1_000L);
        registrationEnd = new Date(2_000L);
        eventDate = new Date(3_000L);
    }

    /**
     * Returns whether i.s Valid Create Event Input_accepts Valid Input
     */
    @Test
    public void isValidCreateEventInput_acceptsValidInput() {
        EventSubmissionInput input = new EventSubmissionInput(
                "Title",
                "Location",
                true,
                eventDate,
                12.5,
                "Description",
                10,
                registrationStart,
                registrationEnd,
                "Sports",
                Event.VISIBILITY_PUBLIC
        );

        assertTrue(validator.isValid(input));
    }

    /**
     * Returns whether i.s Valid Create Event Input_rejects Missing Required Fields
     */
    @Test
    public void isValidCreateEventInput_rejectsMissingRequiredFields() {
        EventSubmissionInput input = new EventSubmissionInput(
                " ",
                "Location",
                true,
                eventDate,
                12.5,
                "Description",
                10,
                registrationStart,
                registrationEnd,
                "Sports",
                Event.VISIBILITY_PUBLIC
        );

        assertFalse(validator.isValid(input));
    }

    /**
     * Returns whether i.s Valid Update Event Input_rejects Negative Price
     */
    @Test
    public void isValidUpdateEventInput_rejectsNegativePrice() {
        EventSubmissionInput input = new EventSubmissionInput(
                "Title",
                "Location",
                true,
                eventDate,
                -1.0,
                "Description",
                10,
                registrationStart,
                registrationEnd,
                null,
                Event.VISIBILITY_PUBLIC
        );

        assertFalse(validator.isValid(input));
    }

    /**
     * Returns whether i.s Valid Update Event Input_rejects Invalid Participant Count
     */
    @Test
    public void isValidUpdateEventInput_rejectsInvalidParticipantCount() {
        EventSubmissionInput input = new EventSubmissionInput(
                "Title",
                "Location",
                true,
                eventDate,
                10.0,
                "Description",
                0,
                registrationStart,
                registrationEnd,
                null,
                Event.VISIBILITY_PUBLIC
        );

        assertFalse(validator.isValid(input));
    }

    /**
     * Returns whether i.s Valid Update Event Input_rejects Invalid Date Ordering
     */
    @Test
    public void isValidUpdateEventInput_rejectsInvalidDateOrdering() {
        EventSubmissionInput input = new EventSubmissionInput(
                "Title",
                "Location",
                true,
                new Date(1_500L),
                10.0,
                "Description",
                10,
                registrationStart,
                registrationEnd,
                null,
                Event.VISIBILITY_PUBLIC
        );

        assertFalse(validator.isValid(input));
    }
}









