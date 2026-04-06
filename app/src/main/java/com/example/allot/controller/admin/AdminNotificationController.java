package com.example.allot.controller.admin;

import android.content.Context;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.NotificationRepository;
import com.example.allot.model.notification.NotificationItem;
import java.util.List;

/**
 * Controller for admin notification operations.
 * Handles loading all entrant notifications with admin access checks.
 */
public class AdminNotificationController {
    private final NotificationRepository notificationRepository;
    private final UserController userController;

    /**
     * Creates an AdminNotificationController with default dependencies.
     *
     * @param context the context used to access shared preferences
     */
    public AdminNotificationController(Context context) {
        this(new NotificationRepository(), new UserController(context));
    }

    /**
     * Creates an AdminNotificationController with provided dependencies.
     *
     * @param notificationRepository the notification repository
     * @param userController the user controller
     */
    public AdminNotificationController(NotificationRepository notificationRepository,
                                       UserController userController) {
        this.notificationRepository = notificationRepository;
        this.userController = userController;
    }

    /**
     * Loads all notifications for admin browsing.
     * Only admin users can access this operation.
     *
     * @param listener the listener that receives notifications and success result
     */
    public void loadAllNotifications(OnCompleteListener<List<NotificationItem>> listener) {
        userController.isCurrentUserAdmin((isAdmin, success) -> {
            if (!success || !isAdmin) {
                listener.onComplete(null, false);
                return;
            }
            notificationRepository.getAllNotifications(listener);
        });
    }
}
