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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.util.Locale;

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
    private FirebaseFirestore database;

    private BottomNavBarView bottomNavBar;
    private View eventImageBackground;
    private TextView summaryTitleText;
    private TextView summaryLocationText;
    private TextView summaryDateText;
    private EditText eventNameInput;
    private EditText locationInput;
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
    private String currentCategory;
    private String originalTitle = "";
    private String originalLocation = "";
    private String originalPrice = "";
    private String originalDescription = "";
    private String originalParticipants = "";
    private String originalEventDate = "";
    private String originalRegistrationStart = "";
    private String originalRegistrationEnd = "";
    private boolean isBindingEvent;
    private boolean isLoadingEvent;
    private boolean isSaving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        dateFormat.setLenient(false);
        database = FirebaseFirestore.getInstance();
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        setupMonthSpinner(startMonthSpinner);
        setupMonthSpinner(registrationStartMonthSpinner);
        setupMonthSpinner(registrationEndMonthSpinner);
        setupHeader();
        setupBottomNav();
        setupListeners();
        populateUiFromIntent();
        captureOriginalState();
        updateSaveButtonState();
        loadEventFromFirestore();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void bindViews() {
        bottomNavBar = findViewById(R.id.bottomNavBar);
        eventImageBackground = findViewById(R.id.eventImageBackground);
        summaryTitleText = findViewById(R.id.summaryTitleText);
        summaryLocationText = findViewById(R.id.summaryLocationText);
        summaryDateText = findViewById(R.id.summaryDateText);
        eventNameInput = findViewById(R.id.eventNameInput);
        locationInput = findViewById(R.id.locationInput);
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

    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupBottomNav() {
        bottomNavBar.setSelectedTab(BottomNavBarView.Tab.MY_EVENTS);
    }

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

        saveChangesButton.setOnClickListener(view -> saveChanges());
    }

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
        applySummaryImage(currentCategory);
        populateDateFields(eventDate, startMonthSpinner, startDayInput, startYearInput);
        populateDateFields(registrationStart, registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput);
        populateDateFields(registrationEnd, registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput);

        isBindingEvent = false;
    }

    private void loadEventFromFirestore() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        isLoadingEvent = true;
        updateSaveButtonState();
        database.collection("events")
                .document(currentEventId)
                .get()
                .addOnSuccessListener(this::bindEventSnapshot)
                .addOnFailureListener(exception -> {
                    isLoadingEvent = false;
                    updateSaveButtonState();
                    Toast.makeText(this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
                });
    }

    private void bindEventSnapshot(DocumentSnapshot documentSnapshot) {
        isLoadingEvent = false;
        if (documentSnapshot == null || !documentSnapshot.exists()) {
            updateSaveButtonState();
            Toast.makeText(this, R.string.manage_event_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = documentSnapshot.toObject(Event.class);
        if (event == null) {
            updateSaveButtonState();
            Toast.makeText(this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(event.eventId)) {
            event.eventId = documentSnapshot.getId();
        }

        bindEvent(event);
    }

    private void bindEvent(Event event) {
        isBindingEvent = true;

        currentCategory = cleanText(event.category);
        summaryTitleText.setText(defaultText(cleanText(event.title), getString(R.string.default_event_name)));
        summaryLocationText.setText(defaultText(cleanText(event.location), getString(R.string.default_street_name)));
        summaryDateText.setText(defaultText(formatDate(event.eventDate), getString(R.string.default_date)));

        eventNameInput.setText(cleanText(event.title));
        locationInput.setText(cleanText(event.location));
        priceInput.setText(event.price == null ? "" : formatPriceValue(event.price));
        descriptionInput.setText(cleanText(event.description));

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

    private void clearDateFields(Spinner monthSpinner, EditText dayInput, EditText yearInput) {
        monthSpinner.setSelection(0);
        dayInput.setText("");
        yearInput.setText("");
    }

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

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("location", location);
        updates.put("eventDate", eventDate);
        updates.put("price", price);
        updates.put("description", description);
        updates.put("capacity", participants);
        updates.put("limit", participants);
        updates.put("registrationOpen", registrationStart);
        updates.put("registrationDeadline", registrationEnd);

        isSaving = true;
        updateSaveButtonState();
        database.collection("events")
                .document(currentEventId)
                .update(updates)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        isSaving = false;
                        updateSaveButtonState();
                        Toast.makeText(this, R.string.manage_event_save_failure, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    reloadEventAfterSave();
                });
    }

    private void reloadEventAfterSave() {
        database.collection("events")
                .document(currentEventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    isSaving = false;
                    bindEventSnapshot(documentSnapshot);
                    setResult(RESULT_OK);
                    Toast.makeText(this, R.string.manage_event_save_success, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(exception -> {
                    isSaving = false;
                    updateSaveButtonState();
                    Toast.makeText(this, R.string.manage_event_save_failure, Toast.LENGTH_SHORT).show();
                });
    }

    private void captureOriginalState() {
        originalTitle = readText(eventNameInput);
        originalLocation = readText(locationInput);
        originalPrice = readText(priceInput);
        originalDescription = readText(descriptionInput);
        originalParticipants = readText(participantsInput);
        originalEventDate = currentDateValue(startMonthSpinner, startDayInput, startYearInput);
        originalRegistrationStart = currentDateValue(registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput);
        originalRegistrationEnd = currentDateValue(registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput);
    }

    private boolean hasUnsavedChanges() {
        return !readText(eventNameInput).equals(originalTitle)
                || !readText(locationInput).equals(originalLocation)
                || !readText(priceInput).equals(originalPrice)
                || !readText(descriptionInput).equals(originalDescription)
                || !readText(participantsInput).equals(originalParticipants)
                || !currentDateValue(startMonthSpinner, startDayInput, startYearInput).equals(originalEventDate)
                || !currentDateValue(registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput).equals(originalRegistrationStart)
                || !currentDateValue(registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput).equals(originalRegistrationEnd);
    }

    private void updateSaveButtonState() {
        int color = hasUnsavedChanges() && !isSaving && !isLoadingEvent
                ? SAVE_ACTIVE_COLOR
                : SAVE_INACTIVE_COLOR;
        saveChangesButton.setBackgroundTintList(ColorStateList.valueOf(color));
        saveChangesButton.setEnabled(!isSaving && !isLoadingEvent);
    }

    private boolean isDateInputComplete(Spinner monthSpinner, EditText dayInput, EditText yearInput) {
        return monthSpinner.getSelectedItemPosition() > 0
                && !isBlank(readText(dayInput))
                && !isBlank(readText(yearInput));
    }

    private String readText(EditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private boolean isBlank(String value) {
        return TextUtils.isEmpty(value);
    }

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

    private Double parsePrice(String value) {
        try {
            return Double.parseDouble(value.replace("$", "").trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String currentDateValue(Spinner monthSpinner, EditText dayInput, EditText yearInput) {
        Date date = parseDate(monthSpinner, dayInput, yearInput);
        return date == null ? "" : formatDate(date);
    }

    private Integer parsePositiveInt(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

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

    private void applySummaryImage(String category) {
        int backgroundRes = shouldUsePrimaryImage(category)
                ? R.drawable.bg_event_image_one
                : R.drawable.bg_event_image_two;
        eventImageBackground.setBackgroundResource(backgroundRes);
    }

    private boolean shouldUsePrimaryImage(String category) {
        if (TextUtils.isEmpty(category)) {
            return true;
        }
        return Math.abs(category.hashCode()) % 2 == 0;
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return dateFormat.format(date);
    }

    private String formatPriceValue(Double price) {
        if (price == null) {
            return "";
        }
        if (Math.rint(price) == price) {
            return String.format(Locale.getDefault(), "%.0f", price);
        }
        return String.format(Locale.getDefault(), "%.2f", price);
    }

    private String cleanText(String value) {
        return value == null ? null : value.trim();
    }

    private String defaultText(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }
    }
}
