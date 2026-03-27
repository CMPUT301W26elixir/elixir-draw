package com.example.allot.view.event;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.BuildConfig;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.controller.event.CreateEventController;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.view.shared.EventDisplayFormatter;
import com.example.allot.view.shared.EventFormUiHelper;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.widget.PlaceAutocomplete;
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity;

/**
 * Shows the form for creating a new event and forwards user actions to the controller.
 */
public class CreateEventActivity extends AppCompatActivity {
    private static final String TAG = "CreateEventActivity";

    private TextView nextButton;
    private EditText locationInput;

    private CreateEventController createEventController;
    private EventFormUiHelper formUiHelper;
    private boolean isSaving;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        createEventController = new CreateEventController(this);

        bindViews();
        formUiHelper.setupMonthSpinners(this);
        setupHeader();
        setupListeners();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void bindViews() {
        EditText eventNameInput = findViewById(R.id.eventNameInput);
        locationInput = findViewById(R.id.locationInput);
        CheckBox privateEventCheckbox = findViewById(R.id.privateEventCheckbox);
        CheckBox geolocationCheckbox = findViewById(R.id.geolocationCheckbox);
        Spinner startMonthSpinner = findViewById(R.id.startMonthSpinner);
        EditText startDayInput = findViewById(R.id.startDayInput);
        EditText startYearInput = findViewById(R.id.startYearInput);
        EditText priceInput = findViewById(R.id.priceInput);
        EditText descriptionInput = findViewById(R.id.descriptionInput);
        EditText participantsInput = findViewById(R.id.participantsInput);
        Spinner registrationStartMonthSpinner = findViewById(R.id.registrationStartMonthSpinner);
        EditText registrationStartDayInput = findViewById(R.id.registrationStartDayInput);
        EditText registrationStartYearInput = findViewById(R.id.registrationStartYearInput);
        Spinner registrationEndMonthSpinner = findViewById(R.id.registrationEndMonthSpinner);
        EditText registrationEndDayInput = findViewById(R.id.registrationEndDayInput);
        EditText registrationEndYearInput = findViewById(R.id.registrationEndYearInput);
        nextButton = findViewById(R.id.createEventNextButton);
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
                registrationStartMonthSpinner,
                registrationStartDayInput,
                registrationStartYearInput,
                registrationEndMonthSpinner,
                registrationEndDayInput,
                registrationEndYearInput
        );
    }

    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    private void setupListeners() {
        configureLocationPicker();
        nextButton.setOnClickListener(view -> submitEvent());
    }

    private void configureLocationPicker() {
        locationInput.setKeyListener(null);
        locationInput.setFocusable(false);
        locationInput.setCursorVisible(false);
        locationInput.setOnClickListener(view -> openPlaceAutocomplete());
    }

    private void openPlaceAutocomplete() {
        if (!ensurePlacesInitialized()) {
            Toast.makeText(this, R.string.create_event_places_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new PlaceAutocomplete.IntentBuilder().build(this);
        placeAutocompleteLauncher.launch(intent);
    }

    private boolean ensurePlacesInitialized() {
        if (TextUtils.isEmpty(BuildConfig.PLACES_API_KEY)) {
            return false;
        }

        if (!Places.isInitialized()) {
            Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), BuildConfig.PLACES_API_KEY);
        }
        return true;
    }

    private void submitEvent() {
        if (isSaving) {
            return;
        }

        isSaving = true;
        nextButton.setEnabled(false);

        createEventController.submitEvent(readFormData(), (AppResult<Event> result, boolean success) -> {
            isSaving = false;
            nextButton.setEnabled(true);

            if (result == null) {
                Toast.makeText(this, R.string.create_event_save_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
            if (!result.isSuccess() || result.getData() == null) {
                return;
            }

            Event event = result.getData();
            Intent intent = new Intent(this, EventCreatedActivity.class);
            intent.putExtra(EventCreatedActivity.EXTRA_EVENT_ID, event.getEventId());
            intent.putExtra(EventCreatedActivity.EXTRA_EVENT_TITLE, event.getTitle());
            intent.putExtra(EventCreatedActivity.EXTRA_EVENT_LOCATION, event.getLocation());
            intent.putExtra(EventCreatedActivity.EXTRA_EVENT_DATE, EventDisplayFormatter.date(event));
            intent.putExtra(EventCreatedActivity.EXTRA_EVENT_PRICE, EventDisplayFormatter.price(event));
            intent.putExtra(EventCreatedActivity.EXTRA_EVENT_DEADLINE, EventDisplayFormatter.deadline(event));
            intent.putExtra(EventCreatedActivity.EXTRA_EVENT_CATEGORY, event.getCategory());
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });
    }

    private EventFormData readFormData() {
        return formUiHelper.readFormData();
    }
}
