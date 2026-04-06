package com.example.allot.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
public class TextHelperTest {

    /**
     * Performs clean text trims whitespace and handles null.
     */
    @Test
    public void cleanText_trimsWhitespaceAndHandlesNull() {
        assertEquals("", TextHelper.cleanText(null));
        assertEquals("hello", TextHelper.cleanText("  hello  "));
    }

    /**
     * Performs is blank returns true for null and whitespace only values.
     */
    @Test
    public void isBlank_returnsTrueForNullAndWhitespaceOnlyValues() {
        assertTrue(TextHelper.isBlank(null));
        assertTrue(TextHelper.isBlank("   "));
        assertFalse(TextHelper.isBlank(" value "));
    }

    /**
     * Performs default text returns fallback when value is blank.
     */
    @Test
    public void defaultText_returnsFallbackWhenValueIsBlank() {
        assertEquals("fallback", TextHelper.defaultText(" ", "fallback"));
        assertEquals("value", TextHelper.defaultText("value", "fallback"));
    }
}







