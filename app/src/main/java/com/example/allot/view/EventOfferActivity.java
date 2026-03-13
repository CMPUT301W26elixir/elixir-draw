package com.example.allot.view;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Activity that allows a selected entrant to accept or decline an event offer.
 * Updates the event state in Firestore based on the user's response.
 */
public class EventOfferActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_TITLE = "event_title";

    private FirebaseFirestore database;
    private UserController userController;

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

        database = FirebaseFirestore.getInstance();
        userController = new UserController(this);
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
        eventTitleText.setText(TextUtils.isEmpty(currentEventTitle)
                ? getString(R.string.default_event_name)
                : currentEventTitle);
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

        String deviceId = userController.getCurrentDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            Toast.makeText(this, R.string.event_offer_action_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        setSubmitting(true);
        database.collection("events")
                .document(currentEventId)
                .update(
                        "enrolled", FieldValue.arrayUnion(deviceId),
                        "cancelled", FieldValue.arrayRemove(deviceId),
                        "notEnrolled", FieldValue.arrayRemove(deviceId),
                        "waitingList.status." + deviceId, true
                )
                .addOnSuccessListener(unused -> {
                    setResult(RESULT_OK);
                    Toast.makeText(this, R.string.event_offer_accept_success, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(exception -> {
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

        String deviceId = userController.getCurrentDeviceId();
        if (TextUtils.isEmpty(deviceId)) {
            Toast.makeText(this, R.string.event_offer_action_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        setSubmitting(true);
        database.collection("events")
                .document(currentEventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> handleDeclineSnapshot(documentSnapshot, deviceId))
                .addOnFailureListener(exception -> {
                    setSubmitting(false);
                    Toast.makeText(this, R.string.event_offer_action_failure, Toast.LENGTH_SHORT).show();
                });
    }

    /**
     * Handles the loaded event snapshot for a declined offer, updates the event state,
     * and optionally assigns a replacement offer.
     *
     * @param documentSnapshot the loaded Firestore document snapshot
     * @param deviceId the device ID of the user declining the offer
     */
    private void handleDeclineSnapshot(DocumentSnapshot documentSnapshot, String deviceId) {
        Event event = documentSnapshot == null ? null : documentSnapshot.toObject(Event.class);
        if (event == null) {
            setSubmitting(false);
            Toast.makeText(this, R.string.event_offer_action_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.waitingList == null) {
            event.getWaitingList();
        }
        if (event.waitingList == null) {
            setSubmitting(false);
            Toast.makeText(this, R.string.event_offer_action_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        if (event.waitingList.chosen == null) {
            event.waitingList.chosen = new ArrayList<>();
        }
        if (event.waitingList.status == null) {
            event.waitingList.status = new HashMap<>();
        }
        if (event.chosen == null) {
            event.chosen = new ArrayList<>();
        }
        if (event.enrolled == null) {
            event.enrolled = new ArrayList<>();
        }
        if (event.cancelled == null) {
            event.cancelled = new ArrayList<>();
        }
        if (event.notEnrolled == null) {
            event.notEnrolled = new ArrayList<>();
        }

        event.waitingList.chosen.remove(deviceId);
        event.waitingList.status.remove(deviceId);
        event.chosen.remove(deviceId);
        event.enrolled.remove(deviceId);
        if (!event.cancelled.contains(deviceId)) {
            event.cancelled.add(deviceId);
        }

        if ("open".equalsIgnoreCase(cleanText(event.status))) {
            addReplacementOffer(event, deviceId);
        }

        event.chosen = new ArrayList<>(event.waitingList.chosen);
        event.enrolled = event.waitingList.enrolled();

        database.collection("events")
                .document(currentEventId)
                .update(
                        "chosen", event.chosen,
                        "enrolled", event.enrolled,
                        "cancelled", event.cancelled,
                        "notEnrolled", event.notEnrolled,
                        "waitingList.chosen", event.waitingList.chosen,
                        "waitingList.status", event.waitingList.status
                )
                .addOnSuccessListener(unused -> {
                    setResult(RESULT_OK);
                    Toast.makeText(this, R.string.event_offer_decline_success, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(exception -> {
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

    /**
     * Returns a trimmed string value, or an empty string if the value is null.
     *
     * @param value the text value to clean
     * @return the trimmed text or an empty string
     */
    private String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Adds a replacement offer to the event by randomly selecting
     * an eligible entrant from the waiting list.
     *
     * @param event the event whose replacement offer should be assigned
     * @param declinedDeviceId the device ID of the user who declined the offer
     */
    private void addReplacementOffer(Event event, String declinedDeviceId) {
        if (event == null || event.waitingList == null || event.waitingList.list == null) {
            return;
        }

        List<String> eligibleEntrants = new ArrayList<>();
        for (String entrantId : event.waitingList.list) {
            if (TextUtils.isEmpty(entrantId)) {
                continue;
            }
            if (entrantId.equals(declinedDeviceId)) {
                continue;
            }
            if (event.waitingList.chosen.contains(entrantId)) {
                continue;
            }
            if (event.cancelled.contains(entrantId)) {
                continue;
            }
            eligibleEntrants.add(entrantId);
        }

        if (eligibleEntrants.isEmpty()) {
            return;
        }

        String replacementId = eligibleEntrants.get(new Random().nextInt(eligibleEntrants.size()));
        event.waitingList.chosen.add(replacementId);
        event.waitingList.status.put(replacementId, false);
    }
}