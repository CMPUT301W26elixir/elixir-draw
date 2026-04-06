package com.example.allot.controller.profile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.model.profile.ProfileActionResult;
import com.example.allot.model.profile.ProfileFormSnapshot;
import com.example.allot.model.profile.User;
import org.junit.Before;
import org.junit.Test;

public class ProfileControllerTest {
    private FakeUserController userController;
    private ProfileController controller;

    @Before
    public void setUp() {
        userController = new FakeUserController();
        controller = new ProfileController(userController);
    }

    @Test
    public void loadProfile_returnsSnapshotFromLoadedUser() {
        User user = new User();
        user.setFirstName("Avery");
        user.setLastName("Stone");
        user.setEmail("avery@example.com");
        user.setPhone("780-000-0000");
        user.setNotiEnabled(true);
        userController.userToLoad = user;
        userController.loadSuccess = true;

        controller.loadProfile((snapshot, success) -> {
            assertTrue(success);
            assertNotNull(snapshot);
            assertEquals("Avery", snapshot.getFirstName());
            assertEquals("Stone", snapshot.getLastName());
            assertEquals("avery@example.com", snapshot.getEmail());
        });
    }

    @Test
    public void loadProfile_returnsFailureWhenUserMissing() {
        userController.userToLoad = null;
        userController.loadSuccess = false;

        controller.loadProfile((snapshot, success) -> {
            assertFalse(success);
            assertNull(snapshot);
        });
    }

    @Test
    public void isSaveAvailable_onlyWhenChangedAndIdle() {
        ProfileFormSnapshot original = new ProfileFormSnapshot("A", "B", "a@example.com", "", true);
        ProfileFormSnapshot changed = new ProfileFormSnapshot("A", "C", "a@example.com", "", true);

        assertTrue(controller.isSaveAvailable(original, changed, false, false));
        assertFalse(controller.isSaveAvailable(original, original, false, false));
        assertFalse(controller.isSaveAvailable(original, changed, true, false));
        assertFalse(controller.isSaveAvailable(original, changed, false, true));
    }

    @Test
    public void saveProfile_returnsSavedSnapshotOnSuccess() {
        User savedUser = new User();
        savedUser.setFirstName("Jordan");
        savedUser.setLastName("Lane");
        savedUser.setEmail("jordan@example.com");
        userController.updatedUser = savedUser;
        userController.updateSuccess = true;

        controller.saveProfile(new ProfileFormSnapshot("Jordan", "Lane", "jordan@example.com", "", false),
                (ProfileActionResult result, boolean success) -> {
                    assertTrue(success);
                    assertTrue(result.isSuccess());
                    assertEquals("Profile updated.", result.getMessage());
                    assertEquals("Jordan", result.getFormSnapshot().getFirstName());
                });
    }

    @Test
    public void saveProfile_returnsFailureMessageWhenUpdateFails() {
        userController.updatedUser = null;
        userController.updateSuccess = false;

        controller.saveProfile(new ProfileFormSnapshot("Jordan", "Lane", "jordan@example.com", "", false),
                (ProfileActionResult result, boolean success) -> {
                    assertFalse(success);
                    assertFalse(result.isSuccess());
                    assertEquals("Could not save your profile. Please try again.", result.getMessage());
                });
    }

    @Test
    public void deleteProfile_returnsSuccessAndFailureMessages() {
        userController.deleteResult = true;
        userController.deleteSuccess = true;

        controller.deleteProfile((result, success) -> {
            assertTrue(success);
            assertTrue(result.isSuccess());
            assertEquals("Profile deleted.", result.getMessage());
        });

        userController.deleteResult = false;
        userController.deleteSuccess = false;

        controller.deleteProfile((result, success) -> {
            assertFalse(success);
            assertFalse(result.isSuccess());
            assertEquals("Could not delete your profile. Please try again.", result.getMessage());
        });
    }

    private static class FakeUserController extends UserController {
        private User userToLoad;
        private boolean loadSuccess;
        private User updatedUser;
        private boolean updateSuccess;
        private Boolean deleteResult;
        private boolean deleteSuccess;

        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        @Override
        public void loadCurrentUser(com.example.allot.common.OnCompleteListener<User> listener) {
            listener.onComplete(userToLoad, loadSuccess);
        }

        @Override
        public void updateUserProfile(String firstName, String lastName, String email, String phone,
                                      boolean notiEnabled, com.example.allot.common.OnCompleteListener<User> listener) {
            listener.onComplete(updatedUser, updateSuccess);
        }

        @Override
        public void deleteCurrentUser(com.example.allot.common.OnCompleteListener<Boolean> listener) {
            listener.onComplete(deleteResult, deleteSuccess);
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
