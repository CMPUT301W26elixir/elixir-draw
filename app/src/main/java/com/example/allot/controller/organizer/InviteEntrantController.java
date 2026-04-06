package com.example.allot.controller.organizer;

import android.content.Context;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.profile.User;
import java.util.List;

/**
 * Handles loading events and inviting users for private events.
 */
public class InviteEntrantController {
    private final EventRepository eventRepository;
    private final UserController userController;

    /**
     * Creates a new InviteEntrantController instance.
     *
     * @param context the context
     */
    public InviteEntrantController(Context context) {
        this(new EventRepository(), new UserController(context));
    }

    /**
     * Creates a new InviteEntrantController instance.
     *
     * @param eventRepository the event repository
     * @param userController the user controller
     */
    InviteEntrantController(EventRepository eventRepository, UserController userController) {
        this.eventRepository = eventRepository;
        this.userController = userController;
    }

    /**
     * Performs load event.
     *
     * @param eventId the event id
     * @param listener the listener
     */
    public void loadEvent(String eventId, OnCompleteListener<Event> listener) {
        eventRepository.getEventById(eventId, listener);
    }

    /**
     * Performs search users.
     *
     * @param query the query
     * @param listener the listener
     */
    public void searchUsers(String query, OnCompleteListener<List<User>> listener) {
        userController.searchUsers(query, listener);
    }

    /**
     * Performs invite user.
     *
     * @param eventId the event id
     * @param deviceId the device id
     * @param listener the listener
     */
    public void inviteUser(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        eventRepository.inviteUserToEvent(eventId, deviceId, listener);
    }

    /**
     * Returns the current device id.
     *
     * @return the current device id
     */
    public String getCurrentDeviceId() {
        return userController.getCurrentDeviceId();
    }
}
