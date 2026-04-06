package com.example.allot.view.event;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.allot.BuildConfig;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.controller.event.EditEventController;
import com.example.allot.controller.event.EventPosterController;
import com.example.allot.controller.shared.UserController;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.model.event.EventFormSnapshot;
import com.example.allot.view.explore.MapViewActivity;
import com.example.allot.view.lottery.RunLotteryActivity;
import com.example.allot.view.organizer.EventEntrantsActivity;
import com.example.allot.view.organizer.EventQrCodeActivity;
import com.example.allot.view.organizer.InviteCoOrganizerActivity;
import com.example.allot.view.shared.AppDialogHelper;
import com.example.allot.view.shared.AppNavigator;
import com.example.allot.view.shared.EventDisplayFormatter;
import com.example.allot.view.shared.EventFormUiHelper;
import com.example.allot.view.shared.RegistrationRangePickerView;
import com.example.allot.view.shared.SimpleTextWatcher;
import com.example.allot.view.shared.UiHelper;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.widget.PlaceAutocomplete;
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity;

/**
 * Shows the edit-event form and updates the screen as the user changes values.
 */
public class EditEventActivity extends AppCompatActivity {
    private static final String TAG = "EditEventActivity";
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
    public static final String EXTRA_UI_TEST_MODE = "ui_test_mode";
    public static final String EXTRA_UI_TEST_PRIVATE_EVENT = "ui_test_private_event";
    public static final String EXTRA_UI_TEST_EVENT_ID = "ui_test_event_id";
    public static final String EXTRA_UI_TEST_EVENT_TITLE = "ui_test_event_title";
    public static final String EXTRA_UI_TEST_EVENT_LOCATION = "ui_test_event_location";
    public static final String EXTRA_UI_TEST_EVENT_DATE = "ui_test_event_date";
    public static final String EXTRA_UI_TEST_EVENT_PRICE = "ui_test_event_price";
    public static final String EXTRA_UI_TEST_EVENT_DESCRIPTION = "ui_test_event_description";
    public static final String EXTRA_UI_TEST_EVENT_PARTICIPANTS = "ui_test_event_participants";
    public static final String EXTRA_UI_TEST_EVENT_CATEGORY = "ui_test_event_category";
    public static final String EXTRA_UI_TEST_ORGANIZER_ID = "ui_test_organizer_id";

    private EditEventController manageEventController;

