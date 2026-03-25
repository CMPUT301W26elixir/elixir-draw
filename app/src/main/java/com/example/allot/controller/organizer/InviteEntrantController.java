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

    public InviteEntrantController(Context context) {
        this(new EventRepository(), new UserController(context));
    }

    InviteEntrantController(EventRepository eventRepository, UserController userController) {
        this.eventRepository = eventRepository;
        this.userController = userController;
    }

    public void loadEvent(String eventId, OnCompleteListener<Event> listener) {
        eventRepository.getEventById(eventId, listener);
    }

    public void searchUsers(String query, OnCompleteListener<List<User>> listener) {
        userController.searchUsers(query, listener);
    }

    public void inviteUser(String eventId, String deviceId, OnCompleteListener<Boolean> listener) {
        eventRepository.inviteUserToEvent(eventId, deviceId, listener);
    }

    public String getCurrentDeviceId() {
        return userController.getCurrentDeviceId();
    }
}
