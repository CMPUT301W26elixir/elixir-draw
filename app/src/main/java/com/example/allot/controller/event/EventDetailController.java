package com.example.allot.controller.event;

import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventActionState;
import com.example.allot.model.event.EventDetailData;
import com.example.allot.model.profile.User;
import com.example.allot.view.shared.EventDisplayFormatter;
import com.example.allot.view.shared.UiHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
public class EventDetailController {
    private final EventRepository eventRepository;
    private final UserController userController;
    private final EventActionStateFactory eventActionStateFactory;
    private final EventOfferService eventOfferService;
    private final EventDetailViewService eventDetailViewService;

    public EventDetailController(android.content.Context context) {
        this(
                new EventRepository(),
                new UserController(context),
                new EventActionStateFactory(),
                new EventOfferService(),
                new EventDetailViewService()
        );
    }

    EventDetailController(EventRepository eventRepository,
                          UserController userController,
                          EventActionStateFactory eventActionStateFactory,
                          EventOfferService eventOfferService,
                          EventDetailViewService eventDetailViewService) {
        this.eventRepository = eventRepository;
        this.userController = userController;
        this.eventActionStateFactory = eventActionStateFactory;
        this.eventOfferService = eventOfferService;
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
            listener.onComplete(buildErrorState("Could not load this event right now."), false);
            return;
        }

        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(buildErrorState("Could not load this event right now."), false);
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
    public void joinWaitingList(String eventId, OnCompleteListener<AppResult<Void>> listener) {
        eventRepository.joinWaitingList(eventId, userController.getCurrentDeviceId(), (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(AppResult.failure(R.string.event_detail_join_failure), false);
                return;
            }

            listener.onComplete(AppResult.success(null, R.string.event_detail_join_success), true);
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

    public void acceptOffer(String eventId, OnCompleteListener<AppResult<Void>> listener) {
        eventRepository.acceptOffer(eventId, userController.getCurrentDeviceId(), (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(AppResult.failure(R.string.event_offer_action_failure), false);
                return;
            }

            listener.onComplete(AppResult.success(null, R.string.event_offer_accept_success), true);
        });
    }

    public void declineOffer(String eventId, OnCompleteListener<AppResult<Void>> listener) {
        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(AppResult.failure(R.string.event_offer_action_failure), false);
                return;
            }

            Event updatedEvent = eventOfferService.buildDeclinedOfferState(event, userController.getCurrentDeviceId());
            if (updatedEvent == null) {
                listener.onComplete(AppResult.failure(R.string.event_offer_action_failure), false);
                return;
            }

            eventRepository.saveDeclinedOfferState(eventId, updatedEvent, (result, saveSuccess) -> {
                if (!saveSuccess || result == null || !result) {
                    listener.onComplete(AppResult.failure(R.string.event_offer_action_failure), false);
                    return;
                }

                listener.onComplete(AppResult.success(null, R.string.event_offer_decline_success), true);
            });
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

    public String buildManageLocationText(Event event, String fallbackLocation) {
        return event == null
                ? fallbackLocation
                : EventDisplayFormatter.detailLocation(event.getLocation());
    }

    public String buildManageDateText(Event event, String fallbackDate) {
        return event == null
                ? fallbackDate
                : EventDisplayFormatter.shortDate(event.getEventDate());
    }

    public String buildManageRegistrationText(Date date) {
        return EventDisplayFormatter.shortDate(date);
    }

    private EventDetailData buildErrorState(String message) {
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
                message,
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
}









