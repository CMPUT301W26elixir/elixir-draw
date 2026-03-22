package com.example.allot.view.event;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.controller.event.EditEventController;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.model.event.EventFormSnapshot;
import com.example.allot.view.lottery.RunLotteryActivity;
import com.example.allot.view.organizer.EventEntrantsActivity;
import com.example.allot.view.shared.EventFormUiHelper;
import com.example.allot.view.shared.SimpleTextWatcher;
import com.example.allot.view.shared.UiHelper;
/**
 * Shows the edit-event form and updates the screen as the user changes values.
 */
public class EditEventActivity extends AppCompatActivity {
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

    private EditEventController manageEventController;

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
    private EventFormUiHelper formUiHelper;
    private String currentEventId;
    private Event currentEvent;
    private String currentCategory;
    private EventFormSnapshot originalFormSnapshot = new EventFormSnapshot("", "", "", "", "", "", "", "", false);
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

        manageEventController = new EditEventController();
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        formUiHelper.setupMonthSpinners(this);
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
        formUiHelper = new EventFormUiHelper(
                eventNameInput,
                locationInput,
                geolocationCheckbox,
                startMonthSpinner,
                startDayInput,
                startYearInput,
                priceInput,
                descriptionInput,
                participantsInput,
                registrationStartMonthSpinner,
                registrationStartDayInput,
                registrationStartYearInput,
                registrationEndMonthSpinner,
                registrationEndDayInput,
                registrationEndYearInput
        );
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
        SimpleTextWatcher dirtyStateWatcher = new SimpleTextWatcher() {
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
     * Populates the UI using values passed through the launching intent.
     */
    private void populateUiFromIntent() {
        currentCategory = UiHelper.cleanText(getIntent().getStringExtra(EXTRA_EVENT_CATEGORY));
        EventFormData fallbackViewModel = manageEventController.buildFallbackViewModel(
                UiHelper.cleanText(getIntent().getStringExtra(EXTRA_EVENT_TITLE)),
                UiHelper.cleanText(getIntent().getStringExtra(EXTRA_EVENT_LOCATION)),
                UiHelper.cleanText(getIntent().getStringExtra(EXTRA_EVENT_DATE)),
                UiHelper.cleanText(getIntent().getStringExtra(EXTRA_EVENT_PRICE)),
                UiHelper.cleanText(getIntent().getStringExtra(EXTRA_EVENT_DESCRIPTION)),
                UiHelper.cleanText(getIntent().getStringExtra(EXTRA_EVENT_PARTICIPANTS)),
                UiHelper.cleanText(getIntent().getStringExtra(EXTRA_REGISTRATION_START)),
                UiHelper.cleanText(getIntent().getStringExtra(EXTRA_REGISTRATION_END))
        );
        bindFormViewModel(fallbackViewModel);
        updateSummary(
                getIntent().getStringExtra(EXTRA_EVENT_TITLE),
                getIntent().getStringExtra(EXTRA_EVENT_LOCATION),
                getIntent().getStringExtra(EXTRA_EVENT_DATE),
                currentCategory
        );
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
        manageEventController.loadEvent(currentEventId, (event, success) -> {
            isLoadingEvent = false;
            if (!success || event == null) {
                updateSaveButtonState();
                Toast.makeText(EditEventActivity.this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            bindEvent(event);
        });
    }

    private void bindEvent(Event event) {
        if (event == null) {
            return;
        }

        isBindingEvent = true;
        currentEvent = event;
        currentCategory = UiHelper.cleanText(event.getCategory());

        EventFormData viewModel = manageEventController.buildViewModel(event);
        bindFormViewModel(viewModel);
        updateSummary(viewModel.getTitle(), viewModel.getLocation(), manageEventController.buildSummaryDate(readFormData()), currentCategory);

        originalFormSnapshot = manageEventController.buildSnapshot(readFormData());

        isBindingEvent = false;
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
        Class<?> destination = manageEventController.shouldOpenEntrantsScreen(currentEvent)
                ? EventEntrantsActivity.class
                : RunLotteryActivity.class;
        startActivity(new android.content.Intent(this, destination)
                .putExtra(RunLotteryActivity.EXTRA_EVENT_ID, currentEventId));
        overridePendingTransition(0, 0);
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

        isSaving = true;
        updateSaveButtonState();
        manageEventController.saveChanges(currentEventId, readFormData(), (AppResult<Event> result, boolean success) -> {
            isSaving = false;
            if (result == null) {
                updateSaveButtonState();
                Toast.makeText(this, R.string.manage_event_save_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!result.isSuccess() || result.getData() == null) {
                updateSaveButtonState();
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                return;
            }

            reloadEventAfterSave(result);
        });
    }

    /**
     * Reloads the event after a successful save so the UI reflects the latest values.
     */
    private void reloadEventAfterSave(AppResult<Event> result) {
        bindEvent(result.getData());
        setResult(RESULT_OK);
        Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Captures the current form state so unsaved changes can be detected.
     */
    private void captureOriginalState() {
        originalFormSnapshot = manageEventController.buildSnapshot(readFormData());
    }

    /**
     * Checks whether the current form differs from the original loaded state.
     *
     * @return true if unsaved changes exist, otherwise false
     */
    private boolean hasUnsavedChanges() {
        return !manageEventController.buildSnapshot(readFormData()).equals(originalFormSnapshot);
    }

    /**
     * Updates the save button color and enabled state based on form and loading state.
     */
    private void updateSaveButtonState() {
        int color = manageEventController.isSaveEnabled(
                readFormData(),
                originalFormSnapshot,
                isSaving,
                isLoadingEvent
        )
                ? SAVE_ACTIVE_COLOR
                : SAVE_INACTIVE_COLOR;
        saveChangesButton.setBackgroundTintList(ColorStateList.valueOf(color));
        saveChangesButton.setEnabled(!isSaving && !isLoadingEvent);
    }

    private EventFormData readFormData() {
        return formUiHelper.readFormData();
    }

    /**
     * Applies the summary card image based on the event category.
     *
     * @param category the event category
     */
    private void applySummaryImage(String category) {
        eventImageBackground.setBackgroundResource(UiHelper.eventImageBackgroundRes(category));
    }

    /**
     * Binds the form values from the provided view model.
     *
     * @param viewModel the form values to display
     */
    private void bindFormViewModel(EventFormData viewModel) {
        formUiHelper.bindForm(viewModel);
    }

    private void updateSummary(String title, String location, String date, String category) {
        summaryTitleText.setText(UiHelper.defaultText(title, getString(R.string.default_event_name)));
        summaryLocationText.setText(UiHelper.defaultText(location, getString(R.string.default_street_name)));
        summaryDateText.setText(UiHelper.defaultText(date, getString(R.string.default_date)));
        applySummaryImage(category);
    }
}









