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
 * EventController
 * This class handles all logic related to Events in the application.
 * It communicates with Firebase Firestore to:
 * - Create events
 * - Delete events
 * - Join/leave waiting lists
 * - Retrieve events
 * - Filter/search events
 * This class acts as the Controller in the MVC architecture.
 */
public class EventController {

    // Tag used for Android logging
    private static final String TAG = "EventLogic";

    // Constant representing an event that is open for registration
    private static final String OPEN_STATUS = "open";

    // Reference to the Firebase Firestore database
    public FirebaseFirestore database;

    /**
     * Constructor
     * Initializes connection to Firebase Firestore.
     */
    public EventController() {
        this.database = FirebaseFirestore.getInstance();
    }

    /**
     * Creates a new event in Firestore and associates it with the organizer.
     *
     * Steps:
     * 1. Save the event inside the "events" collection.
     * 2. Add the event ID to the organizer's "myEvents" array.
     *
     * Used by organizers when creating an event.
     *
     * US 02.01.01
     */
    public void createNewEventForUser(Event event, String organizerId, OnCompleteListener<Boolean> listener) {

        // Save the event object to Firestore
        database.collection("events").document(event.eventId)
                .set(event)
                .addOnCompleteListener(task -> {

                    // If event creation failed
                    if (!task.isSuccessful()) {
                        listener.onComplete(false, false);
                        return;
                    }

                    // Event saved successfully, now update the organizer's event list
                    database.collection("users").document(organizerId)
                            .update("myEvents", FieldValue.arrayUnion(event.eventId))
                            .addOnCompleteListener(userTask -> {

                                // Return result to listener
                                listener.onComplete(userTask.isSuccessful(), userTask.isSuccessful());
                            });
                });
    }

