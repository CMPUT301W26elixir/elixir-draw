package com.example.allot.controller.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class EventJoinDistanceValidatorTest {
    private EventJoinDistanceValidator validator;

    @Before
    public void setUp() {
        validator = new EventJoinDistanceValidator();
    }

    @Test
    public void isWithinAllowedRadius_returnsTrueWhenEntrantIsWithin50Km() {
        assertTrue(validator.isWithinAllowedRadius(
                53.5461,
                -113.4938,
                53.5232,
                -113.5263
        ));
    }

    @Test
    public void isWithinAllowedRadius_returnsFalseWhenEntrantIsOutside50Km() {
        assertFalse(validator.isWithinAllowedRadius(
                53.9333,
                -116.5765,
                53.5232,
                -113.5263
        ));
    }

    @Test
    public void isWithinAllowedRadius_returnsFalseWhenCoordinatesMissing() {
        assertFalse(validator.isWithinAllowedRadius(
                null,
                -113.4938,
                53.5232,
                -113.5263
        ));
    }
}
