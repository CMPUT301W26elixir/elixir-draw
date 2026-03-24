package com.example.allot.view.event;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.controller.event.EventDetailController;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventComment;
import com.example.allot.model.event.EventDetailData;
import com.example.allot.view.shared.AppDialogHelper;
import com.example.allot.view.shared.EventDisplayFormatter;
import com.example.allot.view.shared.UiHelper;
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

    private EventDetailController eventDetailController;

    private String currentEventId;
    private boolean isJoiningWaitlist;
    private boolean isLeavingWaitlist;
    private boolean isPostingComment;
    private Event currentEvent;
    private EventDetailData currentScreenState;
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
    private LinearLayout commentsContainer;
    private TextView commentEmptyText;
    private EditText commentInputText;
    private TextView commentSubmitButton;

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
        commentsContainer = findViewById(R.id.commentsContainer);
        commentEmptyText = findViewById(R.id.commentEmptyText);
        commentInputText = findViewById(R.id.commentInputText);
        commentSubmitButton = findViewById(R.id.commentSubmitButton);
    }

    /**
     * Sets up button listeners for navigation and waitlist actions.
     */
    private void setupListeners() {
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> getOnBackPressedDispatcher().onBackPressed());
        joinWaitingListButton.setOnClickListener(view -> onWaitlistButtonPressed());
        commentSubmitButton.setOnClickListener(view -> postComment(null, null));
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
                getIntent().getStringExtra(EXTRA_EVENT_CATEGORY)
        ));
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
                showLotteryCriteriaDialog();
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

        eventDetailController.joinWaitingList(currentEventId, (AppResult<Void> result, boolean success) -> {
            isJoiningWaitlist = false;
            confirmButton.setEnabled(true);
            joinWaitingListButton.setEnabled(true);

            if (result == null) {
                Toast.makeText(this, R.string.event_detail_join_failure, Toast.LENGTH_SHORT).show();
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
    }

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

        rootComments.sort(commentComparator());
        for (EventComment root : rootComments) {
            renderCommentTree(root, 0, repliesByParent, commentsContainer);
        }
    }

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
        TextView replyButton = commentView.findViewById(R.id.commentReplyButton);
        LinearLayout replyListContainer = commentView.findViewById(R.id.commentReplyListContainer);
        LinearLayout replyInputContainer = commentView.findViewById(R.id.commentReplyInputContainer);

        authorText.setText(resolveAuthorName(comment));
        dateText.setText(formatCommentDate(comment.getCreatedAt()));
        bodyText.setText(UiHelper.cleanText(comment.getText()));

        replyButton.setOnClickListener(view -> toggleReplyInput(replyInputContainer, comment));
        container.addView(commentView);

        List<EventComment> replies = repliesByParent.get(comment.getCommentId());
        if (replies == null || replies.isEmpty()) {
            return;
        }

        replies.sort(commentComparator());
        for (EventComment reply : replies) {
            renderCommentTree(reply, depth + 1, repliesByParent, replyListContainer);
        }
    }

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
                @Override
                public void onSuccess() {
                    replyInputContainer.removeAllViews();
                }

                @Override
                public void onFailure() {
                    replySubmit.setEnabled(true);
                }
            }, replyInput, replySubmit);
        });

        replyInputContainer.addView(inputView);
    }

    private void postComment(String parentId, CommentPostCallback callback) {
        postComment(parentId, callback, commentInputText, commentSubmitButton);
    }

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
            loadEventDetails();
        });
    }

    private String resolveAuthorName(EventComment comment) {
        if (comment == null || UiHelper.isBlank(comment.getAuthorName())) {
            return getString(R.string.event_comment_author_unknown);
        }
        return comment.getAuthorName();
    }

    private String formatCommentDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        return formatter.format(date);
    }

    private Comparator<EventComment> commentComparator() {
        return (first, second) -> {
            Date firstDate = first == null ? null : first.getCreatedAt();
            Date secondDate = second == null ? null : second.getCreatedAt();
            if (firstDate == null && secondDate == null) {
                return 0;
            }
            if (firstDate == null) {
                return -1;
            }
            if (secondDate == null) {
                return 1;
            }
            return firstDate.compareTo(secondDate);
        };
    }

    private interface CommentPostCallback {
        void onSuccess();
        void onFailure();
    }
}









