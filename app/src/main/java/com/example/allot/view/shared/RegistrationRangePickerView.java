package com.example.allot.view.shared;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import com.example.allot.R;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Inline two-tap calendar widget for choosing a registration date range.
 */
public class RegistrationRangePickerView extends LinearLayout {
    public interface OnRangeChangedListener {
        /**
         * Handles the range changed callback.
         *
         * @param startDate the start date
         * @param endDate the end date
         */
        void onRangeChanged(@Nullable Calendar startDate, @Nullable Calendar endDate);
    }

    private static final int CELL_COUNT = 42;

    private final List<TextView> dayCells = new ArrayList<>();
    private final List<Calendar> cellDates = new ArrayList<>();
    private final SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM", Locale.getDefault());
    private final Calendar displayedMonth = normalizedMonth(Calendar.getInstance());

    private TextView monthLabel;
    private TextView yearLabel;
    private LinearLayout calendarRowsContainer;
    private Calendar selectedStartDate;
    private Calendar selectedEndDate;
    private boolean suppressCallbacks;
    private OnRangeChangedListener onRangeChangedListener;

    /**
     * Creates a new RegistrationRangePickerView instance.
     *
     * @param context the context
     */
    public RegistrationRangePickerView(Context context) {
        super(context);
        init();
    }

    /**
     * Creates a new RegistrationRangePickerView instance.
     *
     * @param context the context
     * @param attrs the attrs
     */
    public RegistrationRangePickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Creates a new RegistrationRangePickerView instance.
     *
     * @param context the context
     * @param attrs the attrs
     * @param defStyleAttr the def style attr
     */
    public RegistrationRangePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * Performs init.
     */
    private void init() {
        inflate(getContext(), R.layout.view_registration_range_picker, this);
        setOrientation(VERTICAL);

        monthLabel = findViewById(R.id.registrationMonthLabel);
        yearLabel = findViewById(R.id.registrationYearLabel);
        calendarRowsContainer = findViewById(R.id.registrationCalendarRows);
        ImageButton previousMonthButton = findViewById(R.id.previousMonthButton);
        ImageButton nextMonthButton = findViewById(R.id.nextMonthButton);

        previousMonthButton.setOnClickListener(view -> shiftDisplayedMonth(-1));
        nextMonthButton.setOnClickListener(view -> shiftDisplayedMonth(1));

        buildDayGrid();
        renderCalendar();
    }

    /**
     * Updates the on range changed listener.
     *
     * @param listener the listener
     */
    public void setOnRangeChangedListener(@Nullable OnRangeChangedListener listener) {
        onRangeChangedListener = listener;
    }

    /**
     * Performs bind range.
     *
     * @param startMonth the start month
     * @param startDay the start day
     * @param startYear the start year
     * @param endMonth the end month
     * @param endDay the end day
     * @param endYear the end year
     */
    public void bindRange(String startMonth,
                          String startDay,
                          String startYear,
                          String endMonth,
                          String endDay,
                          String endYear) {
        suppressCallbacks = true;
        selectedStartDate = parseDate(startMonth, startDay, startYear);
        selectedEndDate = parseDate(endMonth, endDay, endYear);

        if (selectedStartDate != null && selectedEndDate != null && selectedEndDate.before(selectedStartDate)) {
            Calendar temp = selectedStartDate;
            selectedStartDate = selectedEndDate;
            selectedEndDate = temp;
        }

        Calendar anchorDate = selectedStartDate != null ? selectedStartDate : selectedEndDate;
        if (anchorDate != null) {
            displayedMonth.setTime(anchorDate.getTime());
            displayedMonth.set(Calendar.DAY_OF_MONTH, 1);
        } else {
            Calendar now = Calendar.getInstance();
            displayedMonth.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1, 0, 0, 0);
            displayedMonth.set(Calendar.MILLISECOND, 0);
        }

