package com.example.allot.view;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.AdapterView;
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
import com.example.allot.model.Event;
import com.example.allot.model.UpdateEventInput;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity for managing and editing an existing event.
 * Loads event details, allows the organizer to modify them,
 * and saves updates back to Firestore.
 */
public class ManageEventActivity extends AppCompatActivity {
    private static final int SAVE_INACTIVE_COLOR = Color.parseColor("#A6A8A5");
    private static final int SAVE_ACTIVE_COLOR = Color.parseColor("#FFFFFF");

    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_TITLE = "event_title";
    public static final String EXTRA_EVENT_LOCATION = "event_location";
    public static final String EXTRA_EVENT_DATE = "event_date";
    public static final String EXTRA_EVENT_PRICE = "event_price";
    public static final String EXTRA_EVENT_DESCRIPTION = "event_description";
    public static final String EXTRA_EVENT_PARTICIPANTS = "event_participants";
    public static final String EXTRA_REGISTRATION_START = "registration_start";
    public static final String EXTRA_REGISTRATION_END = "registration_end";
    public static final String EXTRA_EVENT_CATEGORY = "event_category";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    private EventController eventController;

    private View eventImageBackground;
    private TextView entrantsLotteryButton;
    private TextView summaryTitleText;
    private TextView summaryLocationText;
    private TextView summaryDateText;
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
    private TextView saveChangesButton;
    private String currentEventId;
    private Event currentEvent;
    private String currentCategory;
    private String originalTitle = "";
    private String originalLocation = "";
    private String originalPrice = "";
    private String originalDescription = "";
    private String originalParticipants = "";
    private String originalEventDate = "";
    private String originalRegistrationStart = "";
    private String originalRegistrationEnd = "";
    private boolean originalGeolocationEnabled;
    private boolean isBindingEvent;
    private boolean isLoadingEvent;
    private boolean isSaving;
    private boolean shouldRefreshOnResume;

