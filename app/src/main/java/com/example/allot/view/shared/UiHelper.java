package com.example.allot.view.shared;

import android.content.Context;
import android.widget.EditText;
import com.example.allot.R;
import com.example.allot.common.TextHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public final class UiHelper {

    private UiHelper() {
    }

    public static String cleanText(String value) {
        return TextHelper.cleanText(value);
    }

    public static boolean isBlank(String value) {
        return TextHelper.isBlank(value);
    }

    public static String defaultText(String value, String fallback) {
        return TextHelper.defaultText(value, fallback);
    }

    public static String readText(EditText editText) {
        if (editText == null || editText.getText() == null) {
            return "";
        }
        return cleanText(editText.getText().toString());
    }

    public static int dpToPx(Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    public static int eventImageBackgroundRes(String category) {
        return Math.abs(cleanText(category).hashCode()) % 2 == 0
                ? R.drawable.bg_event_image_one
                : R.drawable.bg_event_image_two;
    }

    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }
}









