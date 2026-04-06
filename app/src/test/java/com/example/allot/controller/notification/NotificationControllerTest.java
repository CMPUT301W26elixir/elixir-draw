package com.example.allot.controller.notification;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.data.NotificationRepository;
import com.example.allot.model.notification.NotificationItem;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the notification controller.
 */
public class NotificationControllerTest {
    private FakeNotificationRepository notificationRepository;
    private NotificationController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        notificationRepository = new FakeNotificationRepository();
        controller = new NotificationController(notificationRepository);
    }

    /**
     * Performs notify selected entrants returns failure for empty list.
     */
    @Test
    public void notifySelectedEntrants_returnsFailureForEmptyList() {
        controller.notifySelectedEntrants(new ArrayList<>(), "event-1", "Spring Gala", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
            assertTrue(notificationRepository.savedNotifications.isEmpty());
        });
    }

    /**
     * Performs notify selected entrants saves notification for each entrant.
     */
    @Test
    public void notifySelectedEntrants_savesNotificationForEachEntrant() {
        controller.notifySelectedEntrants(Arrays.asList("u1", "u2"), "event-1", "Spring Gala", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertEquals(2, notificationRepository.savedNotifications.size());
            assertEquals("u1", notificationRepository.savedNotifications.get(0).getUserId());
            assertEquals("You've been selected!", notificationRepository.savedNotifications.get(0).getTitle());
            assertTrue(notificationRepository.savedNotifications.get(0).getMessage().contains("Spring Gala"));
            assertEquals("u2", notificationRepository.savedNotifications.get(1).getUserId());
        });
    }

    /**
     * Performs notify not selected entrants saves expected content.
     */
    @Test
    public void notifyNotSelectedEntrants_savesExpectedContent() {
        controller.notifyNotSelectedEntrants(Arrays.asList("u1"), "event-9", "Hack Night", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertEquals(1, notificationRepository.savedNotifications.size());
            NotificationItem item = notificationRepository.savedNotifications.get(0);
            assertEquals("Better luck next time", item.getTitle());
            assertTrue(item.getMessage().contains("Hack Night"));
        });
    }

    /**
     * Performs get notifications for user delegates to repository.
     */
    @Test
    public void getNotificationsForUser_delegatesToRepository() {
        List<NotificationItem> expected = Arrays.asList(
                new NotificationItem("u1", "event-1", "Title", "Message")
        );
        notificationRepository.notificationsToReturn = expected;

        controller.getNotificationsForUser("u1", (items, success) -> {
            assertTrue(success);
            assertEquals(expected, items);
            assertEquals("u1", notificationRepository.lastRequestedUserId);
        });
    }

    /**
     * Stores and retrieves fake notification.
     */
    private static class FakeNotificationRepository extends NotificationRepository {
        private final List<NotificationItem> savedNotifications = new ArrayList<>();
        private List<NotificationItem> notificationsToReturn = new ArrayList<>();
        private String lastRequestedUserId;

        /**
         * Creates a new FakeNotificationRepository instance.
         */
        private FakeNotificationRepository() {
            super((FirebaseFirestore) null);
        }

        /**
         * Performs save notification.
         *
         * @param notification the notification
         * @param listener the listener
         */
        @Override
        public void saveNotification(NotificationItem notification, OnCompleteListener<Boolean> listener) {
            savedNotifications.add(notification);
            listener.onComplete(true, true);
        }

        /**
         * Performs get notifications for user.
         *
         * @param userId the user id
         * @param listener the listener
         */
        @Override
        public void getNotificationsForUser(String userId, OnCompleteListener<List<NotificationItem>> listener) {
            lastRequestedUserId = userId;
            listener.onComplete(notificationsToReturn, true);
        }
    }
}
