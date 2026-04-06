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
     * Updates up.
     */
    @Before
    public void setUp() {
        userRepository = new FakeUserRepository();
        userController = new FakeUserController();
        controller = new AdminProfileController(userRepository, userController);
    }

    /**
     * Loads all profiles_requires admin and delegates when authorized.
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
     * Loads all profiles_returns failure when admin lookup fails.
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
     * Loads all profiles_propagates repository failure.
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
     * Deletes profile_requires admin and delegates when authorized.
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
     * Deletes profile_returns failure when admin lookup fails.
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
     * Deletes profile_propagates repository delete failure.
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
         * Handles fake User Repository.
         */
        private FakeUserRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Returns whether g.et All Users
         */
        @Override
        public void getAllUsers(com.example.allot.common.OnCompleteListener<List<User>> listener) {
            listener.onComplete(allUsers, getAllUsersSuccess);
        }

        /**
         * Deletes user as admin.
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
         * Handles fake User Controller.
         */
        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        /**
         * Returns whether i.s Current User Admin
         */
        @Override
        public void isCurrentUserAdmin(com.example.allot.common.OnCompleteListener<Boolean> listener) {
            listener.onComplete(isAdmin, adminLookupSuccess);
        }
    }

    private static class FakeDeviceSessionStore implements DeviceSessionManager.DeviceSessionStore {
        private final String deviceId;

        /**
         * Handles fake Device Session Store.
         */
        private FakeDeviceSessionStore(String deviceId) {
            this.deviceId = deviceId;
        }

        /**
         * Returns whether g.et Device Id
         */
        @Override
        public String getDeviceId() {
            return deviceId;
        }

        /**
         * Saves device id.
         */
        @Override
        public void saveDeviceId(String deviceId) {
        }
    }
}
