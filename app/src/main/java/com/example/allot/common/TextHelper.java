package com.example.allot.common;
public final class TextHelper {

    private TextHelper() {
    }

    public static String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    public static boolean isBlank(String value) {
        return cleanText(value).isEmpty();
    }

    public static String defaultText(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}







