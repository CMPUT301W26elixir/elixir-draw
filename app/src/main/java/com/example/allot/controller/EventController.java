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
import java.util.Locale;
import java.util.Map;
import java.util.Random;
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

    /**
     * Creates an EventController and connects it to Firestore.
     */
    public EventController() {
        // Connect to the database tools
        this.eventRepository = new EventRepository();
    }

    /**
     * Creates an EventController with the provided repository.
     *
     * @param eventRepository the repository used for event data access
     */
    public EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
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
        if (!validateEventInput(input.getTitle(),
                input.getLocation(),
                input.getPrice(),
                input.getDescription(),
                input.getParticipants(),
                input.getEventDate(),
                input.getRegistrationStart(),
                input.getRegistrationEnd())) {
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

            List<Event> openEvents = buildBrowsableEventList(
                    events,
                    normalize(searchTerm),
                    normalize(category)
            );
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
        if (!validateEventInput(input.getTitle(),
                input.getLocation(),
                input.getPrice(),
                input.getDescription(),
                input.getParticipants(),
                input.getEventDate(),
                input.getRegistrationStart(),
                input.getRegistrationEnd())) {
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

            // Handles the loaded event snapshot for a declined offer, updates the event state,
            // and optionally assigns a replacement offer.
            if (event.waitingList == null) {
                event.getWaitingList();
            }
            if (event.waitingList == null) {
                listener.onComplete(false, false);
                return;
            }

            if (event.waitingList.chosen == null) {
                event.waitingList.chosen = new ArrayList<>();
            }
            if (event.waitingList.status == null) {
                event.waitingList.status = new HashMap<>();
            }
            if (event.chosen == null) {
                event.chosen = new ArrayList<>();
            }
            if (event.enrolled == null) {
                event.enrolled = new ArrayList<>();
            }
            if (event.cancelled == null) {
                event.cancelled = new ArrayList<>();
            }
            if (event.notEnrolled == null) {
                event.notEnrolled = new ArrayList<>();
            }

            event.waitingList.chosen.remove(deviceId);
            event.waitingList.status.remove(deviceId);
            event.chosen.remove(deviceId);
            event.enrolled.remove(deviceId);
            if (!event.cancelled.contains(deviceId)) {
                event.cancelled.add(deviceId);
            }

            if ("open".equalsIgnoreCase(normalizeNullable(event.status))) {
                addReplacementOffer(event, deviceId);
            }

            event.chosen = new ArrayList<>(event.waitingList.chosen);
            event.enrolled = event.waitingList.enrolled();
            event.notEnrolled = event.waitingList.notEnrolled();

            eventRepository.saveDeclinedOfferState(eventId, event, listener);
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

            callback.onComplete(buildEventDetailState(event, deviceId), true);
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

    /**
     * Builds a list of open events that match the given normalized filters.
     *
     * @param events the loaded events
     * @param normalizedSearchTerm the normalized search term
     * @param normalizedCategory the normalized category filter
     * @return a sorted list of browsable events
     */
    private List<Event> buildBrowsableEventList(List<Event> events,
                                                String normalizedSearchTerm,
                                                String normalizedCategory) {
        List<Event> openEvents = new ArrayList<>();

        if (events == null) {
            return openEvents;
        }

        for (Event event : events) {
            if (!isBrowsable(event)) {
                continue;
            }

            if (!matchesCategory(event, normalizedCategory)) {
                continue;
            }

            if (!matchesSearch(event, normalizedSearchTerm)) {
                continue;
            }

            openEvents.add(event);
        }

        sortBrowsableEvents(openEvents);
        return openEvents;
    }

    /**
     * Checks whether an event should be shown in the browsable event list.
     *
     * @param event the event to check
     * @return true if the event is browsable, otherwise false
     */
    private boolean isBrowsable(Event event) {
        if (event == null) {
            return false;
        }

        if (!OPEN_STATUS.equalsIgnoreCase(safeString(event.status))) {
            return false;
        }

        return event.registrationDeadline == null
                || event.registrationDeadline.getTime() > System.currentTimeMillis();
    }

    /**
     * Checks whether an event matches the given normalized category.
     *
     * @param event the event to check
     * @param normalizedCategory the normalized category filter
     * @return true if the event matches the category, otherwise false
     */
    private boolean matchesCategory(Event event, String normalizedCategory) {
        if (normalizedCategory.isEmpty()) {
            return true;
        }

        // --- APPLY THE SELECTED CHIP FILTER ---
        // If the chip text isn't anywhere in the title, category, or description, skip it!
        return normalize(event.category).equals(normalizedCategory)
                || containsNormalized(event.title, normalizedCategory)
                || containsNormalized(event.description, normalizedCategory);
        // ---------------------------------------
    }

    /**
     * Checks whether an event matches the given normalized search term.
     *
     * @param event the event to check
     * @param normalizedSearchTerm the normalized search term
     * @return true if the event matches the search term, otherwise false
     */
    private boolean matchesSearch(Event event, String normalizedSearchTerm) {
        if (normalizedSearchTerm.isEmpty()) {
            return true;
        }

        return containsNormalized(event.title, normalizedSearchTerm)
                || containsNormalized(event.description, normalizedSearchTerm)
                || containsNormalized(event.location, normalizedSearchTerm)
                || containsNormalized(event.category, normalizedSearchTerm);
    }

    /**
     * Checks whether a string contains the given normalized search term.
     *
     * @param value the string value to search
     * @param normalizedSearchTerm the normalized search term
     * @return true if the value contains the search term, otherwise false
     */
    private boolean containsNormalized(String value, String normalizedSearchTerm) {
        return normalize(value).contains(normalizedSearchTerm);
    }

    /**
     * Sorts browsable events by registration deadline, event date, and title.
     *
     * @param events the list of events to sort
     */
    private void sortBrowsableEvents(List<Event> events) {
        Collections.sort(events, Comparator
                .comparingLong(this::getDeadlineSortValue)
                .thenComparingLong(this::getEventDateSortValue)
                .thenComparing(event -> safeString(event.title), String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Gets the value used to sort an event by registration deadline.
     *
     * @param event the event to evaluate
     * @return the deadline time in milliseconds, or Long.MAX_VALUE if unavailable
     */
    private long getDeadlineSortValue(Event event) {
        if (event == null || event.registrationDeadline == null) {
            return Long.MAX_VALUE;
        }

        return event.registrationDeadline.getTime();
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
     * Normalizes a string by trimming whitespace and converting it to lowercase.
     *
     * @param value the string to normalize
     * @return the normalized string
     */
    private String normalize(String value) {
        return safeString(value).trim().toLowerCase(Locale.getDefault());
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
     * Returns a trimmed string value, or an empty string if the value is null.
     *
     * @param value the text value to clean
     * @return the trimmed text or an empty string
     */
    private String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Validates the common input used when creating or updating events.
     *
     * @param title the event title
     * @param location the event location
     * @param price the event price
     * @param description the event description
     * @param participants the participant count
     * @param eventDate the event date
     * @param registrationStart the registration opening date
     * @param registrationEnd the registration closing date
     * @return true if the input is valid, otherwise false
     */
    private boolean validateEventInput(String title,
                                       String location,
                                       Double price,
                                       String description,
                                       Integer participants,
                                       java.util.Date eventDate,
                                       java.util.Date registrationStart,
                                       java.util.Date registrationEnd) {
        return !isBlank(title)
                && !isBlank(location)
                && !isBlank(description)
                && price != null
                && price >= 0
                && participants != null
                && participants > 0
                && eventDate != null
                && registrationStart != null
                && registrationEnd != null
                && !registrationEnd.before(registrationStart)
                && !eventDate.before(registrationEnd);
    }

    /**
     * Adds a replacement offer to the event by randomly selecting
     * an eligible entrant from the waiting list.
     *
     * @param event the event whose replacement offer should be assigned
     * @param declinedDeviceId the device ID of the user who declined the offer
     */
    private void addReplacementOffer(Event event, String declinedDeviceId) {
        if (event == null || event.waitingList == null || event.waitingList.list == null) {
            return;
        }

        List<String> eligibleEntrants = new ArrayList<>();
        for (String entrantId : event.waitingList.list) {
            if (isBlank(entrantId)) {
                continue;
            }
            if (entrantId.equals(declinedDeviceId)) {
                continue;
            }
            if (event.waitingList.chosen.contains(entrantId)) {
                continue;
            }
            if (event.cancelled.contains(entrantId)) {
                continue;
            }
            eligibleEntrants.add(entrantId);
        }

        if (eligibleEntrants.isEmpty()) {
            return;
        }

        String replacementId = eligibleEntrants.get(new Random().nextInt(eligibleEntrants.size()));
        event.waitingList.chosen.add(replacementId);
        event.waitingList.status.put(replacementId, false);
    }

    /**
     * Builds detail-screen state for the current event and user.
     *
     * @param event the event to evaluate
     * @param deviceId the current user device ID
     * @return the derived detail-screen state
     */
    private EventDetailState buildEventDetailState(Event event, String deviceId) {
        if (isCurrentUserOrganizer(event, deviceId)) {
            return new EventDetailState(event, EventDetailState.ActionType.MANAGE, false, true, true, null);
        }

        if (isCurrentUserEnrolled(event, deviceId)) {
            return new EventDetailState(event, EventDetailState.ActionType.ENROLLED, false, false, true, null);
        }

        if (isCurrentUserSelected(event, deviceId)) {
            return new EventDetailState(event, EventDetailState.ActionType.OFFER, false, true, true, null);
        }

        if (shouldShowReplacementState(event, deviceId)) {
            return new EventDetailState(
                    event,
                    EventDetailState.ActionType.NOT_SELECTED_REPLACEMENT,
                    false,
                    false,
                    false,
                    "You were not selected in the main draw, but you may still receive an offer if spots open up."
            );
        }

        if (shouldShowFinalizedNotSelectedState(event, deviceId)) {
            return new EventDetailState(
                    event,
                    EventDetailState.ActionType.NOT_SELECTED_FINAL,
                    false,
                    false,
                    false,
                    "Registration is finalized and you were not selected for this event."
            );
        }

        boolean isOnWaitingList = isCurrentUserOnWaitingList(event, deviceId);
        return new EventDetailState(
                event,
                isOnWaitingList ? EventDetailState.ActionType.LEAVE_WAITLIST : EventDetailState.ActionType.JOIN_WAITLIST,
                isOnWaitingList,
                true,
                true,
                null
        );
    }

    /**
     * Checks whether the current user is enrolled in the event.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the current user is enrolled, otherwise false
     */
    private boolean isCurrentUserEnrolled(Event event, String deviceId) {
        return containsUser(event == null ? null : event.enrolled, deviceId);
    }

    /**
     * Checks whether the current user has been selected in the event draw.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the current user has been selected, otherwise false
     */
    private boolean isCurrentUserSelected(Event event, String deviceId) {
        return containsUser(event == null ? null : event.chosen, deviceId)
                || containsUser(event != null && event.waitingList != null ? event.waitingList.chosen : null, deviceId);
    }

    /**
     * Checks whether the UI should show the replacement-state message
     * for a user who was not selected in the main draw.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the replacement-state message should be shown
     */
    private boolean shouldShowReplacementState(Event event, String deviceId) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event, deviceId)
                && !isCurrentUserSelected(event, deviceId)
                && !isCurrentUserEnrolled(event, deviceId)
                && !"finalized".equalsIgnoreCase(normalizeNullable(event == null ? null : event.status));
    }

    /**
     * Checks whether the UI should show the finalized not-selected state.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the finalized not-selected state should be shown
     */
    private boolean shouldShowFinalizedNotSelectedState(Event event, String deviceId) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event, deviceId)
                && !isCurrentUserSelected(event, deviceId)
                && !isCurrentUserEnrolled(event, deviceId)
                && "finalized".equalsIgnoreCase(normalizeNullable(event == null ? null : event.status));
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
     * Checks whether the current user is on the event waiting list.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the current user is on the waiting list, otherwise false
     */
    private boolean isCurrentUserOnWaitingList(Event event, String deviceId) {
        if (event == null || event.waitingList == null || event.waitingList.list == null) {
            return false;
        }
        return event.waitingList.list.contains(deviceId);
    }

    /**
     * Checks whether the current user is the organizer of the event.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the current user is the organizer, otherwise false
     */
    private boolean isCurrentUserOrganizer(Event event, String deviceId) {
        if (event == null) {
            return false;
        }
        return !isBlank(deviceId) && deviceId.equals(event.organizerId);
    }
}
