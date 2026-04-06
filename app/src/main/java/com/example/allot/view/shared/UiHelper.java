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
     * Creates a new UiHelper instance.
     */
    private UiHelper() {
    }

    /**
     * Returns the result of clean text.
     *
     * @param value the value
     * @return the result of this call
     */
    public static String cleanText(String value) {
        return TextHelper.cleanText(value);
    }

    /**
     * Returns whether blank.
     *
     * @param value the value
     * @return whether blank
     */
    public static boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }

    /**
     * Returns the result of default text.
     *
     * @param value the value
     * @param fallback the fallback
     * @return the result of this call
     */
    public static String defaultText(String value, String fallback) {
        return TextHelper.defaultText(value, fallback);
    }

    /**
     * Returns the result of read text.
     *
     * @param editText the edit text
     * @return the result of this call
     */
    public static String readText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return cleanText(editText.getText().toString());
    }

    /**
     * Returns the result of dp to px.
     *
     * @param context the context
     * @param dp the dp
     * @return the result of this call
     */
    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /**
     * Returns the result of event image background res.
     *
     * @param category the category
     * @return the result of this call
     */
    public static int eventImageBackgroundRes(String category) {
        return R.drawable.no_image;
    }

    /**
     * Returns the result of format date.
     *
     * @param date the date
     * @param pattern the pattern
     * @return the result of this call
     */
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }
}









