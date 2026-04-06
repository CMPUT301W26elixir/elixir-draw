package com.example.allot.controller.profile;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.data.UserRepository;
import com.google.firebase.storage.FirebaseStorage;
import org.junit.Before;
import org.junit.Test;

public class ProfilePhotoControllerTest {
    private FakeUserRepository userRepository;
    private ProfilePhotoController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        userRepository = new FakeUserRepository();
        controller = new ProfilePhotoController(userRepository, (FirebaseStorage) null);
    }

    /**
     * Performs upload photo rejects blank device id or null photo.
     */
    @Test
    public void uploadPhoto_rejectsBlankDeviceIdOrNullPhoto() {
        controller.uploadPhoto(" ", null, (result, success) -> {
            assertFalse(success);
            assertTrue(result == null);
        });

        controller.uploadPhoto("device-1", null, (result, success) -> {
            assertFalse(success);
            assertTrue(result == null);
        });
    }

    /**
     * Performs delete photo rejects blank device id.
     */
    @Test
    public void deletePhoto_rejectsBlankDeviceId() {
        controller.deletePhoto(" ", "https://example.com/photo.jpg", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
        });
    }

    /**
     * Performs delete photo succeeds without storage delete when url blank.
     */
    @Test
    public void deletePhoto_succeedsWithoutStorageDeleteWhenUrlBlank() {
        userRepository.updateFieldsSuccess = true;

        controller.deletePhoto("device-1", " ", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertTrue(userRepository.updatedFieldsCalled);
        });
    }

    private static class FakeUserRepository extends UserRepository {
        private boolean updateFieldsSuccess;
        private boolean updatedFieldsCalled;

        /**
         * Creates a new FakeUserRepository instance.
         */
        private FakeUserRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Performs update user fields.
         *
         * @param deviceId the device id
         * @param updates the updates
         * @param listener the listener
         */
        @Override
        public void updateUserFields(String deviceId, java.util.Map<String, Object> updates,
                                     com.example.allot.common.OnCompleteListener<Boolean> listener) {
            updatedFieldsCalled = true;
            listener.onComplete(updateFieldsSuccess, updateFieldsSuccess);
        }
    }
}
