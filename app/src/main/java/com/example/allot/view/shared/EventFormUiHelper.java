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
    private final CheckBox privateEventCheckbox;
    private final CheckBox geolocationCheckbox;
    private final Spinner eventMonthSpinner;
    private final EditText eventDayInput;
    private final EditText eventYearInput;
    private final EditText priceInput;
    private final EditText descriptionInput;
    private final EditText participantsInput;
    private final RegistrationRangePickerView registrationRangePickerView;

    /**
     * Creates a new EventFormUiHelper instance.
     *
     * @param titleInput the title input
     * @param locationInput the location input
     * @param privateEventCheckbox the private event checkbox
     * @param geolocationCheckbox the geolocation checkbox
     * @param eventMonthSpinner the event month spinner
     * @param eventDayInput the event day input
     * @param eventYearInput the event year input
     * @param priceInput the price input
     * @param descriptionInput the description input
     * @param participantsInput the participants input
     * @param registrationRangePickerView the registration range picker view
     */
    public EventFormUiHelper(EditText titleInput,
                             EditText locationInput,
                             CheckBox privateEventCheckbox,
                             CheckBox geolocationCheckbox,
                             Spinner eventMonthSpinner,
                             EditText eventDayInput,
                             EditText eventYearInput,
                             EditText priceInput,
                             EditText descriptionInput,
                             EditText participantsInput,
                             RegistrationRangePickerView registrationRangePickerView) {
        this.titleInput = titleInput;
        this.locationInput = locationInput;
        this.privateEventCheckbox = privateEventCheckbox;
        this.geolocationCheckbox = geolocationCheckbox;
        this.eventMonthSpinner = eventMonthSpinner;
        this.eventDayInput = eventDayInput;
        this.eventYearInput = eventYearInput;
        this.priceInput = priceInput;
        this.descriptionInput = descriptionInput;
        this.participantsInput = participantsInput;
        this.registrationRangePickerView = registrationRangePickerView;
    }

    /**
     * Updates the up month spinners.
     *
     * @param context the context
     */
    public void setupMonthSpinners(Context context) {
        setupMonthSpinner(context, eventMonthSpinner);
    }

    /**
     * Returns the result of read form data.
     *
     * @return the result of this call
     */
    public EventFormData readFormData() {
        return new EventFormData(
                readText(titleInput),
                readText(locationInput),
                privateEventCheckbox != null && privateEventCheckbox.isChecked(),
                geolocationCheckbox.isChecked(),
                readMonth(eventMonthSpinner),
                readText(eventDayInput),
                readText(eventYearInput),
                readText(priceInput),
                readText(descriptionInput),
                readText(participantsInput),
                registrationRangePickerView == null ? "" : registrationRangePickerView.getStartMonth(),
                registrationRangePickerView == null ? "" : registrationRangePickerView.getStartDay(),
                registrationRangePickerView == null ? "" : registrationRangePickerView.getStartYear(),
                registrationRangePickerView == null ? "" : registrationRangePickerView.getEndMonth(),
                registrationRangePickerView == null ? "" : registrationRangePickerView.getEndDay(),
                registrationRangePickerView == null ? "" : registrationRangePickerView.getEndYear()
        );
    }

    /**
     * Performs bind form.
     *
     * @param formData the form data
     */
    public void bindForm(EventFormData formData) {
        if (formData == null) {
            return;
        }

        titleInput.setText(formData.getTitle());
        locationInput.setText(formData.getLocation());
        if (privateEventCheckbox != null) {
            privateEventCheckbox.setChecked(formData.isPrivateEvent());
        }
        geolocationCheckbox.setChecked(formData.isGeolocationEnabled());
        priceInput.setText(formData.getPrice());
        descriptionInput.setText(formData.getDescription());
        participantsInput.setText(formData.getParticipants());

        bindDate(eventMonthSpinner, eventDayInput, eventYearInput,
                formData.getEventMonth(), formData.getEventDay(), formData.getEventYear());
        if (registrationRangePickerView != null) {
            registrationRangePickerView.bindRange(
                    formData.getRegistrationStartMonth(),
                    formData.getRegistrationStartDay(),
                    formData.getRegistrationStartYear(),
                    formData.getRegistrationEndMonth(),
                    formData.getRegistrationEndDay(),
                    formData.getRegistrationEndYear()
            );
        }
    }

    /**
     * Performs bind date.
     *
     * @param monthSpinner the month spinner
     * @param dayInput the day input
     * @param yearInput the year input
     * @param month the month
     * @param day the day
     * @param year the year
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
     * Updates the up month spinner.
     *
     * @param context the context
     * @param spinner the spinner
     */
    private void setupMonthSpinner(Context context, Spinner spinner) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                context,
                android.R.layout.simple_spinner_item,
                context.getResources().getTextArray(R.array.create_event_months)
        ) {
            /**
             * Returns the view.
             *
             * @param position the position
             * @param convertView the convert view
             * @param parent the parent
             * @return the view
             */
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

            /**
             * Returns the drop down view.
             *
             * @param position the position
             * @param convertView the convert view
             * @param parent the parent
             * @return the drop down view
             */
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
     * Updates the spinner to month.
     *
     * @param spinner the spinner
     * @param month the month
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
     * Returns the result of read text.
     *
     * @param editText the edit text
     * @return the result of this call
     */
    private String readText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    /**
     * Returns the result of read month.
     *
     * @param spinner the spinner
     * @return the result of this call
     */
    private String readMonth(Spinner spinner) {
        return spinner.getSelectedItemPosition() <= 0 ? "" : spinner.getSelectedItem().toString();
    }
}









