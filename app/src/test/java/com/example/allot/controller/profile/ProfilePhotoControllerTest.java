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

    @Before
    public void setUp() {
        userRepository = new FakeUserRepository();
        controller = new ProfilePhotoController(userRepository, (FirebaseStorage) null);
    }

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

    @Test
    public void deletePhoto_rejectsBlankDeviceId() {
        controller.deletePhoto(" ", "https://example.com/photo.jpg", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
        });
    }

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

        private FakeUserRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        @Override
        public void updateUserFields(String deviceId, java.util.Map<String, Object> updates,
                                     com.example.allot.common.OnCompleteListener<Boolean> listener) {
            updatedFieldsCalled = true;
            listener.onComplete(updateFieldsSuccess, updateFieldsSuccess);
        }
    }
}
