package com.example.allot.view;

import android.app.Dialog;
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
    private boolean isCurrentUserOrganizer;

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
        entrantCountText = findViewById(R.id.entrantCountText);
        errorText = findViewById(R.id.eventErrorText);
        loadingIndicator = findViewById(R.id.eventLoadingIndicator);
    }

    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        joinWaitingListButton.setOnClickListener(view -> onWaitlistButtonPressed());
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
        joinWaitingListButton.setText(R.string.event_detail_join_waiting_list);
        joinWaitingListButton.setBackgroundResource(R.drawable.bg_waitlist_button);
        entrantCountText.setVisibility(View.VISIBLE);
        entrantCountText.setText(getResources().getQuantityString(R.plurals.event_detail_entrant_count, 0, 0));
    }

    private void loadEventDetails() {
        if (TextUtils.isEmpty(currentEventId)) {
            showErrorState(getString(R.string.event_detail_error));
            return;
        }

        setLoading(true);
        eventController.getEventById(currentEventId, new EventController.EventCallback() {
            @Override
            public void onCallback(Event event) {
                setLoading(false);
                if (event == null) {
                    showErrorState(getString(R.string.event_detail_not_found));
                    return;
                }

                currentEvent = event;
                isCurrentUserOrganizer = isCurrentUserOrganizer(event);
                errorText.setVisibility(View.GONE);
                bindEvent(event);
            }

            @Override
            public void onError(Exception exception) {
                setLoading(false);
                showErrorState(getString(R.string.event_detail_error));
            }
        });
    }

    private void bindEvent(Event event) {
        titleText.setText(event.getBrowseTitleText());
        priceText.setText(event.getBrowsePriceText());
        locationText.setVisibility(View.VISIBLE);
        locationText.setText(buildLocationText(event.location));
        dateText.setVisibility(View.VISIBLE);
        dateText.setText(buildEventDateText(formatDate(event.eventDate)));
        bindOptionalText(heroDeadlineText, event.getBrowseDeadlineText());
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
            joinWaitingListButton.setText(R.string.event_detail_manage_event);
            joinWaitingListButton.setBackgroundResource(R.drawable.bg_waitlist_button);
            return;
        }

        boolean isOnWaitingList = isCurrentUserOnWaitingList(event);
        waitlistStatusText.setVisibility(isOnWaitingList ? View.VISIBLE : View.GONE);
        joinWaitingListButton.setText(isOnWaitingList
                ? R.string.event_detail_leave_waiting_list
                : R.string.event_detail_join_waiting_list);
        joinWaitingListButton.setBackgroundResource(isOnWaitingList
                ? R.drawable.bg_waitlist_button_inactive
                : R.drawable.bg_waitlist_button);
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
            Toast.makeText(this, R.string.event_detail_manage_event_coming_soon, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isCurrentUserOnWaitingList(currentEvent)) {
            leaveWaitingList();
            return;
        }
        showLotteryCriteriaDialog();
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
            if (currentEvent.choosingLimit > 0) {
                selectedCount = currentEvent.choosingLimit;
            } else if (currentEvent.capacity > 0) {
                selectedCount = currentEvent.capacity;
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
