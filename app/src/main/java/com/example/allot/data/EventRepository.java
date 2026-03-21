package com.example.allot.data;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.model.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles Firebase access for event-related data operations.
 */
public class EventRepository {
    private final FirebaseFirestore database;

    /**
     * Creates an EventRepository and connects it to Firestore.
     */
    public EventRepository() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Creates an EventRepository with a provided Firestore instance.
     *
     * @param database the Firestore instance to use
     */
    public EventRepository(FirebaseFirestore database) {
        this.database = database;
    }

    /**
     * Saves a new event and links it to the organizer's event list.
     *
     * @param event the event to create
     * @param organizerId the organizer creating the event
     * @param listener the listener that receives the result
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
     * Removes an event and unlinks it from the organizer's event list.
     *
     * @param eventId the event to remove
     * @param organizerId the organizer who owns the event
     * @param listener the listener that receives the result
     */
    public void removeEvent(String eventId, String organizerId, OnCompleteListener<Boolean> listener) {
        database.collection("events").document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    database.collection("users").document(organizerId)
                            .update("myEvents", FieldValue.arrayRemove(eventId))
                            .addOnSuccessListener(aVoid2 -> listener.onComplete(true, true))
                            .addOnFailureListener(e -> listener.onComplete(false, false));
                })
                .addOnFailureListener(e -> listener.onComplete(false, false));
    }

    /**
     * Adds a user to the waiting list for an event.
     *
     * @param eventId the event ID
     * @param deviceId the user device ID
     * @param listener the listener that receives the result
     */
    public void joinWaitingList(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update("waitingList.list", FieldValue.arrayUnion(deviceId))
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    /**
     * Removes a user from the waiting list for an event.
     *
     * @param eventId the event ID
     * @param deviceId the user device ID
     * @param listener the listener that receives the result
     */
    public void leaveWaitingList(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update("waitingList.list", FieldValue.arrayRemove(deviceId))
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    /**
     * Loads an event by its ID.
     *
     * @param eventId the event ID
     * @param listener the listener that receives the event
     */
    public void getEventById(String eventId, OnCompleteListener<Event> listener) {
        database.collection("events")
                .document(eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        listener.onComplete(null, false);
                        return;
                    }

                    DocumentSnapshot document = task.getResult();
                    if (document == null || !document.exists()) {
                        listener.onComplete(null, true);
                        return;
                    }

                    Event event = document.toObject(Event.class);
                    if (event != null && (event.eventId == null || event.eventId.trim().isEmpty())) {
                        event.eventId = document.getId();
                    }
                    listener.onComplete(event, true);
                });
    }

    /**
     * Loads all events in the collection.
     *
     * @param listener the listener that receives the events
     */
    public void getAllEvents(OnCompleteListener<List<Event>> listener) {
        database.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        listener.onComplete(null, false);
                        return;
                    }

                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Event event = document.toObject(Event.class);
                        // Ensure the ID is attached so it can be clicked later
                        if (event != null && (event.eventId == null || event.eventId.trim().isEmpty())) {
                            event.eventId = document.getId();
                        }
                        events.add(event);
                    }
                    listener.onComplete(events, true);
                });
    }

    /**
     * Loads all open events in the collection.
     *
     * @param listener the listener that receives the events
     */
    public void getOpenEvents(OnCompleteListener<List<Event>> listener) {
        database.collection("events")
                .whereEqualTo("status", "open")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        listener.onComplete(null, false);
                        return;
                    }

                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Event event = document.toObject(Event.class);
                        // Ensure the ID is attached so it can be clicked later
                        if (event != null && (event.eventId == null || event.eventId.trim().isEmpty())) {
                            event.eventId = document.getId();
                        }
                        events.add(event);
                    }
                    listener.onComplete(events, true);
                });
    }

    /**
     * Updates the given event using a Firestore update map.
     *
     * @param eventId the event ID to update
     * @param updates the update payload
     * @param listener the listener that receives the result
     */
    public void updateEvent(String eventId, Map<String, Object> updates, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update(updates)
                .addOnCompleteListener(task -> listener.onComplete(task.isSuccessful(), task.isSuccessful()));
    }

    /**
     * Saves the accepted-offer state for the current user.
     *
     * @param eventId the event ID
     * @param deviceId the user device ID
     * @param listener the listener that receives the result
     */
    public void acceptOffer(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update(
                        "enrolled", FieldValue.arrayUnion(deviceId),
                        "cancelled", FieldValue.arrayRemove(deviceId),
                        "notEnrolled", FieldValue.arrayRemove(deviceId),
                        "waitingList.status." + deviceId, true
                )
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    /**
     * Saves the declined-offer state for the current event.
     *
     * @param eventId the event ID
     * @param event the updated event state
     * @param listener the listener that receives the result
     */
    public void saveDeclinedOfferState(String eventId, Event event, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update(
                        "chosen", event.chosen,
                        "enrolled", event.enrolled,
                        "cancelled", event.cancelled,
                        "notEnrolled", event.notEnrolled,
                        "waitingList.chosen", event.waitingList.chosen,
                        "waitingList.status", event.waitingList.status
                )
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }
}