    private View eventImageBackground;
    private View posterUploadCard;
    private View posterUploadPlaceholder;
    private android.widget.ImageView posterPreviewImage;
    private TextView posterUploadHintText;
    private TextView posterRemoveButton;
    private TextView entrantsLotteryButton;
    private TextView inviteEntrantsButton;
    private TextView inviteCoOrganizerButton;
    private TextView viewQrCodeButton;
    private TextView viewEntrantMapButton;
    private TextView summaryTitleText;
    private TextView summaryLocationText;
    private TextView summaryDateText;
    private EditText eventNameInput;
    private EditText locationInput;
    private CheckBox privateEventCheckbox;
    private CheckBox geolocationCheckbox;
    private Spinner startMonthSpinner;
    private EditText startDayInput;
    private EditText startYearInput;
    private EditText priceInput;
    private EditText descriptionInput;
    private EditText participantsInput;
    private RegistrationRangePickerView registrationRangePickerView;
    private TextView saveChangesButton;
    private TextView deleteEventButton;
    private EventFormUiHelper formUiHelper;
    private UserController userController;
    private String currentEventId;
    private Event currentEvent;
    private String currentCategory;
    private EventFormSnapshot originalFormSnapshot = new EventFormSnapshot("", "", "", "", "", "", "", "", false, false);
    private boolean isBindingEvent;
    private boolean isLoadingEvent;
    private boolean isSaving;
    private boolean isDeletingEvent;
    private boolean shouldRefreshOnResume;
    private Uri selectedPosterUri;
    private boolean removePosterRequested;
    private EventPosterController eventPosterController;
    private final ActivityResultLauncher<Intent> placeAutocompleteLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    AutocompletePrediction prediction = PlaceAutocomplete.getPredictionFromIntent(result.getData());
                    if (prediction != null) {
                        String selectedAddress = prediction.getFullText(null).toString();
                        locationInput.setText(selectedAddress);
                        locationInput.setSelection(selectedAddress.length());
                    }
                    return;
                }

                if (result.getResultCode() == PlaceAutocompleteActivity.RESULT_ERROR && result.getData() != null) {
                    Status status = PlaceAutocomplete.getResultStatusFromIntent(result.getData());
                    if (status != null) {
                        Log.e(TAG, "Places autocomplete failed: code=" + status.getStatusCode()
                                + ", message=" + status.getStatusMessage());
                    }
                    Toast.makeText(this, R.string.create_event_places_error, Toast.LENGTH_SHORT).show();
                }
            });
    private final ActivityResultLauncher<String> posterPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }

                selectedPosterUri = uri;
                removePosterRequested = false;
                renderPosterState();
                updateSaveButtonState();
            });

    /**
     * Handles on Create.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        manageEventController = new EditEventController(this);
        eventPosterController = new EventPosterController();
        userController = new UserController(this);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        formUiHelper.setupMonthSpinners(this);
        setupHeader();
        setupListeners();
        populateUiFromIntent();
        captureOriginalState();
        updateSaveButtonState();
        if (isUiTestMode()) {
            bindUiTestEvent();
            return;
        }
        loadEventFromFirestore();
    }

    /**
     * Handles finish.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Binds views.
     */
    private void bindViews() {
        eventImageBackground = findViewById(R.id.eventImageBackground);
        posterUploadCard = findViewById(R.id.posterUploadCard);
        posterUploadPlaceholder = findViewById(R.id.posterUploadPlaceholder);
        posterPreviewImage = findViewById(R.id.posterPreviewImage);
        posterUploadHintText = findViewById(R.id.posterUploadHintText);
        posterRemoveButton = findViewById(R.id.posterRemoveButton);
        entrantsLotteryButton = findViewById(R.id.entrantsLotteryButton);
        inviteEntrantsButton = findViewById(R.id.inviteEntrantsButton);
        inviteCoOrganizerButton = findViewById(R.id.inviteCoOrganizerButton);
        viewQrCodeButton = findViewById(R.id.viewQrCodeButton);
        viewEntrantMapButton = findViewById(R.id.viewEntrantMapButton);
        summaryTitleText = findViewById(R.id.summaryTitleText);
        summaryLocationText = findViewById(R.id.summaryLocationText);
        summaryDateText = findViewById(R.id.summaryDateText);
        eventNameInput = findViewById(R.id.eventNameInput);
        locationInput = findViewById(R.id.locationInput);
        privateEventCheckbox = findViewById(R.id.privateEventCheckbox);
        geolocationCheckbox = findViewById(R.id.geolocationCheckbox);
        startMonthSpinner = findViewById(R.id.startMonthSpinner);
        startDayInput = findViewById(R.id.startDayInput);
        startYearInput = findViewById(R.id.startYearInput);
        priceInput = findViewById(R.id.priceInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        participantsInput = findViewById(R.id.participantsInput);
        registrationRangePickerView = findViewById(R.id.registrationRangePickerView);
        saveChangesButton = findViewById(R.id.saveChangesButton);
        deleteEventButton = findViewById(R.id.deleteEventButton);
        formUiHelper = new EventFormUiHelper(
                eventNameInput,
                locationInput,
                privateEventCheckbox,
                geolocationCheckbox,
                startMonthSpinner,
                startDayInput,
                startYearInput,
                priceInput,
                descriptionInput,
                participantsInput,
                registrationRangePickerView
        );
    }

    /**
     * Updates up header.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Updates up listeners.
     */
    private void setupListeners() {
        SimpleTextWatcher dirtyStateWatcher = new SimpleTextWatcher() {
            /**
             * Handles after Text Changed.
             */
            @Override
            public void afterTextChanged(Editable editable) {
                if (!isBindingEvent) {
                    updateSaveButtonState();
                }
            }
        };

        eventNameInput.addTextChangedListener(dirtyStateWatcher);
        locationInput.addTextChangedListener(dirtyStateWatcher);
        configureLocationPicker();
        priceInput.addTextChangedListener(dirtyStateWatcher);
        descriptionInput.addTextChangedListener(dirtyStateWatcher);
        participantsInput.addTextChangedListener(dirtyStateWatcher);
        startDayInput.addTextChangedListener(dirtyStateWatcher);
        startYearInput.addTextChangedListener(dirtyStateWatcher);
        geolocationCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isBindingEvent) {
                updateSaveButtonState();
            }
        });
        if (privateEventCheckbox != null) {
            privateEventCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isBindingEvent) {
                    updateSaveButtonState();
                }
            });
        }

        /**
         * Handles on Item Selected Listener.
         */
        AdapterView.OnItemSelectedListener dateSelectionListener = new AdapterView.OnItemSelectedListener() {
            /**
             * Handles on Item Selected.
             */
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isBindingEvent) {
                    updateSaveButtonState();
                }
            }

            /**
             * Handles on Nothing Selected.
             */
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };

        startMonthSpinner.setOnItemSelectedListener(dateSelectionListener);
        if (registrationRangePickerView != null) {
            registrationRangePickerView.setOnRangeChangedListener((startDate, endDate) -> {
                if (!isBindingEvent) {
                    updateSaveButtonState();
                }
            });
        }

        entrantsLotteryButton.setOnClickListener(view -> openLotteryScreen());
        inviteEntrantsButton.setOnClickListener(view -> openInviteScreen());
        inviteCoOrganizerButton.setOnClickListener(view -> openInviteCoOrganizerScreen());
        viewEntrantMapButton.setOnClickListener(view -> openEntrantMapScreen());
        posterUploadCard.setOnClickListener(view -> posterPickerLauncher.launch("image/*"));
        posterRemoveButton.setOnClickListener(view -> {
            selectedPosterUri = null;
            removePosterRequested = true;
            renderPosterState();
            updateSaveButtonState();
        });
        viewQrCodeButton.setOnClickListener(view -> openQrCodeScreen());
        saveChangesButton.setOnClickListener(view -> saveChanges());
        if (deleteEventButton != null) {
            deleteEventButton.setOnClickListener(view -> showDeleteEventDialog());
        }
        renderPosterState();
    }

    /**
     * Handles configure Location Picker.
     */
    private void configureLocationPicker() {
        locationInput.setKeyListener(null);
        locationInput.setFocusable(false);
        locationInput.setCursorVisible(false);
        locationInput.setOnClickListener(view -> openPlaceAutocomplete());
    }

    /**
     * Opens place autocomplete.
     */
    private void openPlaceAutocomplete() {
        if (!ensurePlacesInitialized()) {
            Toast.makeText(this, R.string.create_event_places_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new PlaceAutocomplete.IntentBuilder().build(this);
        placeAutocompleteLauncher.launch(intent);
    }

    /**
     * Handles ensure Places Initialized.
     */
    private boolean ensurePlacesInitialized() {
        if (TextUtils.isEmpty(BuildConfig.PLACES_API_KEY)) {
            return false;
        }

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), BuildConfig.PLACES_API_KEY);
        }
        return true;
    }

    /**
     * Handles populate Ui From Intent.
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
     * Returns whether i.s Ui Test Mode
     */
    private boolean isUiTestMode() {
        return getIntent().getBooleanExtra(EXTRA_UI_TEST_MODE, false);
    }

    /**
     * Binds ui test event.
     */
    private void bindUiTestEvent() {
        String title = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_TITLE), "UI Test Event");
        String location = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_LOCATION), "Test Location");
        String dateText = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_DATE), "Apr 12");
        String price = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_PRICE), "0");
        String description = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_DESCRIPTION), "Test description");
        String participants = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_PARTICIPANTS), "20");
        String category = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_CATEGORY), "General");
        String eventId = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_EVENT_ID), "ui-test-event");
        boolean isPrivateEvent = getIntent().getBooleanExtra(EXTRA_UI_TEST_PRIVATE_EVENT, false);
        String organizerId = safeString(getIntent().getStringExtra(EXTRA_UI_TEST_ORGANIZER_ID),
                userController == null ? "" : userController.getCurrentDeviceId());

        EventFormData viewModel = EventFormData.forBinding(
                title,
                location,
                isPrivateEvent,
                false,
                "",
                "",
                "",
                price,
                description,
                participants,
                "",
                "",
                "",
                "",
                "",
                ""
        );
        bindFormViewModel(viewModel);
        updateSummary(title, location, dateText, category);

        Event event = new Event();
        event.setEventId(eventId);
        event.setOrganizerId(organizerId);
        event.setTitle(title);
        event.setLocation(location);
        event.setDescription(description);
        event.setCategory(category);
        event.setCapacity(parseParticipants(participants));
        event.setVisibility(isPrivateEvent ? Event.VISIBILITY_PRIVATE : Event.VISIBILITY_PUBLIC);

        currentEvent = event;
        currentCategory = category;
        updateInviteButtonVisibility(event);
        updateDeleteButtonVisibility(event);
        renderPosterState();

        originalFormSnapshot = manageEventController.buildSnapshot(readFormData());
        updateSaveButtonState();
    }

    /**
     * Handles parse Participants.
     */
    private int parseParticipants(String participants) {
        if (TextUtils.isEmpty(participants)) {
            return 0;
        }
        try {
            return Integer.parseInt(participants.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    /**
     * Handles safe String.
     */
    private String safeString(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    /**
     * Loads event from firestore.
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

    /**
     * Binds event.
     */
    private void bindEvent(Event event) {
        if (event == null) {
            return;
        }

        isBindingEvent = true;
        currentEvent = event;
        currentCategory = UiHelper.cleanText(event.getCategory());
        selectedPosterUri = null;
        removePosterRequested = false;

        EventFormData viewModel = manageEventController.buildViewModel(event);
        bindFormViewModel(viewModel);
        updateSummary(viewModel.getTitle(), viewModel.getLocation(), manageEventController.buildSummaryDate(readFormData()), currentCategory);
        updateInviteButtonVisibility(event);
        updateDeleteButtonVisibility(event);
        renderPosterState();

        originalFormSnapshot = manageEventController.buildSnapshot(readFormData());

        isBindingEvent = false;
        updateSaveButtonState();
    }

    /**
     * Opens lottery screen.
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
     * Opens invite co organizer screen.
     */
    private void openInviteCoOrganizerScreen() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, InviteCoOrganizerActivity.class);
        intent.putExtra(InviteCoOrganizerActivity.EXTRA_EVENT_ID, currentEventId);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Opens qr code screen.
     */
    private void openQrCodeScreen() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.event_qr_generation_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, EventQrCodeActivity.class);
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_ID, currentEventId);
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_TITLE, summaryTitleText.getText().toString());
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_LOCATION, summaryLocationText.getText().toString());
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_DATE, summaryDateText.getText().toString());
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_PRICE, priceInput.getText().toString());
        intent.putExtra(
                EventCreatedActivity.EXTRA_EVENT_DEADLINE,
                currentEvent != null
                        ? EventDisplayFormatter.deadline(currentEvent)
                        : getIntent().getStringExtra(EXTRA_REGISTRATION_END)
        );
        intent.putExtra(EventCreatedActivity.EXTRA_EVENT_CATEGORY, currentCategory);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Opens entrant map screen.
     */
    private void openEntrantMapScreen() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_entrants_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, MapViewActivity.class);
        intent.putExtra(MapViewActivity.EXTRA_EVENT_ID, currentEventId);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Updates delete button visibility.
     */
    private void updateDeleteButtonVisibility(Event event) {
        if (deleteEventButton == null || userController == null) {
            return;
        }

        String deviceId = userController.getCurrentDeviceId();
        boolean isOrganizer = event != null
                && deviceId != null
                && deviceId.equals(event.getOrganizerId());
        deleteEventButton.setVisibility(isOrganizer ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows delete event dialog.
     */
    private void showDeleteEventDialog() {
        if (isDeletingEvent || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        Dialog dialog = AppDialogHelper.createDialog(this, R.layout.dialog_admin_delete_event, true);
        TextView titleText = dialog.findViewById(R.id.dialogTitle);
        TextView messageText = dialog.findViewById(R.id.dialogMessage);
        TextView eventTitleText = dialog.findViewById(R.id.eventTitleText);
        Button cancelButton = dialog.findViewById(R.id.cancelButton);
        Button deleteButton = dialog.findViewById(R.id.deleteEventButton);

        if (titleText != null) {
            titleText.setText(R.string.manage_event_delete_title);
        }
        if (messageText != null) {
            messageText.setText(R.string.manage_event_delete_message);
        }
        if (eventTitleText != null) {
            eventTitleText.setText(currentEvent == null ? "" : currentEvent.getTitle());
        }

        if (cancelButton != null) {
            cancelButton.setText(R.string.manage_event_delete_cancel);
            cancelButton.setOnClickListener(view -> dialog.dismiss());
        }
        if (deleteButton != null) {
            deleteButton.setText(R.string.manage_event_delete_confirm);
            deleteButton.setOnClickListener(view -> confirmDeleteEvent(dialog, cancelButton, deleteButton));
        }

        AppDialogHelper.showWrapContent(dialog, UiHelper.dpToPx(this, 320));
    }

    /**
     * Handles confirm Delete Event.
     */
    private void confirmDeleteEvent(Dialog dialog, Button cancelButton, Button deleteButton) {
        if (isDeletingEvent || TextUtils.isEmpty(currentEventId) || userController == null) {
            return;
        }

        isDeletingEvent = true;
        if (cancelButton != null) {
            cancelButton.setEnabled(false);
        }
        if (deleteButton != null) {
            deleteButton.setEnabled(false);
        }

        String organizerId = userController.getCurrentDeviceId();
        manageEventController.deleteEvent(currentEventId, organizerId, (result, success) -> {
            isDeletingEvent = false;
            if (result == null || !result.isSuccess()) {
                if (cancelButton != null) {
                    cancelButton.setEnabled(true);
                }
                if (deleteButton != null) {
                    deleteButton.setEnabled(true);
                }
                Toast.makeText(this, R.string.manage_event_delete_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            Toast.makeText(this, R.string.manage_event_delete_success, Toast.LENGTH_SHORT).show();
            AppNavigator.openMyEventsHosting(this, true);
        });
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
     * Saves changes.
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

        boolean formChanged = !manageEventController.buildSnapshot(readFormData()).equals(originalFormSnapshot);
        if (!formChanged) {
            applyPosterChangesIfNeeded(null, true);
            return;
        }

        manageEventController.saveChanges(currentEventId, readFormData(), (AppResult<Event> result, boolean success) -> {
            if (result == null) {
                isSaving = false;
                updateSaveButtonState();
                Toast.makeText(this, R.string.manage_event_save_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!result.isSuccess() || result.getData() == null) {
                isSaving = false;
                updateSaveButtonState();
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                return;
            }

            applyPosterChangesIfNeeded(result, false);
        });
    }

    /**
     * Handles apply Poster Changes If Needed.
     */
    private void applyPosterChangesIfNeeded(AppResult<Event> formResult, boolean posterOnly) {
        if (selectedPosterUri == null && !removePosterRequested) {
            isSaving = false;
            updateSaveButtonState();
            if (posterOnly) {
                Toast.makeText(this, R.string.manage_event_save_success, Toast.LENGTH_SHORT).show();
                return;
            }
            reloadEventAfterSave(formResult);
            return;
        }

        if (removePosterRequested) {
            String currentPosterUrl = currentEvent == null ? null : currentEvent.getPosterUrl();
            eventPosterController.deletePoster(currentEventId, currentPosterUrl, (deleted, success) -> {
                isSaving = false;
                if (!success || deleted == null || !deleted) {
                    updateSaveButtonState();
                    Toast.makeText(this, R.string.event_poster_upload_failure, Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, R.string.event_poster_upload_success, Toast.LENGTH_SHORT).show();
                loadEventFromFirestore();
            });
            return;
        }

        eventPosterController.uploadPoster(currentEventId, selectedPosterUri, (posterUrl, success) -> {
            isSaving = false;
            if (!success) {
                updateSaveButtonState();
                Toast.makeText(this, R.string.event_poster_upload_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, R.string.event_poster_upload_success, Toast.LENGTH_SHORT).show();
            loadEventFromFirestore();
        });
    }

    /**
     * Handles reload Event After Save.
     */
    private void reloadEventAfterSave(AppResult<Event> result) {
        bindEvent(result.getData());
        setResult(RESULT_OK);
        Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
    }

    /**
     * Handles capture Original State.
     */
    private void captureOriginalState() {
        originalFormSnapshot = manageEventController.buildSnapshot(readFormData());
    }

    /**
     * Returns whether h.as Unsaved Changes
     */
    private boolean hasUnsavedChanges() {
        return !manageEventController.buildSnapshot(readFormData()).equals(originalFormSnapshot)
                || selectedPosterUri != null
                || removePosterRequested;
    }

    /**
     * Updates save button state.
     */
    private void updateSaveButtonState() {
        boolean formSaveEnabled = manageEventController.isSaveEnabled(
                readFormData(),
                originalFormSnapshot,
                isSaving,
                isLoadingEvent
        );
        boolean isPosterPending = selectedPosterUri != null || removePosterRequested;
        int color = (formSaveEnabled || (!isSaving && !isLoadingEvent && isPosterPending))
                ? SAVE_ACTIVE_COLOR
                : SAVE_INACTIVE_COLOR;
        saveChangesButton.setBackgroundTintList(ColorStateList.valueOf(color));
        saveChangesButton.setEnabled(!isSaving && !isLoadingEvent);
    }

    /**
     * Handles render Poster State.
     */
    private void renderPosterState() {
        String currentPosterUrl = currentEvent == null ? null : currentEvent.getPosterUrl();
        boolean hasSelectedPoster = selectedPosterUri != null;
        boolean hasExistingPoster = !TextUtils.isEmpty(currentPosterUrl) && !removePosterRequested;

        if (hasSelectedPoster) {
            posterPreviewImage.setVisibility(View.VISIBLE);
            posterUploadPlaceholder.setVisibility(View.GONE);
            posterRemoveButton.setVisibility(View.VISIBLE);
            posterUploadHintText.setText(R.string.manage_event_banner_change);
            Glide.with(this).load(selectedPosterUri).into(posterPreviewImage);
            return;
        }

        if (hasExistingPoster) {
            posterPreviewImage.setVisibility(View.VISIBLE);
            posterUploadPlaceholder.setVisibility(View.GONE);
            posterRemoveButton.setVisibility(View.VISIBLE);
            posterUploadHintText.setText(R.string.manage_event_banner_change);
            Glide.with(this).load(currentPosterUrl).into(posterPreviewImage);
            return;
        }

        posterPreviewImage.setVisibility(View.GONE);
        posterPreviewImage.setImageDrawable(null);
        posterUploadPlaceholder.setVisibility(View.VISIBLE);
        posterRemoveButton.setVisibility(View.GONE);
        posterUploadHintText.setText(R.string.manage_event_banner_placeholder);
    }

    /**
     * Handles read Form Data.
     */
    private EventFormData readFormData() {
        return formUiHelper.readFormData();
    }

    /**
     * Handles apply Summary Image.
     */
    private void applySummaryImage(String category) {
        eventImageBackground.setBackgroundResource(UiHelper.eventImageBackgroundRes(category));
    }

    /**
     * Binds form view model.
     */
    private void bindFormViewModel(EventFormData viewModel) {
        formUiHelper.bindForm(viewModel);
    }

    /**
     * Updates summary.
     */
    private void updateSummary(String title, String location, String date, String category) {
        summaryTitleText.setText(UiHelper.defaultText(title, getString(R.string.default_event_name)));
        summaryLocationText.setText(UiHelper.defaultText(location, getString(R.string.default_street_name)));
        summaryDateText.setText(UiHelper.defaultText(date, getString(R.string.default_date)));
        applySummaryImage(category);
    }

    /**
     * Updates invite button visibility.
     */
    private void updateInviteButtonVisibility(Event event) {
        if (inviteEntrantsButton != null) {
            inviteEntrantsButton.setVisibility(event != null && event.isPrivate() ? View.VISIBLE : View.GONE);
        }
        if (inviteCoOrganizerButton != null) {
            inviteCoOrganizerButton.setVisibility(event != null ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Opens invite screen.
     */
    private void openInviteScreen() {
        if (TextUtils.isEmpty(currentEventId)) {
            Toast.makeText(this, R.string.manage_event_load_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new android.content.Intent(this, com.example.allot.view.organizer.InviteEntrantActivity.class)
                .putExtra(com.example.allot.view.organizer.InviteEntrantActivity.EXTRA_EVENT_ID, currentEventId));
        overridePendingTransition(0, 0);
    }
}
