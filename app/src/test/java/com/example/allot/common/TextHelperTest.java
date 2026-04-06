package com.example.allot.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
public class TextHelperTest {

    /**
     * Handles clean Text_trims Whitespace And Handles Null.
     */
    @Test
    public void cleanText_trimsWhitespaceAndHandlesNull() {
        assertEquals("", TextHelper.cleanText(null));
        assertEquals("hello", TextHelper.cleanText("  hello  "));
    }

    /**
     * Returns whether i.s Blank_returns True For Null And Whitespace Only Values
     */
    @Test
    public void isBlank_returnsTrueForNullAndWhitespaceOnlyValues() {
        assertTrue(TextHelper.isBlank(null));
        assertTrue(TextHelper.isBlank("   "));
        assertFalse(TextHelper.isBlank(" value "));
    }

    /**
     * Handles default Text_returns Fallback When Value Is Blank.
     */
    @Test
    public void defaultText_returnsFallbackWhenValueIsBlank() {
        assertEquals("fallback", TextHelper.defaultText(" ", "fallback"));
        assertEquals("value", TextHelper.defaultText("value", "fallback"));
    }
}







