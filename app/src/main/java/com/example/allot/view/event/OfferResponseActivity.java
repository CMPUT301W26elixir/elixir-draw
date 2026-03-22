package com.example.allot.view.event;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.allot.R;
import com.example.allot.controller.event.EventDetailController;
import com.example.allot.view.shared.UiHelper;
import com.google.android.material.button.MaterialButton;
public class OfferResponseActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_TITLE = "event_title";

    private EventDetailController eventDetailController;

    private String currentEventId;
    private String currentEventTitle;
    private boolean isSubmitting;

    private TextView eventTitleText;
    private TextView stateText;
    private ProgressBar loadingIndicator;
    private MaterialButton acceptButton;
    private MaterialButton declineButton;

    /**
     * Initializes the activity, reads event data from the intent,
     * binds views, sets static content, and registers button listeners.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_offer);

        eventDetailController = new EventDetailController(this);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        currentEventTitle = getIntent().getStringExtra(EXTRA_EVENT_TITLE);

        bindViews();
        setupHeader();
        bindStaticContent();
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
        eventTitleText = findViewById(R.id.offerEventTitleText);
        stateText = findViewById(R.id.offerStateText);
        loadingIndicator = findViewById(R.id.offerLoadingIndicator);
        acceptButton = findViewById(R.id.acceptOfferButton);
        declineButton = findViewById(R.id.declineOfferButton);
    }

    /**
     * Sets up the header back button behavior.
     */
    private void setupHeader() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
    }

    /**
     * Displays the event title or a fallback value if no title was provided.
     */
    private void bindStaticContent() {
        eventTitleText.setText(UiHelper.defaultText(currentEventTitle, getString(R.string.default_event_name)));
    }

    /**
     * Registers click listeners for the accept and decline actions.
     */
    private void setupListeners() {
        acceptButton.setOnClickListener(view -> acceptOffer());
        declineButton.setOnClickListener(view -> declineOffer());
    }

    /**
     * Accepts the current offer and updates the event state in Firestore.
     * Marks the current user as enrolled and updates their waiting list status.
     */
    private void acceptOffer() {
        if (isSubmitting || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        setSubmitting(true);
        eventDetailController.acceptOffer(currentEventId, (result, success) -> {
            if (success && result != null && result.isSuccess()) {
                setResult(RESULT_OK);
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            setSubmitting(false);
            Toast.makeText(this, R.string.event_offer_action_failure, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Starts the decline flow by loading the current event state from Firestore.
     */
    private void declineOffer() {
        if (isSubmitting || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        setSubmitting(true);
        eventDetailController.declineOffer(currentEventId, (result, success) -> {
            if (success && result != null && result.isSuccess()) {
                setResult(RESULT_OK);
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            setSubmitting(false);
            Toast.makeText(this, R.string.event_offer_action_failure, Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Updates the submitting state of the screen, including loading visibility
     * and button enabled states.
     *
     * @param submitting true if an action is currently being submitted, false otherwise
     */
    private void setSubmitting(boolean submitting) {
        isSubmitting = submitting;
        loadingIndicator.setVisibility(submitting ? View.VISIBLE : View.GONE);
        stateText.setVisibility(submitting ? View.VISIBLE : View.GONE);
        if (submitting) {
            stateText.setText(R.string.event_offer_saving);
        }
        acceptButton.setEnabled(!submitting);
        declineButton.setEnabled(!submitting);
        acceptButton.setAlpha(submitting ? 0.6f : 1f);
        declineButton.setAlpha(submitting ? 0.6f : 1f);
    }

}









