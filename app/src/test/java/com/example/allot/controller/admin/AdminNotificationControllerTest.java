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

/**
 * Tests the admin notification controller.
 */
public class AdminNotificationControllerTest {
    private FakeNotificationRepository notificationRepository;
    private FakeUserController userController;
    private AdminNotificationController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        notificationRepository = new FakeNotificationRepository();
        userController = new FakeUserController();
        controller = new AdminNotificationController(notificationRepository, userController);
    }

    /**
     * Performs load all notifications requires admin and delegates when authorized.
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
     * Performs load all notifications returns failure when admin lookup fails.
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
     * Performs load all notifications propagates repository failure.
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

    /**
     * Stores and retrieves fake notification.
     */
    private static class FakeNotificationRepository extends NotificationRepository {
        private List<NotificationItem> notifications;
        private boolean getAllNotificationsSuccess = true;

        /**
         * Creates a new FakeNotificationRepository instance.
         */
        private FakeNotificationRepository() {
            super((FirebaseFirestore) null);
        }

        /**
         * Performs get all notifications.
         *
         * @param listener the listener
         */
        @Override
        public void getAllNotifications(com.example.allot.common.OnCompleteListener<List<NotificationItem>> listener) {
            listener.onComplete(notifications, getAllNotificationsSuccess);
        }
    }

    /**
     * Coordinates fake user.
     */
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
