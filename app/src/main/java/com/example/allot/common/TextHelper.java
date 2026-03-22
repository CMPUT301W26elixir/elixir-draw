package com.example.allot.common;

/**
 * Holds small text helpers used across the app.
 */
public final class TextHelper {

    /**
     * Prevents this utility class from being instantiated.
     */
    private TextHelper() {
    }

    /**
     * Trims a string and turns null into an empty string.
     *
     * @param value the value to clean
     * @return a trimmed string, or an empty string when the value is null
     */
    public static String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Checks whether a value is empty after it is cleaned.
     *
     * @param value the value to check
     * @return true when the value is null, empty, or only whitespace
     */
    public static boolean isBlank(String value) {
        return cleanText(value).isEmpty();
    }

    /**
     * Returns a backup value when the main text is blank.
     *
     * @param value the preferred value
     * @param fallback the value to use when the preferred value is blank
     * @return the preferred value when it has content, otherwise the fallback
     */
    public static String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}







