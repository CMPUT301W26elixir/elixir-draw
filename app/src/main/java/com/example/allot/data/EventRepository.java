package com.example.allot.data;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.event.EventOfferService;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventComment;
import com.example.allot.model.event.WaitlistJoinLocation;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Handles Firestore reads and writes for events.
 */
public class EventRepository {
    static final int MAX_BATCH_OPERATIONS = 500;
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
     * @param latitude the temporary join latitude captured by the client
     * @param longitude the temporary join longitude captured by the client
     * @param joinedAt the temporary join timestamp captured by the client
     * @param listener the listener that receives the result
     */
    public void joinWaitingList(String eventId,
                                String deviceId,
                                Double latitude,
                                Double longitude,
                                Date joinedAt,
                                OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update(buildJoinWaitingListUpdates(deviceId, latitude, longitude, joinedAt))
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
                .update(buildLeaveWaitingListUpdates(deviceId))
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    Map<String, Object> buildJoinWaitingListUpdates(String deviceId,
                                                    Double latitude,
                                                    Double longitude,
                                                    Date joinedAt) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("waitingList.list", FieldValue.arrayUnion(deviceId));
        if (latitude != null && longitude != null) {
            updates.put("waitingList.joinLocations." + deviceId,
                    new WaitlistJoinLocation(latitude, longitude, joinedAt));
        }
        return updates;
    }

    Map<String, Object> buildLeaveWaitingListUpdates(String deviceId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("waitingList.list", FieldValue.arrayRemove(deviceId));
        updates.put("waitingList.joinLocations." + deviceId, FieldValue.delete());
        return updates;
    }

    /**
     * Adds a comment or reply to the given event.
     *
     * @param eventId the event ID
     * @param comment the comment to add
     * @param listener the listener that receives the result
     */
    public void addComment(String eventId, EventComment comment, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update("comments", FieldValue.arrayUnion(comment))
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

    /**
     * Deletes an event and removes all references from user documents.
     * This is an admin operation that completely removes the event from the system.
     *
     * @param eventId the event ID to delete
     * @param listener the listener that receives the result
     */
    public void deleteEventAsAdmin(String eventId, OnCompleteListener<Boolean> listener) {
        database.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        listener.onComplete(false, false);
                        return;
                    }

                    List<UserCleanupTarget> cleanupTargets = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        cleanupTargets.add(new UserCleanupTarget(
                                document.getReference().getPath(),
                                document.getId()));
                    }

                    List<CleanupOperation> cleanupOperations = buildEventCleanupOperations(eventId, cleanupTargets);
                    cleanupOperations.add(CleanupOperation.deleteEvent(eventId));
                    List<List<CleanupOperation>> batches = chunkCleanupOperations(cleanupOperations);
                    commitEventCleanupOperations(database, batches, 0, listener);
                });
    }

    void commitEventCleanupOperations(FirebaseFirestore database,
                                      List<List<CleanupOperation>> batches,
                                      int startIndex,
                                      OnCompleteListener<Boolean> listener) {
        if (startIndex >= batches.size()) {
            listener.onComplete(true, true);
            return;
        }

        WriteBatch batch = database.batch();
        for (CleanupOperation operation : batches.get(startIndex)) {
            operation.apply(batch, database);
        }

        batch.commit().addOnCompleteListener(commitTask -> {
            if (!commitTask.isSuccessful()) {
                listener.onComplete(false, false);
                return;
            }

            commitEventCleanupOperations(database, batches, startIndex + 1, listener);
        });
    }

    static List<CleanupOperation> buildEventCleanupOperations(String eventId, Iterable<UserCleanupTarget> cleanupTargets) {
        List<CleanupOperation> operations = new ArrayList<>();
        for (UserCleanupTarget cleanupTarget : cleanupTargets) {
            operations.add(CleanupOperation.removeEventFromUser(cleanupTarget.getDocumentPath(), eventId));
        }
        return operations;
    }

    static List<List<CleanupOperation>> chunkCleanupOperations(List<CleanupOperation> operations) {
        List<List<CleanupOperation>> batches = new ArrayList<>();
        for (int i = 0; i < operations.size(); i += MAX_BATCH_OPERATIONS) {
            int endIndex = Math.min(i + MAX_BATCH_OPERATIONS, operations.size());
            batches.add(new ArrayList<>(operations.subList(i, endIndex)));
        }
        return batches;
    }

    static final class CleanupOperation {
        enum Type {
            REMOVE_EVENT_FROM_USER,
            DELETE_EVENT
        }

        private final Type type;
        private final String documentPath;
        private final String eventId;

        private CleanupOperation(Type type, String documentPath, String eventId) {
            this.type = type;
            this.documentPath = documentPath;
            this.eventId = eventId;
        }

        static CleanupOperation removeEventFromUser(String documentPath, String eventId) {
            return new CleanupOperation(Type.REMOVE_EVENT_FROM_USER, documentPath, eventId);
        }

        static CleanupOperation deleteEvent(String eventId) {
            return new CleanupOperation(Type.DELETE_EVENT, null, eventId);
        }

        Type getType() {
            return type;
        }

        String getDocumentPath() {
            return documentPath;
        }

        String getEventId() {
            return eventId;
        }

        void apply(WriteBatch batch, FirebaseFirestore database) {
            if (type == Type.REMOVE_EVENT_FROM_USER) {
                DocumentReference reference = database.document(documentPath);
                batch.update(reference,
                        "myEvents", FieldValue.arrayRemove(eventId),
                        "savedEvents", FieldValue.arrayRemove(eventId),
                        "history", FieldValue.arrayRemove(eventId));
                return;
            }

            if (type == Type.DELETE_EVENT) {
                batch.delete(database.collection("events").document(eventId));
            }
        }
    }

    static final class UserCleanupTarget {
        private final String documentPath;
        private final String userId;

        UserCleanupTarget(String documentPath, String userId) {
            this.documentPath = documentPath;
            this.userId = userId;
        }

        String getDocumentPath() {
            return documentPath;
        }

        String getUserId() {
            return userId;
        }
    }

}
