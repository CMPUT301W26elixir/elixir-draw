package com.example.allot.view.event;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.controller.event.CreateEventController;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.view.shared.EventDisplayFormatter;
import com.example.allot.view.shared.EventFormUiHelper;
/**
 * Shows the form for creating a new event and forwards user actions to the controller.
 */
public class CreateEventActivity extends AppCompatActivity {
    private TextView nextButton;

    private CreateEventController createEventController;
    private EventFormUiHelper formUiHelper;
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

        createEventController = new CreateEventController(this);

        bindViews();
        formUiHelper.setupMonthSpinners(this);
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
        EditText eventNameInput = findViewById(R.id.eventNameInput);
        EditText locationInput = findViewById(R.id.locationInput);
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

    /**
     * Sets up the header back button behavior.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
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









