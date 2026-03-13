package com.example.allot.view;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ManageEventActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        dateFormat.setLenient(false);

        bindViews();
        setupMonthSpinner(startMonthSpinner);
        setupMonthSpinner(registrationStartMonthSpinner);
        setupMonthSpinner(registrationEndMonthSpinner);
        setupHeader();
        setupBottomNav();
        populateUiFromIntent();
        setupListeners();
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
        saveChangesButton.setOnClickListener(view ->
                Toast.makeText(this, R.string.manage_event_save_stub, Toast.LENGTH_SHORT).show());
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
        String title = cleanText(getIntent().getStringExtra(EXTRA_EVENT_TITLE));
        String location = cleanText(getIntent().getStringExtra(EXTRA_EVENT_LOCATION));
        String eventDate = cleanText(getIntent().getStringExtra(EXTRA_EVENT_DATE));
        String price = cleanText(getIntent().getStringExtra(EXTRA_EVENT_PRICE));
        String description = cleanText(getIntent().getStringExtra(EXTRA_EVENT_DESCRIPTION));
        String participants = cleanText(getIntent().getStringExtra(EXTRA_EVENT_PARTICIPANTS));
        String registrationStart = cleanText(getIntent().getStringExtra(EXTRA_REGISTRATION_START));
        String registrationEnd = cleanText(getIntent().getStringExtra(EXTRA_REGISTRATION_END));
        String category = cleanText(getIntent().getStringExtra(EXTRA_EVENT_CATEGORY));

        summaryTitleText.setText(defaultText(title, getString(R.string.default_event_name)));
        summaryLocationText.setText(defaultText(location, getString(R.string.default_street_name)));
        summaryDateText.setText(defaultText(eventDate, getString(R.string.default_date)));
        eventNameInput.setText(title);
        locationInput.setText(location);
        priceInput.setText(price);
        descriptionInput.setText(description);
        participantsInput.setText(participants);
        applySummaryImage(category);
        populateDateFields(eventDate, startMonthSpinner, startDayInput, startYearInput);
        populateDateFields(registrationStart, registrationStartMonthSpinner, registrationStartDayInput, registrationStartYearInput);
        populateDateFields(registrationEnd, registrationEndMonthSpinner, registrationEndDayInput, registrationEndYearInput);
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

    private String cleanText(String value) {
        return value == null ? null : value.trim();
    }

    private String defaultText(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }
}
