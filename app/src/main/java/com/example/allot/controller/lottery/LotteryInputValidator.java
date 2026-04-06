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
     * Creates a new LotteryInputValidator instance.
     */
    public LotteryInputValidator() {
        drawDateFormat.setLenient(false);
    }

    /**
     * Returns the result of parse draw date.
     *
     * @param value the value
     * @return the result of this call
     */
    public Date parseDrawDate(String value) {
        try {
            return drawDateFormat.parse(value);
        } catch (ParseException exception) {
            return null;
        }
    }

    /**
     * Returns the result of parse positive int.
     *
     * @param value the value
     * @return the result of this call
     */
    public Integer parsePositiveInt(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Returns whether valid.
     *
     * @param drawDateValue the draw date value
     * @param attendeesValue the attendees value
     * @return whether valid
     */
    public boolean isValid(String drawDateValue, String attendeesValue) {
        Date drawDate = parseDrawDate(drawDateValue);
        Integer attendees = parsePositiveInt(attendeesValue);
        return drawDate != null && attendees != null && attendees > 0;
    }
}









