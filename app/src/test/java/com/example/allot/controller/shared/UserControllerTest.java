package com.example.allot.controller.shared;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.UserRepository;
import com.example.allot.model.profile.User;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the user controller.
 */
public class UserControllerTest {
    private FakeUserRepository userRepository;
    private UserController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        userRepository = new FakeUserRepository();
        controller = new UserController(userRepository, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
    }

    /**
     * Performs load current user backfills missing device id on loaded user.
     */
    @Test
    public void loadCurrentUser_backfillsMissingDeviceIdOnLoadedUser() {
        User user = new User();
        user.setFirstName("Alex");
        userRepository.findUser = user;
        userRepository.findSuccess = true;

        controller.loadCurrentUser((result, success) -> {
            assertTrue(success);
            assertEquals("device-1", result.getDeviceId());
        });
    }

    /**
     * Performs load current user returns null when missing but successful.
     */
    @Test
    public void loadCurrentUser_returnsNullWhenMissingButSuccessful() {
        userRepository.findUser = null;
        userRepository.findSuccess = true;

        controller.loadCurrentUser((result, success) -> {
            assertTrue(success);
            assertNull(result);
        });
    }

    /**
     * Performs load or create user returns existing user and backfills device id.
     */
    @Test
    public void loadOrCreateUser_returnsExistingUserAndBackfillsDeviceId() {
        User user = new User();
        userRepository.getUser = user;
        userRepository.getUserSuccess = true;

        controller.loadOrCreateUser((result, success) -> {
            assertTrue(success);
            assertEquals("device-1", result.getDeviceId());
            assertEquals("device-1", userRepository.backfilledDeviceId);
        });
    }

    /**
     * Performs load or create user creates new user when missing.
     */
    @Test
    public void loadOrCreateUser_createsNewUserWhenMissing() {
        userRepository.getUser = null;
        userRepository.getUserSuccess = false;
        User created = new User();
        created.setDeviceId("device-1");
        userRepository.createdUser = created;

        controller.loadOrCreateUser((result, success) -> {
            assertTrue(success);
            assertEquals(created, result);
            assertEquals("device-1", userRepository.createdDeviceId);
        });
    }

    /**
     * Performs update user profile rejects invalid fields.
     */
    @Test
    public void updateUserProfile_rejectsInvalidFields() {
        controller.updateUserProfile(" ", "Lane", "bad-email", "", true, (user, success) -> {
            assertFalse(success);
            assertNull(user);
        });
        assertNull(userRepository.updateProfileDeviceId);
    }

    /**
     * Performs has completed profile requires name and email.
     */
    @Test
    public void hasCompletedProfile_requiresNameAndEmail() {
        User complete = new User();
        complete.setFirstName("Jordan");
        complete.setLastName("Lane");
        complete.setEmail("jordan@example.com");

        User incomplete = new User();
        incomplete.setFirstName("Jordan");
        incomplete.setEmail(" ");

        assertTrue(controller.hasCompletedProfile(complete));
        assertFalse(controller.hasCompletedProfile(incomplete));
        assertFalse(controller.hasCompletedProfile(null));
    }

    /**
     * Performs toggle saved event and admin check delegate to repository.
     */
    @Test
    public void toggleSavedEvent_andAdminCheck_delegateToRepository() {
        userRepository.toggleSavedResult = true;
        controller.toggleSavedEvent("event-1", true, (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertEquals("device-1", userRepository.toggleSavedDeviceId);
            assertEquals("event-1", userRepository.toggleSavedEventId);
        });

        User adminUser = new User();
        adminUser.setRole("admin");
        userRepository.getUser = adminUser;
        userRepository.getUserSuccess = true;

        controller.isCurrentUserAdmin((result, success) -> {
            assertTrue(success);
            assertTrue(result);
        });
    }

    /**
     * Stores and retrieves fake user.
     */
    private static class FakeUserRepository extends UserRepository {
        private User findUser;
        private boolean findSuccess;
        private User getUser;
        private boolean getUserSuccess;
        private String backfilledDeviceId;
        private User createdUser;
        private String createdDeviceId;
        private User updatedUser;
        private boolean updateProfileSuccess;
        private String updateProfileDeviceId;
        private String updateProfileFirstName;
        private String updateProfileLastName;
        private String updateProfileEmail;
        private String toggleSavedDeviceId;
        private String toggleSavedEventId;
        private Boolean toggleSavedResult;

        /**
         * Creates a new FakeUserRepository instance.
         */
        private FakeUserRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Performs find user by device id.
         *
         * @param deviceId the device id
         * @param listener the listener
         */
        @Override
        public void findUserByDeviceId(String deviceId, com.example.allot.common.OnCompleteListener<User> listener) {
            listener.onComplete(findUser, findSuccess);
        }

        /**
         * Performs get user by device id.
         *
         * @param deviceId the device id
         * @param listener the listener
         */
        @Override
        public void getUserByDeviceId(String deviceId, com.example.allot.common.OnCompleteListener<User> listener) {
            listener.onComplete(getUser, getUserSuccess);
        }

        /**
         * Performs backfill device id.
         *
         * @param deviceId the device id
         */
        @Override
        public void backfillDeviceId(String deviceId) {
            backfilledDeviceId = deviceId;
        }

        /**
         * Performs create new user.
         *
         * @param deviceId the device id
         * @param listener the listener
         */
        @Override
        public void createNewUser(String deviceId, com.example.allot.common.OnCompleteListener<User> listener) {
            createdDeviceId = deviceId;
            listener.onComplete(createdUser, createdUser != null);
        }

        /**
         * Performs update user profile.
         *
         * @param deviceId the device id
         * @param firstName the first name
         * @param lastName the last name
         * @param email the email
         * @param phone the phone
         * @param notiEnabled the noti enabled
         * @param listener the listener
         */
        @Override
        public void updateUserProfile(String deviceId, String firstName, String lastName, String email,
                                      String phone, boolean notiEnabled,
                                      com.example.allot.common.OnCompleteListener<User> listener) {
            updateProfileDeviceId = deviceId;
            updateProfileFirstName = firstName;
            updateProfileLastName = lastName;
            updateProfileEmail = email;
            listener.onComplete(updatedUser, updateProfileSuccess);
        }

        /**
         * Performs toggle saved event.
         *
         * @param deviceId the device id
         * @param eventId the event id
         * @param isSaving whether saving
         * @param listener the listener
         */
        @Override
        public void toggleSavedEvent(String deviceId, String eventId, boolean isSaving,
                                     com.example.allot.common.OnCompleteListener<Boolean> listener) {
            toggleSavedDeviceId = deviceId;
            toggleSavedEventId = eventId;
            listener.onComplete(toggleSavedResult, Boolean.TRUE.equals(toggleSavedResult));
        }
    }

    /**
     * Represents the fake device session store.
     */
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
