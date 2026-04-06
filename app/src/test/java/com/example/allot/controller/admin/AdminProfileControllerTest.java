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

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        userRepository = new FakeUserRepository();
        userController = new FakeUserController();
        controller = new AdminProfileController(userRepository, userController);
    }

    /**
     * Performs load all profiles requires admin and delegates when authorized.
     */
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

    /**
     * Performs load all profiles returns failure when admin lookup fails.
     */
    @Test
    public void loadAllProfiles_returnsFailureWhenAdminLookupFails() {
        userController.adminLookupSuccess = false;
        userRepository.allUsers = Arrays.asList(new User(), new User());

        controller.loadAllProfiles((profiles, success) -> {
            assertFalse(success);
            assertTrue(profiles == null);
        });
    }

    /**
     * Performs load all profiles propagates repository failure.
     */
    @Test
    public void loadAllProfiles_propagatesRepositoryFailure() {
        userController.isAdmin = true;
        userRepository.getAllUsersSuccess = false;

        controller.loadAllProfiles((profiles, success) -> {
            assertFalse(success);
            assertTrue(profiles == null);
        });
    }

    /**
     * Performs delete profile requires admin and delegates when authorized.
     */
    @Test
    public void deleteProfile_requiresAdminAndDelegatesWhenAuthorized() {
        userController.isAdmin = false;

        controller.deleteProfile("device-9", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
        });

        userController.isAdmin = true;
        userRepository.deleteResult = true;
        userRepository.deleteSuccess = true;

        controller.deleteProfile("device-9", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertEquals("device-9", userRepository.deletedDeviceId);
        });
    }

    /**
     * Performs delete profile returns failure when admin lookup fails.
     */
    @Test
    public void deleteProfile_returnsFailureWhenAdminLookupFails() {
        userController.adminLookupSuccess = false;

        controller.deleteProfile("device-9", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
        });
    }

    /**
     * Performs delete profile propagates repository delete failure.
     */
    @Test
    public void deleteProfile_propagatesRepositoryDeleteFailure() {
        userController.isAdmin = true;
        userRepository.deleteResult = false;
        userRepository.deleteSuccess = false;

        controller.deleteProfile("device-9", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
            assertEquals("device-9", userRepository.deletedDeviceId);
        });
    }

    private static class FakeUserRepository extends UserRepository {
        private List<User> allUsers;
        private String deletedDeviceId;
        private boolean deleteResult;
        private boolean getAllUsersSuccess = true;
        private boolean deleteSuccess;

        /**
         * Creates a new FakeUserRepository instance.
         */
        private FakeUserRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Performs get all users.
         *
         * @param listener the listener
         */
        @Override
        public void getAllUsers(com.example.allot.common.OnCompleteListener<List<User>> listener) {
            listener.onComplete(allUsers, getAllUsersSuccess);
        }

        /**
         * Performs delete user as admin.
         *
         * @param deviceId the device id
         * @param listener the listener
         */
        @Override
        public void deleteUserAsAdmin(String deviceId, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            deletedDeviceId = deviceId;
            listener.onComplete(deleteResult, deleteSuccess);
        }
    }

    private static class FakeUserController extends UserController {
        private boolean isAdmin;
        private boolean adminLookupSuccess = true;

        /**
         * Creates a new FakeUserController instance.
         */
        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        /**
         * Performs is current user admin.
         *
         * @param listener the listener
         */
        @Override
        public void isCurrentUserAdmin(com.example.allot.common.OnCompleteListener<Boolean> listener) {
            listener.onComplete(isAdmin, adminLookupSuccess);
        }
    }

    private static class FakeDeviceSessionStore implements DeviceSessionManager.DeviceSessionStore {
        private final String deviceId;

        /**
         * Creates a new FakeDeviceSessionStore instance.
         *
         * @param deviceId the device id
         */
        private FakeDeviceSessionStore(String deviceId) {
            this.deviceId = deviceId;
        }

        /**
         * Returns the device id.
         *
         * @return the device id
         */
        @Override
        public String getDeviceId() {
            return deviceId;
        }

        /**
         * Performs save device id.
         *
         * @param deviceId the device id
         */
        @Override
        public void saveDeviceId(String deviceId) {
        }
    }
}
