package com.example.allot.data;

import android.util.Log;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.common.TextHelper;
import com.example.allot.model.profile.User;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles Firestore reads and writes for users.
 */
public class UserRepository {
    private static final String TAG = "UserRepository";
    static final int MAX_BATCH_OPERATIONS = 500;

    private final CollectionReference usersCollection;

    /**
     * Creates a UserRepository and connects it to Firestore.
     */
    public UserRepository() {
        this(FirebaseFirestore.getInstance());
    }

    /**
     * Creates a UserRepository with a provided Firestore instance.
     *
     * @param database the Firestore instance to use
     */
    public UserRepository(FirebaseFirestore database) {
        this.usersCollection = database == null ? null : database.collection("users");
    }

    /**
     * Loads a user by device ID.
     *
     * @param deviceId the device ID to look up
     * @param listener the listener that receives the user
     */
    public void getUserByDeviceId(String deviceId, OnCompleteListener<User> listener) {
        DocumentReference userRef = usersCollection.document(deviceId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    User user = document.toObject(User.class);
                    if (user != null && isBlank(user.getDeviceId())) {
                        user.setDeviceId(deviceId);
                    }
                    listener.onComplete(user, user != null);
                } else {
                    listener.onComplete(null, false);
                }
            } else {
                Log.d(TAG, "Failed to get user", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Loads a user by device ID without treating a missing document as an error.
     *
     * @param deviceId the device ID to look up
     * @param listener the listener that receives the user or null when none exists
     */
    public void findUserByDeviceId(String deviceId, OnCompleteListener<User> listener) {
        DocumentReference userRef = usersCollection.document(deviceId);

        userRef.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d(TAG, "Failed to find user", task.getException());
                listener.onComplete(null, false);
                return;
            }

            DocumentSnapshot document = task.getResult();
            if (document == null || !document.exists()) {
                listener.onComplete(null, true);
                return;
            }

            User user = document.toObject(User.class);
            if (user != null && isBlank(user.getDeviceId())) {
                user.setDeviceId(deviceId);
            }
            listener.onComplete(user, user != null);
        });
    }

    /**
     * Creates a new user with the provided device ID.
     */
    public void createNewUser(String deviceId, OnCompleteListener<User> listener) {
        User user = new User();
        user.setDeviceId(deviceId);
        DocumentReference userRef = usersCollection.document(deviceId);
        userRef.set(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User created: " + deviceId);
                if (listener != null) {
                    listener.onComplete(user, true);
                }
            } else {
                Log.d(TAG, "Failed to create user", task.getException());
                if (listener != null) {
                    listener.onComplete(null, false);
                }
            }
        });
    }

    /**
     * Updates the current user's profile information.
     */
    public void updateUserProfile(String deviceId,
                                  String firstName,
                                  String lastName,
                                  String email,
                                  String phone,
                                  boolean notiEnabled,
                                  OnCompleteListener<User> listener) {
        DocumentReference userRef = usersCollection.document(deviceId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("deviceId", deviceId);
        updates.put("firstName", firstName.trim());
        updates.put("lastName", lastName.trim());
        updates.put("email", email.trim());
        updates.put("phone", normalizePhone(phone));
        updates.put("notiEnabled", notiEnabled);
        userRef.set(updates, SetOptions.merge()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                getUserByDeviceId(deviceId, listener);
            } else {
                Log.d(TAG, "Failed to update user", task.getException());
                listener.onComplete(null, false);
            }
        });
    }

    /**
     * Updates specific fields for a user in Firestore.
     */
    public void updateUserFields(String deviceId, Map<String, Object> updates, OnCompleteListener<Boolean> listener) {
        usersCollection.document(deviceId)
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onComplete(true, true))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to update user fields for " + deviceId, e);
                    listener.onComplete(false, false);
                });
    }

    /**
     * Updates the FCM token for the user. Uses merge to create doc if it doesn't exist.
     */
    public void updateFcmToken(String deviceId, String fcmToken) {
        if (deviceId == null || fcmToken == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("fcmToken", fcmToken);
        data.put("deviceId", deviceId);
        
        usersCollection.document(deviceId)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM Token updated successfully"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update FCM token", e));
    }

    /**
     * Handles toggle Saved Event.
     */
    public void toggleSavedEvent(String deviceId, String eventId, boolean isSaving, OnCompleteListener<Boolean> listener) {
        DocumentReference userRef = usersCollection.document(deviceId);
        FieldValue updateAction = isSaving ?
                FieldValue.arrayUnion(eventId) :
                FieldValue.arrayRemove(eventId);

        userRef.update("savedEvents", updateAction)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        listener.onComplete(true, true);
                    } else {
                        Log.e(TAG, "Failed to toggle saved event", task.getException());
                        listener.onComplete(false, false);
                    }
                });
    }

    /**
     * Deletes current user.
     */
    public void deleteCurrentUser(String deviceId, OnCompleteListener<Boolean> listener) {
        FirebaseFirestore database = usersCollection.getFirestore();
        database.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.d(TAG, "Failed to load events for cleanup", task.getException());
                        listener.onComplete(false, false);
                        return;
                    }

                    List<EventCleanupTarget> cleanupTargets = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        cleanupTargets.add(new EventCleanupTarget(
                                document.getReference().getPath(),
                                document.getString("organizerId")));
                    }

                    List<CleanupOperation> cleanupOperations = buildCleanupOperations(deviceId, cleanupTargets);
                    cleanupOperations.add(CleanupOperation.deleteUser(deviceId));
                    List<List<CleanupOperation>> batches = chunkCleanupOperations(cleanupOperations);
                    commitCleanupOperations(database, batches, 0, (result, success) -> {
                        if (!success || result == null || !result) {
                            listener.onComplete(false, false);
                            return;
                        }

                        deleteProfilePhotoFromStorage(deviceId, listener);
                    });
                });
    }

    /**
     * Handles commit Cleanup Operations.
     */
    void commitCleanupOperations(FirebaseFirestore database,
                                 List<List<CleanupOperation>> batches,
                                 int startIndex,
                                 OnCompleteListener<Boolean> listener) {
        if (startIndex >= batches.size()) {
            listener.onComplete(true, true);
            return;
        }

        WriteBatch batch = database.batch();
        for (CleanupOperation operation : batches.get(startIndex)) {
            operation.apply(batch, database, usersCollection);
        }

        batch.commit().addOnCompleteListener(commitTask -> {
            if (!commitTask.isSuccessful()) {
                Log.d(TAG, "Failed to delete user profile", commitTask.getException());
                listener.onComplete(false, false);
                return;
            }
            commitCleanupOperations(database, batches, startIndex + 1, listener);
        });
    }

    /**
     * Builds cleanup operations.
     */
    static List<CleanupOperation> buildCleanupOperations(String deviceId, Iterable<EventCleanupTarget> cleanupTargets) {
        List<CleanupOperation> operations = new ArrayList<>();
        for (EventCleanupTarget cleanupTarget : cleanupTargets) {
            if (deviceId.equals(cleanupTarget.getOrganizerId())) {
                operations.add(CleanupOperation.deleteEvent(cleanupTarget.getDocumentPath()));
                continue;
            }
            operations.add(CleanupOperation.removeUserFromEvent(cleanupTarget.getDocumentPath(), deviceId));
        }
        return operations;
    }

    /**
     * Handles chunk Cleanup Operations.
     */
    static List<List<CleanupOperation>> chunkCleanupOperations(List<CleanupOperation> operations) {
        List<List<CleanupOperation>> batches = new ArrayList<>();
        for (int i = 0; i < operations.size(); i += MAX_BATCH_OPERATIONS) {
            int endIndex = Math.min(i + MAX_BATCH_OPERATIONS, operations.size());
            batches.add(new ArrayList<>(operations.subList(i, endIndex)));
        }
        return batches;
    }

    public static final class CleanupOperation {
        public enum Type { REMOVE_USER_FROM_EVENT, DELETE_EVENT, DELETE_USER }
        private final Type type;
        private final String documentPath;
        private final String deviceId;

        /**
         * Handles cleanup Operation.
         */
        private CleanupOperation(Type type, String documentPath, String deviceId) {
            this.type = type;
            this.documentPath = documentPath;
            this.deviceId = deviceId;
        }

        /**
         * Handles remove User From Event.
         */
        static CleanupOperation removeUserFromEvent(String documentPath, String deviceId) {
            return new CleanupOperation(Type.REMOVE_USER_FROM_EVENT, documentPath, deviceId);
        }

        /**
         * Deletes event.
         */
        static CleanupOperation deleteEvent(String documentPath) {
            return new CleanupOperation(Type.DELETE_EVENT, documentPath, null);
        }

        /**
         * Deletes user.
         */
        static CleanupOperation deleteUser(String deviceId) {
            return new CleanupOperation(Type.DELETE_USER, null, deviceId);
        }

        /**
         * Returns whether get Type.
         */
        public Type getType() { return type; }
        /**
         * Returns whether get Document Path.
         */
        public String getDocumentPath() { return documentPath; }
        /**
         * Returns whether get Device Id.
         */
        public String getDeviceId() { return deviceId; }

        /**
         * Handles apply.
         */
        void apply(WriteBatch batch, FirebaseFirestore database, CollectionReference usersCollection) {
            if (type == Type.REMOVE_USER_FROM_EVENT) {
                DocumentReference reference = database.document(documentPath);
                batch.update(reference,
                        "waitingList.list", FieldValue.arrayRemove(deviceId),
                        "waitingList.chosen", FieldValue.arrayRemove(deviceId),
                        "chosen", FieldValue.arrayRemove(deviceId),
                        "enrolled", FieldValue.arrayRemove(deviceId),
                        "cancelled", FieldValue.arrayRemove(deviceId),
                        "notEnrolled", FieldValue.arrayRemove(deviceId),
                        "coOrganizers", FieldValue.arrayRemove(deviceId),
                        "coOrganizerInvites", FieldValue.arrayRemove(deviceId),
                        FieldPath.of("waitingList", "status", deviceId), FieldValue.delete());
                return;
            }
            if (type == Type.DELETE_EVENT) {
                batch.delete(database.document(documentPath));
                return;
            }
            batch.delete(usersCollection.document(deviceId));
        }
    }

    static final class EventCleanupTarget {
        private final String documentPath;
        private final String organizerId;
        /**
         * Documents event Cleanup Target.
         */
        EventCleanupTarget(String documentPath, String organizerId) {
            this.documentPath = documentPath;
            this.organizerId = organizerId;
        }
        /**
         * Returns whether get Document Path.
         */
        String getDocumentPath() { return documentPath; }
        /**
         * Returns whether get Organizer Id.
         */
        String getOrganizerId() { return organizerId; }
    }

    /**
     * Handles backfill Device Id.
     */
    public void backfillDeviceId(String deviceId) {
        usersCollection.document(deviceId)
                .update("deviceId", deviceId)
                .addOnFailureListener(e -> Log.d(TAG, "Failed to backfill device ID", e));
    }

    /**
     * Handles search Users.
     */
    public void searchUsers(String query, OnCompleteListener<List<User>> listener) {
        String safeQuery = query == null ? "" : query.trim().toLowerCase();
        if (safeQuery.isEmpty()) {
            listener.onComplete(new ArrayList<>(), true);
            return;
        }
        usersCollection.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                listener.onComplete(new ArrayList<>(), false);
                return;
            }
            List<User> results = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                User user = document.toObject(User.class);
                if (user == null) continue;
                if (isBlank(user.getDeviceId())) user.setDeviceId(document.getId());
                String name = safeString(user.getName());
                String email = safeString(user.getEmail());
                String phone = safeString(user.getPhone());
                if (name.contains(safeQuery) || email.contains(safeQuery) || phone.contains(safeQuery)) {
                    results.add(user);
                }
            }
            listener.onComplete(results, true);
        });
    }

    /**
     * Returns whether is Blank.
     */
    private boolean isBlank(String value) { return TextHelper.isBlank(value); }
    /**
     * Documents to Lower Case.
     */
    private String safeString(String value) { return value == null ? "" : value.trim().toLowerCase(); }
    /**
     * Documents trim.
     */
    private String normalizePhone(String phone) { return phone == null ? "" : phone.trim(); }

    /**
     * Returns whether g.et All Users
     */
    public void getAllUsers(OnCompleteListener<List<User>> listener) {
        usersCollection.get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.d(TAG, "Failed to load all users", task.getException());
                listener.onComplete(null, false);
                return;
            }
            List<User> users = new ArrayList<>();
            for (QueryDocumentSnapshot document : task.getResult()) {
                User user = document.toObject(User.class);
                if (user != null && isBlank(user.getDeviceId())) {
                    user.setDeviceId(document.getId());
                }
                users.add(user);
            }
            listener.onComplete(users, true);
        });
    }

    /**
     * Deletes user as admin.
     */
    public void deleteUserAsAdmin(String deviceId, OnCompleteListener<Boolean> listener) {
        FirebaseFirestore database = usersCollection.getFirestore();
        database.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        listener.onComplete(false, false);
                        return;
                    }
                    List<EventCleanupTarget> cleanupTargets = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        cleanupTargets.add(new EventCleanupTarget(
                                document.getReference().getPath(),
                                document.getString("organizerId")));
                    }
                    List<CleanupOperation> cleanupOperations = buildCleanupOperations(deviceId, cleanupTargets);
                    cleanupOperations.add(CleanupOperation.deleteUser(deviceId));
                    List<List<CleanupOperation>> batches = chunkCleanupOperations(cleanupOperations);
                    commitCleanupOperations(database, batches, 0, (result, success) -> {
                        if (!success || result == null || !result) {
                            listener.onComplete(false, false);
                            return;
                        }

                        deleteProfilePhotoFromStorage(deviceId, listener);
                    });
                });
    }

    /**
     * Deletes profile photo from storage.
     */
    private void deleteProfilePhotoFromStorage(String deviceId, OnCompleteListener<Boolean> listener) {
        if (isBlank(deviceId)) {
            listener.onComplete(true, true);
            return;
        }

        /**
         * Returns whether get Error Code.
         */
        FirebaseStorage.getInstance()
                .getReference()
                .child("user_profiles")
                .child(deviceId)
                .child("photo.jpg")
                .delete()
                .addOnSuccessListener(unused -> listener.onComplete(true, true))
                .addOnFailureListener(exception -> {
                    if (exception instanceof StorageException
                            /**
                             * Returns whether get Error Code.
                             */
                            && ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                        listener.onComplete(true, true);
                        return;
                    }

                    Log.w(TAG, "Failed to delete profile photo from Storage for user " + deviceId, exception);
                    listener.onComplete(false, false);
                });
    }
}
