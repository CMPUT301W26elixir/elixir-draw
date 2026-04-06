package com.example.allot.controller.organizer;

import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.profile.User;
import java.util.List;

/**
 * Handles loading and inviting co-organizers for an event.
 */
public class InviteCoOrganizerController {
    private final EventRepository eventRepository;
    private final UserController userController;

    /**
     * Creates a new InviteCoOrganizerController instance.
     *
     * @param context the context
     */
    public InviteCoOrganizerController(android.content.Context context) {
        this(new EventRepository(), new UserController(context));
    }

    /**
     * Creates a new InviteCoOrganizerController instance.
     *
     * @param eventRepository the event repository
     * @param userController the user controller
     */
    InviteCoOrganizerController(EventRepository eventRepository, UserController userController) {
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
     * Performs invite co organizer.
     *
     * @param eventId the event id
     * @param userId the user id
     * @param listener the listener
     */
    public void inviteCoOrganizer(String eventId, String userId, OnCompleteListener<Boolean> listener) {
        eventRepository.inviteCoOrganizer(eventId, userId, listener);
    }

    /**
     * Returns the current device id.
     *
     * @return the current device id
     */
    public String getCurrentDeviceId() {
        return userController.getCurrentDeviceId();
    }

    /**
     * Returns whether organizer or co organizer.
     *
     * @param event the event
     * @return whether organizer or co organizer
     */
    public boolean isOrganizerOrCoOrganizer(Event event) {
        if (event == null) {
            return false;
        }
        String deviceId = userController.getCurrentDeviceId();
        if (deviceId == null) {
            return false;
        }
        if (deviceId.equals(event.getOrganizerId())) {
            return true;
        }
        return event.getCoOrganizers() != null && event.getCoOrganizers().contains(deviceId);
    }
}
