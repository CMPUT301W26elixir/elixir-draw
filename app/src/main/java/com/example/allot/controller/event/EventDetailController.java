package com.example.allot.controller.event;

import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventActionState;
import com.example.allot.model.event.EventComment;
import com.example.allot.model.event.EventDetailData;
import com.example.allot.model.profile.User;
import com.example.allot.view.shared.EventDisplayFormatter;
import com.example.allot.view.shared.UiHelper;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
/**
 * Builds and updates the data shown on the event detail screen.
 */
public class EventDetailController {
    private final EventRepository eventRepository;
    private final UserController userController;
    private final EventActionStateFactory eventActionStateFactory;
    private final EventDetailViewService eventDetailViewService;

    public EventDetailController(android.content.Context context) {
        this(
                new EventRepository(),
                new UserController(context),
                new EventActionStateFactory(),
                new EventDetailViewService()
        );
    }

    EventDetailController(EventRepository eventRepository,
                          UserController userController,
                          EventActionStateFactory eventActionStateFactory,
                          EventDetailViewService eventDetailViewService) {
        this.eventRepository = eventRepository;
        this.userController = userController;
        this.eventActionStateFactory = eventActionStateFactory;
        this.eventDetailViewService = eventDetailViewService;
    }

    /**
     * Builds the fallback event detail state from intent data.
     */
    public EventDetailData buildFallbackState(String title,
                                                     String price,
                                                     String location,
                                                     String date,
                                                     String deadline,
                                                     String category) {
        return new EventDetailData(
                EventDetailData.Status.CONTENT,
                title,
                price,
                buildLocationText(location),
                buildEventDateText(date),
                deadline,
                "Organizer TBA",
                null,
                null,
                null,
                getHeroBackgroundRes(category),
                0,
                false,
                true,
                R.string.event_detail_join_waiting_list,
                R.drawable.bg_waitlist_button,
                R.color.black,
                null,
                true,
                null,
                null,
                null,
                EventDetailData.NextAction.SHOW_JOIN_DIALOG
        );
    }

    /**
     * Loads the full event detail state from Firestore.
     */
    public void loadEventActionState(String eventId, OnCompleteListener<EventDetailData> listener) {
        if (isBlank(eventId)) {
            listener.onComplete(buildErrorState(), false);
            return;
        }

        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(buildErrorState(), false);
                return;
            }

