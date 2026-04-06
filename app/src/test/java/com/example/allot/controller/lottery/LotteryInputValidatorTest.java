package com.example.allot.controller.lottery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
public class LotteryInputValidatorTest {
    private LotteryInputValidator validator;

    /**
     * Updates up.
     */
    @Before
    public void setUp() {
        validator = new LotteryInputValidator();
    }

    /**
     * Returns whether i.s Valid_returns True For Valid Date And Attendee Count
     */
    @Test
    public void isValid_returnsTrueForValidDateAndAttendeeCount() {
        assertTrue(validator.isValid("March 21, 2026", "25"));
    }

    /**
     * Handles parse Draw Date_returns Null For Invalid Date.
     */
    @Test
    public void parseDrawDate_returnsNullForInvalidDate() {
        assertNull(validator.parseDrawDate("not a date"));
    }

    /**
     * Handles parse Positive Int_returns Null For Non Positive Or Invalid Input.
     */
    @Test
    public void parsePositiveInt_returnsNullForNonPositiveOrInvalidInput() {
        assertNull(validator.parsePositiveInt("abc"));
        assertNotNull(validator.parsePositiveInt("0"));
        assertFalse(validator.isValid("March 21, 2026", "0"));
    }
}









