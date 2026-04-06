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

public class UserControllerTest {
    private FakeUserRepository userRepository;
    private UserController controller;

    /**
     * Updates up.
     */
    @Before
    public void setUp() {
        userRepository = new FakeUserRepository();
        controller = new UserController(userRepository, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
    }

    /**
     * Loads current user_backfills missing device id on loaded user.
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
     * Loads current user_returns null when missing but successful.
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
     * Loads or create user_returns existing user and backfills device id.
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
     * Loads or create user_creates new user when missing.
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
     * Updates user profile_rejects invalid fields.
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
     * Returns whether h.as Completed Profile_requires Name And Email
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
     * Handles toggle Saved Event_and Admin Check_delegate To Repository.
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
         * Handles fake User Repository.
         */
        private FakeUserRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Handles find User By Device Id.
         */
        @Override
        public void findUserByDeviceId(String deviceId, com.example.allot.common.OnCompleteListener<User> listener) {
            listener.onComplete(findUser, findSuccess);
        }

        /**
         * Returns whether g.et User By Device Id
         */
        @Override
        public void getUserByDeviceId(String deviceId, com.example.allot.common.OnCompleteListener<User> listener) {
            listener.onComplete(getUser, getUserSuccess);
        }

        /**
         * Handles backfill Device Id.
         */
        @Override
        public void backfillDeviceId(String deviceId) {
            backfilledDeviceId = deviceId;
        }

        /**
         * Creates new user.
         */
        @Override
        public void createNewUser(String deviceId, com.example.allot.common.OnCompleteListener<User> listener) {
            createdDeviceId = deviceId;
            listener.onComplete(createdUser, createdUser != null);
        }

        /**
         * Updates user profile.
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
         * Handles toggle Saved Event.
         */
        @Override
        public void toggleSavedEvent(String deviceId, String eventId, boolean isSaving,
                                     com.example.allot.common.OnCompleteListener<Boolean> listener) {
            toggleSavedDeviceId = deviceId;
            toggleSavedEventId = eventId;
            listener.onComplete(toggleSavedResult, Boolean.TRUE.equals(toggleSavedResult));
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
