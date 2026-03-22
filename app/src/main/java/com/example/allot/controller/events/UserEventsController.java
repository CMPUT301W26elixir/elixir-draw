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
public class UserEventsController {
    public static class RegisteredEventGroups {
        private final List<EventListItem> selectedItems;
        private final List<EventListItem> waitingItems;
        private final List<EventListItem> notSelectedItems;
        private final List<EventListItem> pastItems;

        RegisteredEventGroups(List<EventListItem> selectedItems,
                              List<EventListItem> waitingItems,
                              List<EventListItem> notSelectedItems,
                              List<EventListItem> pastItems) {
            this.selectedItems = copyItems(selectedItems);
            this.waitingItems = copyItems(waitingItems);
            this.notSelectedItems = copyItems(notSelectedItems);
            this.pastItems = copyItems(pastItems);
        }

        public List<EventListItem> getSelectedItems() { return copyItems(selectedItems); }
        public List<EventListItem> getWaitingItems() { return copyItems(waitingItems); }
        public List<EventListItem> getNotSelectedItems() { return copyItems(notSelectedItems); }
        public List<EventListItem> getPastItems() { return copyItems(pastItems); }
    }

    public static class HostedEventGroups {
        private final List<EventListItem> ongoingItems;
        private final List<EventListItem> completedItems;

        HostedEventGroups(List<EventListItem> ongoingItems, List<EventListItem> completedItems) {
            this.ongoingItems = copyItems(ongoingItems);
            this.completedItems = copyItems(completedItems);
        }

        public List<EventListItem> getOngoingItems() { return copyItems(ongoingItems); }
        public List<EventListItem> getCompletedItems() { return copyItems(completedItems); }
    }

    private final UserController userController;
    private final EventRepository eventRepository;
    private final UserEventsSectionService myEventsSectionService;
    private final EventListItemMapper eventListItemMapper;

    public UserEventsController(Context context) {
        this(new UserController(context), new EventRepository(), new UserEventsSectionService(), new EventListItemMapper());
    }

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
     * Loads the registered tab groups.
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
                    mapItems(sections.getSelectedEvents()),
                    mapItems(sections.getWaitingEvents()),
                    mapItems(sections.getNotSelectedEvents()),
                    mapItems(sections.getPastEvents())
            ), true);
        });
    }

    /**
     * Loads the hosting tab groups.
     */
    public void loadHostedGroups(OnCompleteListener<HostedEventGroups> listener) {
        eventRepository.getHostedEvents(userController.getCurrentDeviceId(), (events, success) -> {
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
     * Loads the simple My Events fragment list.
     */
    public void loadMyEventsList(OnCompleteListener<List<EventListItem>> listener) {
        userController.loadOrCreateUser((user, success) -> {
            List<String> eventIds = success && user != null ? user.getMyEvents() : new ArrayList<>();
            if (eventIds != null && !eventIds.isEmpty()) {
                loadEventsByIds(eventIds, (events, loadSuccess) -> listener.onComplete(
                        eventListItemMapper.mapEvents(events, user.getSavedEvents() != null ? user.getSavedEvents() : new ArrayList<>()),
                        loadSuccess
                ));
            } else {
                listener.onComplete(new ArrayList<>(), true);
            }
        });
    }

    /**
     * Loads the simple Saved Events fragment list.
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

    private List<EventListItem> mapItems(List<Event> events) {
        return eventListItemMapper.mapEvents(events, new ArrayList<>());
    }

    private static List<EventListItem> copyItems(List<EventListItem> items) {
        return items == null ? new ArrayList<>() : new ArrayList<>(items);
    }

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

    private List<Event> buildRegisteredEventList(List<Event> events, String deviceId) {
        List<Event> registeredEvents = new ArrayList<>();
        if (events == null || deviceId == null || deviceId.trim().isEmpty()) {
            return registeredEvents;
        }

        for (Event event : events) {
            if (event == null) {
                continue;
            }

            if (containsUser(event.getWaitingList() == null ? null : event.getWaitingList().list, deviceId)
                    || containsUser(event.getWaitingList() == null ? null : event.getWaitingList().chosen, deviceId)
                    || containsUser(event.getChosen(), deviceId)
                    || containsUser(event.getEnrolled(), deviceId)
                    || containsUser(event.getNotEnrolled(), deviceId)) {
                registeredEvents.add(event);
            }
        }
        return registeredEvents;
    }

    private boolean containsUser(List<String> users, String deviceId) {
        return users != null && users.contains(deviceId);
    }
}









