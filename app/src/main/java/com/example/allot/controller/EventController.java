package com.example.allot.controller;

import android.util.Log;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.data.EventRepository;
import com.example.allot.model.BrowseFilter;
import com.example.allot.model.CreateEventInput;
import com.example.allot.model.Event;
import com.example.allot.model.EventDetailState;
import com.example.allot.model.UpdateEventInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Handles event-related Firestore operations, including creating events,
 * joining and leaving waiting lists, retrieving events, and filtering
 * browsable or registered event lists.
 */
public class EventController {
    private static final String TAG = "EventLogic";
    private static final String OPEN_STATUS = "open";

    private final EventRepository eventRepository;
    private final EventInputValidator eventInputValidator;
    private final EventBrowseService eventBrowseService;
    private final EventOfferService eventOfferService;
    private final EventDetailStateFactory eventDetailStateFactory;

    /**
     * Creates an EventController and connects it to Firestore.
     */
    public EventController() {
        // Connect to the database tools
        this(
                new EventRepository(),
                new EventInputValidator(),
                new EventBrowseService(),
                new EventOfferService(),
                new EventDetailStateFactory()
        );
    }

    /**
     * Creates an EventController with the provided repository.
     *
     * @param eventRepository the repository used for event data access
     */
    public EventController(EventRepository eventRepository) {
        this(
                eventRepository,
                new EventInputValidator(),
                new EventBrowseService(),
                new EventOfferService(),
                new EventDetailStateFactory()
        );
    }

    /**
     * Creates an EventController with explicit collaborators.
     *
     * @param eventRepository the repository used for event data access
     * @param eventInputValidator the validator used for create/update input
     * @param eventBrowseService the browse service used for filtering and sorting
     * @param eventOfferService the offer service used for decline flows
     * @param eventDetailStateFactory the state factory used for detail-screen state
     */
    public EventController(EventRepository eventRepository,
                           EventInputValidator eventInputValidator,
                           EventBrowseService eventBrowseService,
                           EventOfferService eventOfferService,
                           EventDetailStateFactory eventDetailStateFactory) {
        this.eventRepository = eventRepository;
        this.eventInputValidator = eventInputValidator;
        this.eventBrowseService = eventBrowseService;
        this.eventOfferService = eventOfferService;
        this.eventDetailStateFactory = eventDetailStateFactory;
    }

    /**
     * Saves a new event to Firestore and adds the event ID to the organizer's
     * list of created events.
     *
     * @param event the event to create
     * @param organizerId the device ID of the organizer creating the event
     * @param listener the listener that receives the success result
     */
    public void createNewEventForUser(Event event, String organizerId, OnCompleteListener<Boolean> listener) {
        eventRepository.createNewEventForUser(event, organizerId, listener);
    }

