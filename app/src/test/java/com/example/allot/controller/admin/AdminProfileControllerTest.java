package com.example.allot.controller.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.UserRepository;
import com.example.allot.model.profile.User;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class AdminProfileControllerTest {
    private FakeUserRepository userRepository;
    private FakeUserController userController;
    private AdminProfileController controller;

    @Before
    public void setUp() {
        userRepository = new FakeUserRepository();
        userController = new FakeUserController();
        controller = new AdminProfileController(userRepository, userController);
    }

    @Test
    public void loadAllProfiles_requiresAdminAndDelegatesWhenAuthorized() {
        userController.isAdmin = false;

        controller.loadAllProfiles((profiles, success) -> {
            assertFalse(success);
            assertTrue(profiles == null);
        });

        userController.isAdmin = true;
        userRepository.allUsers = Arrays.asList(new User(), new User());

        controller.loadAllProfiles((profiles, success) -> {
            assertTrue(success);
            assertEquals(2, profiles.size());
        });
    }

    @Test
    public void deleteProfile_requiresAdminAndDelegatesWhenAuthorized() {
        userController.isAdmin = false;

        controller.deleteProfile("device-9", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
        });

        userController.isAdmin = true;
        userRepository.deleteResult = true;

        controller.deleteProfile("device-9", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertEquals("device-9", userRepository.deletedDeviceId);
        });
    }

    private static class FakeUserRepository extends UserRepository {
        private List<User> allUsers;
        private String deletedDeviceId;
        private boolean deleteResult;

        private FakeUserRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        @Override
        public void getAllUsers(com.example.allot.common.OnCompleteListener<List<User>> listener) {
            listener.onComplete(allUsers, true);
        }

        @Override
        public void deleteUserAsAdmin(String deviceId, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            deletedDeviceId = deviceId;
            listener.onComplete(deleteResult, deleteResult);
        }
    }

    private static class FakeUserController extends UserController {
        private boolean isAdmin;

        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        @Override
        public void isCurrentUserAdmin(com.example.allot.common.OnCompleteListener<Boolean> listener) {
            listener.onComplete(isAdmin, true);
        }
    }

    private static class FakeDeviceSessionStore implements DeviceSessionManager.DeviceSessionStore {
        private final String deviceId;

        private FakeDeviceSessionStore(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public String getDeviceId() {
            return deviceId;
        }

        @Override
        public void saveDeviceId(String deviceId) {
        }
    }
}
