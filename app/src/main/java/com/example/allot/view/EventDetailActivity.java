package com.example.allot.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.allot.R;
import com.example.allot.controller.EventController;
import com.example.allot.controller.UserController;
import com.example.allot.model.Event;
import com.example.allot.model.EventDetailState;
import com.example.allot.model.User;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity that displays detailed information about a selected event.
 * Supports joining and leaving the waiting list, viewing offer states,
 * and navigating to organizer management screens when applicable.
 */
public class EventDetailActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_TITLE = "event_title";
    public static final String EXTRA_EVENT_LOCATION = "event_location";
    public static final String EXTRA_EVENT_DATE = "event_date";
    public static final String EXTRA_EVENT_PRICE = "event_price";
    public static final String EXTRA_EVENT_DEADLINE = "event_deadline";
    public static final String EXTRA_EVENT_CATEGORY = "event_category";

    private EventController eventController;
    private UserController userController;

    private String currentEventId;
    private boolean isJoiningWaitlist;
    private boolean isLeavingWaitlist;
    private Event currentEvent;
    private boolean isCurrentUserOrganizer;
    private boolean shouldRefreshOnResume;

    private FrameLayout heroImageFrame;
    private TextView heroDeadlineText;
    private TextView titleText;
    private TextView priceText;
    private TextView locationText;
    private TextView dateText;
    private TextView organizerText;
    private TextView descriptionText;
    private TextView registrationOpenText;
    private TextView registrationDeadlineText;
    private TextView waitlistStatusText;
    private TextView joinWaitingListButton;
    private TextView actionSubtextText;
    private TextView entrantCountText;
    private TextView errorText;
    private ProgressBar loadingIndicator;

    /**
     * Initializes the activity, binds views, shows fallback content,
     * registers listeners, and loads the full event details.
     *
     * @param savedInstanceState the saved activity state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventController = new EventController();
        userController = new UserController(this);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);

        bindViews();
        bindFallbackContent();
        setupListeners();
        loadEventDetails();
    }

    /**
     * Binds all view references used by this activity.
     */
    private void bindViews() {
        heroImageFrame = findViewById(R.id.heroImageFrame);
        heroDeadlineText = findViewById(R.id.heroDeadlineText);
        titleText = findViewById(R.id.eventTitleText);
        priceText = findViewById(R.id.eventPriceText);
        locationText = findViewById(R.id.eventLocationText);
        dateText = findViewById(R.id.eventDateText);
        organizerText = findViewById(R.id.eventOrganizerText);
        descriptionText = findViewById(R.id.eventDescriptionText);
        registrationOpenText = findViewById(R.id.registrationOpenText);
        registrationDeadlineText = findViewById(R.id.registrationDeadlineText);
        waitlistStatusText = findViewById(R.id.waitlistStatusText);
        joinWaitingListButton = findViewById(R.id.joinWaitingListButton);
        actionSubtextText = findViewById(R.id.actionSubtextText);
        entrantCountText = findViewById(R.id.entrantCountText);
        errorText = findViewById(R.id.eventErrorText);
        loadingIndicator = findViewById(R.id.eventLoadingIndicator);
    }

    /**
     * Sets up button listeners for navigation and waitlist actions.
     */
    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        joinWaitingListButton.setOnClickListener(view -> onWaitlistButtonPressed());
    }

    /**
     * Binds fallback event content from the intent extras before
     * the full event details are loaded from Firestore.
     */
    private void bindFallbackContent() {
        isCurrentUserOrganizer = false;
        titleText.setText(getIntent().getStringExtra(EXTRA_EVENT_TITLE));
        priceText.setText(getIntent().getStringExtra(EXTRA_EVENT_PRICE));
        locationText.setVisibility(View.VISIBLE);
        locationText.setText(buildLocationText(getIntent().getStringExtra(EXTRA_EVENT_LOCATION)));
        dateText.setVisibility(View.VISIBLE);
        dateText.setText(buildEventDateText(getIntent().getStringExtra(EXTRA_EVENT_DATE)));
        organizerText.setVisibility(View.VISIBLE);
        organizerText.setText(getString(R.string.event_detail_organizer_tba));
        bindOptionalText(heroDeadlineText, getIntent().getStringExtra(EXTRA_EVENT_DEADLINE));
        applyHeroBackground(getIntent().getStringExtra(EXTRA_EVENT_CATEGORY));

        descriptionText.setVisibility(View.GONE);
        registrationOpenText.setVisibility(View.GONE);
        registrationDeadlineText.setVisibility(View.GONE);
        waitlistStatusText.setVisibility(View.GONE);
        actionSubtextText.setVisibility(View.GONE);
        joinWaitingListButton.setText(R.string.event_detail_join_waiting_list);
        joinWaitingListButton.setBackgroundResource(R.drawable.bg_waitlist_button);
        joinWaitingListButton.setEnabled(true);
        joinWaitingListButton.setClickable(true);
        joinWaitingListButton.setTextColor(getResources().getColor(R.color.black));
        entrantCountText.setVisibility(View.VISIBLE);
        entrantCountText.setText(getResources().getQuantityString(R.plurals.event_detail_entrant_count, 0, 0));
    }

    /**
     * Loads the full event details for the current event ID.
     * Shows an error state if the event cannot be loaded.
     */
    private void loadEventDetails() {
        if (TextUtils.isEmpty(currentEventId)) {
            showErrorState(getString(R.string.event_detail_error));
            return;
        }

        setLoading(true);
        eventController.getEventDetailState(currentEventId, userController.getCurrentDeviceId(), (state, success) -> {
            setLoading(false);
            if (!success || state == null || state.getEvent() == null) {
                showErrorState(getString(R.string.event_detail_error));
                return;
            }

            Event event = state.getEvent();
            currentEvent = event;
            isCurrentUserOrganizer = isCurrentUserOrganizer(event);
            errorText.setVisibility(View.GONE);
            bindEvent(event);
            bindWaitlistState(state);
        });
    }

    /**
     * Binds the loaded event data to the UI.
     *
     * @param event the event to display
     */
    private void bindEvent(Event event) {
        titleText.setText(event.title);
        priceText.setText(EventDisplayFormatter.price(event));
        locationText.setVisibility(View.VISIBLE);
        locationText.setText(buildLocationText(event.location));
        dateText.setVisibility(View.VISIBLE);
        dateText.setText(buildEventDateText(formatDate(event.eventDate)));
        bindOptionalText(heroDeadlineText, EventDisplayFormatter.deadline(event));
        bindOptionalText(descriptionText, cleanText(event.description));

        registrationOpenText.setVisibility(View.VISIBLE);
        registrationOpenText.setText(buildRegistrationOpenText(event.registrationOpen));
        registrationDeadlineText.setVisibility(View.VISIBLE);
        registrationDeadlineText.setText(buildRegistrationDeadlineText(event.registrationDeadline));

        applyHeroBackground(event.category);
        bindEntrantCount(event);
        bindOrganizer(event.organizerId);
    }

    /**
     * Loads and displays the organizer name for the given organizer ID.
     *
     * @param organizerId the device ID of the organizer
     */
    private void bindOrganizer(String organizerId) {
        organizerText.setVisibility(View.VISIBLE);
        organizerText.setText(getString(R.string.event_detail_organizer_tba));

        if (TextUtils.isEmpty(organizerId)) {
            return;
        }

        userController.getUserByDeviceId(organizerId, (User user, boolean success) -> {
            if (!success || user == null || TextUtils.isEmpty(cleanText(user.getName()))) {
                organizerText.setText(getString(R.string.event_detail_organizer_tba));
                return;
            }

            organizerText.setText(user.getName());
        });
    }

    /**
     * Displays the current number of entrants on the waiting list.
     *
     * @param event the event whose entrant count should be shown
     */
    private void bindEntrantCount(Event event) {
        int entrantCount = 0;
        if (event.waitingList != null && event.waitingList.list != null) {
            entrantCount = event.waitingList.list.size();
        }

        entrantCountText.setVisibility(View.VISIBLE);
        entrantCountText.setText(getResources().getQuantityString(
                R.plurals.event_detail_entrant_count,
                entrantCount,
                entrantCount
        ));
    }

    /**
     * Updates the footer and action button based on the current user's
     * relationship to the event.
     *
     * @param event the event whose waitlist and offer state should be shown
     */
    private void bindWaitlistState(EventDetailState state) {
        if (state.getActionType() == EventDetailState.ActionType.MANAGE) {
            waitlistStatusText.setVisibility(View.GONE);
            actionSubtextText.setVisibility(View.GONE);
            joinWaitingListButton.setText(R.string.event_detail_manage_event);
            joinWaitingListButton.setBackgroundResource(R.drawable.bg_waitlist_button);
            joinWaitingListButton.setEnabled(true);
            joinWaitingListButton.setClickable(true);
            joinWaitingListButton.setTextColor(getResources().getColor(R.color.black));
            entrantCountText.setVisibility(View.VISIBLE);
            return;
        }

        if (state.getActionType() == EventDetailState.ActionType.ENROLLED) {
            showFooterState(
                    false,
                    false,
                    R.string.event_detail_enrolled,
                    R.drawable.bg_event_detail_offer_green,
                    getResources().getColor(R.color.white),
                    null,
                    true
            );
            return;
        }

        if (state.getActionType() == EventDetailState.ActionType.OFFER) {
            showFooterState(
                    false,
                    true,
                    R.string.event_detail_offer,
                    R.drawable.bg_event_detail_offer_green,
                    getResources().getColor(R.color.white),
                    null,
                    true
            );
            return;
        }

        if (state.getActionType() == EventDetailState.ActionType.NOT_SELECTED_REPLACEMENT) {
            showFooterState(
                    false,
                    false,
                    R.string.event_detail_not_selected_main_draw,
                    R.drawable.bg_event_detail_offer_yellow,
                    getResources().getColor(R.color.black),
                    state.getSubtext(),
                    false
            );
            return;
        }

        if (state.getActionType() == EventDetailState.ActionType.NOT_SELECTED_FINAL) {
            showFooterState(
                    false,
                    false,
                    R.string.event_detail_not_selected_final,
                    R.drawable.bg_event_detail_offer_grey,
                    getResources().getColor(R.color.black),
                    state.getSubtext(),
                    false
            );
            return;
        }

        showFooterState(
                state.shouldShowWaitlistMessage(),
                state.isButtonEnabled(),
                state.getActionType() == EventDetailState.ActionType.LEAVE_WAITLIST
                        ? R.string.event_detail_leave_waiting_list
                        : R.string.event_detail_join_waiting_list,
                state.getActionType() == EventDetailState.ActionType.LEAVE_WAITLIST
                        ? R.drawable.bg_waitlist_button_inactive
                        : R.drawable.bg_waitlist_button,
                getResources().getColor(R.color.black),
                state.getSubtext(),
                state.shouldShowEntrantCount()
        );
    }

    /**
     * Displays the footer action state for the event detail screen.
     *
     * @param showWaitlistMessage true to show the waitlist status message
     * @param buttonEnabled true to enable the action button
     * @param buttonTextRes the string resource for the action button text
     * @param buttonBackgroundRes the drawable resource for the button background
     * @param buttonTextColor the text color for the button
     * @param subtext optional subtext shown below the button
     * @param showEntrantCount true to show the entrant count text
     */
    private void showFooterState(boolean showWaitlistMessage,
                                 boolean buttonEnabled,
                                 int buttonTextRes,
                                 int buttonBackgroundRes,
                                 int buttonTextColor,
                                 String subtext,
                                 boolean showEntrantCount) {
        waitlistStatusText.setVisibility(showWaitlistMessage ? View.VISIBLE : View.GONE);
        joinWaitingListButton.setText(buttonTextRes);
        joinWaitingListButton.setBackgroundResource(buttonBackgroundRes);
        joinWaitingListButton.setTextColor(buttonTextColor);
        joinWaitingListButton.setEnabled(buttonEnabled);
        joinWaitingListButton.setClickable(buttonEnabled);

        if (TextUtils.isEmpty(subtext)) {
            actionSubtextText.setVisibility(View.GONE);
        } else {
            actionSubtextText.setVisibility(View.VISIBLE);
            actionSubtextText.setText(subtext);
        }

        entrantCountText.setVisibility(showEntrantCount ? View.VISIBLE : View.GONE);
    }

    /**
     * Checks whether the current user is enrolled in the event.
     *
     * @param event the event to check
     * @return true if the current user is enrolled, otherwise false
     */
    private boolean isCurrentUserEnrolled(Event event) {
        return containsUser(event == null ? null : event.enrolled, userController.getCurrentDeviceId());
    }

    /**
     * Checks whether the current user has an active offer for the event.
     *
     * @param event the event to check
     * @return true if the current user is selected but not yet enrolled
     */
    private boolean hasActiveOffer(Event event) {
        return isCurrentUserSelected(event) && !isCurrentUserEnrolled(event);
    }

    /**
     * Checks whether the current user has been selected in the event draw.
     *
     * @param event the event to check
     * @return true if the current user has been selected, otherwise false
     */
    private boolean isCurrentUserSelected(Event event) {
        String deviceId = userController.getCurrentDeviceId();
        return containsUser(event == null ? null : event.chosen, deviceId)
                || containsUser(event != null && event.waitingList != null ? event.waitingList.chosen : null, deviceId);
    }

    /**
     * Checks whether the UI should show the replacement-state message
     * for a user who was not selected in the main draw.
     *
     * @param event the event to check
     * @return true if the replacement-state message should be shown
     */
    private boolean shouldShowReplacementState(Event event) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event)
                && !isCurrentUserSelected(event)
                && !isCurrentUserEnrolled(event)
                && !"finalized".equalsIgnoreCase(cleanText(event == null ? null : event.status));
    }

    /**
     * Checks whether the UI should show the finalized not-selected state.
     *
     * @param event the event to check
     * @return true if the finalized not-selected state should be shown
     */
    private boolean shouldShowFinalizedNotSelectedState(Event event) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event)
                && !isCurrentUserSelected(event)
                && !isCurrentUserEnrolled(event)
                && "finalized".equalsIgnoreCase(cleanText(event == null ? null : event.status));
    }

    /**
     * Checks whether any selection results have been published for the event.
     *
     * @param event the event to check
     * @return true if selection results exist, otherwise false
     */
    private boolean hasPublishedSelectionResults(Event event) {
        return (event != null && event.chosen != null && !event.chosen.isEmpty())
                || (event != null && event.enrolled != null && !event.enrolled.isEmpty())
                || (event != null && event.cancelled != null && !event.cancelled.isEmpty())
                || (event != null && event.notEnrolled != null && !event.notEnrolled.isEmpty())
                || (event != null && event.waitingList != null && event.waitingList.chosen != null && !event.waitingList.chosen.isEmpty());
    }

    /**
     * Checks whether a user ID appears in a list of user IDs.
     *
     * @param users the list of user IDs to search
     * @param deviceId the user device ID to look for
     * @return true if the user exists in the list, otherwise false
     */
    private boolean containsUser(java.util.List<String> users, String deviceId) {
        return users != null && !TextUtils.isEmpty(deviceId) && users.contains(deviceId);
    }

    /**
     * Checks whether the current user is on the event waiting list.
     *
     * @param event the event to check
     * @return true if the current user is on the waiting list, otherwise false
     */
    private boolean isCurrentUserOnWaitingList(Event event) {
        if (event == null || event.waitingList == null || event.waitingList.list == null) {
            return false;
        }
        return event.waitingList.list.contains(userController.getCurrentDeviceId());
    }

    /**
     * Checks whether the current user is the organizer of the event.
     *
     * @param event the event to check
     * @return true if the current user is the organizer, otherwise false
     */
    private boolean isCurrentUserOrganizer(Event event) {
        if (event == null) {
            return false;
        }
        String currentDeviceId = userController.getCurrentDeviceId();
        return !TextUtils.isEmpty(currentDeviceId) && currentDeviceId.equals(event.organizerId);
    }

    /**
     * Handles presses on the main action button based on the current
     * user and event state.
     */
    private void onWaitlistButtonPressed() {
        if (isCurrentUserOrganizer) {
            openManageEventScreen();
            return;
        }

        if (hasActiveOffer(currentEvent)) {
            openOfferScreen();
            return;
        }

        if (!joinWaitingListButton.isEnabled()) {
            return;
        }

        if (isCurrentUserOnWaitingList(currentEvent)) {
            leaveWaitingList();
            return;
        }
        showLotteryCriteriaDialog();
    }

    /**
     * Opens the organizer event management screen for the current event.
     */
    private void openManageEventScreen() {
        if (TextUtils.isEmpty(currentEventId)) {
            return;
        }

        shouldRefreshOnResume = true;
        Intent intent = new Intent(this, ManageEventActivity.class);
        intent.putExtra(ManageEventActivity.EXTRA_EVENT_ID, currentEventId);
        intent.putExtra(ManageEventActivity.EXTRA_EVENT_TITLE,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_TITLE) : currentEvent.title);
        intent.putExtra(ManageEventActivity.EXTRA_EVENT_LOCATION,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_LOCATION) : buildLocationText(currentEvent.location));
        intent.putExtra(ManageEventActivity.EXTRA_EVENT_DATE,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_DATE) : formatLongDate(currentEvent.eventDate));
        intent.putExtra(ManageEventActivity.EXTRA_EVENT_PRICE,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_PRICE) : EventDisplayFormatter.price(currentEvent));
        intent.putExtra(ManageEventActivity.EXTRA_EVENT_DESCRIPTION,
                currentEvent == null ? null : cleanText(currentEvent.description));
        intent.putExtra(ManageEventActivity.EXTRA_EVENT_PARTICIPANTS,
                currentEvent == null ? null : String.valueOf(currentEvent.capacity));
        intent.putExtra(ManageEventActivity.EXTRA_REGISTRATION_START,
                currentEvent == null ? null : formatLongDate(currentEvent.registrationOpen));
        intent.putExtra(ManageEventActivity.EXTRA_REGISTRATION_END,
                currentEvent == null ? null : formatLongDate(currentEvent.registrationDeadline));
        intent.putExtra(ManageEventActivity.EXTRA_EVENT_CATEGORY,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_CATEGORY) : currentEvent.category);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Opens the event offer screen for the current event.
     */
    private void openOfferScreen() {
        if (TextUtils.isEmpty(currentEventId)) {
            return;
        }

        shouldRefreshOnResume = true;
        Intent intent = new Intent(this, EventOfferActivity.class);
        intent.putExtra(EventOfferActivity.EXTRA_EVENT_ID, currentEventId);
        intent.putExtra(EventOfferActivity.EXTRA_EVENT_TITLE,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_TITLE) : currentEvent.title);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    /**
     * Refreshes the event details when returning from related screens
     * that may have changed the event state.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (shouldRefreshOnResume && !TextUtils.isEmpty(currentEventId)) {
            shouldRefreshOnResume = false;
            loadEventDetails();
        }
    }

    /**
     * Shows the lottery criteria dialog before joining the waiting list.
     */
    private void showLotteryCriteriaDialog() {
        if (TextUtils.isEmpty(currentEventId) || isJoiningWaitlist || isLeavingWaitlist) {
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_join_waitlist, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);

        ImageView closeButton = dialogView.findViewById(R.id.closeLotteryCriteriaDialogButton);
        TextView eligibilityBodyText = dialogView.findViewById(R.id.eligibilityBodyText);
        TextView selectionBodyText = dialogView.findViewById(R.id.selectionBodyText);
        MaterialButton confirmButton = dialogView.findViewById(R.id.confirmJoinWaitlistButton);

        eligibilityBodyText.setText(buildEligibilityCriteriaText());
        selectionBodyText.setText(buildSelectionCriteriaText());

        closeButton.setOnClickListener(view -> dialog.dismiss());
        confirmButton.setOnClickListener(view -> joinWaitingList(dialog, confirmButton));

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(dpToPx(342), android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    /**
     * Attempts to join the current event's waiting list.
     *
     * @param dialog the dialog that initiated the join action
     * @param confirmButton the confirmation button shown in the dialog
     */
    private void joinWaitingList(Dialog dialog, MaterialButton confirmButton) {
        if (isJoiningWaitlist || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        isJoiningWaitlist = true;
        confirmButton.setEnabled(false);
        joinWaitingListButton.setEnabled(false);

        eventController.joinWaitingList(currentEventId, userController.getCurrentDeviceId(), (result, success) -> {
            isJoiningWaitlist = false;
            confirmButton.setEnabled(true);
            joinWaitingListButton.setEnabled(true);

            if (!success || result == null || !result) {
                Toast.makeText(this, R.string.event_detail_join_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(this, R.string.event_detail_join_success, Toast.LENGTH_SHORT).show();
            loadEventDetails();
        });
    }

    /**
     * Attempts to remove the current user from the waiting list.
     */
    private void leaveWaitingList() {
        if (isLeavingWaitlist || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        isLeavingWaitlist = true;
        joinWaitingListButton.setEnabled(false);

        eventController.leaveWaitingList(currentEventId, userController.getCurrentDeviceId(), (result, success) -> {
            isLeavingWaitlist = false;
            joinWaitingListButton.setEnabled(true);

            if (!success || result == null || !result) {
                Toast.makeText(this, R.string.event_detail_leave_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, R.string.event_detail_leave_success, Toast.LENGTH_SHORT).show();
            loadEventDetails();
        });
    }

    /**
     * Sets a TextView to the provided value or hides it if the value is empty.
     *
     * @param view the TextView to update
     * @param value the value to display
     */
    private void bindOptionalText(TextView view, String value) {
        if (TextUtils.isEmpty(cleanText(value))) {
            view.setVisibility(View.GONE);
            return;
        }

        view.setVisibility(View.VISIBLE);
        view.setText(value);
    }

    /**
     * Applies the hero background image based on the event category.
     *
     * @param category the event category
     */
    private void applyHeroBackground(String category) {
        int backgroundRes = shouldUsePrimaryBackground(category)
                ? R.drawable.bg_event_image_one
                : R.drawable.bg_event_image_two;
        heroImageFrame.setBackgroundResource(backgroundRes);
    }

    /**
     * Determines which hero background image should be used.
     *
     * @param category the event category
     * @return true if the primary background should be used, otherwise false
     */
    private boolean shouldUsePrimaryBackground(String category) {
        String normalizedCategory = cleanText(category);
        if (TextUtils.isEmpty(normalizedCategory)) {
            return true;
        }
        return Math.abs(normalizedCategory.hashCode()) % 2 == 0;
    }

    /**
     * Builds the location text shown in the UI.
     *
     * @param location the event location
     * @return the formatted location text or a fallback value
     */
    private String buildLocationText(String location) {
        String value = cleanText(location);
        if (TextUtils.isEmpty(value)) {
            return getString(R.string.event_detail_address_tba);
        }
        return value;
    }

    /**
     * Builds the event date text shown in the UI.
     *
     * @param eventDate the event date text
     * @return the formatted event date text or a fallback value
     */
    private String buildEventDateText(String eventDate) {
        String value = cleanText(eventDate);
        if (TextUtils.isEmpty(value)) {
            return getString(R.string.event_detail_date_tba);
        }
        return value;
    }

    /**
     * Builds the eligibility criteria text shown in the waiting list dialog.
     *
     * @return the formatted eligibility criteria text
     */
    private String buildEligibilityCriteriaText() {
        String closeDate = getString(R.string.event_detail_registration_tba);
        if (currentEvent != null && currentEvent.registrationDeadline != null) {
            closeDate = formatLongDate(currentEvent.registrationDeadline);
        }
        return getString(R.string.lottery_criteria_eligibility_body, closeDate);
    }

    /**
     * Builds the selection criteria text shown in the waiting list dialog.
     *
     * @return the formatted selection criteria text
     */
    private String buildSelectionCriteriaText() {
        int selectedCount = 0;
        if (currentEvent != null) {
            if (currentEvent.capacity > 0) {
                selectedCount = currentEvent.capacity;
            } else if (currentEvent.limit > 0) {
                selectedCount = currentEvent.limit;
            }
        }
        return getString(R.string.lottery_criteria_selection_body, selectedCount);
    }

    /**
     * Builds the registration open text shown in the UI.
     *
     * @param registrationOpen the event registration open date
     * @return the formatted registration open text
     */
    private String buildRegistrationOpenText(Date registrationOpen) {
        String value = registrationOpen == null
                ? getString(R.string.event_detail_registration_tba)
                : formatLongDate(registrationOpen);
        return getString(R.string.event_detail_registration_opens, value);
    }

    /**
     * Builds the registration deadline text shown in the UI.
     *
     * @param registrationDeadline the event registration deadline
     * @return the formatted registration deadline text
     */
    private String buildRegistrationDeadlineText(Date registrationDeadline) {
        String value = registrationDeadline == null
                ? getString(R.string.event_detail_registration_tba)
                : formatLongDate(registrationDeadline);
        return getString(R.string.event_detail_registration_closes, value);
    }

    /**
     * Formats a date using the long month pattern.
     *
     * @param date the date to format
     * @return the formatted date string, or null if the date is null
     */
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date);
    }

    /**
     * Formats a date using the abbreviated month pattern.
     *
     * @param date the date to format
     * @return the formatted date string, or null if the date is null
     */
    private String formatLongDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);
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
     * Shows or hides the loading indicator.
     *
     * @param isLoading true to show the loading indicator, false to hide it
     */
    private void setLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    /**
     * Shows an error message in the error text view.
     *
     * @param message the error message to display
     */
    private void showErrorState(String message) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }

    /**
     * Converts density-independent pixels to physical pixels.
     *
     * @param dp the value in density-independent pixels
     * @return the converted value in physical pixels
     */
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
