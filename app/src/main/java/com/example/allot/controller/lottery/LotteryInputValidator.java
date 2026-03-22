package com.example.allot.controller.lottery;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * Checks the organizer inputs used to run a lottery draw.
 */
public class LotteryInputValidator {
    private final SimpleDateFormat drawDateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

    /**
     * Creates a validator with strict date parsing.
     */
    public LotteryInputValidator() {
        drawDateFormat.setLenient(false);
    }

    /**
     * Parses the draw date entered by the user.
     *
     * @param value the date text to parse
     * @return the parsed Date, or null if parsing fails
     */
    public Date parseDrawDate(String value) {
        try {
            return drawDateFormat.parse(value);
        } catch (ParseException exception) {
            return null;
        }
    }

    /**
     * Parses the attendee count entered by the organizer.
     *
     * @param value the text value to parse
     * @return the parsed integer, or null if the value is invalid
     */
    public Integer parsePositiveInt(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Validates the lottery form values.
     *
     * @param drawDateValue the draw date text
     * @param attendeesValue the attendee count text
     * @return true if both values are valid, otherwise false
     */
    public boolean isValid(String drawDateValue, String attendeesValue) {
        Date drawDate = parseDrawDate(drawDateValue);
        Integer attendees = parsePositiveInt(attendeesValue);
        return drawDate != null && attendees != null && attendees > 0;
    }
}









