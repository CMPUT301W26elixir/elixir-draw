package com.example.allot.controller.notification;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.data.NotificationRepository;
import com.example.allot.model.notification.NotificationItem;
import java.util.List;

/**
 * Handles sending and retrieving notifications for entrants.
 */
public class NotificationController {
    private final NotificationRepository notificationRepository;

    /**
     * Creates a new NotificationController instance.
     */
    public NotificationController() {
        this.notificationRepository = new NotificationRepository();
    }

    /**
     * Creates a new NotificationController instance.
     *
     * @param notificationRepository the notification repository
     */
    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Performs notify selected entrants.
     *
     * @param entrantIds the entrant ids
     * @param eventId the event id
     * @param eventName the event name
     * @param listener the listener
     */
    public void notifySelectedEntrants(List<String> entrantIds,
                                       String eventId,
                                       String eventName,
                                       OnCompleteListener<Boolean> listener) {
        if (entrantIds == null || entrantIds.isEmpty()) {
            listener.onComplete(false, false);
            return;
        }

        sendNotifications(entrantIds, 0, eventId, eventName, listener);
    }

    /**
     * Performs notify not selected entrants.
     *
     * @param entrantIds the entrant ids
     * @param eventId the event id
     * @param eventName the event name
     * @param listener the listener
     */
    public void notifyNotSelectedEntrants(List<String> entrantIds,
                                          String eventId,
                                          String eventName,
                                          OnCompleteListener<Boolean> listener) {
        if (entrantIds == null || entrantIds.isEmpty()) {
            listener.onComplete(false, false);
            return;
        }

        sendNotSelectedNotifications(entrantIds, 0, eventId, eventName, listener);
    }

    /**
     * Performs get notifications for user.
     *
     * @param userId the user id
     * @param listener the listener
     */
    public void getNotificationsForUser(String userId,
                                        OnCompleteListener<List<NotificationItem>> listener) {
        notificationRepository.getNotificationsForUser(userId, listener);
    }

    /**
     * Performs send notifications.
     *
     * @param entrantIds the entrant ids
     * @param index the index
     * @param eventId the event id
     * @param eventName the event name
     * @param listener the listener
     */
    private void sendNotifications(List<String> entrantIds,
                                   int index,
                                   String eventId,
                                   String eventName,
                                   OnCompleteListener<Boolean> listener) {
        if (index >= entrantIds.size()) {
            listener.onComplete(true, true);
            return;
        }

        String userId = entrantIds.get(index);
        NotificationItem notification = new NotificationItem(
                userId,
                eventId,
                "You've been selected!",
                "Congratulations! You were chosen from the waiting list for " + eventName + ". Open the app to accept or decline."
        );

        notificationRepository.saveNotification(notification, (result, success) ->
                sendNotifications(entrantIds, index + 1, eventId, eventName, listener));
    }

    /**
     * Performs send not selected notifications.
     *
     * @param entrantIds the entrant ids
     * @param index the index
     * @param eventId the event id
     * @param eventName the event name
     * @param listener the listener
     */
    private void sendNotSelectedNotifications(List<String> entrantIds,
                                              int index,
                                              String eventId,
                                              String eventName,
                                              OnCompleteListener<Boolean> listener) {
        if (index >= entrantIds.size()) {
            listener.onComplete(true, true);
            return;
        }

        String userId = entrantIds.get(index);
        NotificationItem notification = new NotificationItem(
                userId,
                eventId,
                "Better luck next time",
                "Unfortunately, you were not selected in the lottery draw for " + eventName + ". You remain on the waiting list."
        );

        notificationRepository.saveNotification(notification, (result, success) ->
                sendNotSelectedNotifications(entrantIds, index + 1, eventId, eventName, listener));
    }
}
