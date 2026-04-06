package com.example.allot.controller.profile;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.data.UserRepository;
import com.example.allot.model.profile.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles profile photo upload and deletion for users.
 */
public class ProfilePhotoController {
    private static final String TAG = "ProfilePhotoController";
    private static final int MAX_DOWNLOAD_URL_RETRIES = 3;
    private static final long DOWNLOAD_URL_RETRY_DELAY_MS = 600L;

    private final UserRepository userRepository;
    private final FirebaseStorage storage;

    /**
     * Creates a new ProfilePhotoController instance.
     */
    public ProfilePhotoController() {
        this(new UserRepository(), FirebaseStorage.getInstance());
    }

    /**
     * Creates a new ProfilePhotoController instance.
     *
     * @param userRepository the user repository
     * @param storage the storage
     */
    ProfilePhotoController(UserRepository userRepository, FirebaseStorage storage) {
        this.userRepository = userRepository;
        this.storage = storage;
    }

    /**
     * Performs upload photo.
     *
     * @param deviceId the device id
     * @param photoUri the photo uri
     * @param listener the listener
     */
    public void uploadPhoto(String deviceId, Uri photoUri, OnCompleteListener<String> listener) {
        if (isBlank(deviceId) || photoUri == null) {
            listener.onComplete(null, false);
            return;
        }

        userRepository.getUserByDeviceId(deviceId, (user, success) -> {
            if (!success) {
                listener.onComplete(null, false);
                return;
            }

            String previousPhotoUrl = user == null ? null : user.getProfilePhotoUrl();
            StorageReference photoRef = storage.getReference()
                    .child("user_profiles")
                    .child(deviceId)
                    .child("photo.jpg");

            photoRef.putFile(photoUri)
                    .addOnSuccessListener(taskSnapshot ->
                            fetchDownloadUrlWithRetry(photoRef, deviceId, previousPhotoUrl, listener, 0)
                    )
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Profile photo upload failed for user " + deviceId, exception);
                        listener.onComplete(null, false);
                    });
        });
    }

    /**
     * Performs delete photo.
     *
     * @param deviceId the device id
     * @param profilePhotoUrl the profile photo url
     * @param listener the listener
     */
    public void deletePhoto(String deviceId, String profilePhotoUrl, OnCompleteListener<Boolean> listener) {
        if (isBlank(deviceId)) {
            listener.onComplete(false, false);
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("profilePhotoUrl", null);
        userRepository.updateUserFields(deviceId, updates, (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(false, false);
                return;
            }

            if (isBlank(profilePhotoUrl)) {
                listener.onComplete(true, true);
                return;
            }

            try {
                storage.getReferenceFromUrl(profilePhotoUrl)
                        .delete()
                        .addOnCompleteListener(task -> listener.onComplete(true, true));
            } catch (IllegalArgumentException exception) {
                listener.onComplete(true, true);
            }
        });
    }

    /**
     * Performs fetch download url with retry.
     *
     * @param photoRef the photo ref
     * @param deviceId the device id
     * @param previousPhotoUrl the previous photo url
     * @param listener the listener
     * @param attempt the attempt
     */
    private void fetchDownloadUrlWithRetry(StorageReference photoRef,
                                           String deviceId,
                                           String previousPhotoUrl,
                                           OnCompleteListener<String> listener,
                                           int attempt) {
        photoRef.getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    String photoUrl = uri == null ? null : uri.toString();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("profilePhotoUrl", photoUrl);
                    userRepository.updateUserFields(deviceId, updates, (result, success) -> {
                        if (!success || result == null || !result) {
                            listener.onComplete(null, false);
                            return;
                        }

                        deletePreviousPhotoIfNeeded(previousPhotoUrl, photoRef);
                        listener.onComplete(photoUrl, true);
                    });
                })
                .addOnFailureListener(exception -> {
                    if (isObjectNotFound(exception) && attempt < MAX_DOWNLOAD_URL_RETRIES - 1) {
                        int nextAttempt = attempt + 1;
                        new Handler(Looper.getMainLooper()).postDelayed(
                                () -> fetchDownloadUrlWithRetry(photoRef, deviceId, previousPhotoUrl, listener, nextAttempt),
                                DOWNLOAD_URL_RETRY_DELAY_MS
                        );
                        return;
                    }

                    Log.e(TAG, "Failed to retrieve profile photo URL for user " + deviceId, exception);
                    listener.onComplete(null, false);
                });
    }

    /**
     * Performs delete previous photo if needed.
     *
     * @param previousPhotoUrl the previous photo url
     * @param currentPhotoRef the current photo ref
     */
    private void deletePreviousPhotoIfNeeded(String previousPhotoUrl, StorageReference currentPhotoRef) {
        if (isBlank(previousPhotoUrl)) {
            return;
        }

        try {
            StorageReference previousRef = storage.getReferenceFromUrl(previousPhotoUrl);
            if (previousRef.getPath().equals(currentPhotoRef.getPath())) {
                return;
            }

            previousRef.delete().addOnFailureListener(exception ->
                    Log.w(TAG, "Failed deleting previous profile photo", exception)
            );
        } catch (IllegalArgumentException exception) {
            Log.w(TAG, "Skipping invalid previous profile photo URL: " + previousPhotoUrl, exception);
        }
    }

    /**
     * Returns whether object not found.
     *
     * @param exception the exception
     * @return whether object not found
     */
    private boolean isObjectNotFound(Exception exception) {
        if (!(exception instanceof StorageException)) {
            return false;
        }
        StorageException storageException = (StorageException) exception;
        return storageException.getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND;
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
