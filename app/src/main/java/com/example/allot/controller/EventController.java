package com.example.allot.controller;

import android.util.Log;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Handles event-related Firestore operations, including creating events,
 * joining and leaving waiting lists, retrieving events, and filtering
 * browsable or registered event lists.
 */
public class EventController {
    private static final String TAG = "EventLogic";
    private static final String OPEN_STATUS = "open";

    public FirebaseFirestore database;

    /**
     * Creates an EventController and connects it to Firestore.
     */
    public EventController() {
        // Connect to the database tools
        this.database = FirebaseFirestore.getInstance();
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
        database.collection("events").document(event.eventId)
                .set(event)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        listener.onComplete(false, false);
                        return;
                    }
                    // Event saved, now update the user
                    database.collection("users").document(organizerId)
                            .update("myEvents", FieldValue.arrayUnion(event.eventId))
                            .addOnCompleteListener(userTask -> {
                                listener.onComplete(userTask.isSuccessful(), userTask.isSuccessful());
                            });
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
        database.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Event " + eventId + " removed successfully.");
                    // Remove event ID from user's myEvents
                    database.collection("users").document(organizerId)
                            .update("myEvents", FieldValue.arrayRemove(eventId))
                            .addOnSuccessListener(aVoid2 -> {
                                Log.d(TAG, "Event removed from user's myEvents.");
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Failed to remove event from user: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to remove event: " + e.getMessage());
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
        database.collection("events")
                .document(eventId)
                .update("waitingList.list", FieldValue.arrayUnion(deviceId))
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Failed to join waiting list for event " + eventId, exception);
                    listener.onComplete(false, false);
                });
    }

    /**
     * Removes a user from the waiting list of an event.
     *
     * @param eventId the ID of the event to leave
     * @param deviceId the device ID of the user leaving the waiting list
     * @param listener the listener that receives the success result
     */
    public void leaveWaitingList(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update("waitingList.list", FieldValue.arrayRemove(deviceId))
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Failed to leave waiting list for event " + eventId, exception);
                    listener.onComplete(false, false);
                });
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
        database.collection("events").document(eventId)
                .update("waitingList", FieldValue.arrayUnion(userId))
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Failed to join waiting list", task.getException());
                        listener.onComplete(false, false);
                        return;
                    }

                    // Now add the event to the user's joined events/history
                    database.collection("users").document(userId)
                            .update("history", FieldValue.arrayUnion(eventId))
                            .addOnCompleteListener(userTask -> {

                                if (userTask.isSuccessful()) {
                                    Log.d(TAG, "User joined event successfully.");
                                    listener.onComplete(true, true);
                                } else {
                                    Log.e(TAG, "Failed to update user history", userTask.getException());
                                    listener.onComplete(false, false);
                                }
                            });
                });
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
     * Loads a single event by its ID.
     *
     * @param eventId the ID of the event to retrieve
     * @param callback the callback that receives the retrieved event
     */
    public void getEventById(String eventId, EventCallback callback) {
        database.collection("events")
                .document(eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Exception exception = task.getException();
                        Log.e(TAG, "Error getting event " + eventId, exception);
                        callback.onError(exception);
                        return;
                    }

                    DocumentSnapshot document = task.getResult();
                    if (document == null || !document.exists()) {
                        callback.onCallback(null);
                        return;
                    }

                    Event event = document.toObject(Event.class);
                    if (event != null && isBlank(event.eventId)) {
                        event.eventId = document.getId();
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

        database.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Event> matchingEvents = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (eventIds.contains(document.getId())) {
                                Event event = document.toObject(Event.class);
                                // Ensure the ID is attached so it can be clicked later
                                if (event.eventId == null || event.eventId.isEmpty()) {
                                    event.eventId = document.getId();
                                }
                                matchingEvents.add(event);
                            }
                        }
                        callback.onCallback(matchingEvents);
                    } else {
                        callback.onError(task.getException());
                    }
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
        database.collection("events")
                .whereEqualTo("status", OPEN_STATUS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> openEvents = buildBrowsableEventList(
                                task.getResult(),
                                normalize(searchTerm),
                                normalize(category)
                        );
                        callback.onCallback(openEvents);
                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "Error getting events", exception);
                        callback.onError(exception);
                    }
                });
    }

    /**
     * Gets all events that the given user is registered for.
     *
     * @param deviceId the device ID of the user
     * @param callback the callback that receives the registered events
     */
    public void getRegisteredEventsForUser(String deviceId, EventListCallback callback) {
        database.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Event> registeredEvents = buildRegisteredEventList(task.getResult(), deviceId);
                        callback.onCallback(registeredEvents);
                    } else {
                        Exception exception = task.getException();
                        Log.e(TAG, "Error getting registered events", exception);
                        callback.onError(exception);
                    }
                });
    }

    /**
     * Builds a list of events that the given user is registered for.
     *
     * @param querySnapshot the Firestore query snapshot containing events
     * @param deviceId the device ID of the user
     * @return a list of registered events
     */
    private List<Event> buildRegisteredEventList(QuerySnapshot querySnapshot, String deviceId) {
        List<Event> registeredEvents = new ArrayList<>();

        if (querySnapshot == null || isBlank(deviceId)) {
            return registeredEvents;
        }

        for (QueryDocumentSnapshot document : querySnapshot) {
            Event event = document.toObject(Event.class);
            hydrateMissingFields(event, document);

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
     * @param querySnapshot the Firestore query snapshot containing events
     * @param normalizedSearchTerm the normalized search term
     * @param normalizedCategory the normalized category filter
     * @return a sorted list of browsable events
     */
    private List<Event> buildBrowsableEventList(QuerySnapshot querySnapshot,
                                                String normalizedSearchTerm,
                                                String normalizedCategory) {
        List<Event> openEvents = new ArrayList<>();

        if (querySnapshot == null) {
            return openEvents;
        }

        for (QueryDocumentSnapshot document : querySnapshot) {
            Event event = document.toObject(Event.class);
            hydrateMissingFields(event, document);

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
     * Fills missing event fields using document data and default values.
     *
     * @param event the event to update
     * @param document the document snapshot source
     */
    private void hydrateMissingFields(Event event, QueryDocumentSnapshot document) {
        if (event == null) {
            return;
        }

        if (isBlank(event.eventId)) {
            event.eventId = document.getId();
        }

        if (isBlank(event.status)) {
            event.status = OPEN_STATUS;
        }
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

        return normalize(event.category).equals(normalizedCategory);
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

}