    /**
     * Deletes an event from Firestore.
     *
     * Also removes the event from the organizer's "myEvents" list.
     *
     * Used when an organizer deletes an event.
     */
    public void removeEvent(String eventId, String organizerId) {

        database.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {

                    Log.d(TAG, "Event " + eventId + " removed successfully.");

                    // Remove the event ID from the organizer's event list
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
     * Adds a user (deviceId) to an event waiting list.
     *
     * Firestore stores the waiting list inside:
     * waitingList.list
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
     * Adds a user to an event.
     *
     * Two updates occur:
     * 1. User is added to the event waiting list
     * 2. Event ID is stored in the user's history
     */
    public void joinEvent(String eventId, String userId, OnCompleteListener<Boolean> listener) {

        // Add user to the event waiting list
        database.collection("events").document(eventId)
                .update("waitingList", FieldValue.arrayUnion(userId))
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Failed to join waiting list", task.getException());
                        listener.onComplete(false, false);
                        return;
                    }

                    // Add event to the user's event history
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
     * Retrieves all open events available for users to join.
     */
    public void getAllOpenEvents(EventListCallback callback) {
        getFilteredOpenEvents("", null, callback);
    }

    /**
     * Searches open events by text input.
     */
    public void searchOpenEvents(String searchTerm, EventListCallback callback) {
        getFilteredOpenEvents(searchTerm, null, callback);
    }

    /**
     * Retrieves open events that belong to a specific category.
     */
    public void getOpenEventsByCategory(String category, EventListCallback callback) {
        getFilteredOpenEvents("", category, callback);
    }

    /**
     * Retrieves a single event using its ID.
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

                    // If event does not exist
                    if (document == null || !document.exists()) {
                        callback.onCallback(null);
                        return;
                    }

                    // Convert Firestore document to Event object
                    Event event = document.toObject(Event.class);

                    // Ensure event ID is attached
                    if (event != null && isBlank(event.eventId)) {
                        event.eventId = document.getId();
                    }

                    callback.onCallback(event);
                });
    }

    /**
     * Retrieves multiple events based on a list of event IDs.
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

                            // Check if this event matches one of the IDs
                            if (eventIds.contains(document.getId())) {

                                Event event = document.toObject(Event.class);

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
     * Retrieves open events and applies filtering
     * (search term and category).
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
     * Retrieves events that a user is registered for.
     */
    public void getRegisteredEventsForUser(String deviceId, EventListCallback callback) {

        database.collection("events")
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        List<Event> registeredEvents =
                                buildRegisteredEventList(task.getResult(), deviceId);

                        callback.onCallback(registeredEvents);

                    } else {

                        Exception exception = task.getException();
                        Log.e(TAG, "Error getting registered events", exception);
                        callback.onError(exception);
                    }
                });
    }

    /**
     * Builds a list of events that the user is registered for.
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

        // Sort events by event date then title
        Collections.sort(registeredEvents, Comparator
                .comparingLong(this::getEventDateSortValue)
                .thenComparing(event -> safeString(event.title),
                        String.CASE_INSENSITIVE_ORDER));

        return registeredEvents;
    }

    /**
     * Checks whether a user is registered for an event.
     */
    private boolean isUserRegistered(Event event, String deviceId) {

        if (event == null || isBlank(deviceId)) {
            return false;
        }

        if (event.waitingList != null) {

            if (containsUser(event.waitingList.list, deviceId)
                    || containsUser(event.waitingList.chosen, deviceId)) {

                return true;
            }
        }

        return containsUser(event.chosen, deviceId)
                || containsUser(event.enrolled, deviceId)
                || containsUser(event.notEnrolled, deviceId);
    }

    /**
     * Helper method to check if a list contains a specific user.
     */
    private boolean containsUser(List<String> users, String deviceId) {
        return users != null && users.contains(deviceId);
    }

    /**
     * Callback interface used when returning a list of events.
     * Firestore operations are asynchronous.
     */
    public interface EventListCallback {

        void onCallback(List<Event> events);

        default void onError(Exception exception) {}
    }

    /**
     * Callback interface used when returning a single event.
     */
    public interface EventCallback {

        void onCallback(Event event);

        default void onError(Exception exception) {}
    }

    /**
     * Builds the list of events that users can browse.
     * Filters by:
     * - Status
     * - Category
     * - Search term
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

            if (!isBrowsable(event)) continue;
            if (!matchesCategory(event, normalizedCategory)) continue;
            if (!matchesSearch(event, normalizedSearchTerm)) continue;

            openEvents.add(event);
        }

        sortBrowsableEvents(openEvents);

        return openEvents;
    }

    /**
     * Ensures event fields are populated correctly if missing.
     */
    private void hydrateMissingFields(Event event, QueryDocumentSnapshot document) {

        if (event == null) return;

        if (isBlank(event.eventId)) {
            event.eventId = document.getId();
        }

        if (isBlank(event.status)) {
            event.status = OPEN_STATUS;
        }
    }

    /**
     * Determines if an event should be visible to users.
     */
    private boolean isBrowsable(Event event) {

        if (event == null) return false;

        if (!OPEN_STATUS.equalsIgnoreCase(safeString(event.status))) {
            return false;
        }

        return event.registrationDeadline == null
                || event.registrationDeadline.getTime() > System.currentTimeMillis();
    }

    /**
     * Checks if event matches the requested category.
     */
    private boolean matchesCategory(Event event, String normalizedCategory) {

        if (normalizedCategory.isEmpty()) return true;

        return normalize(event.category).equals(normalizedCategory);
    }

    /**
     * Checks if event matches the search term.
     */
    private boolean matchesSearch(Event event, String normalizedSearchTerm) {

        if (normalizedSearchTerm.isEmpty()) return true;

        return containsNormalized(event.title, normalizedSearchTerm)
                || containsNormalized(event.description, normalizedSearchTerm)
                || containsNormalized(event.location, normalizedSearchTerm)
                || containsNormalized(event.category, normalizedSearchTerm);
    }

    /**
     * Checks if a field contains a search term (case-insensitive).
     */
    private boolean containsNormalized(String value, String normalizedSearchTerm) {
        return normalize(value).contains(normalizedSearchTerm);
    }

    /**
     * Sorts events by:
     * 1. Registration deadline
     * 2. Event date
     * 3. Title
     */
    private void sortBrowsableEvents(List<Event> events) {

        Collections.sort(events, Comparator
                .comparingLong(this::getDeadlineSortValue)
                .thenComparingLong(this::getEventDateSortValue)
                .thenComparing(event -> safeString(event.title),
                        String.CASE_INSENSITIVE_ORDER));
    }

    /**
     * Helper method to safely get deadline time.
     */
    private long getDeadlineSortValue(Event event) {

        if (event == null || event.registrationDeadline == null) {
            return Long.MAX_VALUE;
        }

        return event.registrationDeadline.getTime();
    }

    /**
     * Helper method to safely get event date time.
     */
    private long getEventDateSortValue(Event event) {

        if (event == null || event.eventDate == null) {
            return Long.MAX_VALUE;
        }

        return event.eventDate.getTime();
    }

    /**
     * Converts strings to normalized form for searching.
     */
    private String normalize(String value) {
        return safeString(value).trim().toLowerCase(Locale.getDefault());
    }

    /**
     * Prevents null string errors.
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /**
     * Checks if a string is blank.
     */
    private boolean isBlank(String value) {
        return safeString(value).trim().isEmpty();
    }

}