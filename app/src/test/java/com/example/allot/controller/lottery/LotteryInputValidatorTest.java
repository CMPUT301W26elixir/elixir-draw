package com.example.allot.controller.lottery;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
public class LotteryInputValidatorTest {
    private LotteryInputValidator validator;

    @Before
    public void setUp() {
        validator = new LotteryInputValidator();
    }

    @Test
    public void isValid_returnsTrueForValidDateAndAttendeeCount() {
        assertTrue(validator.isValid("March 21, 2026", "25"));
    }

    @Test
    public void parseDrawDate_returnsNullForInvalidDate() {
        assertNull(validator.parseDrawDate("not a date"));
    }

    @Test
    public void parsePositiveInt_returnsNullForNonPositiveOrInvalidInput() {
        assertNull(validator.parsePositiveInt("abc"));
        assertNotNull(validator.parsePositiveInt("0"));
        assertFalse(validator.isValid("March 21, 2026", "0"));
    }
}









