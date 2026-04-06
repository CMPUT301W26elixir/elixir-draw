package com.example.allot.common;

/**
 * Holds small text helpers used across the app.
 */
public final class TextHelper {

    /**
     * Creates a new TextHelper instance.
     */
    private TextHelper() {
    }

    /**
     * Returns the result of clean text.
     *
     * @param value the value
     * @return the result of this call
     */
    public static String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Returns whether blank.
     *
     * @param value the value
     * @return whether blank
     */
    public static boolean isBlank(String value) {
        return cleanText(value).isEmpty();
    }

    /**
     * Returns the result of default text.
     *
     * @param value the value
     * @param fallback the fallback
     * @return the result of this call
     */
    public static String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}







