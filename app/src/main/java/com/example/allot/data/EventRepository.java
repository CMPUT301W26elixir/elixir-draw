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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
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
     * Creates a new EventRepository instance.
     */
    public EventRepository() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Creates a new EventRepository instance.
     *
     * @param database the database
     */
    public EventRepository(FirebaseFirestore database) {
        this.database = database;
    }

    /**
     * Performs create new event for user.
     *
     * @param event the event
     * @param organizerId the organizer id
     * @param listener the listener
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
     * Performs join waiting list.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param latitude the latitude
     * @param longitude the longitude
     * @param joinedAt the joined at
     * @param listener the listener
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
     * Performs leave waiting list.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
     */
    public void leaveWaitingList(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update(buildLeaveWaitingListUpdates(deviceId))
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    /**
     * Performs update waitlist location.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param latitude the latitude
     * @param longitude the longitude
     * @param joinedAt the joined at
     * @param listener the listener
     */
    public void updateWaitlistLocation(String eventId,
                                       String deviceId,
                                       Double latitude,
                                       Double longitude,
                                       Date joinedAt,
                                       OnCompleteListener<Boolean> listener) {
        if (isBlank(eventId) || isBlank(deviceId) || latitude == null || longitude == null) {
            listener.onComplete(false, false);
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("waitingList.joinLocations." + deviceId,
                new WaitlistJoinLocation(latitude, longitude, joinedAt == null ? new Date() : joinedAt));

        database.collection("events")
                .document(eventId)
                .update(updates)
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    /**
     * Returns the result of build join waiting list updates.
     *
     * @param deviceId the device id
     * @param latitude the latitude
     * @param longitude the longitude
     * @param joinedAt the joined at
     * @return the result of this call
     */
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

    /**
     * Returns the result of build leave waiting list updates.
     *
     * @param deviceId the device id
     * @return the result of this call
     */
    Map<String, Object> buildLeaveWaitingListUpdates(String deviceId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("waitingList.list", FieldValue.arrayRemove(deviceId));
        updates.put("waitingList.joinLocations." + deviceId, FieldValue.delete());
        return updates;
    }

    /**
     * Performs add comment.
     *
     * @param eventId the event id
     * @param comment the comment
     * @param listener the listener
     */
    public void addComment(String eventId, EventComment comment, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update("comments", FieldValue.arrayUnion(comment))
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }
     /* Invites a user to a private event and adds it to their My Events list.
     *
     * @param eventId the event ID
     * @param deviceId the user device ID to invite
     * @param listener the listener that receives the result
     */
    /**
     * Performs invite user to event.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
     */
    public void inviteUserToEvent(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        if (isBlank(eventId) || isBlank(deviceId)) {
            listener.onComplete(false, false);
            return;
        }

        getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(false, false);
                return;
            }

            if (!event.isPrivate()) {
                listener.onComplete(false, true);
                return;
            }

            WriteBatch batch = database.batch();
            DocumentReference eventRef = database.collection("events").document(eventId);
            DocumentReference userRef = database.collection("users").document(deviceId);
            batch.update(eventRef, "invited", FieldValue.arrayUnion(deviceId));
            batch.update(userRef, "myEvents", FieldValue.arrayUnion(eventId));
            batch.commit()
                    .addOnSuccessListener(unused -> listener.onComplete(true, true))
                    .addOnFailureListener(exception -> listener.onComplete(false, false));
        });
    }

    /**
     * Performs accept invite.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
     */
    public void acceptInvite(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        if (isBlank(eventId) || isBlank(deviceId)) {
            listener.onComplete(false, false);
            return;
        }

        getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(false, false);
                return;
            }

            if (!event.isPrivate() || !event.isInvited(deviceId)) {
                listener.onComplete(false, true);
                return;
            }

            WriteBatch batch = database.batch();
            DocumentReference eventRef = database.collection("events").document(eventId);
            DocumentReference userRef = database.collection("users").document(deviceId);
            batch.update(eventRef,
                    "invited", FieldValue.arrayRemove(deviceId),
                    "waitingList.list", FieldValue.arrayUnion(deviceId));
            batch.update(userRef, "myEvents", FieldValue.arrayUnion(eventId));
            batch.commit()
                    .addOnSuccessListener(unused -> listener.onComplete(true, true))
                    .addOnFailureListener(exception -> listener.onComplete(false, false));
        });
    }

    /**
     * Performs decline invite.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
     */
    public void declineInvite(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        if (isBlank(eventId) || isBlank(deviceId)) {
            listener.onComplete(false, false);
            return;
        }

        WriteBatch batch = database.batch();
        DocumentReference eventRef = database.collection("events").document(eventId);
        DocumentReference userRef = database.collection("users").document(deviceId);
        batch.update(eventRef, "invited", FieldValue.arrayRemove(deviceId));
        batch.update(userRef, "myEvents", FieldValue.arrayRemove(eventId));
        batch.commit()
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    /**
     * Performs get event by id.
     *
     * @param eventId the event id
     * @param listener the listener
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
     * Performs get all events.
     *
     * @param listener the listener
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
     * Performs get open events.
     *
     * @param listener the listener
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
     * Performs get hosted events.
     *
     * @param organizerId the organizer id
     * @param listener the listener
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
     * Performs get managed events.
     *
     * @param deviceId the device id
     * @param listener the listener
     */
    public void getManagedEvents(String deviceId, OnCompleteListener<List<Event>> listener) {
        getAllEvents((events, success) -> {
            if (!success || events == null) {
                listener.onComplete(null, false);
                return;
            }

            List<Event> managedEvents = new ArrayList<>();
            for (Event event : events) {
                if (event == null) {
                    continue;
                }
                /**
                 * Returns whether contains.
                 */
                if (deviceId != null
                        && (deviceId.equals(event.getOrganizerId())
                        || (event.getCoOrganizers() != null && event.getCoOrganizers().contains(deviceId)))) {
                    managedEvents.add(event);
                }
            }
            listener.onComplete(managedEvents, true);
        });
    }

    /**
     * Performs invite co organizer.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
     */
    public void inviteCoOrganizer(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update("coOrganizerInvites", FieldValue.arrayUnion(deviceId))
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    /**
     * Performs accept co organizer invite.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
     */
    public void acceptCoOrganizerInvite(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update(
                        "coOrganizerInvites", FieldValue.arrayRemove(deviceId),
                        "coOrganizers", FieldValue.arrayUnion(deviceId)
                )
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    /**
     * Performs decline co organizer invite.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
     */
    public void declineCoOrganizerInvite(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update("coOrganizerInvites", FieldValue.arrayRemove(deviceId))
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> listener.onComplete(false, false));
    }

    /**
     * Performs update event.
     *
     * @param eventId the event id
     * @param updates the updates
     * @param listener the listener
     */
    public void updateEvent(String eventId, Map<String, Object> updates, OnCompleteListener<Boolean> listener) {
        database.collection("events")
                .document(eventId)
                .update(updates)
                .addOnCompleteListener(task -> listener.onComplete(task.isSuccessful(), task.isSuccessful()));
    }

    /**
     * Performs accept offer.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
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
     * Performs decline offer.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
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
     * Performs delete event as admin.
     *
     * @param eventId the event id
     * @param listener the listener
     */
    public void deleteEventAsAdmin(String eventId, OnCompleteListener<Boolean> listener) {
        getEventById(eventId, (event, eventLoaded) -> {
            if (!eventLoaded || event == null) {
                listener.onComplete(false, false);
                return;
            }

            deleteEventWithCleanup(event, listener);
        });
    }

    /**
     * Performs delete event as organizer.
     *
     * @param eventId the event id
     * @param organizerId the organizer id
     * @param listener the listener
     */
    public void deleteEventAsOrganizer(String eventId, String organizerId, OnCompleteListener<Boolean> listener) {
        if (isBlank(eventId) || isBlank(organizerId)) {
            listener.onComplete(false, false);
            return;
        }

        getEventById(eventId, (event, eventLoaded) -> {
            if (!eventLoaded || event == null) {
                listener.onComplete(false, false);
                return;
            }

            if (!organizerId.equals(event.getOrganizerId())) {
                listener.onComplete(false, false);
                return;
            }

            deleteEventWithCleanup(event, listener);
        });
    }

    /**
     * Performs cancel selected entrant.
     *
     * @param eventId the event id
     * @param entrantId the entrant id
     * @param listener the listener
     */
    public void cancelSelectedEntrant(String eventId, String entrantId, OnCompleteListener<Boolean> listener) {
        if (isBlank(eventId) || isBlank(entrantId)) {
            listener.onComplete(false, false);
            return;
        }

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

                    Event updatedEvent = eventOfferService.buildManualCancellationState(event, entrantId);
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
     * Performs delete event with cleanup.
     *
     * @param event the event
     * @param listener the listener
     */
    private void deleteEventWithCleanup(Event event, OnCompleteListener<Boolean> listener) {
        if (event == null) {
            listener.onComplete(false, false);
            return;
        }

        String eventId = event.getEventId();
        if (isBlank(eventId)) {
            listener.onComplete(false, false);
            return;
        }

        String posterUrl = event.getPosterUrl();
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
                    commitEventCleanupOperations(database, batches, 0, (result, success) -> {
                        if (!success || result == null || !result) {
                            listener.onComplete(false, false);
                            return;
                        }

                        deletePosterFromStorage(posterUrl, listener);
                    });
                });
    }

    /**
     * Performs delete poster from storage.
     *
     * @param posterUrl the poster url
     * @param listener the listener
     */
    private void deletePosterFromStorage(String posterUrl, OnCompleteListener<Boolean> listener) {
        if (posterUrl == null || posterUrl.trim().isEmpty()) {
            listener.onComplete(true, true);
            return;
        }

        /**
         * Returns whether get Error Code.
         */
        try {
            FirebaseStorage.getInstance()
                    .getReferenceFromUrl(posterUrl)
                    .delete()
                    .addOnSuccessListener(unused -> listener.onComplete(true, true))
                    .addOnFailureListener(exception -> {
                        if (exception instanceof StorageException
                                && ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                            listener.onComplete(true, true);
                            return;
                        }
                        listener.onComplete(false, false);
                    });
        } catch (IllegalArgumentException exception) {
            listener.onComplete(false, false);
        }
    }

    /**
     * Performs commit event cleanup operations.
     *
     * @param database the database
     * @param batches the batches
     * @param startIndex the start index
     * @param listener the listener
     */
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

    /**
     * Returns the result of build event cleanup operations.
     *
     * @param eventId the event id
     * @param cleanupTargets the cleanup targets
     * @return the result of this call
     */
    static List<CleanupOperation> buildEventCleanupOperations(String eventId, Iterable<UserCleanupTarget> cleanupTargets) {
        List<CleanupOperation> operations = new ArrayList<>();
        for (UserCleanupTarget cleanupTarget : cleanupTargets) {
            operations.add(CleanupOperation.removeEventFromUser(cleanupTarget.getDocumentPath(), eventId));
        }
        return operations;
    }

    /**
     * Returns the result of chunk cleanup operations.
     *
     * @param operations the operations
     * @return the result of this call
     */
    static List<List<CleanupOperation>> chunkCleanupOperations(List<CleanupOperation> operations) {
        List<List<CleanupOperation>> batches = new ArrayList<>();
        for (int i = 0; i < operations.size(); i += MAX_BATCH_OPERATIONS) {
            int endIndex = Math.min(i + MAX_BATCH_OPERATIONS, operations.size());
            batches.add(new ArrayList<>(operations.subList(i, endIndex)));
        }
        return batches;
    }

    /**
     * Represents the cleanup operation.
     */
    static final class CleanupOperation {
        /**
         * Enumerates the available type values.
         */
        enum Type {
            REMOVE_EVENT_FROM_USER,
            DELETE_EVENT
        }

        private final Type type;
        private final String documentPath;
        private final String eventId;

        /**
         * Creates a new CleanupOperation instance.
         *
         * @param type the type
         * @param documentPath the document path
         * @param eventId the event id
         */
        private CleanupOperation(Type type, String documentPath, String eventId) {
            this.type = type;
            this.documentPath = documentPath;
            this.eventId = eventId;
        }

        /**
         * Returns the result of remove event from user.
         *
         * @param documentPath the document path
         * @param eventId the event id
         * @return the result of this call
         */
        static CleanupOperation removeEventFromUser(String documentPath, String eventId) {
            return new CleanupOperation(Type.REMOVE_EVENT_FROM_USER, documentPath, eventId);
        }

        /**
         * Returns the result of delete event.
         *
         * @param eventId the event id
         * @return the result of this call
         */
        static CleanupOperation deleteEvent(String eventId) {
            return new CleanupOperation(Type.DELETE_EVENT, null, eventId);
        }

        /**
         * Returns the type.
         *
         * @return the type
         */
        Type getType() {
            return type;
        }

        /**
         * Returns the document path.
         *
         * @return the document path
         */
        String getDocumentPath() {
            return documentPath;
        }

        /**
         * Returns the event id.
         *
         * @return the event id
         */
        String getEventId() {
            return eventId;
        }

        /**
         * Performs apply.
         *
         * @param batch the batch
         * @param database the database
         */
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

    /**
     * Represents the user cleanup target.
     */
    static final class UserCleanupTarget {
        private final String documentPath;
        private final String userId;

        /**
         * Creates a new UserCleanupTarget instance.
         *
         * @param documentPath the document path
         * @param userId the user id
         */
        UserCleanupTarget(String documentPath, String userId) {
            this.documentPath = documentPath;
            this.userId = userId;
        }

        /**
         * Returns the document path.
         *
         * @return the document path
         */
        String getDocumentPath() {
            return documentPath;
        }

        /**
         * Returns the user id.
         *
         * @return the user id
         */
        String getUserId() {
            return userId;
        }
    }

    /**
     * Returns whether blank.
     *
     * @param value the value
     * @return whether blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