    /**
     * Validates user input, builds a new event model, and saves it.
     *
     * @param input the create-event input values
     * @param organizerId the device ID of the organizer creating the event
     * @param listener the listener that receives the created event
     */
    public void createEvent(CreateEventInput input, String organizerId, OnCompleteListener<Event> listener) {
        if (!eventInputValidator.isValid(input)) {
            listener.onComplete(null, false);
            return;
        }

        Event event = new Event(UUID.randomUUID().toString(), organizerId, input.getTitle(), input.getParticipants());
        event.title = input.getTitle().trim();
        event.location = input.getLocation().trim();
        event.geoloc = input.isGeolocationEnabled();
        event.eventDate = input.getEventDate();
        event.price = input.getPrice();
        event.description = input.getDescription().trim();
        event.capacity = input.getParticipants();
        event.limit = input.getParticipants();
        event.registrationOpen = input.getRegistrationStart();
        event.registrationDeadline = input.getRegistrationEnd();
        event.status = OPEN_STATUS;
        event.category = normalizeNullable(input.getCategory());

        eventRepository.createNewEventForUser(event, organizerId, (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(null, false);
                return;
            }

            listener.onComplete(event, true);
        });
    }

    /**
     * Removes an event from Firestore and removes the event ID from the
     * organizer's list of created events.
     *
     * @param eventId the ID of the event to remove
     * @param organizerId the device ID of the organizer who owns the event
     */
    public void removeEvent(String eventId, String organizerId) {
        eventRepository.removeEvent(eventId, organizerId, (result, success) -> {
            if (success && result != null && result) {
                Log.d(TAG, "Event " + eventId + " removed successfully.");
            } else {
                Log.e(TAG, "Failed to remove event: " + eventId);
            }
        });
    }

    /**
     * Adds a user to the waiting list of an event.
     *
     * @param eventId the ID of the event to join
     * @param deviceId the device ID of the user joining the waiting list
     * @param listener the listener that receives the success result
     */
    public void joinWaitingList(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        eventRepository.joinWaitingList(eventId, deviceId, listener);
    }

    /**
     * Removes a user from the waiting list of an event.
     *
     * @param eventId the ID of the event to leave
     * @param deviceId the device ID of the user leaving the waiting list
     * @param listener the listener that receives the success result
     */
    public void leaveWaitingList(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        eventRepository.leaveWaitingList(eventId, deviceId, listener);
    }

    /**
     * Adds a user to an event's waiting list and adds the event to the
     * user's history.
     *
     * @param eventId the ID of the event to join
     * @param userId the device ID of the user joining the event
     * @param listener the listener that receives the success result
     */
    public void joinEvent(String eventId, String userId, OnCompleteListener<Boolean> listener) {
        // First add the user to the event waiting list
        // Now add the event to the user's joined events/history
        joinWaitingList(eventId, userId, listener);
    }

    /**
     * Gets all events that are currently open for browsing.
     *
     * @param callback the callback that receives the list of open events
     */
    public void getAllOpenEvents(EventListCallback callback) {
        getFilteredOpenEvents("", null, callback);
    }

    /**
     * Searches open events using the given search term.
     *
     * @param searchTerm the text used to search open events
     * @param callback the callback that receives the matching events
     */
    public void searchOpenEvents(String searchTerm, EventListCallback callback) {
        getFilteredOpenEvents(searchTerm, null, callback);
    }

    /**
     * Gets open events that match the given category.
     *
     * @param category the category to filter by
     * @param callback the callback that receives the matching events
     */
    public void getOpenEventsByCategory(String category, EventListCallback callback) {
        getFilteredOpenEvents("", category, callback);
    }

    /**
     * Loads browseable events using the provided search term and selected category.
     *
     * @param searchTerm the text used to search events
     * @param selectedCategory the selected category filter
     * @param callback the callback that receives the filtered events
     */
    public void loadBrowseEvents(String searchTerm, String selectedCategory, EventListCallback callback) {
        BrowseFilter browseFilter = new BrowseFilter(searchTerm, selectedCategory);
        getFilteredOpenEvents(browseFilter.getSearchTerm(), browseFilter.getSelectedCategory(), callback);
    }

    /**
     * Loads a single event by its ID.
     *
     * @param eventId the ID of the event to retrieve
     * @param callback the callback that receives the retrieved event
     */
    public void getEventById(String eventId, EventCallback callback) {
        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success) {
                callback.onError(new IllegalStateException("Failed to load event"));
                return;
            }

            callback.onCallback(event);
        });
    }

    /**
     * Loads events whose IDs appear in the provided list.
     *
     * @param eventIds the list of event IDs to retrieve
     * @param callback the callback that receives the matching events
     */
    public void getEventsByIds(List<String> eventIds, EventListCallback callback) {
        if (eventIds == null || eventIds.isEmpty()) {
            callback.onCallback(new ArrayList<>());
            return;
        }

        eventRepository.getAllEvents((events, success) -> {
            if (!success || events == null) {
                callback.onError(new IllegalStateException("Failed to load events"));
                return;
            }

            List<Event> matchingEvents = new ArrayList<>();
            for (Event event : events) {
                if (event != null && eventIds.contains(event.eventId)) {
                    matchingEvents.add(event);
                }
            }
            callback.onCallback(matchingEvents);
        });
    }

    /**
     * Gets open events filtered by search term and category.
     *
     * @param searchTerm the text used to search events
     * @param category the category used to filter events
     * @param callback the callback that receives the filtered events
     */
    public void getFilteredOpenEvents(String searchTerm, String category, EventListCallback callback) {
        eventRepository.getOpenEvents((events, success) -> {
            if (!success || events == null) {
                callback.onError(new IllegalStateException("Failed to load events"));
                return;
            }

            BrowseFilter browseFilter = new BrowseFilter(searchTerm, category);
            List<Event> openEvents = eventBrowseService.buildBrowsableEventList(events, browseFilter);
            callback.onCallback(openEvents);
        });
    }

    /**
     * Gets all events that the given user is registered for.
     *
     * @param deviceId the device ID of the user
     * @param callback the callback that receives the registered events
     */
    public void getRegisteredEventsForUser(String deviceId, EventListCallback callback) {
        eventRepository.getAllEvents((events, success) -> {
            if (!success || events == null) {
                callback.onError(new IllegalStateException("Failed to load events"));
                return;
            }

            List<Event> registeredEvents = buildRegisteredEventList(events, deviceId);
            callback.onCallback(registeredEvents);
        });
    }

    /**
     * Updates an existing event after validating the provided input.
     *
     * @param eventId the ID of the event to update
     * @param input the updated event input values
     * @param listener the listener that receives the refreshed event
     */
    public void updateEvent(String eventId, UpdateEventInput input, OnCompleteListener<Event> listener) {
        if (!eventInputValidator.isValid(input)) {
            listener.onComplete(null, false);
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", input.getTitle().trim());
        updates.put("location", input.getLocation().trim());
        updates.put("eventDate", input.getEventDate());
        updates.put("price", input.getPrice());
        updates.put("description", input.getDescription().trim());
        updates.put("capacity", input.getParticipants());
        updates.put("limit", input.getParticipants());
        updates.put("geoloc", input.isGeolocationEnabled());
        updates.put("registrationOpen", input.getRegistrationStart());
        updates.put("registrationDeadline", input.getRegistrationEnd());

        eventRepository.updateEvent(eventId, updates, (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(null, false);
                return;
            }

            eventRepository.getEventById(eventId, listener);
        });
    }

    /**
     * Accepts the current offer and updates the event state in Firestore.
     * Marks the current user as enrolled and updates their waiting list status.
     *
     * @param eventId the event ID to update
     * @param deviceId the user device ID
     * @param listener the listener that receives the result
     */
    public void acceptOffer(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        eventRepository.acceptOffer(eventId, deviceId, listener);
    }

    /**
     * Starts the decline flow by loading the current event state from Firestore.
     *
     * @param eventId the event ID to update
     * @param deviceId the user device ID
     * @param listener the listener that receives the result
     */
    public void declineOffer(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(false, false);
                return;
            }

            Event updatedEvent = eventOfferService.buildDeclinedOfferState(event, deviceId);
            if (updatedEvent == null) {
                listener.onComplete(false, false);
                return;
            }

            eventRepository.saveDeclinedOfferState(eventId, updatedEvent, listener);
        });
    }

    /**
     * Loads the detail-screen state for the given event and current user.
     *
     * @param eventId the event ID
     * @param deviceId the current user device ID
     * @param callback the callback that receives the detail state
     */
    public void getEventDetailState(String eventId, String deviceId, OnCompleteListener<EventDetailState> callback) {
        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                callback.onComplete(null, false);
                return;
            }

            callback.onComplete(eventDetailStateFactory.create(event, deviceId), true);
        });
    }

    /**
     * Builds a list of events that the given user is registered for.
     *
     * @param events the loaded events
     * @param deviceId the device ID of the user
     * @return a list of registered events
     */
    private List<Event> buildRegisteredEventList(List<Event> events, String deviceId) {
        List<Event> registeredEvents = new ArrayList<>();

        if (events == null || isBlank(deviceId)) {
            return registeredEvents;
        }

        for (Event event : events) {
            if (!isUserRegistered(event, deviceId)) {
                continue;
            }

            registeredEvents.add(event);
        }

        Collections.sort(registeredEvents, Comparator
                .comparingLong(this::getEventDateSortValue)
                .thenComparing(event -> safeString(event.title), String.CASE_INSENSITIVE_ORDER));
        return registeredEvents;
    }

    /**
     * Checks whether a user is registered for an event.
     *
     * @param event the event to check
     * @param deviceId the device ID of the user
     * @return true if the user is registered, otherwise false
     */
    private boolean isUserRegistered(Event event, String deviceId) {
        if (event == null || isBlank(deviceId)) {
            return false;
        }

        if (event.waitingList != null) {
            if (containsUser(event.waitingList.list, deviceId) || containsUser(event.waitingList.chosen, deviceId)) {
                return true;
            }
        }

        return containsUser(event.chosen, deviceId)
                || containsUser(event.enrolled, deviceId)
                || containsUser(event.notEnrolled, deviceId);
    }

    /**
     * Checks whether a user ID exists in a list of user IDs.
     *
     * @param users the list of user IDs to search
     * @param deviceId the device ID to look for
     * @return true if the user exists in the list, otherwise false
     */
    private boolean containsUser(List<String> users, String deviceId) {
        return users != null && users.contains(deviceId);
    }

    /**
     * Gets the value used to sort an event by event date.
     *
     * @param event the event to evaluate
     * @return the event date time in milliseconds, or Long.MAX_VALUE if unavailable
     */
    private long getEventDateSortValue(Event event) {
        if (event == null || event.eventDate == null) {
            return Long.MAX_VALUE;
        }

        return event.eventDate.getTime();
    }

    /**
     * Returns a safe string value, replacing null with an empty string.
     *
     * @param value the string to sanitize
     * @return the original string, or an empty string if null
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /**
     * Checks whether a string is blank after trimming whitespace.
     *
     * @param value the string to check
     * @return true if the string is blank, otherwise false
     */
    private boolean isBlank(String value) {
        return safeString(value).trim().isEmpty();
    }

    /**
     * Returns a trimmed string value, or null if the value is null.
     *
     * @param value the string to normalize
     * @return the normalized string
     */
    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }

    /**
     * Callback interface used when a list of events is loaded asynchronously.
     */
    public interface EventListCallback {
        /**
         * Called when the event list has been loaded successfully.
         *
         * @param events the loaded list of events
         */
        void onCallback(List<Event> events);

        /**
         * Called when loading the event list fails.
         *
         * @param exception the exception that caused the failure
         */
        default void onError(Exception exception) {
        }
    }

    /**
     * Callback interface used when a single event is loaded asynchronously.
     */
    public interface EventCallback {
        /**
         * Called when the event has been loaded successfully.
         *
         * @param event the loaded event
         */
        void onCallback(Event event);

        /**
         * Called when loading the event fails.
         *
         * @param exception the exception that caused the failure
         */
        default void onError(Exception exception) {
        }
    }
}
