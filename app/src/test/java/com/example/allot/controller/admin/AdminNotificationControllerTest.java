package com.example.allot.controller.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.NotificationRepository;
import com.example.allot.model.notification.NotificationItem;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class AdminNotificationControllerTest {
    private FakeNotificationRepository notificationRepository;
    private FakeUserController userController;
    private AdminNotificationController controller;

    /**
     * Updates up.
     */
    @Before
    public void setUp() {
        notificationRepository = new FakeNotificationRepository();
        userController = new FakeUserController();
        controller = new AdminNotificationController(notificationRepository, userController);
    }

    /**
     * Loads all notifications_requires admin and delegates when authorized.
     */
    @Test
    public void loadAllNotifications_requiresAdminAndDelegatesWhenAuthorized() {
        userController.isAdmin = false;

        controller.loadAllNotifications((items, success) -> {
            assertFalse(success);
            assertTrue(items == null);
        });

        userController.isAdmin = true;
        notificationRepository.notifications = Arrays.asList(
                new NotificationItem("u1", "e1", "T1", "M1"),
                new NotificationItem("u2", "e2", "T2", "M2")
        );

        controller.loadAllNotifications((items, success) -> {
            assertTrue(success);
            assertEquals(2, items.size());
        });
    }

    /**
     * Loads all notifications_returns failure when admin lookup fails.
     */
    @Test
    public void loadAllNotifications_returnsFailureWhenAdminLookupFails() {
        userController.adminLookupSuccess = false;
        notificationRepository.notifications = Arrays.asList(
                new NotificationItem("u1", "e1", "T1", "M1")
        );

        controller.loadAllNotifications((items, success) -> {
            assertFalse(success);
            assertTrue(items == null);
        });
    }

    /**
     * Loads all notifications_propagates repository failure.
     */
    @Test
    public void loadAllNotifications_propagatesRepositoryFailure() {
        userController.isAdmin = true;
        notificationRepository.getAllNotificationsSuccess = false;

        controller.loadAllNotifications((items, success) -> {
            assertFalse(success);
            assertTrue(items == null);
        });
    }

    private static class FakeNotificationRepository extends NotificationRepository {
        private List<NotificationItem> notifications;
        private boolean getAllNotificationsSuccess = true;

        /**
         * Handles fake Notification Repository.
         */
        private FakeNotificationRepository() {
            super((FirebaseFirestore) null);
        }

        /**
         * Returns whether g.et All Notifications
         */
        @Override
        public void getAllNotifications(com.example.allot.common.OnCompleteListener<List<NotificationItem>> listener) {
            listener.onComplete(notifications, getAllNotificationsSuccess);
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