            EventActionState state = eventActionStateFactory.create(event, userController.getCurrentDeviceId());
            loadOrganizerName(event.getOrganizerId(), organizerName ->
                    listener.onComplete(buildLoadedState(event, state, organizerName), true));
        });
    }

    /**
     * Returns the next action the view should take when the primary button is pressed.
     */
    public EventDetailData.NextAction resolveNextAction(EventDetailData state) {
        return state == null ? EventDetailData.NextAction.NONE : state.getNextAction();
    }

    /**
     * Joins the waiting list for the current event.
     */
    public void joinWaitingList(String eventId,
                                Double latitude,
                                Double longitude,
                                Date joinedAt,
                                OnCompleteListener<AppResult<Void>> listener) {
        String deviceId = userController.getCurrentDeviceId();
        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(AppResult.failure(R.string.event_detail_join_failure), false);
                return;
            }

            if (event.isPrivate() && !event.isInvited(deviceId)) {
                listener.onComplete(AppResult.failure(R.string.event_detail_invite_only), false);
                return;
            }

            eventRepository.joinWaitingList(eventId, deviceId, latitude, longitude, joinedAt, (result, joinSuccess) -> {
                if (!joinSuccess || result == null || !result) {
                    listener.onComplete(AppResult.failure(R.string.event_detail_join_failure), false);
                    return;
                }

                listener.onComplete(AppResult.success(null, R.string.event_detail_join_success), true);
            });
        });
    }

    /**
     * Leaves the waiting list for the current event.
     */
    public void leaveWaitingList(String eventId, OnCompleteListener<AppResult<Void>> listener) {
        eventRepository.leaveWaitingList(eventId, userController.getCurrentDeviceId(), (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(AppResult.failure(R.string.event_detail_leave_failure), false);
                return;
            }

            listener.onComplete(AppResult.success(null, R.string.event_detail_leave_success), true);
        });
    }

    /**
     * Adds a comment or reply to the event.
     *
     * @param eventId the event ID
     * @param message the comment text
     * @param parentId the parent comment ID if this is a reply
     * @param listener the callback that receives the action result
     */
    public void addComment(String eventId,
                           String message,
                           String parentId,
                           OnCompleteListener<AppResult<Void>> listener) {
        if (isBlank(eventId) || isBlank(message)) {
            listener.onComplete(AppResult.failure(R.string.event_comment_post_failure), false);
            return;
        }

        String deviceId = userController.getCurrentDeviceId();
        userController.getUserByDeviceId(deviceId, (User user, boolean success) -> {
            String authorName = "Anonymous";
            if (success && user != null && !isBlank(cleanText(user.getName()))) {
                authorName = user.getName();
            }

            EventComment comment = new EventComment(
                    UUID.randomUUID().toString(),
                    deviceId,
                    authorName,
                    message.trim(),
                    new Date(),
                    isBlank(parentId) ? null : parentId
            );

            eventRepository.addComment(eventId, comment, (result, addSuccess) -> {
                if (!addSuccess || result == null || !result) {
                    listener.onComplete(AppResult.failure(R.string.event_comment_post_failure), false);
                    return;
                }

                listener.onComplete(AppResult.success(null, R.string.event_comment_post_success), true);
            });
        });
    }

    /**
     * Deletes a comment thread (comment + replies) for the given event.
     *
     * @param eventId the event ID
     * @param commentId the comment to delete
     * @param listener the callback that receives the action result
     */
    public void deleteCommentThread(String eventId,
                                    String commentId,
                                    OnCompleteListener<AppResult<Void>> listener) {
        if (isBlank(eventId) || isBlank(commentId)) {
            listener.onComplete(AppResult.failure(R.string.event_comment_delete_failure), false);
            return;
        }

        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(AppResult.failure(R.string.event_comment_delete_failure), false);
                return;
            }

            String currentDeviceId = userController.getCurrentDeviceId();
            boolean isOrganizer = currentDeviceId != null && currentDeviceId.equals(event.getOrganizerId());
            if (isOrganizer) {
                deleteCommentThreadInternal(eventId, commentId, event, listener);
                return;
            }

            userController.isCurrentUserAdmin((isAdmin, adminCheckSuccess) -> {
                if (!adminCheckSuccess || !isAdmin) {
                    listener.onComplete(AppResult.failure(R.string.event_comment_delete_failure), false);
                    return;
                }

                deleteCommentThreadInternal(eventId, commentId, event, listener);
            });
        });
    }

    private void deleteCommentThreadInternal(String eventId,
                                             String commentId,
                                             Event event,
                                             OnCompleteListener<AppResult<Void>> listener) {
        List<EventComment> comments = event.getComments();
        if (comments == null || comments.isEmpty()) {
            listener.onComplete(AppResult.success(null, R.string.event_comment_delete_success), true);
            return;
        }

        Set<String> deleteIds = collectThreadIds(comments, commentId);
        if (deleteIds.isEmpty()) {
            listener.onComplete(AppResult.success(null, R.string.event_comment_delete_success), true);
            return;
        }

        List<EventComment> remaining = new java.util.ArrayList<>();
        for (EventComment comment : comments) {
            if (comment == null || deleteIds.contains(comment.getCommentId())) {
                continue;
            }
            remaining.add(comment);
        }

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("comments", remaining);
        eventRepository.updateEvent(eventId, updates, (result, updateSuccess) -> {
            if (!updateSuccess || result == null || !result) {
                listener.onComplete(AppResult.failure(R.string.event_comment_delete_failure), false);
                return;
            }
            listener.onComplete(AppResult.success(null, R.string.event_comment_delete_success), true);
        });
    }

    /**
     * Accepts the user's current offer for the given event.
     *
     * @param eventId the event whose offer is being accepted
     * @param listener the callback that receives the action result
     */
    public void acceptOffer(String eventId, OnCompleteListener<AppResult<Void>> listener) {
        eventRepository.acceptOffer(eventId, userController.getCurrentDeviceId(), (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(AppResult.failure(R.string.event_offer_action_failure), false);
                return;
            }

            listener.onComplete(AppResult.success(null, R.string.event_offer_accept_success), true);
        });
    }

    /**
     * Returns the current device ID.
     */
    public String getCurrentDeviceId() {
        return userController.getCurrentDeviceId();
    }

    /**
     * Returns whether the current user has admin privileges.
     */
    public void isCurrentUserAdmin(OnCompleteListener<Boolean> listener) {
        userController.isCurrentUserAdmin(listener);
    }

    /**
     * Declines the user's current offer and saves any new offer changes.
     *
     * @param eventId the event whose offer is being declined
     * @param listener the callback that receives the action result
     */
    public void declineOffer(String eventId, OnCompleteListener<AppResult<Void>> listener) {
        eventRepository.declineOffer(eventId, userController.getCurrentDeviceId(), (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(AppResult.failure(R.string.event_offer_action_failure), false);
                return;
            }
            listener.onComplete(AppResult.success(null, R.string.event_offer_decline_success), true);
        });
    }

    /**
     * Accepts an invite to a private event.
     */
    public void acceptInvite(String eventId, OnCompleteListener<AppResult<Void>> listener) {
        eventRepository.acceptInvite(eventId, userController.getCurrentDeviceId(), (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(AppResult.failure(R.string.event_invite_action_failure), false);
                return;
            }

            listener.onComplete(AppResult.success(null, R.string.event_invite_accept_success), true);
        });
    }

    /**
     * Declines an invite to a private event.
     */
    public void declineInvite(String eventId, OnCompleteListener<AppResult<Void>> listener) {
        eventRepository.declineInvite(eventId, userController.getCurrentDeviceId(), (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(AppResult.failure(R.string.event_invite_action_failure), false);
                return;
            }

            listener.onComplete(AppResult.success(null, R.string.event_invite_decline_success), true);
        });
    }

    /**
     * Builds the eligibility text shown in the join dialog.
     */
    public String buildEligibilityCriteriaText(Event event) {
        String closeDate = "TBA";
        if (event != null && event.getRegistrationDeadline() != null) {
            closeDate = formatLongDate(event.getRegistrationDeadline());
        }
        return String.format(Locale.getDefault(),
                "- All entrants who join before registration closes are eligible\n- Registration closes on %s",
                closeDate);
    }

    /**
     * Builds the selection text shown in the join dialog.
     */
    public String buildSelectionCriteriaText(Event event) {
        return String.format(Locale.getDefault(),
                "- %d participants will be selected\n- Selection is conducted through a randomized system draw\n- The draw occurs after registration closes",
                eventDetailViewService.getSelectionCriteriaCount(event));
    }

    private void loadOrganizerName(String organizerId, java.util.function.Consumer<String> consumer) {
        if (isBlank(organizerId)) {
            consumer.accept("Organizer TBA");
            return;
        }

        userController.getUserByDeviceId(organizerId, (User user, boolean success) -> {
            if (!success || user == null || isBlank(cleanText(user.getName()))) {
                consumer.accept("Organizer TBA");
                return;
            }

            consumer.accept(user.getName());
        });
    }

    private EventDetailData buildLoadedState(Event event, EventActionState detailState, String organizerName) {
        EventDetailViewService.FooterState footerState = eventDetailViewService.buildFooterState(detailState);
        return new EventDetailData(
                EventDetailData.Status.CONTENT,
                event.getTitle(),
                EventDisplayFormatter.price(event),
                buildLocationText(event.getLocation()),
                buildEventDateText(formatDate(event.getEventDate())),
                EventDisplayFormatter.deadline(event),
                organizerName,
                cleanText(event.getDescription()),
                buildRegistrationOpenText(event.getRegistrationOpen()),
                buildRegistrationDeadlineText(event.getRegistrationDeadline()),
                getHeroBackgroundRes(event.getCategory()),
                eventDetailViewService.getEntrantCount(event),
                footerState.shouldShowWaitlistMessage(),
                footerState.isButtonEnabled(),
                footerState.getButtonTextRes(),
                footerState.getButtonBackgroundRes(),
                footerState.getButtonTextColorRes(),
                footerState.getSubtext(),
                footerState.shouldShowEntrantCount(),
                null,
                event,
                detailState,
                resolveAction(detailState)
        );
    }

    /**
     * Builds the location text used on manage screens.
     *
     * @param event the loaded event
     * @param fallbackLocation the fallback location passed through the intent
     * @return the location text shown in the management UI
     */
    public String buildManageLocationText(Event event, String fallbackLocation) {
        return event == null
                ? fallbackLocation
                : EventDisplayFormatter.detailLocation(event.getLocation());
    }

    /**
     * Builds the event date text used on manage screens.
     *
     * @param event the loaded event
     * @param fallbackDate the fallback date passed through the intent
     * @return the date text shown in the management UI
     */
    public String buildManageDateText(Event event, String fallbackDate) {
        return event == null
                ? fallbackDate
                : EventDisplayFormatter.shortDate(event.getEventDate());
    }

    /**
     * Formats a registration date for manage screens.
     *
     * @param date the registration date to format
     * @return the formatted registration text, or a fallback when the date is missing
     */
    public String buildManageRegistrationText(Date date) {
        return EventDisplayFormatter.shortDate(date);
    }

    private EventDetailData buildErrorState() {
        return new EventDetailData(
                EventDetailData.Status.ERROR,
                null, null, null, null, null, null, null, null, null,
                R.drawable.bg_event_image_one,
                0,
                false,
                false,
                R.string.event_detail_join_waiting_list,
                R.drawable.bg_waitlist_button,
                R.color.black,
                null,
                false,
                "Could not load this event right now.",
                null,
                null,
                EventDetailData.NextAction.NONE
        );
    }

    private EventDetailData.NextAction resolveAction(EventActionState detailState) {
        if (eventDetailViewService.isManageAction(detailState)) {
            return EventDetailData.NextAction.NAVIGATE_MANAGE;
        }
        if (eventDetailViewService.hasActiveOffer(detailState)) {
            return EventDetailData.NextAction.NAVIGATE_OFFER;
        }
        if (detailState != null && detailState.getActionType() == EventActionState.ActionType.INVITED) {
            return EventDetailData.NextAction.SHOW_INVITE_DIALOG;
        }
        if (detailState != null && detailState.getActionType() == EventActionState.ActionType.INVITE_ONLY) {
            return EventDetailData.NextAction.NONE;
        }
        if (eventDetailViewService.isOnWaitingList(detailState)) {
            return EventDetailData.NextAction.LEAVE_WAITLIST;
        }
        return EventDetailData.NextAction.SHOW_JOIN_DIALOG;
    }

    private int getHeroBackgroundRes(String category) {
        return UiHelper.eventImageBackgroundRes(category);
    }

    private String buildLocationText(String location) {
        return EventDisplayFormatter.detailLocation(location);
    }

    private String buildEventDateText(String eventDate) {
        return EventDisplayFormatter.detailDate(eventDate);
    }

    private String buildRegistrationOpenText(Date registrationOpen) {
        return EventDisplayFormatter.labeledShortDate("Registration opens", registrationOpen);
    }

    private String buildRegistrationDeadlineText(Date registrationDeadline) {
        return EventDisplayFormatter.labeledShortDate("Registration closes", registrationDeadline);
    }

    private String formatDate(Date date) {
        return EventDisplayFormatter.longDate(date);
    }

    private String formatLongDate(Date date) {
        return EventDisplayFormatter.shortDate(date);
    }

    private String cleanText(String value) {
        return UiHelper.cleanText(value);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Set<String> collectThreadIds(List<EventComment> comments, String rootId) {
        Set<String> deleteIds = new HashSet<>();
        if (comments == null || comments.isEmpty() || isBlank(rootId)) {
            return deleteIds;
        }

        deleteIds.add(rootId);
        boolean added;
        do {
            added = false;
            for (EventComment comment : comments) {
                if (comment == null || isBlank(comment.getParentId())) {
                    continue;
                }
                if (deleteIds.contains(comment.getParentId())
                        && !deleteIds.contains(comment.getCommentId())) {
                    deleteIds.add(comment.getCommentId());
                    added = true;
                }
            }
        } while (added);

        return deleteIds;
    }
}









