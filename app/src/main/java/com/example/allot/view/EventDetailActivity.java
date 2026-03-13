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
import com.example.allot.model.User;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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
    private User currentUser;
    private boolean isCurrentUserOrganizer;
    private boolean shouldRefreshOnResume;

    private MaterialButton adminDeleteEventButton;

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
        adminDeleteEventButton = findViewById(R.id.adminDeleteEventButton);
    }

    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        joinWaitingListButton.setOnClickListener(view -> onWaitlistButtonPressed());
        adminDeleteEventButton.setOnClickListener(view -> confirmAdminDelete());
    }

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

    private void loadEventDetails() {
        if (TextUtils.isEmpty(currentEventId)) {
            showErrorState(getString(R.string.event_detail_error));
            return;
        }

        setLoading(true);

        // Load event and user in parallel; bind UI once both have returned
        final Event[] loadedEvent = {null};
        final User[]  loadedUser  = {null};
        final boolean[] eventDone = {false};
        final boolean[] userDone  = {false};

        eventController.getEventById(currentEventId, new EventController.EventCallback() {
            @Override
            public void onCallback(Event event) {
                loadedEvent[0] = event;
                eventDone[0]   = true;
                if (userDone[0]) onBothLoaded(loadedEvent[0], loadedUser[0]);
            }
            @Override
            public void onError(Exception exception) {
                setLoading(false);
                showErrorState(getString(R.string.event_detail_error));
            }
        });

        userController.loadOrCreateUser((user, success) -> {
            loadedUser[0] = (success && user != null) ? user : null;
            userDone[0]   = true;
            if (eventDone[0]) onBothLoaded(loadedEvent[0], loadedUser[0]);
        });
    }

    private void onBothLoaded(Event event, User user) {
        setLoading(false);

        if (event == null) {
            showErrorState(getString(R.string.event_detail_not_found));
            return;
        }

        currentEvent          = event;
        currentUser           = user;
        isCurrentUserOrganizer = isCurrentUserOrganizer(event);
        errorText.setVisibility(View.GONE);

        // Show the admin delete button only for admins
        boolean isAdmin = user != null && user.isAdmin();
        adminDeleteEventButton.setVisibility(isAdmin ? View.VISIBLE : View.GONE);

        bindEvent(event);
    }

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
        bindWaitlistState(event);
    }

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

    private void bindWaitlistState(Event event) {
        if (isCurrentUserOrganizer(event)) {
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

        if (isCurrentUserEnrolled(event)) {
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

        if (isCurrentUserSelected(event)) {
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

        if (shouldShowReplacementState(event)) {
            showFooterState(
                    false,
                    false,
                    R.string.event_detail_not_selected_main_draw,
                    R.drawable.bg_event_detail_offer_yellow,
                    getResources().getColor(R.color.black),
                    getString(R.string.event_detail_not_selected_main_draw_body),
                    false
            );
            return;
        }

        if (shouldShowFinalizedNotSelectedState(event)) {
            showFooterState(
                    false,
                    false,
                    R.string.event_detail_not_selected_final,
                    R.drawable.bg_event_detail_offer_grey,
                    getResources().getColor(R.color.black),
                    getString(R.string.event_detail_not_selected_final_body),
                    false
            );
            return;
        }

        boolean isOnWaitingList = isCurrentUserOnWaitingList(event);
        showFooterState(
                isOnWaitingList,
                true,
                isOnWaitingList ? R.string.event_detail_leave_waiting_list : R.string.event_detail_join_waiting_list,
                isOnWaitingList ? R.drawable.bg_waitlist_button_inactive : R.drawable.bg_waitlist_button,
                getResources().getColor(R.color.black),
                null,
                true
        );
    }

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

    private boolean isCurrentUserEnrolled(Event event) {
        return containsUser(event == null ? null : event.enrolled, userController.getCurrentDeviceId());
    }

    private boolean hasActiveOffer(Event event) {
        return isCurrentUserSelected(event) && !isCurrentUserEnrolled(event);
    }

    private boolean isCurrentUserSelected(Event event) {
        String deviceId = userController.getCurrentDeviceId();
        return containsUser(event == null ? null : event.chosen, deviceId)
                || containsUser(event != null && event.waitingList != null ? event.waitingList.chosen : null, deviceId);
    }

    private boolean shouldShowReplacementState(Event event) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event)
                && !isCurrentUserSelected(event)
                && !isCurrentUserEnrolled(event)
                && !"finalized".equalsIgnoreCase(cleanText(event == null ? null : event.status));
    }

    private boolean shouldShowFinalizedNotSelectedState(Event event) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event)
                && !isCurrentUserSelected(event)
                && !isCurrentUserEnrolled(event)
                && "finalized".equalsIgnoreCase(cleanText(event == null ? null : event.status));
    }

    private boolean hasPublishedSelectionResults(Event event) {
        return (event != null && event.chosen != null && !event.chosen.isEmpty())
                || (event != null && event.enrolled != null && !event.enrolled.isEmpty())
                || (event != null && event.cancelled != null && !event.cancelled.isEmpty())
                || (event != null && event.notEnrolled != null && !event.notEnrolled.isEmpty())
                || (event != null && event.waitingList != null && event.waitingList.chosen != null && !event.waitingList.chosen.isEmpty());
    }

    private boolean containsUser(java.util.List<String> users, String deviceId) {
        return users != null && !TextUtils.isEmpty(deviceId) && users.contains(deviceId);
    }

    private boolean isCurrentUserOnWaitingList(Event event) {
        if (event == null || event.waitingList == null || event.waitingList.list == null) {
            return false;
        }
        return event.waitingList.list.contains(userController.getCurrentDeviceId());
    }

    private boolean isCurrentUserOrganizer(Event event) {
        if (event == null) {
            return false;
        }
        String currentDeviceId = userController.getCurrentDeviceId();
        return !TextUtils.isEmpty(currentDeviceId) && currentDeviceId.equals(event.organizerId);
    }

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

    private void confirmAdminDelete() {
        if (TextUtils.isEmpty(currentEventId)) return;
        String title = currentEvent != null ? currentEvent.title : null;

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_admin_delete_event, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);

        android.widget.TextView titleText = dialogView.findViewById(R.id.deleteEventTitleText);
        android.widget.ImageView closeBtn = dialogView.findViewById(R.id.closeDeleteEventDialogButton);
        android.widget.Button    cancelBtn = dialogView.findViewById(R.id.cancelDeleteEventButton);
        android.widget.Button    confirmBtn = dialogView.findViewById(R.id.confirmDeleteEventButton);

        titleText.setText(title != null
                ? title : getString(R.string.admin_delete_event_title_fallback));

        closeBtn.setOnClickListener(v -> dialog.dismiss());
        cancelBtn.setOnClickListener(v -> dialog.dismiss());
        confirmBtn.setOnClickListener(v -> {
            cancelBtn.setEnabled(false);
            confirmBtn.setEnabled(false);
            eventController.adminDeleteEvent(currentEventId, (result, success) -> {
                if (!success || result == null || !result) {
                    cancelBtn.setEnabled(true);
                    confirmBtn.setEnabled(true);
                    Toast.makeText(this,
                            R.string.admin_delete_event_failure, Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                Toast.makeText(this,
                        R.string.admin_delete_event_success, Toast.LENGTH_SHORT).show();
                finish();
            });
        });

        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(dpToPx(342), dpToPx(342));
            window.setBackgroundDrawableResource(android.R.color.transparent);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (shouldRefreshOnResume && !TextUtils.isEmpty(currentEventId)) {
            shouldRefreshOnResume = false;
            loadEventDetails();
        }
    }

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

    private void bindOptionalText(TextView view, String value) {
        if (TextUtils.isEmpty(cleanText(value))) {
            view.setVisibility(View.GONE);
            return;
        }

        view.setVisibility(View.VISIBLE);
        view.setText(value);
    }

    private void applyHeroBackground(String category) {
        int backgroundRes = shouldUsePrimaryBackground(category)
                ? R.drawable.bg_event_image_one
                : R.drawable.bg_event_image_two;
        heroImageFrame.setBackgroundResource(backgroundRes);
    }

    private boolean shouldUsePrimaryBackground(String category) {
        String normalizedCategory = cleanText(category);
        if (TextUtils.isEmpty(normalizedCategory)) {
            return true;
        }
        return Math.abs(normalizedCategory.hashCode()) % 2 == 0;
    }

    private String buildLocationText(String location) {
        String value = cleanText(location);
        if (TextUtils.isEmpty(value)) {
            return getString(R.string.event_detail_address_tba);
        }
        return value;
    }

    private String buildEventDateText(String eventDate) {
        String value = cleanText(eventDate);
        if (TextUtils.isEmpty(value)) {
            return getString(R.string.event_detail_date_tba);
        }
        return value;
    }

    private String buildEligibilityCriteriaText() {
        String closeDate = getString(R.string.event_detail_registration_tba);
        if (currentEvent != null && currentEvent.registrationDeadline != null) {
            closeDate = formatLongDate(currentEvent.registrationDeadline);
        }
        return getString(R.string.lottery_criteria_eligibility_body, closeDate);
    }

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

    private String buildRegistrationOpenText(Date registrationOpen) {
        String value = registrationOpen == null
                ? getString(R.string.event_detail_registration_tba)
                : formatLongDate(registrationOpen);
        return getString(R.string.event_detail_registration_opens, value);
    }

    private String buildRegistrationDeadlineText(Date registrationDeadline) {
        String value = registrationDeadline == null
                ? getString(R.string.event_detail_registration_tba)
                : formatLongDate(registrationDeadline);
        return getString(R.string.event_detail_registration_closes, value);
    }

    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date);
    }

    private String formatLongDate(Date date) {
        if (date == null) {
            return null;
        }
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date);
    }

    private String cleanText(String value) {
        return value == null ? null : value.trim();
    }

    private void setLoading(boolean isLoading) {
        loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }

    private void showErrorState(String message) {
        errorText.setVisibility(View.VISIBLE);
        errorText.setText(message);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}