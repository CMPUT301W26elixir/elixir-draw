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

    public NotificationController() {
        this.notificationRepository = new NotificationRepository();
    }

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Sends a "selected" notification to a list of chosen entrants.
     *
     * @param entrantIds list of device IDs of chosen entrants
     * @param eventId    the event they were selected for
     * @param eventName  the name of the event for the notification message
     * @param listener   called when all notifications have been sent
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
     * Sends a "not selected" notification to a list of entrants.
     *
     * @param entrantIds list of device IDs of entrants not chosen
     * @param eventId    the event they were not selected for
     * @param eventName  the name of the event for the notification message
     * @param listener   called when all notifications have been sent
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
     * Fetches all notifications for a given user.
     *
     * @param userId   the device ID of the user
     * @param listener called with the list of notifications
     */
    public void getNotificationsForUser(String userId,
                                        OnCompleteListener<List<NotificationItem>> listener) {
        notificationRepository.getNotificationsForUser(userId, listener);
    }

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
