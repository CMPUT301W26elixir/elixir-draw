package com.example.allot.view.event;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.allot.BuildConfig;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.controller.event.CreateEventController;
import com.example.allot.controller.event.EventPosterController;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.view.shared.EventDisplayFormatter;
import com.example.allot.view.shared.EventFormUiHelper;
import com.example.allot.view.shared.RegistrationRangePickerView;
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
    private ImageView posterPreviewImage;
    private android.view.View posterUploadCard;
    private android.view.View posterUploadPlaceholder;
    private TextView posterUploadHintText;
    private TextView posterRemoveButton;
    private Uri selectedPosterUri;

    private CreateEventController createEventController;
    private EventPosterController eventPosterController;
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
    private final ActivityResultLauncher<String> posterPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }

                selectedPosterUri = uri;
                renderPosterSelection();
            });

    /**
     * Handles the create callback.
     *
     * @param savedInstanceState the saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        createEventController = new CreateEventController(this);
        eventPosterController = new EventPosterController();

        bindViews();
        formUiHelper.setupMonthSpinners(this);
        setupHeader();
        setupListeners();
    }

    /**
     * Performs finish.
     */
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    /**
     * Performs bind views.
     */
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
        RegistrationRangePickerView registrationRangePickerView = findViewById(R.id.registrationRangePickerView);
        nextButton = findViewById(R.id.createEventNextButton);
        posterUploadCard = findViewById(R.id.posterUploadCard);
        posterPreviewImage = findViewById(R.id.posterPreviewImage);
        posterUploadPlaceholder = findViewById(R.id.posterUploadPlaceholder);
        posterUploadHintText = findViewById(R.id.posterUploadHintText);
        posterRemoveButton = findViewById(R.id.posterRemoveButton);
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
     * Updates the up header.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Updates the up listeners.
     */
    private void setupListeners() {
        configureLocationPicker();
        posterUploadCard.setOnClickListener(view -> posterPickerLauncher.launch("image/*"));
        posterRemoveButton.setOnClickListener(view -> {
            selectedPosterUri = null;
            renderPosterSelection();
        });
        nextButton.setOnClickListener(view -> submitEvent());
        renderPosterSelection();
    }

    /**
     * Performs configure location picker.
     */
    private void configureLocationPicker() {
        locationInput.setKeyListener(null);
        locationInput.setFocusable(false);
        locationInput.setCursorVisible(false);
        locationInput.setOnClickListener(view -> openPlaceAutocomplete());
    }

    /**
     * Performs open place autocomplete.
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
     * Returns the result of ensure places initialized.
     *
     * @return the result of this call
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
     * Performs submit event.
     */
    private void submitEvent() {
        if (isSaving) {
            return;
        }

        isSaving = true;
        nextButton.setEnabled(false);

        createEventController.submitEvent(readFormData(), (AppResult<Event> result, boolean success) -> {
            if (result == null) {
                isSaving = false;
                nextButton.setEnabled(true);
                Toast.makeText(this, R.string.create_event_save_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!result.isSuccess() || result.getData() == null) {
                isSaving = false;
                nextButton.setEnabled(true);
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                return;
            }

            Event event = result.getData();
            if (selectedPosterUri == null) {
                isSaving = false;
                nextButton.setEnabled(true);
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                openEventCreatedScreen(event);
                return;
            }

            eventPosterController.uploadPoster(event.getEventId(), selectedPosterUri, (posterUrl, posterSuccess) -> {
                isSaving = false;
                nextButton.setEnabled(true);

                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                if (!posterSuccess) {
                    Toast.makeText(this, R.string.event_poster_upload_failure, Toast.LENGTH_SHORT).show();
                } else {
                    event.setPosterUrl(posterUrl);
                    Toast.makeText(this, R.string.event_poster_upload_success, Toast.LENGTH_SHORT).show();
                }

                openEventCreatedScreen(event);
            });
        });
    }

    /**
     * Performs render poster selection.
     */
    private void renderPosterSelection() {
        boolean hasPoster = selectedPosterUri != null;
        posterPreviewImage.setVisibility(hasPoster ? android.view.View.VISIBLE : android.view.View.GONE);
        posterUploadPlaceholder.setVisibility(hasPoster ? android.view.View.GONE : android.view.View.VISIBLE);
        posterRemoveButton.setVisibility(hasPoster ? android.view.View.VISIBLE : android.view.View.GONE);
        posterUploadHintText.setText(hasPoster
                ? R.string.create_event_poster_change
                : R.string.create_event_poster_placeholder);

        if (hasPoster) {
            Glide.with(this).load(selectedPosterUri).into(posterPreviewImage);
        } else {
            posterPreviewImage.setImageDrawable(null);
        }
    }

    /**
     * Performs open event created screen.
     *
     * @param event the event
     */
    private void openEventCreatedScreen(Event event) {
        if (event == null) {
            return;
        }

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
    }

    /**
     * Returns the result of read form data.
     *
     * @return the result of this call
     */
    private EventFormData readFormData() {
        return formUiHelper.readFormData();
    }
}