        renderCalendar();
        suppressCallbacks = false;
    }

    /**
     * Returns the start month.
     *
     * @return the start month
     */
    public String getStartMonth() {
        return formatMonth(selectedStartDate);
    }

    /**
     * Returns the start day.
     *
     * @return the start day
     */
    public String getStartDay() {
        return formatDay(selectedStartDate);
    }

    /**
     * Returns the start year.
     *
     * @return the start year
     */
    public String getStartYear() {
        return formatYear(selectedStartDate);
    }

    /**
     * Returns the end month.
     *
     * @return the end month
     */
    public String getEndMonth() {
        return formatMonth(selectedEndDate);
    }

    /**
     * Returns the end day.
     *
     * @return the end day
     */
    public String getEndDay() {
        return formatDay(selectedEndDate);
    }

    /**
     * Returns the end year.
     *
     * @return the end year
     */
    public String getEndYear() {
        return formatYear(selectedEndDate);
    }

    /**
     * Performs build day grid.
     */
    private void buildDayGrid() {
        calendarRowsContainer.removeAllViews();
        dayCells.clear();
        cellDates.clear();

        Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.varela_round_regular);
        int cellHeight = UiHelper.dpToPx(getContext(), 38);
        int cellMargin = UiHelper.dpToPx(getContext(), 3);

        for (int row = 0; row < 6; row++) {
            LinearLayout rowLayout = new LinearLayout(getContext());
            rowLayout.setOrientation(HORIZONTAL);
            LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            if (row > 0) {
                rowParams.topMargin = UiHelper.dpToPx(getContext(), 4);
            }
            rowLayout.setLayoutParams(rowParams);

            for (int column = 0; column < 7; column++) {
                AppCompatTextView cellView = new AppCompatTextView(getContext());
                LayoutParams cellParams = new LayoutParams(0, cellHeight, 1f);
                if (column > 0) {
                    cellParams.setMarginStart(cellMargin);
                }
                cellView.setLayoutParams(cellParams);
                cellView.setGravity(Gravity.CENTER);
                cellView.setTextSize(16f);
                cellView.setTypeface(typeface);
                cellView.setIncludeFontPadding(false);
                cellView.setClickable(true);
                cellView.setFocusable(true);
                int index = (row * 7) + column;
                cellView.setOnClickListener(view -> handleDateTap(index));
                rowLayout.addView(cellView);
                dayCells.add(cellView);
                cellDates.add(null);
            }

            calendarRowsContainer.addView(rowLayout);
        }
    }

    /**
     * Performs shift displayed month.
     *
     * @param monthDelta the month delta
     */
    private void shiftDisplayedMonth(int monthDelta) {
        displayedMonth.add(Calendar.MONTH, monthDelta);
        displayedMonth.set(Calendar.DAY_OF_MONTH, 1);
        renderCalendar();
    }

    /**
     * Performs render calendar.
     */
    private void renderCalendar() {
        monthLabel.setText(monthFormatter.format(displayedMonth.getTime()));
        yearLabel.setText(String.valueOf(displayedMonth.get(Calendar.YEAR)));

        Calendar firstVisibleDate = (Calendar) displayedMonth.clone();
        int firstDayOfWeek = firstVisibleDate.get(Calendar.DAY_OF_WEEK);
        firstVisibleDate.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - Calendar.SUNDAY));

        for (int i = 0; i < CELL_COUNT; i++) {
            Calendar cellDate = (Calendar) firstVisibleDate.clone();
            cellDate.add(Calendar.DAY_OF_MONTH, i);
            cellDates.set(i, cellDate);
            bindDayCell(dayCells.get(i), cellDate);
        }
    }

    /**
     * Performs bind day cell.
     *
     * @param cellView the cell view
     * @param cellDate the cell date
     */
    private void bindDayCell(TextView cellView, Calendar cellDate) {
        boolean isCurrentMonth = cellDate.get(Calendar.MONTH) == displayedMonth.get(Calendar.MONTH)
                && cellDate.get(Calendar.YEAR) == displayedMonth.get(Calendar.YEAR);
        boolean isStart = isSameDay(cellDate, selectedStartDate);
        boolean isEnd = isSameDay(cellDate, selectedEndDate);
        boolean isInRange = isWithinSelectedRange(cellDate);

        cellView.setText(String.valueOf(cellDate.get(Calendar.DAY_OF_MONTH)));
        if (isStart || isEnd) {
            cellView.setBackgroundResource(R.drawable.bg_registration_calendar_day_selected);
            cellView.setTextColor(ContextCompat.getColor(getContext(), R.color.screen_bg));
            return;
        }

        if (isInRange) {
            cellView.setBackgroundResource(R.drawable.bg_registration_calendar_day_in_range);
            cellView.setTextColor(ContextCompat.getColor(getContext(), R.color.text_primary));
            return;
        }

        cellView.setBackground(null);
        cellView.setTextColor(ContextCompat.getColor(
                getContext(),
                isCurrentMonth ? R.color.text_primary : R.color.text_secondary
        ));
        cellView.setAlpha(isCurrentMonth ? 1f : 0.55f);
    }

    /**
     * Performs handle date tap.
     *
     * @param index the index
     */
    private void handleDateTap(int index) {
        if (index < 0 || index >= cellDates.size()) {
            return;
        }

        Calendar tappedDate = copyOf(cellDates.get(index));
        if (tappedDate == null) {
            return;
        }

        if (selectedStartDate == null || (selectedStartDate != null && selectedEndDate != null)) {
            selectedStartDate = tappedDate;
            selectedEndDate = null;
        } else if (tappedDate.before(selectedStartDate)) {
            selectedEndDate = selectedStartDate;
            selectedStartDate = tappedDate;
        } else {
            selectedEndDate = tappedDate;
        }

        displayedMonth.set(tappedDate.get(Calendar.YEAR), tappedDate.get(Calendar.MONTH), 1, 0, 0, 0);
        displayedMonth.set(Calendar.MILLISECOND, 0);
        renderCalendar();
        dispatchRangeChanged();
    }

    /**
     * Performs dispatch range changed.
     */
    private void dispatchRangeChanged() {
        if (suppressCallbacks || onRangeChangedListener == null) {
            return;
        }
        onRangeChangedListener.onRangeChanged(copyOf(selectedStartDate), copyOf(selectedEndDate));
    }

    /**
     * Returns whether within selected range.
     *
     * @param date the date
     * @return whether within selected range
     */
    private boolean isWithinSelectedRange(Calendar date) {
        if (selectedStartDate == null || selectedEndDate == null) {
            return false;
        }
        return !date.before(selectedStartDate) && !date.after(selectedEndDate);
    }

    /**
     * Returns whether same day.
     *
     * @param first the first
     * @param second the second
     * @return whether same day
     */
    private boolean isSameDay(@Nullable Calendar first, @Nullable Calendar second) {
        return first != null
                && second != null
                && first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
                && first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Returns the result of parse date.
     *
     * @param month the month
     * @param day the day
     * @param year the year
     * @return the result of this call
     */
    @Nullable
    private Calendar parseDate(String month, String day, String year) {
        if (TextUtils.isEmpty(month) || TextUtils.isEmpty(day) || TextUtils.isEmpty(year)) {
            return null;
        }

        String normalizedMonth = month.trim();
        String[] shortMonths = new DateFormatSymbols(Locale.getDefault()).getShortMonths();
        int monthIndex = -1;
        for (int i = 0; i < shortMonths.length; i++) {
            String candidate = shortMonths[i];
            if (candidate != null && normalizedMonth.equalsIgnoreCase(candidate.replace(".", ""))) {
                monthIndex = i;
                break;
            }
            if (candidate != null && normalizedMonth.equalsIgnoreCase(candidate)) {
                monthIndex = i;
                break;
            }
        }

        if (monthIndex < 0) {
            return null;
        }

        try {
            int parsedDay = Integer.parseInt(day.trim());
            int parsedYear = Integer.parseInt(year.trim());
            Calendar calendar = Calendar.getInstance();
            calendar.setLenient(false);
            calendar.set(parsedYear, monthIndex, parsedDay, 0, 0, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.getTime();
            return normalizedDate(calendar);
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Returns the result of format month.
     *
     * @param calendar the calendar
     * @return the result of this call
     */
    private String formatMonth(@Nullable Calendar calendar) {
        return calendar == null ? "" : monthFormatter.format(calendar.getTime());
    }

    /**
     * Returns the result of format day.
     *
     * @param calendar the calendar
     * @return the result of this call
     */
    private String formatDay(@Nullable Calendar calendar) {
        return calendar == null ? "" : String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Returns the result of format year.
     *
     * @param calendar the calendar
     * @return the result of this call
     */
    private String formatYear(@Nullable Calendar calendar) {
        return calendar == null ? "" : String.valueOf(calendar.get(Calendar.YEAR));
    }

    /**
     * Returns the result of copy of.
     *
     * @param source the source
     * @return the result of this call
     */
    @Nullable
    private Calendar copyOf(@Nullable Calendar source) {
        return source == null ? null : normalizedDate((Calendar) source.clone());
    }

    /**
     * Returns the result of normalized month.
     *
     * @param source the source
     * @return the result of this call
     */
    private static Calendar normalizedMonth(Calendar source) {
        Calendar calendar = normalizedDate((Calendar) source.clone());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar;
    }

    /**
     * Returns the result of normalized date.
     *
     * @param source the source
     * @return the result of this call
     */
    private static Calendar normalizedDate(Calendar source) {
        source.set(Calendar.HOUR_OF_DAY, 0);
        source.set(Calendar.MINUTE, 0);
        source.set(Calendar.SECOND, 0);
        source.set(Calendar.MILLISECOND, 0);
        return source;
    }
}
