package com.example.allot.view.event;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bumptech.glide.Glide;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.controller.event.EventDetailController;
import com.example.allot.controller.explore.ExploreController;
import com.example.allot.controller.shared.UserController;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventComment;
import com.example.allot.model.event.EventDetailData;
import com.example.allot.view.shared.AppDialogHelper;
import com.example.allot.view.shared.DeferredOnboardingNavigator;
import com.example.allot.view.shared.EventDisplayFormatter;
import com.example.allot.view.shared.UiHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
/**
 * Displays the full event detail screen and reacts to the main event actions.
 */
public class EventDetailActivity extends AppCompatActivity {
    public static final String EXTRA_EVENT_ID = "event_id";
    public static final String EXTRA_EVENT_TITLE = "event_title";
    public static final String EXTRA_EVENT_LOCATION = "event_location";
    public static final String EXTRA_EVENT_DATE = "event_date";
    public static final String EXTRA_EVENT_PRICE = "event_price";
    public static final String EXTRA_EVENT_DEADLINE = "event_deadline";
    public static final String EXTRA_EVENT_CATEGORY = "event_category";
    public static final String EXTRA_AUTO_OPEN_JOIN_DIALOG = "auto_open_join_dialog";
    public static final String EXTRA_AUTO_SAVE_EVENT = "auto_save_event";
    public static final String EXTRA_UI_TEST_SKIP_NETWORK_LOAD = "ui_test_skip_network_load";
    public static final String EXTRA_UI_TEST_BYPASS_PROFILE_GATE = "ui_test_bypass_profile_gate";
    public static final String EXTRA_UI_TEST_START_ON_WAITLIST = "ui_test_start_on_waitlist";
    private static final int LOCATION_PERMISSION_REQUEST = 1002;

    private EventDetailController eventDetailController;
    private ExploreController exploreController;
    private UserController userController;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private String currentEventId;
    private boolean isJoiningWaitlist;
    private boolean isLeavingWaitlist;
    private boolean isPostingComment;
    private boolean isVotingComment;
    private Event currentEvent;
    private EventDetailData currentScreenState;
    private boolean shouldRefreshOnResume;
    private boolean isOrganizer;
    private boolean isAdmin;
    private boolean isSyncingWaitlistLocation;
    private boolean hasAttemptedAutoWaitlistLocationSync;

    private FrameLayout heroImageFrame;
    private ImageView heroPosterImage;
    private CommentSortMode commentSortMode = CommentSortMode.NEWEST;
    private List<String> userSavedEventIds = new ArrayList<>();
    private boolean isFavoriteSaved;
    private boolean isFavoriteLoading = true;
    private boolean isFavoriteUpdating;
    private boolean shouldAutoOpenJoinDialog;
    private boolean shouldAutoSaveEvent;

    private ImageView favoriteButton;
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
    private LinearLayout commentsContainer;
    private TextView commentEmptyText;
    private EditText commentInputText;
    private TextView commentSubmitButton;
    private Spinner commentSortSpinner;
    private Dialog pendingJoinDialog;
    private MaterialButton pendingJoinConfirmButton;

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

        eventDetailController = new EventDetailController(this);
        exploreController = new ExploreController(this);
        userController = new UserController(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        currentEventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        shouldAutoOpenJoinDialog = getIntent().getBooleanExtra(EXTRA_AUTO_OPEN_JOIN_DIALOG, false);
        shouldAutoSaveEvent = getIntent().getBooleanExtra(EXTRA_AUTO_SAVE_EVENT, false);

        bindViews();
        bindFallbackContent();
        setupListeners();
        if (isUiTestFallbackMode()) {
            return;
        }
        loadAdminStatus();
        loadEventDetails();
    }

    /**
     * Loads admin status.
     */
    private void loadAdminStatus() {
        eventDetailController.isCurrentUserAdmin((adminValue, success) -> {
            isAdmin = success && Boolean.TRUE.equals(adminValue);
            if (currentEvent != null) {
                renderComments(currentEvent);
            }
        });
    }

    /**
     * Binds all view references used by this activity.
     */
    private void bindViews() {
        heroImageFrame = findViewById(R.id.heroImageFrame);
        heroPosterImage = findViewById(R.id.heroPosterImage);
        favoriteButton = findViewById(R.id.favoriteButton);
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
        commentsContainer = findViewById(R.id.commentsContainer);
        commentEmptyText = findViewById(R.id.commentEmptyText);
        commentInputText = findViewById(R.id.commentInputText);
        commentSubmitButton = findViewById(R.id.commentSubmitButton);
        commentSortSpinner = findViewById(R.id.commentSortSpinner);
    }

    /**
     * Sets up button listeners for navigation and waitlist actions.
     */
    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        favoriteButton.setOnClickListener(view -> onFavoritePressed());
        joinWaitingListButton.setOnClickListener(view -> onWaitlistButtonPressed());
        commentSubmitButton.setOnClickListener(view -> postComment(null, null));