    /**
     * Initializes the activity, binds views, configures inputs,
     * populates intent data, and loads the latest event from Firestore.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        dateFormat.setLenient(false);
        eventController = new EventController();
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        setupMonthSpinner(startMonthSpinner);
        setupMonthSpinner(registrationStartMonthSpinner);
        setupMonthSpinner(registrationEndMonthSpinner);
        setupHeader();
        setupListeners();
        populateUiFromIntent();
        captureOriginalState();
        updateSaveButtonState();
        loadEventFromFirestore();
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
        eventImageBackground = findViewById(R.id.eventImageBackground);
        entrantsLotteryButton = findViewById(R.id.entrantsLotteryButton);
        summaryTitleText = findViewById(R.id.summaryTitleText);
        summaryLocationText = findViewById(R.id.summaryLocationText);
        summaryDateText = findViewById(R.id.summaryDateText);
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
        saveChangesButton = findViewById(R.id.saveChangesButton);
    }

    /**
     * Sets up the header back button behavior.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Sets up text, checkbox, spinner, and button listeners used by the activity.
     */
    private void setupListeners() {
        TextWatcher dirtyStateWatcher = new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (!isBindingEvent) {
                    updateSaveButtonState();
                }
            }
        };

        eventNameInput.addTextChangedListener(dirtyStateWatcher);
        locationInput.addTextChangedListener(dirtyStateWatcher);
        priceInput.addTextChangedListener(dirtyStateWatcher);
        descriptionInput.addTextChangedListener(dirtyStateWatcher);
        participantsInput.addTextChangedListener(dirtyStateWatcher);
        startDayInput.addTextChangedListener(dirtyStateWatcher);
        startYearInput.addTextChangedListener(dirtyStateWatcher);
        registrationStartDayInput.addTextChangedListener(dirtyStateWatcher);
        registrationStartYearInput.addTextChangedListener(dirtyStateWatcher);
        registrationEndDayInput.addTextChangedListener(dirtyStateWatcher);
        registrationEndYearInput.addTextChangedListener(dirtyStateWatcher);
        geolocationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isBindingEvent) {
                updateSaveButtonState();
            }
        });

        AdapterView.OnItemSelectedListener dateSelectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isBindingEvent) {
                    updateSaveButtonState();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        startMonthSpinner.setOnItemSelectedListener(dateSelectionListener);
        registrationStartMonthSpinner.setOnItemSelectedListener(dateSelectionListener);
        registrationEndMonthSpinner.setOnItemSelectedListener(dateSelectionListener);

        entrantsLotteryButton.setOnClickListener(view -> openLotteryScreen());
        saveChangesButton.setOnClickListener(view -> saveChanges());
    }

    /**
     * Configures a month spinner with the available month values and custom styling.
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
     * Populates the UI using values passed through the launching intent.
     */
    private void populateUiFromIntent() {
        isBindingEvent = true;

        String title = cleanText(getIntent().getStringExtra(EXTRA_EVENT_TITLE));
        String location = cleanText(getIntent().getStringExtra(EXTRA_EVENT_LOCATION));
        String eventDate = cleanText(getIntent().getStringExtra(EXTRA_EVENT_DATE));
        String price = cleanText(getIntent().getStringExtra(EXTRA_EVENT_PRICE));
        String description = cleanText(getIntent().getStringExtra(EXTRA_EVENT_DESCRIPTION));
        String participants = cleanText(getIntent().getStringExtra(EXTRA_EVENT_PARTICIPANTS));
        String registrationStart = cleanText(getIntent().getStringExtra(EXTRA_REGISTRATION_START));
        String registrationEnd = cleanText(getIntent().getStringExtra(EXTRA_REGISTRATION_END));
        currentCategory = cleanText(getIntent().getStringExtra(EXTRA_EVENT_CATEGORY));

        summaryTitleText.setText(defaultText(title, getString(R.string.default_event_name)));
        summaryLocationText.setText(defaultText(location, getString(R.string.default_street_name)));
        summaryDateText.setText(defaultText(eventDate, getString(R.string.default_date)));
        eventNameInput.setText(title);
        locationInput.setText(location);
        priceInput.setText(price);
        descriptionInput.setText(description);
        participantsInput.setText(participants);
        geolocationCheckbox.setChecked(false);
        applySummaryImage(currentCategory);
        populateDateFields(eventDate, startMonthSpinner, startDayInput, startYearInput);
        populateDateFields(registrationStart, registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput);
        populateDateFields(registrationEnd, registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput);

        isBindingEvent = false;
    }

    /**
     * Loads the latest event data from Firestore.
     */
    private void loadEventFromFirestore() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        isLoadingEvent = true;
        updateSaveButtonState();
        eventController.getEventById(currentEventId, new EventController.EventCallback() {
            @Override
            public void onCallback(Event event) {
                isLoadingEvent = false;
                if (event == null) {
                    updateSaveButtonState();
                    Toast.makeText(ManageEventActivity.this, R.string.manage_event_not_found, Toast.LENGTH_SHORT).show();
                    return;
                }

                bindEventSnapshot(event);
            }

            @Override
            public void onError(Exception exception) {
                isLoadingEvent = false;
                updateSaveButtonState();
                Toast.makeText(ManageEventActivity.this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Binds the loaded Firestore event snapshot to the UI.
     *
     * @param event the loaded Firestore event snapshot
     */
    private void bindEventSnapshot(Event event) {
        bindEvent(event);
    }

    /**
     * Binds an event model to the form and summary UI.
     *
     * @param event the event to display
     */
    private void bindEvent(Event event) {
        isBindingEvent = true;
        currentEvent = event;

        currentCategory = cleanText(event.category);
        summaryTitleText.setText(defaultText(cleanText(event.title), getString(R.string.default_event_name)));
        summaryLocationText.setText(defaultText(cleanText(event.location), getString(R.string.default_street_name)));
        summaryDateText.setText(defaultText(formatDate(event.eventDate), getString(R.string.default_date)));

        eventNameInput.setText(cleanText(event.title));
        locationInput.setText(cleanText(event.location));
        priceInput.setText(event.price == null ? "" : formatPriceValue(event.price));
        descriptionInput.setText(cleanText(event.description));
        geolocationCheckbox.setChecked(Boolean.TRUE.equals(event.geoloc));

        int participantCount = event.capacity > 0 ? event.capacity : event.limit;
        participantsInput.setText(participantCount > 0 ? String.valueOf(participantCount) : "");

        clearDateFields(startMonthSpinner, startDayInput, startYearInput);
        clearDateFields(registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput);
        clearDateFields(registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput);
        populateDateFields(formatDate(event.eventDate), startMonthSpinner, startDayInput, startYearInput);
        populateDateFields(formatDate(event.registrationOpen), registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput);
        populateDateFields(formatDate(event.registrationDeadline), registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput);
        applySummaryImage(currentCategory);

        isBindingEvent = false;
        captureOriginalState();
        updateSaveButtonState();
    }

    /**
     * Opens the appropriate lottery-related screen for the current event.
     */
    private void openLotteryScreen() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_lottery_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        shouldRefreshOnResume = true;
        Class<?> destination = currentEvent != null && hasDrawResults(currentEvent)
                ? ManageEntrantsActivity.class
                : ManageLotteryActivity.class;
        startActivity(new android.content.Intent(this, destination)
                .putExtra(ManageLotteryActivity.EXTRA_EVENT_ID, currentEventId));
        overridePendingTransition(0, 0);
    }

    /**
     * Checks whether the event already has draw results.
     *
     * @param event the event to check
     * @return true if draw results exist, otherwise false
     */
    private boolean hasDrawResults(Event event) {
        return (event.chosen != null && !event.chosen.isEmpty())
                || (event.enrolled != null && !event.enrolled.isEmpty())
                || (event.cancelled != null && !event.cancelled.isEmpty())
                || (event.notEnrolled != null && !event.notEnrolled.isEmpty())
                || (event.waitingList != null && event.waitingList.chosen != null && !event.waitingList.chosen.isEmpty());
    }

    /**
     * Reloads the event when returning from a related screen that may have changed it.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (shouldRefreshOnResume && !TextUtils.isEmpty(currentEventId)) {
            shouldRefreshOnResume = false;
            loadEventFromFirestore();
        }
    }

    /**
     * Clears the date fields for the provided month, day, and year inputs.
     *
     * @param monthSpinner the month spinner
     * @param dayInput the day input
     * @param yearInput the year input
     */
    private void clearDateFields(Spinner monthSpinner, EditText dayInput, EditText yearInput) {
        monthSpinner.setSelection(0);
        dayInput.setText("");
        yearInput.setText("");
    }

    /**
     * Validates the current form and saves event changes to Firestore.
     */
    private void saveChanges() {
        if (isSaving || isLoadingEvent || !hasUnsavedChanges()) {
            return;
        }

        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_event_not_found, Toast.LENGTH_SHORT).show();
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

        UpdateEventInput input = new UpdateEventInput(
                title,
                location,
                geolocationCheckbox.isChecked(),
                eventDate,
                price,
                description,
                participants,
                registrationStart,
                registrationEnd
        );

        isSaving = true;
        updateSaveButtonState();
        eventController.updateEvent(currentEventId, input, (event, success) -> {
            isSaving = false;

            if (!success || event == null) {
                updateSaveButtonState();
                Toast.makeText(this, R.string.manage_event_save_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            reloadEventAfterSave(event);
        });
    }

    /**
     * Reloads the event after a successful save so the UI reflects the latest values.
     */
    private void reloadEventAfterSave(Event event) {
        bindEventSnapshot(event);
        setResult(RESULT_OK);
        Toast.makeText(this, R.string.manage_event_save_success, Toast.LENGTH_SHORT).show();
    }

    /**
     * Captures the current form state so unsaved changes can be detected.
     */
    private void captureOriginalState() {
        originalTitle = readText(eventNameInput);
        originalLocation = readText(locationInput);
        originalPrice = readText(priceInput);
        originalDescription = readText(descriptionInput);
        originalParticipants = readText(participantsInput);
        originalEventDate = currentDateValue(startMonthSpinner, startDayInput, startYearInput);
        originalRegistrationStart = currentDateValue(registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput);
        originalRegistrationEnd = currentDateValue(registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput);
        originalGeolocationEnabled = geolocationCheckbox.isChecked();
    }

    /**
     * Checks whether the current form differs from the original loaded state.
     *
     * @return true if unsaved changes exist, otherwise false
     */
    private boolean hasUnsavedChanges() {
        return !readText(eventNameInput).equals(originalTitle)
                || !readText(locationInput).equals(originalLocation)
                || !readText(priceInput).equals(originalPrice)
                || !readText(descriptionInput).equals(originalDescription)
                || !readText(participantsInput).equals(originalParticipants)
                || geolocationCheckbox.isChecked() != originalGeolocationEnabled
                || !currentDateValue(startMonthSpinner, startDayInput, startYearInput).equals(originalEventDate)
                || !currentDateValue(registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput).equals(originalRegistrationStart)
                || !currentDateValue(registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput).equals(originalRegistrationEnd);
    }

    /**
     * Updates the save button color and enabled state based on form and loading state.
     */
    private void updateSaveButtonState() {
        int color = hasUnsavedChanges() && !isSaving && !isLoadingEvent
                ? SAVE_ACTIVE_COLOR
                : SAVE_INACTIVE_COLOR;
        saveChangesButton.setBackgroundTintList(ColorStateList.valueOf(color));
        saveChangesButton.setEnabled(!isSaving && !isLoadingEvent);
    }

    /**
     * Checks whether all parts of a date input have been filled in.
     *
     * @param monthSpinner the month spinner
     * @param dayInput the day input
     * @param yearInput the year input
     * @return true if the date input is complete, otherwise false
     */
    private boolean isDateInputComplete(Spinner monthSpinner, EditText dayInput, EditText yearInput) {
        return monthSpinner.getSelectedItemPosition() > 0
                && !isBlank(readText(dayInput))
                && !isBlank(readText(yearInput));
    }

    /**
     * Returns the trimmed text value from an EditText.
     *
     * @param editText the input field to read
     * @return the trimmed text or an empty string
     */
    private String readText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    /**
     * Checks whether a string is blank.
     *
     * @param value the string to check
     * @return true if blank, otherwise false
     */
    private boolean isBlank(String value) {
        return TextUtils.isEmpty(value);
    }

    /**
     * Parses a date from the provided month, day, and year fields.
     *
     * @param monthSpinner the month spinner
     * @param dayInput the day input
     * @param yearInput the year input
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
     * @return the parsed price, or null if invalid
     */
    private Double parsePrice(String value) {
        try {
            return Double.parseDouble(value.replace("$", "").trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Returns the current date field values as a formatted date string.
     *
     * @param monthSpinner the month spinner
     * @param dayInput the day input
     * @param yearInput the year input
     * @return the formatted date string, or an empty string if invalid
     */
    private String currentDateValue(Spinner monthSpinner, EditText dayInput, EditText yearInput) {
        Date date = parseDate(monthSpinner, dayInput, yearInput);
        return date == null ? "" : formatDate(date);
    }

    /**
     * Parses a positive integer from text.
     *
     * @param value the text to parse
     * @return the parsed integer, or null if invalid
     */
    private Integer parsePositiveInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Populates month, day, and year inputs from a formatted date string.
     *
     * @param value the formatted date string
     * @param monthSpinner the month spinner
     * @param dayInput the day input
     * @param yearInput the year input
     */
    private void populateDateFields(String value, Spinner monthSpinner, EditText dayInput, EditText yearInput) {
        if (TextUtils.isEmpty(value)) {
            return;
        }

        try {
            Date date = dateFormat.parse(value);
            if (date == null) {
                return;
            }

            SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
            SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

            String month = monthFormat.format(date);
            String day = dayFormat.format(date);
            String year = yearFormat.format(date);

            setSpinnerToMonth(monthSpinner, month);
            dayInput.setText(day);
            yearInput.setText(year);
        } catch (ParseException ignored) {
        }
    }

    /**
     * Sets a spinner to the entry that matches the provided month text.
     *
     * @param spinner the spinner to update
     * @param month the month text to match
     */
    private void setSpinnerToMonth(Spinner spinner, String month) {
        if (spinner == null || TextUtils.isEmpty(month)) {
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {
            Object item = spinner.getItemAtPosition(i);
            if (item != null && month.equalsIgnoreCase(item.toString())) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    /**
     * Applies the summary card image based on the event category.
     *
     * @param category the event category
     */
    private void applySummaryImage(String category) {
        int backgroundRes = shouldUsePrimaryImage(category)
                ? R.drawable.bg_event_image_one
                : R.drawable.bg_event_image_two;
        eventImageBackground.setBackgroundResource(backgroundRes);
    }

    /**
     * Determines which summary image should be used for the given category.
     *
     * @param category the event category
     * @return true if the primary image should be used, otherwise false
     */
    private boolean shouldUsePrimaryImage(String category) {
        if (TextUtils.isEmpty(category)) {
            return true;
        }
        return Math.abs(category.hashCode()) % 2 == 0;
    }

    /**
     * Formats a date using the activity's date format.
     *
     * @param date the date to format
     * @return the formatted date string, or null if the date is null
     */
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return dateFormat.format(date);
    }

    /**
     * Formats a price value for display in the form.
     *
     * @param price the price to format
     * @return the formatted price string
     */
    private String formatPriceValue(Double price) {
        if (price == null) {
            return "";
        }
        if (Math.rint(price) == price) {
            return String.format(Locale.getDefault(), "%.0f", price);
        }
        return String.format(Locale.getDefault(), "%.2f", price);
    }

    /**
     * Trims a string value if it is not null.
     *
     * @param value the text to clean
     * @return the trimmed text, or null if the value is null
     */
    private String cleanText(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Returns the value if non-empty, otherwise returns the fallback text.
     *
     * @param value the primary text value
     * @param fallback the fallback text
     * @return the value or the fallback
     */
    private String defaultText(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    /**
     * Simple abstract TextWatcher with empty beforeTextChanged and onTextChanged methods.
     */
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
