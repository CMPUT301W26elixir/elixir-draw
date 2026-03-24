package com.example.allot.view.shared;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.example.allot.R;
import com.example.allot.model.event.EventFormData;

/**
 * Reads and writes event form views so activities stay smaller.
 */
public class EventFormUiHelper {
    private final EditText titleInput;
    private final EditText locationInput;
    private final CheckBox geolocationCheckbox;
    private final Spinner eventMonthSpinner;
    private final EditText eventDayInput;
    private final EditText eventYearInput;
    private final EditText priceInput;
    private final EditText descriptionInput;
    private final EditText participantsInput;
    private final Spinner registrationStartMonthSpinner;
    private final EditText registrationStartDayInput;
    private final EditText registrationStartYearInput;
    private final Spinner registrationEndMonthSpinner;
    private final EditText registrationEndDayInput;
    private final EditText registrationEndYearInput;

    /**
     * Creates a helper around the event form widgets.
     *
     * @param titleInput the title field
     * @param locationInput the location field
     * @param geolocationCheckbox the geolocation checkbox
     * @param eventMonthSpinner the event month spinner
     * @param eventDayInput the event day field
     * @param eventYearInput the event year field
     * @param priceInput the price field
     * @param descriptionInput the description field
     * @param participantsInput the participant field
     * @param registrationStartMonthSpinner the registration start month spinner
     * @param registrationStartDayInput the registration start day field
     * @param registrationStartYearInput the registration start year field
     * @param registrationEndMonthSpinner the registration end month spinner
     * @param registrationEndDayInput the registration end day field
     * @param registrationEndYearInput the registration end year field
     */
    public EventFormUiHelper(EditText titleInput,
                             EditText locationInput,
                             CheckBox geolocationCheckbox,
                             Spinner eventMonthSpinner,
                             EditText eventDayInput,
                             EditText eventYearInput,
                             EditText priceInput,
                             EditText descriptionInput,
                             EditText participantsInput,
                             Spinner registrationStartMonthSpinner,
                             EditText registrationStartDayInput,
                             EditText registrationStartYearInput,
                             Spinner registrationEndMonthSpinner,
                             EditText registrationEndDayInput,
                             EditText registrationEndYearInput) {
        this.titleInput = titleInput;
        this.locationInput = locationInput;
        this.geolocationCheckbox = geolocationCheckbox;
        this.eventMonthSpinner = eventMonthSpinner;
        this.eventDayInput = eventDayInput;
        this.eventYearInput = eventYearInput;
        this.priceInput = priceInput;
        this.descriptionInput = descriptionInput;
        this.participantsInput = participantsInput;
        this.registrationStartMonthSpinner = registrationStartMonthSpinner;
        this.registrationStartDayInput = registrationStartDayInput;
        this.registrationStartYearInput = registrationStartYearInput;
        this.registrationEndMonthSpinner = registrationEndMonthSpinner;
        this.registrationEndDayInput = registrationEndDayInput;
        this.registrationEndYearInput = registrationEndYearInput;
    }

    /**
     * Applies the shared month options to each month spinner in the form.
     *
     * @param context the context used to load resources
     */
    public void setupMonthSpinners(Context context) {
        setupMonthSpinner(context, eventMonthSpinner);
        setupMonthSpinner(context, registrationStartMonthSpinner);
        setupMonthSpinner(context, registrationEndMonthSpinner);
    }

    /**
     * Reads the current widget values into a form data object.
     *
     * @return the current event form data
     */
    public EventFormData readFormData() {
        return new EventFormData(
                readText(titleInput),
                readText(locationInput),
                geolocationCheckbox.isChecked(),
                readMonth(eventMonthSpinner),
                readText(eventDayInput),
                readText(eventYearInput),
                readText(priceInput),
                readText(descriptionInput),
                readText(participantsInput),
                readMonth(registrationStartMonthSpinner),
                readText(registrationStartDayInput),
                readText(registrationStartYearInput),
                readMonth(registrationEndMonthSpinner),
                readText(registrationEndDayInput),
                readText(registrationEndYearInput)
        );
    }

    /**
     * Writes saved form values back into the widgets.
     *
     * @param formData the data to bind into the form
     */
    public void bindForm(EventFormData formData) {
        if (formData == null) {
            return;
        }

        titleInput.setText(formData.getTitle());
        locationInput.setText(formData.getLocation());
        geolocationCheckbox.setChecked(formData.isGeolocationEnabled());
        priceInput.setText(formData.getPrice());
        descriptionInput.setText(formData.getDescription());
        participantsInput.setText(formData.getParticipants());

        bindDate(eventMonthSpinner, eventDayInput, eventYearInput,
                formData.getEventMonth(), formData.getEventDay(), formData.getEventYear());
        bindDate(registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput,
                formData.getRegistrationStartMonth(),
                formData.getRegistrationStartDay(),
                formData.getRegistrationStartYear());
        bindDate(registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput,
                formData.getRegistrationEndMonth(),
                formData.getRegistrationEndDay(),
                formData.getRegistrationEndYear());
    }

    /**
     * Binds one logical date across a month spinner and two text fields.
     *
     * @param monthSpinner the spinner that holds the month
     * @param dayInput the field that holds the day
     * @param yearInput the field that holds the year
     * @param month the month value to select
     * @param day the day value to show
     * @param year the year value to show
     */
    private void bindDate(Spinner monthSpinner,
                          EditText dayInput,
                          EditText yearInput,
                          String month,
                          String day,
                          String year) {
        monthSpinner.setSelection(0);
        dayInput.setText("");
        yearInput.setText("");
        setSpinnerToMonth(monthSpinner, month);
        dayInput.setText(day);
        yearInput.setText(year);
    }

    /**
     * Configures a month spinner with the app's shared month labels and colors.
     *
     * @param context the context used to load resources
     * @param spinner the spinner being configured
     */
    private void setupMonthSpinner(Context context, Spinner spinner) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                context,
                android.R.layout.simple_spinner_item,
                context.getResources().getTextArray(R.array.create_event_months)
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(position == 0
                            ? context.getResources().getColor(R.color.text_secondary)
                            : Color.WHITE);
                    textView.setTextSize(16f);
                    textView.setPadding(12, textView.getPaddingTop(), 12, textView.getPaddingBottom());
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Selects the requested month value in a spinner when it is present.
     *
     * @param spinner the spinner to update
     * @param month the month value to select
     */
    private void setSpinnerToMonth(Spinner spinner, String month) {
        String normalizedMonth = UiHelper.cleanText(month);
        if (spinner == null || TextUtils.isEmpty(normalizedMonth)) {
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && normalizedMonth.equalsIgnoreCase(item.toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    /**
     * Reads trimmed text from an input field.
     *
     * @param editText the field to read
     * @return the trimmed field value, or an empty string when the field is empty
     */
    private String readText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    /**
     * Reads the selected month from a spinner, skipping the placeholder option.
     *
     * @param spinner the spinner to read
     * @return the selected month, or an empty string when no real month is selected
     */
    private String readMonth(Spinner spinner) {
        return spinner.getSelectedItemPosition() <= 0 ? "" : spinner.getSelectedItem().toString();
    }
}









