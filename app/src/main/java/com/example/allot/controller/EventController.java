package com.example.allot.controller;

import android.util.Log;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.model.Event;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EventController {
    private static final String TAG = "EventLogic";
    private static final String OPEN_STATUS = "open";

    public FirebaseFirestore database;

    public EventController() {
        // Connect to the database tools
        this.database = FirebaseFirestore.getInstance();
    }

    /**
     * This takes an Event and saves it in the "events" folder in the cloud.
     * Use this for Organizer tasks! US 02.01.01
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
     * Removes an event from Firestore.
     * Used by organizers to delete their event.
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
     * US 01.01.03: Get a list of events that are open for joining for entrant
     * Search is filtered client-side because Firestore does not support
     * case-insensitive substring matching across multiple fields.
     */
    public void getAllOpenEvents(EventListCallback callback) {
        getFilteredOpenEvents("", null, callback);
    }

    public void searchOpenEvents(String searchTerm, EventListCallback callback) {
        getFilteredOpenEvents(searchTerm, null, callback);
    }

    public void getOpenEventsByCategory(String category, EventListCallback callback) {
        getFilteredOpenEvents("", category, callback);
    }

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

    // Waits for internet to finish
    public interface EventListCallback {
        void onCallback(List<Event> events);

        default void onError(Exception exception) {
        }
    }

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

    private boolean matchesCategory(Event event, String normalizedCategory) {
        if (normalizedCategory.isEmpty()) {
            return true;
        }

        return normalize(event.category).equals(normalizedCategory);
    }

    private boolean matchesSearch(Event event, String normalizedSearchTerm) {
        if (normalizedSearchTerm.isEmpty()) {
            return true;
        }

        return containsNormalized(event.title, normalizedSearchTerm)
                || containsNormalized(event.description, normalizedSearchTerm)
                || containsNormalized(event.location, normalizedSearchTerm)
                || containsNormalized(event.category, normalizedSearchTerm);
    }

    private boolean containsNormalized(String value, String normalizedSearchTerm) {
        return normalize(value).contains(normalizedSearchTerm);
    }

    private void sortBrowsableEvents(List<Event> events) {
        Collections.sort(events, Comparator
                .comparingLong(this::getDeadlineSortValue)
                .thenComparingLong(this::getEventDateSortValue)
                .thenComparing(event -> safeString(event.title), String.CASE_INSENSITIVE_ORDER));
    }

    private long getDeadlineSortValue(Event event) {
        if (event == null || event.registrationDeadline == null) {
            return Long.MAX_VALUE;
        }

        return event.registrationDeadline.getTime();
    }

    private long getEventDateSortValue(Event event) {
        if (event == null || event.eventDate == null) {
            return Long.MAX_VALUE;
        }

        return event.eventDate.getTime();
    }

    private String normalize(String value) {
        return safeString(value).trim().toLowerCase(Locale.getDefault());
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return safeString(value).trim().isEmpty();
    }
}
