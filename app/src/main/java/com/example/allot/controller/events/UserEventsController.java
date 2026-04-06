package com.example.allot.controller.events;

import android.content.Context;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.view.shared.EventListItem;
import com.example.allot.view.shared.EventListItemMapper;
import java.util.ArrayList;
import java.util.List;
/**
 * Loads and builds the event lists used by the user's event screens.
 */
public class UserEventsController {
    public static class RegisteredEventGroups {
        private final List<EventListItem> invitedItems;
        private final List<EventListItem> selectedItems;
        private final List<EventListItem> waitingItems;
        private final List<EventListItem> notSelectedItems;
        private final List<EventListItem> pastItems;
        private final List<EventListItem> coOrganizerInviteItems;

        /**
         * Creates a new RegisteredEventGroups instance.
         *
         * @param invitedItems the invited items
         * @param selectedItems the selected items
         * @param waitingItems the waiting items
         * @param notSelectedItems the not selected items
         * @param pastItems the past items
         * @param coOrganizerInviteItems the co organizer invite items
         */
        RegisteredEventGroups(List<EventListItem> invitedItems,
                              List<EventListItem> selectedItems,
                              List<EventListItem> waitingItems,
                              List<EventListItem> notSelectedItems,
                              List<EventListItem> pastItems,
                              List<EventListItem> coOrganizerInviteItems) {
            this.invitedItems = copyItems(invitedItems);
            this.selectedItems = copyItems(selectedItems);
            this.waitingItems = copyItems(waitingItems);
            this.notSelectedItems = copyItems(notSelectedItems);
            this.pastItems = copyItems(pastItems);
            this.coOrganizerInviteItems = copyItems(coOrganizerInviteItems);
        }

        /**
         * Returns the invited items.
         *
         * @return the invited items
         */
        public List<EventListItem> getInvitedItems() { return copyItems(invitedItems); }
        /**
         * Returns the selected items.
         *
         * @return the selected items
         */
        public List<EventListItem> getSelectedItems() { return copyItems(selectedItems); }
        /**
         * Returns the waiting items.
         *
         * @return the waiting items
         */
        public List<EventListItem> getWaitingItems() { return copyItems(waitingItems); }
        /**
         * Returns the not selected items.
         *
         * @return the not selected items
         */
        public List<EventListItem> getNotSelectedItems() { return copyItems(notSelectedItems); }
        /**
         * Returns the past items.
         *
         * @return the past items
         */
        public List<EventListItem> getPastItems() { return copyItems(pastItems); }
        /**
         * Returns the co organizer invite items.
         *
         * @return the co organizer invite items
         */
        public List<EventListItem> getCoOrganizerInviteItems() { return copyItems(coOrganizerInviteItems); }
    }

    public static class HostedEventGroups {
        private final List<EventListItem> ongoingItems;
        private final List<EventListItem> completedItems;

        /**
         * Creates a new HostedEventGroups instance.
         *
         * @param ongoingItems the ongoing items
         * @param completedItems the completed items
         */
        HostedEventGroups(List<EventListItem> ongoingItems, List<EventListItem> completedItems) {
            this.ongoingItems = copyItems(ongoingItems);
            this.completedItems = copyItems(completedItems);
        }

        /**
         * Returns the ongoing items.
         *
         * @return the ongoing items
         */
        public List<EventListItem> getOngoingItems() { return copyItems(ongoingItems); }
        /**
         * Returns the completed items.
         *
         * @return the completed items
         */
        public List<EventListItem> getCompletedItems() { return copyItems(completedItems); }
    }

    private final UserController userController;
    private final EventRepository eventRepository;
    private final UserEventsSectionService myEventsSectionService;
    private final EventListItemMapper eventListItemMapper;

    /**
     * Creates a new UserEventsController instance.
     *
     * @param context the context
     */
    public UserEventsController(Context context) {
        this(new UserController(context), new EventRepository(), new UserEventsSectionService(), new EventListItemMapper());
    }

    /**
     * Creates a new UserEventsController instance.
     *
     * @param userController the user controller
     * @param eventRepository the event repository
     * @param myEventsSectionService the my events section service
     * @param eventListItemMapper the event list item mapper
     */
    UserEventsController(UserController userController,
                         EventRepository eventRepository,
                         UserEventsSectionService myEventsSectionService,
                         EventListItemMapper eventListItemMapper) {
        this.userController = userController;
        this.eventRepository = eventRepository;
        this.myEventsSectionService = myEventsSectionService;
        this.eventListItemMapper = eventListItemMapper;
    }

    /**
     * Performs load registered groups.
     *
     * @param listener the listener
     */
    public void loadRegisteredGroups(OnCompleteListener<RegisteredEventGroups> listener) {
        eventRepository.getAllEvents((events, success) -> {
            if (!success || events == null) {
                listener.onComplete(null, false);
                return;
            }

            UserEventsSectionService.RegisteredSections sections = myEventsSectionService.groupRegisteredEvents(
                    buildRegisteredEventList(events, userController.getCurrentDeviceId()),
                    userController.getCurrentDeviceId()
            );

            listener.onComplete(new RegisteredEventGroups(
                    mapItems(sections.getInvitedEvents()),
                    mapItems(sections.getSelectedEvents()),
                    mapItems(sections.getWaitingEvents()),
                    mapItems(sections.getNotSelectedEvents()),
                    mapItems(sections.getPastEvents()),
                    mapItems(sections.getCoOrganizerInvites())
            ), true);
        });
    }

