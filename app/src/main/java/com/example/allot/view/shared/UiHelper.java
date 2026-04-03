package com.example.allot.view.shared;

import android.content.Context;
import android.widget.EditText;
import com.example.allot.R;
import com.example.allot.common.TextHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Holds small UI helper methods used across many screens.
 */
public final class UiHelper {

    /**
     * Prevents this utility class from being instantiated.
     */
    private UiHelper() {
    }

    /**
     * Trims text and replaces null with an empty string.
     *
     * @param value the value to clean
     * @return a cleaned string value
     */
    public static String cleanText(String value) {
        return TextHelper.cleanText(value);
    }

    /**
     * Checks whether a string is blank after trimming.
     *
     * @param value the value to check
     * @return true when the value has no content
     */
    public static boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }

    /**
     * Returns a fallback value when the main value is blank.
     *
     * @param value the preferred value
     * @param fallback the value to use when the preferred value is blank
     * @return the preferred value when it has content, otherwise the fallback
     */
    public static String defaultText(String value, String fallback) {
        return TextHelper.defaultText(value, fallback);
    }

    /**
     * Reads trimmed text from an EditText safely.
     *
     * @param editText the field to read
     * @return the field text, or an empty string when the field is missing
     */
    public static String readText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return cleanText(editText.getText().toString());
    }

    /**
     * Converts density-independent pixels into physical pixels.
     *
     * @param context the context used to read display density
     * @param dp the density-independent pixel value
     * @return the matching pixel value
     */
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Picks one of the shared event-image backgrounds from the category text.
     *
     * @param category the event category
     * @return the drawable resource for the event background
     */
    public static int eventImageBackgroundRes(String category) {
        return R.drawable.no_image;
    }

    /**
     * Formats a date using the provided pattern.
     *
     * @param date the date to format
     * @param pattern the pattern used by SimpleDateFormat
     * @return the formatted date string, or null when the date is null
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }
}









