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
     */
    public InviteCoOrganizerController(android.content.Context context) {
        this(new EventRepository(), new UserController(context));
    }

    /**
     * Creates a new InviteCoOrganizerController instance.
     */
    InviteCoOrganizerController(EventRepository eventRepository, UserController userController) {
        this.eventRepository = eventRepository;
        this.userController = userController;
    }

    /**
     * Loads event.
     */
    public void loadEvent(String eventId, OnCompleteListener<Event> listener) {
        eventRepository.getEventById(eventId, listener);
    }

    /**
     * Handles search Users.
     */
    public void searchUsers(String query, OnCompleteListener<List<User>> listener) {
        userController.searchUsers(query, listener);
    }

    /**
     * Handles invite Co Organizer.
     */
    public void inviteCoOrganizer(String eventId, String userId, OnCompleteListener<Boolean> listener) {
        eventRepository.inviteCoOrganizer(eventId, userId, listener);
    }

    /**
     * Returns whether g.et Current Device Id
     */
    public String getCurrentDeviceId() {
        return userController.getCurrentDeviceId();
    }

    /**
     * Returns whether i.s Organizer Or Co Organizer
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
