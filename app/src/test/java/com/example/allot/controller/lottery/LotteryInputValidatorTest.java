package com.example.allot.controller.lottery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
/**
 * Tests the lottery input validator.
 */
public class LotteryInputValidatorTest {
    private LotteryInputValidator validator;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        validator = new LotteryInputValidator();
    }

    /**
     * Performs is valid returns true for valid date and attendee count.
     */
    @Test
    public void isValid_returnsTrueForValidDateAndAttendeeCount() {
        assertTrue(validator.isValid("March 21, 2026", "25"));
    }

    /**
     * Performs parse draw date returns null for invalid date.
     */
    @Test
    public void parseDrawDate_returnsNullForInvalidDate() {
        assertNull(validator.parseDrawDate("not a date"));
    }

    /**
     * Performs parse positive int returns null for non positive or invalid input.
     */
    @Test
    public void parsePositiveInt_returnsNullForNonPositiveOrInvalidInput() {
        assertNull(validator.parsePositiveInt("abc"));
        assertNotNull(validator.parsePositiveInt("0"));
        assertFalse(validator.isValid("March 21, 2026", "0"));
    }
}