        if (commentSortSpinner != null) {
            commentSortSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                /**
                 * Handles on Item Selected.
                 */
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    commentSortMode = CommentSortMode.fromPosition(position);
                    renderComments(currentEvent);
                }

                /**
                 * Handles on Nothing Selected.
                 */
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {
                }
            });
        }
    }

    /**
     * Binds fallback event content from the intent extras before
     * the full event details are loaded from Firestore.
     */
    private void bindFallbackContent() {
        renderState(eventDetailController.buildFallbackState(
                getIntent().getStringExtra(EXTRA_EVENT_TITLE),
                getIntent().getStringExtra(EXTRA_EVENT_PRICE),
                getIntent().getStringExtra(EXTRA_EVENT_LOCATION),
                getIntent().getStringExtra(EXTRA_EVENT_DATE),
                getIntent().getStringExtra(EXTRA_EVENT_DEADLINE),
                getIntent().getStringExtra(EXTRA_EVENT_CATEGORY),
                shouldStartUiTestOnWaitlist()
        ));
        updateFavoriteUi();
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
        loadFavoriteState();
        eventDetailController.loadEventActionState(currentEventId, (state, success) -> {
            setLoading(false);
            if (!success || state == null) {
                showErrorState(getString(R.string.event_detail_error));
                return;
            }

            renderState(state);
        });
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
     * Handles presses on the main action button based on the current
     * user and event state.
     */
    private void onWaitlistButtonPressed() {
        if (!joinWaitingListButton.isEnabled()) {
            return;
        }

        EventDetailData.NextAction nextAction = eventDetailController.resolveNextAction(currentScreenState);
        switch (nextAction) {
            case NAVIGATE_MANAGE:
                openManageEventScreen();
                return;
            case NAVIGATE_OFFER:
                openOfferScreen();
                return;
            case LEAVE_WAITLIST:
                leaveWaitingList();
                return;
            case SHOW_JOIN_DIALOG:
                requireCompletedProfile(
                        this::showLotteryCriteriaDialog,
                        buildDeferredEventActionIntent(DeferredOnboardingNavigator.ACTION_AUTO_JOIN)
                );
                return;
            case SHOW_INVITE_DIALOG:
                requireCompletedProfile(
                        this::showInviteResponseDialog,
                        buildDeferredEventActionIntent(DeferredOnboardingNavigator.ACTION_AUTO_JOIN)
                );
                return;
            case NONE:
            default:
                break;
        }
    }

    /**
     * Opens the organizer event management screen for the current event.
     */
    private void openManageEventScreen() {
        if (TextUtils.isEmpty(currentEventId)) {
            return;
        }

        shouldRefreshOnResume = true;
        Intent intent = new Intent(this, EditEventActivity.class);
        intent.putExtra(EditEventActivity.EXTRA_EVENT_ID, currentEventId);
        intent.putExtra(EditEventActivity.EXTRA_EVENT_TITLE,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_TITLE) : currentEvent.getTitle());
        intent.putExtra(EditEventActivity.EXTRA_EVENT_LOCATION,
                eventDetailController.buildManageLocationText(currentEvent, getIntent().getStringExtra(EXTRA_EVENT_LOCATION)));
        intent.putExtra(EditEventActivity.EXTRA_EVENT_DATE,
                eventDetailController.buildManageDateText(currentEvent, getIntent().getStringExtra(EXTRA_EVENT_DATE)));
        intent.putExtra(EditEventActivity.EXTRA_EVENT_PRICE,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_PRICE) : EventDisplayFormatter.price(currentEvent));
        intent.putExtra(EditEventActivity.EXTRA_EVENT_DESCRIPTION,
                currentEvent == null ? null : UiHelper.cleanText(currentEvent.getDescription()));
        intent.putExtra(EditEventActivity.EXTRA_EVENT_PARTICIPANTS,
                currentEvent == null ? null : String.valueOf(currentEvent.getCapacity()));
        intent.putExtra(EditEventActivity.EXTRA_REGISTRATION_START,
                currentEvent == null ? null : eventDetailController.buildManageRegistrationText(currentEvent.getRegistrationOpen()));
        intent.putExtra(EditEventActivity.EXTRA_REGISTRATION_END,
                currentEvent == null ? null : eventDetailController.buildManageRegistrationText(currentEvent.getRegistrationDeadline()));
        intent.putExtra(EditEventActivity.EXTRA_EVENT_CATEGORY,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_CATEGORY) : currentEvent.getCategory());
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
        Intent intent = new Intent(this, OfferResponseActivity.class);
        intent.putExtra(OfferResponseActivity.EXTRA_EVENT_ID, currentEventId);
        intent.putExtra(OfferResponseActivity.EXTRA_EVENT_TITLE,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_TITLE) : currentEvent.getTitle());
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
        if (isUiTestFallbackMode()) {
            return;
        }
        if (TextUtils.isEmpty(currentEventId)) {
            return;
        }

        if (shouldRefreshOnResume) {
            shouldRefreshOnResume = false;
            loadEventDetails();
            return;
        }

        loadFavoriteState();
    }

    /**
     * Loads favorite state.
     */
    private void loadFavoriteState() {
        isFavoriteLoading = true;
        updateFavoriteUi();
        exploreController.loadSavedEventIds((savedEventIds, success) -> {
            userSavedEventIds = new ArrayList<>(savedEventIds == null ? new ArrayList<>() : savedEventIds);
            isFavoriteSaved = success && !TextUtils.isEmpty(currentEventId) && userSavedEventIds.contains(currentEventId);
            isFavoriteLoading = false;
            updateFavoriteUi();
            maybeHandleDeferredFavoriteSave();
        });
    }

    /**
     * Handles on Favorite Pressed.
     */
    private void onFavoritePressed() {
        if (favoriteButton == null || isFavoriteLoading || isFavoriteUpdating || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        requireCompletedProfile(
                this::toggleFavoriteSavedState,
                buildDeferredEventActionIntent(DeferredOnboardingNavigator.ACTION_AUTO_SAVE)
        );
    }

    /**
     * Handles toggle Favorite Saved State.
     */
    private void toggleFavoriteSavedState() {
        isFavoriteUpdating = true;
        isFavoriteSaved = !isFavoriteSaved;
        boolean isSaving = isFavoriteSaved;
        updateFavoriteUi();

        exploreController.toggleSavedEvent(userSavedEventIds, currentEventId, isSaving, (savedEventIds, success) -> {
            isFavoriteUpdating = false;
            userSavedEventIds = new ArrayList<>(savedEventIds == null ? new ArrayList<>() : savedEventIds);
            isFavoriteSaved = success ? isSaving : !isSaving;
            updateFavoriteUi();
            if (!success) {
                Toast.makeText(this, R.string.event_detail_save_failure, Toast.LENGTH_SHORT).show();
            }
            loadFavoriteState();
        });
    }

    /**
     * Updates favorite ui.
     */
    private void updateFavoriteUi() {
        if (favoriteButton == null) {
            return;
        }

        favoriteButton.setEnabled(!isFavoriteLoading && !isFavoriteUpdating);
        favoriteButton.setAlpha((isFavoriteLoading || isFavoriteUpdating) ? 0.6f : 1f);
        favoriteButton.setImageResource(isFavoriteSaved ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
        favoriteButton.setColorFilter(ContextCompat.getColor(
                this,
                isFavoriteSaved ? R.color.favorite_active : R.color.white
        ));
    }

    /**
     * Shows the lottery criteria dialog before joining the waiting list.
     */
    private void showLotteryCriteriaDialog() {
        if (TextUtils.isEmpty(currentEventId) || isJoiningWaitlist || isLeavingWaitlist) {
            return;
        }

        Dialog dialog = AppDialogHelper.createDialog(this, R.layout.dialog_join_waitlist, true);
        View dialogView = dialog.findViewById(android.R.id.content);

        ImageView closeButton = dialogView.findViewById(R.id.closeLotteryCriteriaDialogButton);
        TextView eligibilityBodyText = dialogView.findViewById(R.id.eligibilityBodyText);
        TextView selectionBodyText = dialogView.findViewById(R.id.selectionBodyText);
        MaterialButton confirmButton = dialogView.findViewById(R.id.confirmJoinWaitlistButton);

        eligibilityBodyText.setText(eventDetailController.buildEligibilityCriteriaText(currentEvent));
        selectionBodyText.setText(eventDetailController.buildSelectionCriteriaText(currentEvent));

        closeButton.setOnClickListener(view -> dialog.dismiss());
        confirmButton.setOnClickListener(view -> joinWaitingList(dialog, confirmButton));

        AppDialogHelper.showWrapContent(dialog, UiHelper.dpToPx(this, 342));
    }

    /**
     * Shows the invite response dialog for private events.
     */
    private void showInviteResponseDialog() {
        if (TextUtils.isEmpty(currentEventId) || isJoiningWaitlist || isLeavingWaitlist) {
            return;
        }

        Dialog dialog = AppDialogHelper.createDialog(this, R.layout.dialog_invite_response, true);
        View dialogView = dialog.findViewById(android.R.id.content);

        ImageView closeButton = dialogView.findViewById(R.id.closeInviteDialogButton);
        MaterialButton acceptButton = dialogView.findViewById(R.id.acceptInviteButton);
        MaterialButton declineButton = dialogView.findViewById(R.id.declineInviteButton);

        closeButton.setOnClickListener(view -> dialog.dismiss());
        acceptButton.setOnClickListener(view -> acceptInvite(dialog, acceptButton, declineButton));
        declineButton.setOnClickListener(view -> declineInvite(dialog, acceptButton, declineButton));

        AppDialogHelper.showWrapContent(dialog, UiHelper.dpToPx(this, 320));
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
        pendingJoinDialog = dialog;
        pendingJoinConfirmButton = confirmButton;

        if (!shouldCollectJoinLocation()) {
            completeJoinWaitingList(null);
            return;
        }

        if (hasLocationPermission()) {
            captureLocationAndJoin();
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST
        );
    }

    /**
     * Handles accept Invite.
     */
    private void acceptInvite(Dialog dialog, MaterialButton acceptButton, MaterialButton declineButton) {
        if (isJoiningWaitlist || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        isJoiningWaitlist = true;
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        joinWaitingListButton.setEnabled(false);

        eventDetailController.acceptInvite(currentEventId, (AppResult<Void> result, boolean success) -> {
            isJoiningWaitlist = false;
            acceptButton.setEnabled(true);
            declineButton.setEnabled(true);
            joinWaitingListButton.setEnabled(true);

            if (result == null) {
                Toast.makeText(this, R.string.event_invite_action_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!result.isSuccess()) {
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
            loadEventDetails();
        });
    }

    /**
     * Handles decline Invite.
     */
    private void declineInvite(Dialog dialog, MaterialButton acceptButton, MaterialButton declineButton) {
        if (isLeavingWaitlist || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        isLeavingWaitlist = true;
        acceptButton.setEnabled(false);
        declineButton.setEnabled(false);
        joinWaitingListButton.setEnabled(false);

        eventDetailController.declineInvite(currentEventId, (AppResult<Void> result, boolean success) -> {
            isLeavingWaitlist = false;
            acceptButton.setEnabled(true);
            declineButton.setEnabled(true);
            joinWaitingListButton.setEnabled(true);

            if (result == null) {
                Toast.makeText(this, R.string.event_invite_action_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!result.isSuccess()) {
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
            Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
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

        if (isUiTestFallbackMode()) {
            renderState(eventDetailController.buildFallbackState(
                    getIntent().getStringExtra(EXTRA_EVENT_TITLE),
                    getIntent().getStringExtra(EXTRA_EVENT_PRICE),
                    getIntent().getStringExtra(EXTRA_EVENT_LOCATION),
                    getIntent().getStringExtra(EXTRA_EVENT_DATE),
                    getIntent().getStringExtra(EXTRA_EVENT_DEADLINE),
                    getIntent().getStringExtra(EXTRA_EVENT_CATEGORY),
                    false
            ));
            Toast.makeText(this, R.string.event_detail_leave_success, Toast.LENGTH_SHORT).show();
            return;
        }

        isLeavingWaitlist = true;
        joinWaitingListButton.setEnabled(false);

        eventDetailController.leaveWaitingList(currentEventId, (AppResult<Void> result, boolean success) -> {
            isLeavingWaitlist = false;
            joinWaitingListButton.setEnabled(true);

            if (result == null) {
                Toast.makeText(this, R.string.event_detail_leave_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!result.isSuccess()) {
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
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
        if (UiHelper.isBlank(value)) {
            view.setVisibility(View.GONE);
            return;
        }

        view.setVisibility(View.VISIBLE);
        view.setText(value);
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
     * Returns whether h.as Location Permission
     */
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Returns whether s.hould Collect Join Location
     */
    private boolean shouldCollectJoinLocation() {
        return currentEvent != null && Boolean.TRUE.equals(currentEvent.getGeoloc());
    }

    /**
     * Handles capture Location And Join.
     */
    private void captureLocationAndJoin() {
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        fusedLocationProviderClient
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken())
                .addOnSuccessListener(this, location -> {
                    if (location == null) {
                        handleUnavailableLocation();
                        return;
                    }

                    if (isSyncingWaitlistLocation) {
                        completeWaitlistLocationSync(location);
                        return;
                    }

                    completeJoinWaitingList(location);
                })
                .addOnFailureListener(this, exception -> handleUnavailableLocation());
    }

    /**
     * Handles unavailable location.
     */
    private void handleUnavailableLocation() {
        if (isSyncingWaitlistLocation) {
            isSyncingWaitlistLocation = false;
            hasAttemptedAutoWaitlistLocationSync = true;
            return;
        }

        completeJoinWaitingList(null);
    }

    /**
     * Handles complete Join Waiting List.
     */
    private void completeJoinWaitingList(Location location) {
        Double latitude = location == null ? null : location.getLatitude();
        Double longitude = location == null ? null : location.getLongitude();
        eventDetailController.joinWaitingList(currentEventId, latitude, longitude, new java.util.Date(), (AppResult<Void> result, boolean success) -> {
            isJoiningWaitlist = false;
            if (pendingJoinConfirmButton != null) {
                pendingJoinConfirmButton.setEnabled(true);
            }
            joinWaitingListButton.setEnabled(true);

            if (result == null) {
                clearPendingJoinState();
                Toast.makeText(this, R.string.event_detail_join_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!result.isSuccess()) {
                clearPendingJoinState();
                Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (pendingJoinDialog != null) {
                pendingJoinDialog.dismiss();
            }
            clearPendingJoinState();
            Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
            loadEventDetails();
        });
    }

    /**
     * Handles complete Waitlist Location Sync.
     */
    private void completeWaitlistLocationSync(Location location) {
        Double latitude = location == null ? null : location.getLatitude();
        Double longitude = location == null ? null : location.getLongitude();
        eventDetailController.syncWaitlistLocation(currentEventId, latitude, longitude, new java.util.Date(), (result, success) -> {
            isSyncingWaitlistLocation = false;
            hasAttemptedAutoWaitlistLocationSync = true;
            if (result == null || !result.isSuccess()) {
                return;
            }

            loadEventDetails();
        });
    }

    /**
     * Handles fail Join Waiting List.
     */
    private void failJoinWaitingList(int messageResId) {
        isJoiningWaitlist = false;
        if (pendingJoinConfirmButton != null) {
            pendingJoinConfirmButton.setEnabled(true);
        }
        joinWaitingListButton.setEnabled(true);
        clearPendingJoinState();
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show();
    }

    /**
     * Clears pending join state.
     */
    private void clearPendingJoinState() {
        pendingJoinDialog = null;
        pendingJoinConfirmButton = null;
    }

    /**
     * Handles on Request Permissions Result.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != LOCATION_PERMISSION_REQUEST) {
            return;
        }

        if (isSyncingWaitlistLocation) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureLocationAndJoin();
                return;
            }

            isSyncingWaitlistLocation = false;
            hasAttemptedAutoWaitlistLocationSync = true;
            return;
        }

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            captureLocationAndJoin();
            return;
        }

        completeJoinWaitingList(null);
    }

    /**
     * Renders the provided event detail screen state.
     *
     * @param state the state to render
     */
    private void renderState(EventDetailData state) {
        if (state == null) {
            return;
        }

        currentScreenState = state;
        currentEvent = state.getCurrentEvent();
        isOrganizer = currentEvent != null
                && eventDetailController != null
                && currentEvent.getOrganizerId() != null
                && currentEvent.getOrganizerId().equals(eventDetailController.getCurrentDeviceId());

        if (state.getStatus() == EventDetailData.Status.ERROR) {
            showErrorState(state.getErrorMessage());
            return;
        }

        errorText.setVisibility(View.GONE);
        titleText.setText(state.getTitle());
        priceText.setText(state.getPriceText());
        locationText.setVisibility(View.VISIBLE);
        locationText.setText(state.getLocationText());
        dateText.setVisibility(View.VISIBLE);
        dateText.setText(state.getDateText());
        organizerText.setVisibility(View.VISIBLE);
        organizerText.setText(state.getOrganizerText());
        bindOptionalText(heroDeadlineText, state.getHeroDeadlineText());
        bindOptionalText(descriptionText, state.getDescriptionText());
        bindOptionalText(registrationOpenText, state.getRegistrationOpenText());
        bindOptionalText(registrationDeadlineText, state.getRegistrationDeadlineText());
        heroImageFrame.setBackgroundResource(state.getHeroBackgroundRes());
        renderHeroPoster(state.getCurrentEvent());

        entrantCountText.setVisibility(state.shouldShowEntrantCount() ? View.VISIBLE : View.GONE);
        entrantCountText.setText(getResources().getQuantityString(
                R.plurals.event_detail_entrant_count,
                state.getEntrantCount(),
                state.getEntrantCount()
        ));

        showFooterState(
                state.shouldShowWaitlistMessage(),
                state.isButtonEnabled(),
                state.getButtonTextRes(),
                state.getButtonBackgroundRes(),
                ContextCompat.getColor(this, state.getButtonTextColorRes()),
                state.getSubtext(),
                state.shouldShowEntrantCount()
        );

        renderComments(state.getCurrentEvent());
        maybeAutoSyncWaitlistLocation();
        maybeHandleDeferredJoinAction();
    }

    /**
     * Handles maybe Auto Sync Waitlist Location.
     */
    private void maybeAutoSyncWaitlistLocation() {
        if (!shouldAutoSyncWaitlistLocation() || hasAttemptedAutoWaitlistLocationSync || isSyncingWaitlistLocation) {
            return;
        }

        hasAttemptedAutoWaitlistLocationSync = true;
        if (hasLocationPermission()) {
            isSyncingWaitlistLocation = true;
            captureLocationAndJoin();
            return;
        }

        isSyncingWaitlistLocation = true;
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST
        );
    }

    /**
     * Returns whether s.hould Auto Sync Waitlist Location
     */
    private boolean shouldAutoSyncWaitlistLocation() {
        if (currentEvent == null || !Boolean.TRUE.equals(currentEvent.getGeoloc())) {
            return false;
        }

        String deviceId = eventDetailController == null ? null : eventDetailController.getCurrentDeviceId();
        if (TextUtils.isEmpty(deviceId) || currentEvent.getWaitingList() == null) {
            return false;
        }
        if (currentEvent.getWaitingList().list == null || !currentEvent.getWaitingList().list.contains(deviceId)) {
            return false;
        }

        com.example.allot.model.event.WaitlistJoinLocation joinLocation =
                currentEvent.getWaitingList().getJoinLocations().get(deviceId);
        return joinLocation == null
                || joinLocation.getLatitude() == null
                || joinLocation.getLongitude() == null;
    }

    /**
     * Handles require Completed Profile.
     */
    private void requireCompletedProfile(Runnable onReady, Intent onboardingIntent) {
        if (getIntent().getBooleanExtra(EXTRA_UI_TEST_BYPASS_PROFILE_GATE, false)) {
            onReady.run();
            return;
        }

        userController.loadCurrentUser((user, success) -> {
            if (success && userController.hasCompletedProfile(user)) {
                onReady.run();
                return;
            }

            DeferredOnboardingNavigator.openOnboarding(this, onboardingIntent, false);
        });
    }

    /**
     * Builds deferred event action intent.
     */
    private Intent buildDeferredEventActionIntent(String action) {
        return DeferredOnboardingNavigator.createEventActionIntent(
                this,
                currentEventId,
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_TITLE) : currentEvent.getTitle(),
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_LOCATION) : EventDisplayFormatter.location(currentEvent),
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_DATE) : EventDisplayFormatter.date(currentEvent),
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_PRICE) : EventDisplayFormatter.price(currentEvent),
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_DEADLINE) : EventDisplayFormatter.deadline(currentEvent),
                currentEvent == null ? getIntent().getStringExtra(EXTRA_EVENT_CATEGORY) : currentEvent.getCategory(),
                action
        );
    }

    /**
     * Handles maybe Handle Deferred Join Action.
     */
    private void maybeHandleDeferredJoinAction() {
        if (!shouldAutoOpenJoinDialog || currentScreenState == null) {
            return;
        }

        EventDetailData.NextAction nextAction = eventDetailController.resolveNextAction(currentScreenState);
        if (nextAction != EventDetailData.NextAction.SHOW_JOIN_DIALOG
                && nextAction != EventDetailData.NextAction.SHOW_INVITE_DIALOG) {
            return;
        }

        shouldAutoOpenJoinDialog = false;
        if (nextAction == EventDetailData.NextAction.SHOW_INVITE_DIALOG) {
            showInviteResponseDialog();
            return;
        }

        showLotteryCriteriaDialog();
    }

    /**
     * Handles maybe Handle Deferred Favorite Save.
     */
    private void maybeHandleDeferredFavoriteSave() {
        if (!shouldAutoSaveEvent || isFavoriteLoading || isFavoriteUpdating) {
            return;
        }

        shouldAutoSaveEvent = false;
        if (isFavoriteSaved) {
            return;
        }

        toggleFavoriteSavedState();
    }

    /**
     * Handles render Hero Poster.
     */
    private void renderHeroPoster(Event event) {
        String posterUrl = event == null ? null : event.getPosterUrl();
        if (TextUtils.isEmpty(posterUrl)) {
            heroPosterImage.setVisibility(View.GONE);
            heroPosterImage.setImageDrawable(null);
            return;
        }

        heroPosterImage.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(posterUrl)
                .centerCrop()
                .into(heroPosterImage);
    }

    /**
     * Handles render Comments.
     */
    private void renderComments(Event event) {
        List<EventComment> comments = event == null ? null : event.getComments();
        commentsContainer.removeAllViews();

        if (comments == null || comments.isEmpty()) {
            commentEmptyText.setVisibility(View.VISIBLE);
            return;
        }

        commentEmptyText.setVisibility(View.GONE);

        Map<String, List<EventComment>> repliesByParent = new HashMap<>();
        List<EventComment> rootComments = new ArrayList<>();
        for (EventComment comment : comments) {
            if (comment == null) {
                continue;
            }

            if (UiHelper.isBlank(comment.getParentId())) {
                rootComments.add(comment);
                continue;
            }

            repliesByParent
                    .computeIfAbsent(comment.getParentId(), key -> new ArrayList<>())
                    .add(comment);
        }

        rootComments.sort(commentComparator(commentSortMode));
        for (EventComment root : rootComments) {
            renderCommentTree(root, 0, repliesByParent, commentsContainer);
        }
    }

    /**
     * Handles render Comment Tree.
     */
    private void renderCommentTree(EventComment comment,
                                   int depth,
                                   Map<String, List<EventComment>> repliesByParent,
                                   LinearLayout container) {
        if (comment == null) {
            return;
        }

        int layoutRes = depth == 0 ? R.layout.item_event_comment : R.layout.item_event_comment_reply;
        View commentView = getLayoutInflater().inflate(layoutRes, container, false);
        TextView authorText = commentView.findViewById(R.id.commentAuthorText);
        TextView dateText = commentView.findViewById(R.id.commentDateText);
        TextView bodyText = commentView.findViewById(R.id.commentBodyText);
        TextView upvoteButton = commentView.findViewById(R.id.commentUpvoteButton);
        TextView downvoteButton = commentView.findViewById(R.id.commentDownvoteButton);
        TextView replyButton = commentView.findViewById(R.id.commentReplyButton);
        TextView deleteButton = commentView.findViewById(R.id.commentDeleteButton);
        LinearLayout replyListContainer = commentView.findViewById(R.id.commentReplyListContainer);
        LinearLayout replyInputContainer = commentView.findViewById(R.id.commentReplyInputContainer);

        authorText.setText(resolveAuthorName(comment));
        dateText.setText(formatCommentDate(comment.getCreatedAt()));
        bodyText.setText(UiHelper.cleanText(comment.getText()));

        bindVoteButtons(upvoteButton, downvoteButton, comment);
        replyButton.setOnClickListener(view -> toggleReplyInput(replyInputContainer, comment));
        bindDeleteButton(deleteButton, comment);
        container.addView(commentView);

        List<EventComment> replies = repliesByParent.get(comment.getCommentId());
        if (replies == null || replies.isEmpty()) {
            return;
        }

        replies.sort(commentComparator(commentSortMode));
        for (EventComment reply : replies) {
            renderCommentTree(reply, depth + 1, repliesByParent, replyListContainer);
        }
    }

    /**
     * Binds delete button.
     */
    private void bindDeleteButton(TextView deleteButton, EventComment comment) {
        if (deleteButton == null || comment == null || (!isOrganizer && !isAdmin)) {
            if (deleteButton != null) {
                deleteButton.setVisibility(View.GONE);
            }
            return;
        }

        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setOnClickListener(view -> deleteCommentThread(comment.getCommentId()));
    }

    /**
     * Handles toggle Reply Input.
     */
    private void toggleReplyInput(LinearLayout replyInputContainer, EventComment parentComment) {
        if (replyInputContainer.getChildCount() > 0) {
            replyInputContainer.removeAllViews();
            return;
        }

        View inputView = getLayoutInflater().inflate(
                R.layout.item_event_comment_reply_input, replyInputContainer, false);
        EditText replyInput = inputView.findViewById(R.id.replyInputText);
        TextView replySubmit = inputView.findViewById(R.id.replySubmitButton);

        replySubmit.setOnClickListener(view -> {
            if (parentComment == null) {
                return;
            }
            postComment(parentComment.getCommentId(), new CommentPostCallback() {
                /**
                 * Handles on Success.
                 */
                @Override
                public void onSuccess() {
                    replyInputContainer.removeAllViews();
                }

                /**
                 * Handles on Failure.
                 */
                @Override
                public void onFailure() {
                    replySubmit.setEnabled(true);
                }
            }, replyInput, replySubmit);
        });

        replyInputContainer.addView(inputView);
    }

    /**
     * Handles post Comment.
     */
    private void postComment(String parentId, CommentPostCallback callback) {
        postComment(parentId, callback, commentInputText, commentSubmitButton);
    }

    /**
     * Handles post Comment.
     */
    private void postComment(String parentId,
                             CommentPostCallback callback,
                             EditText inputView,
                             TextView submitButton) {
        if (isPostingComment || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        String message = inputView.getText().toString().trim();
        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, R.string.event_comment_post_failure, Toast.LENGTH_SHORT).show();
            return;
        }

        isPostingComment = true;
        submitButton.setEnabled(false);

        eventDetailController.addComment(currentEventId, message, parentId, (result, success) -> {
            isPostingComment = false;
            submitButton.setEnabled(true);

            if (result == null || !result.isSuccess()) {
                Toast.makeText(this, R.string.event_comment_post_failure, Toast.LENGTH_SHORT).show();
                if (callback != null) {
                    callback.onFailure();
                }
                return;
            }

            inputView.setText("");
            Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onSuccess();
            }
            refreshCommentsOnly();
        });
    }

    /**
     * Binds vote buttons.
     */
    private void bindVoteButtons(TextView upvoteButton, TextView downvoteButton, EventComment comment) {
        if (upvoteButton == null || downvoteButton == null || comment == null) {
            return;
        }

        updateVoteButtonText(upvoteButton, downvoteButton, comment);

        upvoteButton.setOnClickListener(view -> voteOnComment(comment, true));
        downvoteButton.setOnClickListener(view -> voteOnComment(comment, false));
    }

    /**
     * Updates vote button text.
     */
    private void updateVoteButtonText(TextView upvoteButton, TextView downvoteButton, EventComment comment) {
        String upvoteLabel = getString(R.string.event_comment_upvote) + " " + Math.max(0, comment.getUpvotes());
        String downvoteLabel = getString(R.string.event_comment_downvote) + " " + Math.max(0, comment.getDownvotes());
        upvoteButton.setText(upvoteLabel);
        downvoteButton.setText(downvoteLabel);
    }

    /**
     * Handles vote On Comment.
     */
    private void voteOnComment(EventComment comment, boolean isUpvote) {
        if (isVotingComment || comment == null || TextUtils.isEmpty(currentEventId)) {
            return;
        }

        isVotingComment = true;
        eventDetailController.voteOnComment(currentEventId, comment.getCommentId(), isUpvote, (result, success) -> {
            isVotingComment = false;
            if (result == null || !result.isSuccess()) {
                int messageRes = result == null ? R.string.event_comment_post_failure : result.getMessageResId();
                Toast.makeText(this, messageRes, Toast.LENGTH_SHORT).show();
                return;
            }
            refreshCommentsOnly();
        });
    }

    /**
     * Deletes comment thread.
     */
    private void deleteCommentThread(String commentId) {
        if (TextUtils.isEmpty(currentEventId) || TextUtils.isEmpty(commentId)) {
            return;
        }

        eventDetailController.deleteCommentThread(currentEventId, commentId, (result, success) -> {
            if (result == null || !result.isSuccess()) {
                Toast.makeText(this, R.string.event_comment_delete_failure, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, result.getMessageResId(), Toast.LENGTH_SHORT).show();
            refreshCommentsOnly();
        });
    }

    /**
     * Handles refresh Comments Only.
     */
    private void refreshCommentsOnly() {
        if (TextUtils.isEmpty(currentEventId)) {
            return;
        }

        eventDetailController.loadEventComments(currentEventId, (event, success) -> {
            if (!success || event == null) {
                return;
            }
            currentEvent = event;
            renderComments(event);
        });
    }

    /**
     * Handles resolve Author Name.
     */
    private String resolveAuthorName(EventComment comment) {
        if (comment == null || UiHelper.isBlank(comment.getAuthorName())) {
            return getString(R.string.event_comment_author_unknown);
        }
        return comment.getAuthorName();
    }

    /**
     * Handles format Comment Date.
     */
    private String formatCommentDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        return formatter.format(date);
    }

    /**
     * Handles comment Comparator.
     */
    private Comparator<EventComment> commentComparator(CommentSortMode mode) {
        return (first, second) -> {
            if (mode == CommentSortMode.MOST_UPVOTED) {
                int firstVotes = first == null ? 0 : first.getUpvotes();
                int secondVotes = second == null ? 0 : second.getUpvotes();
                int voteCompare = Integer.compare(secondVotes, firstVotes);
                if (voteCompare != 0) {
                    return voteCompare;
                }
            } else if (mode == CommentSortMode.MOST_DOWNVOTED) {
                int firstVotes = first == null ? 0 : first.getDownvotes();
                int secondVotes = second == null ? 0 : second.getDownvotes();
                int voteCompare = Integer.compare(secondVotes, firstVotes);
                if (voteCompare != 0) {
                    return voteCompare;
                }
            }

            Date firstDate = first == null ? null : first.getCreatedAt();
            Date secondDate = second == null ? null : second.getCreatedAt();
            if (firstDate == null && secondDate == null) {
                return 0;
            }
            if (firstDate == null) {
                return 1;
            }
            if (secondDate == null) {
                return -1;
            }
            return secondDate.compareTo(firstDate);
        };
    }

    private enum CommentSortMode {
        NEWEST,
        MOST_UPVOTED,
        MOST_DOWNVOTED;

        /**
         * Handles from Position.
         */
        static CommentSortMode fromPosition(int position) {
            if (position == 1) {
                return MOST_UPVOTED;
            }
            if (position == 2) {
                return MOST_DOWNVOTED;
            }
            return NEWEST;
        }
    }

    private interface CommentPostCallback {
        /**
         * Handles on Success.
         */
        void onSuccess();
        /**
         * Handles on Failure.
         */
        void onFailure();
    }

    /**
     * Returns whether i.s Ui Test Fallback Mode
     */
    private boolean isUiTestFallbackMode() {
        return getIntent().getBooleanExtra(EXTRA_UI_TEST_SKIP_NETWORK_LOAD, false);
    }

    /**
     * Returns whether s.hould Start Ui Test On Waitlist
     */
    private boolean shouldStartUiTestOnWaitlist() {
        return getIntent().getBooleanExtra(EXTRA_UI_TEST_START_ON_WAITLIST, false);
    }
}









