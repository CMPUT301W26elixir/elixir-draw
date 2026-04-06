package com.example.allot.controller.event;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.data.EventRepository;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles event poster upload and deletion across event/admin screens.
 */
public class EventPosterController {
    private static final String TAG = "EventPosterController";
    private static final int MAX_DOWNLOAD_URL_RETRIES = 3;
    private static final long DOWNLOAD_URL_RETRY_DELAY_MS = 600L;
    private final EventRepository eventRepository;
    private final FirebaseStorage storage;
    private String lastErrorMessage;

    public EventPosterController() {
        this(new EventRepository(), FirebaseStorage.getInstance());
    }

    public EventPosterController(EventRepository eventRepository, FirebaseStorage storage) {
        this.eventRepository = eventRepository;
        this.storage = storage;
    }

    /**
     * Uploads a poster image and persists its URL on the event.
     */
    public void uploadPoster(String eventId, Uri posterUri, OnCompleteListener<String> listener) {
        lastErrorMessage = null;
        if (isBlank(eventId) || posterUri == null) {
            Log.w(TAG, "uploadPoster called with invalid input. eventId=" + eventId + ", posterUri=" + posterUri);
            lastErrorMessage = "Invalid event ID or poster URI";
            listener.onComplete(null, false);
            return;
        }

        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success) {
                Log.e(TAG, "Could not load existing event before poster upload: " + eventId);
                lastErrorMessage = "Could not load event before poster upload";
                listener.onComplete(null, false);
                return;
            }

            String previousPosterUrl = event == null ? null : event.getPosterUrl();
            StorageReference posterRef = storage.getReference()
                    .child("event_posters")
                    .child(eventId)
                    .child("poster.jpg");

            posterRef.putFile(posterUri)
                    .addOnSuccessListener(taskSnapshot ->
                            fetchDownloadUrlWithRetry(posterRef, eventId, previousPosterUrl, listener, 0)
                    )
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Poster upload failed for event " + eventId, exception);
                        lastErrorMessage = exception == null ? "Poster upload failed" : exception.getMessage();
                        listener.onComplete(null, false);
                    });
        });
    }

    private void fetchDownloadUrlWithRetry(StorageReference posterRef,
                                           String eventId,
                                           String previousPosterUrl,
                                           OnCompleteListener<String> listener,
                                           int attempt) {
        posterRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    String posterUrl = uri == null ? null : uri.toString();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("posterUrl", posterUrl);
                    eventRepository.updateEvent(eventId, updates, (result, success) -> {
                        if (!success || result == null || !result) {
                            Log.e(TAG, "Failed to persist posterUrl on event " + eventId);
                            lastErrorMessage = "Uploaded file but failed to save poster URL to Firestore";
                            listener.onComplete(null, false);
                            return;
                        }

                        deletePreviousPosterIfNeeded(previousPosterUrl, posterRef);
                        listener.onComplete(posterUrl, true);
                    });
                })
                .addOnFailureListener(exception -> {
                    if (isObjectNotFound(exception) && attempt < MAX_DOWNLOAD_URL_RETRIES - 1) {
                        int nextAttempt = attempt + 1;
                        Log.w(
                                TAG,
                                "Download URL unavailable yet. Retrying " + nextAttempt + "/" + MAX_DOWNLOAD_URL_RETRIES
                        );
                        new Handler(Looper.getMainLooper()).postDelayed(
                            () -> fetchDownloadUrlWithRetry(
                                posterRef,
                                eventId,
                                previousPosterUrl,
                                listener,
                                nextAttempt
                            ),
                                DOWNLOAD_URL_RETRY_DELAY_MS
                        );
                        return;
                    }

                    Log.e(TAG, "Failed to retrieve download URL for event " + eventId, exception);
                    lastErrorMessage = exception == null
                            ? "Poster upload failed while fetching download URL"
                            : exception.getMessage();
                    listener.onComplete(null, false);
                });
    }

    private void deletePreviousPosterIfNeeded(String previousPosterUrl, StorageReference currentPosterRef) {
        if (isBlank(previousPosterUrl)) {
            return;
        }

        try {
            StorageReference previousRef = storage.getReferenceFromUrl(previousPosterUrl);
            if (previousRef.getPath().equals(currentPosterRef.getPath())) {
                // Same storage object path (overwrite case), nothing extra to delete.
                return;
            }

            previousRef.delete().addOnFailureListener(exception ->
                    Log.w(TAG, "Failed deleting previous poster after update", exception)
            );
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Skipping delete for invalid previous poster URL: " + previousPosterUrl, exception);
        }
    }

    /**
     * Deletes a poster file from Cloud Storage without changing Firestore state.
     *
     * @param posterUrl the poster download URL
     * @param listener the listener that receives the delete result
     */
    public void deletePosterFile(String posterUrl, OnCompleteListener<Boolean> listener) {
        lastErrorMessage = null;
        if (isBlank(posterUrl)) {
            listener.onComplete(true, true);
            return;
        }

        try {
            storage.getReferenceFromUrl(posterUrl)
                    .delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listener.onComplete(true, true);
                            return;
                        }

                        Exception exception = task.getException();
                        if (isObjectNotFound(exception)) {
                            listener.onComplete(true, true);
                            return;
                        }

                        Log.w(TAG, "Poster file delete failed for URL: " + posterUrl, exception);
                        lastErrorMessage = exception == null
                                ? "Failed to delete poster file from Storage"
                                : exception.getMessage();
                        listener.onComplete(false, false);
                    });
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Invalid poster URL during delete: " + posterUrl, exception);
            lastErrorMessage = "Invalid poster URL in event data";
            listener.onComplete(false, false);
        }
    }

    private boolean isObjectNotFound(Exception exception) {
        if (!(exception instanceof StorageException)) {
            return false;
        }
        StorageException storageException = (StorageException) exception;
        return storageException.getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND;
    }

    /**
     * Deletes poster metadata from Firestore and attempts to delete the file from Storage.
     */
    public void deletePoster(String eventId, String posterUrl, OnCompleteListener<Boolean> listener) {
        lastErrorMessage = null;
        if (isBlank(eventId)) {
            Log.w(TAG, "deletePoster called with blank eventId");
            lastErrorMessage = "Invalid event ID";
            listener.onComplete(false, false);
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("posterUrl", null);
        eventRepository.updateEvent(eventId, updates, (result, success) -> {
            if (!success || result == null || !result) {
                Log.e(TAG, "Failed to clear posterUrl on event " + eventId);
                lastErrorMessage = "Failed to clear poster URL in Firestore";
                listener.onComplete(false, false);
                return;
            }

            if (isBlank(posterUrl)) {
                listener.onComplete(true, true);
                return;
            }

            try {
                storage.getReferenceFromUrl(posterUrl)
                        .delete()
                        .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Exception exception = task.getException();
                                Log.w(TAG, "Poster file delete failed for URL: " + posterUrl, exception);
                                lastErrorMessage = exception == null
                                        ? "Failed to delete poster file from Storage"
                                        : exception.getMessage();
                            }
                            listener.onComplete(true, true);
                        });
            } catch (IllegalArgumentException exception) {
                Log.w(TAG, "Invalid poster URL during delete: " + posterUrl, exception);
                lastErrorMessage = "Invalid poster URL in event data";
                listener.onComplete(true, true);
            }
        });
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
