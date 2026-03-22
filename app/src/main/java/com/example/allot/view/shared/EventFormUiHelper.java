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

    public void setupMonthSpinners(Context context) {
        setupMonthSpinner(context, eventMonthSpinner);
        setupMonthSpinner(context, registrationStartMonthSpinner);
        setupMonthSpinner(context, registrationEndMonthSpinner);
    }

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

    private String readText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private String readMonth(Spinner spinner) {
        return spinner.getSelectedItemPosition() <= 0 ? "" : spinner.getSelectedItem().toString();
    }
}









