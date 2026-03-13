package com.example.allot.view;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.controller.EventController;
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Activity for creating a new event.
 * Collects event details from the user, validates the input,
 * and saves the event to Firestore.
 */
public class CreateEventActivity extends AppCompatActivity {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    private EditText eventNameInput;
    private EditText locationInput;
    private CheckBox geolocationCheckbox;
    private Spinner startMonthSpinner;
    private EditText startDayInput;
    private EditText startYearInput;
    private EditText priceInput;
    private EditText descriptionInput;
    private EditText participantsInput;
    private Spinner registrationStartMonthSpinner;
    private EditText registrationStartDayInput;
    private EditText registrationStartYearInput;
    private Spinner registrationEndMonthSpinner;
    private EditText registrationEndDayInput;
    private EditText registrationEndYearInput;
    private TextView nextButton;

    private EventController eventController;
    private UserController userController;
    private boolean isSaving;

    /**
     * Initializes the activity, binds views, sets up month spinners,
     * configures the header, and registers event listeners.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        dateFormat.setLenient(false);
        eventController = new EventController();
        userController = new UserController(this);

        bindViews();
        setupMonthSpinner(startMonthSpinner);
        setupMonthSpinner(registrationStartMonthSpinner);
        setupMonthSpinner(registrationEndMonthSpinner);
        setupHeader();
        setupListeners();
    }

    /**
     * Finishes the activity without transition animation.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Binds all view references used by the activity.
     */
    private void bindViews() {
        eventNameInput = findViewById(R.id.eventNameInput);
        locationInput = findViewById(R.id.locationInput);
        geolocationCheckbox = findViewById(R.id.geolocationCheckbox);
        startMonthSpinner = findViewById(R.id.startMonthSpinner);
        startDayInput = findViewById(R.id.startDayInput);
        startYearInput = findViewById(R.id.startYearInput);
        priceInput = findViewById(R.id.priceInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        participantsInput = findViewById(R.id.participantsInput);
        registrationStartMonthSpinner = findViewById(R.id.registrationStartMonthSpinner);
        registrationStartDayInput = findViewById(R.id.registrationStartDayInput);
        registrationStartYearInput = findViewById(R.id.registrationStartYearInput);
        registrationEndMonthSpinner = findViewById(R.id.registrationEndMonthSpinner);
        registrationEndDayInput = findViewById(R.id.registrationEndDayInput);
        registrationEndYearInput = findViewById(R.id.registrationEndYearInput);
        nextButton = findViewById(R.id.createEventNextButton);
    }

    /**
     * Sets up the header back button behavior.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Configures a month spinner with the available month values and custom display styling.
     *
     * @param spinner the spinner to configure
     */
    private void setupMonthSpinner(Spinner spinner) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                this,
                android.R.layout.simple_spinner_item,
                getResources().getTextArray(R.array.create_event_months)
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);
                if (textView != null) {
                    textView.setTextColor(position == 0 ? getResources().getColor(R.color.text_secondary) : Color.WHITE);
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
     * Registers click listeners used by the activity.
     */
    private void setupListeners() {
        nextButton.setOnClickListener(view -> submitEvent());
    }

    /**
     * Validates the entered event data, creates a new event,
     * saves it, and opens the success screen if the save succeeds.
     */
    private void submitEvent() {
        if (isSaving) {
            return;
        }

        String title = readText(eventNameInput);
        String location = readText(locationInput);
        String priceValue = readText(priceInput);
        String description = readText(descriptionInput);
        String participantsValue = readText(participantsInput);

        if (isBlank(title)
                || isBlank(location)
                || isBlank(priceValue)
                || isBlank(description)
                || isBlank(participantsValue)
                || !isDateInputComplete(startMonthSpinner, startDayInput, startYearInput)
                || !isDateInputComplete(registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput)
                || !isDateInputComplete(registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput)) {
            Toast.makeText(this, R.string.create_event_validation_required, Toast.LENGTH_SHORT).show();
            return;
        }

        Date eventDate = parseDate(startMonthSpinner, startDayInput, startYearInput);
        Date registrationStart = parseDate(registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput);
        Date registrationEnd = parseDate(registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput);
        if (eventDate == null || registrationStart == null || registrationEnd == null) {
            Toast.makeText(this, R.string.create_event_validation_date, Toast.LENGTH_SHORT).show();
            return;
        }

        Double price = parsePrice(priceValue);
        if (price == null || price < 0) {
            Toast.makeText(this, R.string.create_event_validation_price, Toast.LENGTH_SHORT).show();
            return;
        }

        Integer participants = parsePositiveInt(participantsValue);
        if (participants == null || participants <= 0) {
            Toast.makeText(this, R.string.create_event_validation_participants, Toast.LENGTH_SHORT).show();
            return;
        }

        if (registrationEnd.before(registrationStart) || eventDate.before(registrationEnd)) {
            Toast.makeText(this, R.string.create_event_validation_order, Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event(UUID.randomUUID().toString(), userController.getCurrentDeviceId(), title, participants);
        event.title = title;
        event.location = location;
        event.geoloc = geolocationCheckbox.isChecked();
        event.eventDate = eventDate;
        event.price = price;
        event.description = description;
        event.capacity = participants;
        event.limit = participants;
        event.registrationOpen = registrationStart;
        event.registrationDeadline = registrationEnd;
        event.status = "open";

        isSaving = true;
        nextButton.setEnabled(false);

        eventController.createNewEventForUser(event, userController.getCurrentDeviceId(), (result, success) -> {
            isSaving = false;
            nextButton.setEnabled(true);

            if (!success || result == null || !result) {
                Toast.makeText(this, R.string.create_event_save_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, R.string.create_event_save_success, Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, CreateEventSuccessActivity.class);
            intent.putExtra(CreateEventSuccessActivity.EXTRA_EVENT_ID, event.eventId);
            intent.putExtra(CreateEventSuccessActivity.EXTRA_EVENT_TITLE, event.title);
            intent.putExtra(CreateEventSuccessActivity.EXTRA_EVENT_LOCATION, event.location);
            intent.putExtra(CreateEventSuccessActivity.EXTRA_EVENT_DATE, EventDisplayFormatter.date(event));
            intent.putExtra(CreateEventSuccessActivity.EXTRA_EVENT_PRICE, EventDisplayFormatter.price(event));
            intent.putExtra(CreateEventSuccessActivity.EXTRA_EVENT_DEADLINE, EventDisplayFormatter.deadline(event));
            intent.putExtra(CreateEventSuccessActivity.EXTRA_EVENT_CATEGORY, event.category);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    /**
     * Checks whether all parts of a date input have been filled in.
     *
     * @param monthSpinner the spinner containing the selected month
     * @param dayInput the input field containing the day
     * @param yearInput the input field containing the year
     * @return true if the date input is complete, otherwise false
     */
    private boolean isDateInputComplete(Spinner monthSpinner, EditText dayInput, EditText yearInput) {
        return monthSpinner.getSelectedItemPosition() > 0
                && !isBlank(readText(dayInput))
                && !isBlank(readText(yearInput));
    }

    /**
     * Returns the trimmed text content of an EditText field.
     *
     * @param editText the input field to read from
     * @return the trimmed text value, or an empty string if no text exists
     */
    private String readText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    /**
     * Checks whether a string is blank.
     *
     * @param value the string to check
     * @return true if the value is blank, otherwise false
     */
    private boolean isBlank(String value) {
        return TextUtils.isEmpty(value);
    }

    /**
     * Parses a date from the given month, day, and year input fields.
     *
     * @param monthSpinner the spinner containing the selected month
     * @param dayInput the input field containing the day
     * @param yearInput the input field containing the year
     * @return the parsed date, or null if parsing fails
     */
    private Date parseDate(Spinner monthSpinner, EditText dayInput, EditText yearInput) {
        String month = monthSpinner.getSelectedItemPosition() <= 0 ? "" : monthSpinner.getSelectedItem().toString();
        String day = readText(dayInput);
        String year = readText(yearInput);
        if (isBlank(month) || isBlank(day) || isBlank(year)) {
            return null;
        }

        try {
            return dateFormat.parse(month + " " + Integer.parseInt(day) + ", " + Integer.parseInt(year));
        } catch (ParseException | NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Parses a price value from text.
     *
     * @param value the price text to parse
     * @return the parsed price, or null if parsing fails
     */
    private Double parsePrice(String value) {
        try {
            return Double.parseDouble(value.replace("$", "").trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Parses a positive integer value from text.
     *
     * @param value the text to parse
     * @return the parsed integer, or null if parsing fails
     */
    private Integer parsePositiveInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}