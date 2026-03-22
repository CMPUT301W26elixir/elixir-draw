package com.example.allot.data;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.event.EventOfferService;
import com.example.allot.model.event.Event;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * Handles Firestore reads and writes for events.
 */
public class EventRepository {
    private final FirebaseFirestore database;
    private final EventOfferService eventOfferService = new EventOfferService();

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
        WriteBatch batch = database.batch();
        batch.set(database.collection("events").document(event.getEventId()), event);
        batch.update(database.collection("users").document(organizerId),
                "myEvents", FieldValue.arrayUnion(event.getEventId()));
        batch.commit()
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
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
                    if (event == null) {
                        listener.onComplete(null, false);
                        return;
                    }
                    if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                        event.setEventId(document.getId());
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
                        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                            event.setEventId(document.getId());
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
                        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                            event.setEventId(document.getId());
                        }
                        events.add(event);
                    }
                    listener.onComplete(events, true);
                });
    }

    /**
     * Loads all events hosted by the given organizer.
     *
     * @param organizerId the organizer device ID
     * @param listener the listener that receives the events
     */
    public void getHostedEvents(String organizerId, OnCompleteListener<List<Event>> listener) {
        database.collection("events")
                .whereEqualTo("organizerId", organizerId)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful() || task.getResult() == null) {
                        listener.onComplete(null, false);
                        return;
                    }

                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Event event = document.toObject(Event.class);
                        if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                            event.setEventId(document.getId());
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
     * Declines the current user's offer and saves any replacement selection atomically.
     *
     * @param eventId the event ID
     * @param deviceId the user device ID
     * @param listener the listener that receives the result
     */
    public void declineOffer(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        database.runTransaction((Transaction.Function<Boolean>) transaction -> {
                    DocumentSnapshot snapshot = transaction.get(database.collection("events").document(eventId));
                    if (!snapshot.exists()) {
                        return false;
                    }

                    Event event = snapshot.toObject(Event.class);
                    if (event == null) {
                        return false;
                    }
                    if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                        event.setEventId(snapshot.getId());
                    }

                    Event updatedEvent = eventOfferService.buildDeclinedOfferState(event, deviceId);
                    if (updatedEvent == null) {
                        return false;
                    }

                    transaction.update(snapshot.getReference(),
                            "chosen", updatedEvent.getChosen(),
                            "enrolled", updatedEvent.getEnrolled(),
                            "cancelled", updatedEvent.getCancelled(),
                            "notEnrolled", updatedEvent.getNotEnrolled(),
                            "waitingList.chosen", updatedEvent.getWaitingList().chosen,
                            "waitingList.status", updatedEvent.getWaitingList().status);
                    return true;
                })
                .addOnSuccessListener(result -> listener.onComplete(Boolean.TRUE.equals(result), Boolean.TRUE.equals(result)))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

}