    /**
     * Performs load hosted groups.
     *
     * @param listener the listener
     */
    public void loadHostedGroups(OnCompleteListener<HostedEventGroups> listener) {
        eventRepository.getManagedEvents(userController.getCurrentDeviceId(), (events, success) -> {
            if (!success || events == null) {
                listener.onComplete(null, false);
                return;
            }

            UserEventsSectionService.HostedSections sections = myEventsSectionService.groupHostedEvents(events);
            listener.onComplete(new HostedEventGroups(
                    mapItems(sections.getOngoingEvents()),
                    mapItems(sections.getCompletedEvents())
            ), true);
        });
    }

    /**
     * Performs load my events list.
     *
     * @param listener the listener
     */
    public void loadMyEventsList(OnCompleteListener<List<EventListItem>> listener) {
        userController.loadOrCreateUser((user, success) -> {
            List<String> eventIds = success && user != null ? user.getMyEvents() : new ArrayList<>();
            List<String> savedEventIds = success && user != null ? user.getSavedEvents() : new ArrayList<>();
            if (eventIds != null && !eventIds.isEmpty()) {
                loadEventsByIds(eventIds, (events, loadSuccess) -> listener.onComplete(
                        eventListItemMapper.mapEvents(events, savedEventIds),
                        loadSuccess
                ));
            } else {
                listener.onComplete(new ArrayList<>(), true);
            }
        });
    }

    /**
     * Performs load saved events.
     *
     * @param savedIds the saved ids
     * @param listener the listener
     */
    public void loadSavedEvents(List<String> savedIds, OnCompleteListener<List<EventListItem>> listener) {
        if (savedIds != null && !savedIds.isEmpty()) {
            loadEventsByIds(savedIds, (events, success) -> listener.onComplete(
                    eventListItemMapper.mapEvents(events, buildSavedIds(events)),
                    success
            ));
            return;
        }

        listener.onComplete(new ArrayList<>(), true);
    }

    /**
     * Performs accept co organizer invite.
     *
     * @param eventId the event id
     * @param listener the listener
     */
    public void acceptCoOrganizerInvite(String eventId, OnCompleteListener<Boolean> listener) {
        eventRepository.acceptCoOrganizerInvite(eventId, userController.getCurrentDeviceId(), listener);
    }

    /**
     * Performs decline co organizer invite.
     *
     * @param eventId the event id
     * @param listener the listener
     */
    public void declineCoOrganizerInvite(String eventId, OnCompleteListener<Boolean> listener) {
        eventRepository.declineCoOrganizerInvite(eventId, userController.getCurrentDeviceId(), listener);
    }

    /**
     * Returns the result of map items.
     *
     * @param events the events
     * @return the result of this call
     */
    private List<EventListItem> mapItems(List<Event> events) {
        return eventListItemMapper.mapEvents(events, new ArrayList<>());
    }

    /**
     * Returns the result of copy items.
     *
     * @param items the items
     * @return the result of this call
     */
    private static List<EventListItem> copyItems(List<EventListItem> items) {
        return items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

    /**
     * Returns the result of build saved ids.
     *
     * @param events the events
     * @return the result of this call
     */
    private List<String> buildSavedIds(List<Event> events) {
        List<String> savedIds = new ArrayList<>();
        if (events == null) {
            return savedIds;
        }

        for (Event event : events) {
            if (event != null && event.getEventId() != null) {
                savedIds.add(event.getEventId());
            }
        }

        return savedIds;
    }

    /**
     * Performs load events by ids.
     *
     * @param eventIds the event ids
     * @param listener the listener
     */
    private void loadEventsByIds(List<String> eventIds, OnCompleteListener<List<Event>> listener) {
        if (eventIds == null || eventIds.isEmpty()) {
            listener.onComplete(new ArrayList<>(), true);
            return;
        }

        eventRepository.getAllEvents((events, success) -> {
            if (!success || events == null) {
                listener.onComplete(new ArrayList<>(), false);
                return;
            }

            List<Event> matchingEvents = new ArrayList<>();
            for (Event event : events) {
                if (event != null && eventIds.contains(event.getEventId())) {
                    matchingEvents.add(event);
                }
            }
            listener.onComplete(matchingEvents, true);
        });
    }

    /**
     * Returns the result of build registered event list.
     *
     * @param events the events
     * @param deviceId the device id
     * @return the result of this call
     */
    private List<Event> buildRegisteredEventList(List<Event> events, String deviceId) {
        List<Event> registeredEvents = new ArrayList<>();
        if (events == null || deviceId == null || deviceId.trim().isEmpty()) {
            return registeredEvents;
        }

        for (Event event : events) {
            if (event == null) {
                continue;
            }

            /**
             * Returns whether get Co Organizer Invites.
             */
            if (containsUser(event.getWaitingList() == null ? null : event.getWaitingList().list, deviceId)
                    || containsUser(event.getWaitingList() == null ? null : event.getWaitingList().chosen, deviceId)
                    || containsUser(event.getChosen(), deviceId)
                    || containsUser(event.getEnrolled(), deviceId)
                    || containsUser(event.getNotEnrolled(), deviceId)
                    || event.isInvited(deviceId)
                    /**
                     * Returns whether get Co Organizer Invites.
                     */
                    || containsUser(event.getCoOrganizerInvites(), deviceId)) {
                registeredEvents.add(event);
            }
        }
        return registeredEvents;
    }

    /**
     * Returns the result of contains user.
     *
     * @param users the users
     * @param deviceId the device id
     * @return the result of this call
     */
    private boolean containsUser(List<String> users, String deviceId) {
        return users != null && users.contains(deviceId);
    }
}









