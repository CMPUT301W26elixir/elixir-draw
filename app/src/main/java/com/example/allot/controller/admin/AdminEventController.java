package com.example.allot.controller.admin;

import android.content.Context;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.event.EventPosterController;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import java.util.List;

/**
 * Controller for admin event operations.
 * Handles loading all events and deleting events with admin privileges.
 */
public class AdminEventController {
    private final EventRepository eventRepository;
    private final EventPosterController eventPosterController;
    private final UserController userController;

    /**
     * Creates a new AdminEventController instance.
     *
     * @param context the context
     */
    public AdminEventController(Context context) {
        this(new EventRepository(), new EventPosterController(), new UserController(context));
    }

    /**
     * Creates a new AdminEventController instance.
     *
     * @param eventRepository the event repository
     * @param eventPosterController the event poster controller
     * @param userController the user controller
     */
    public AdminEventController(EventRepository eventRepository,
                                EventPosterController eventPosterController,
                                UserController userController) {
        this.eventRepository = eventRepository;
        this.eventPosterController = eventPosterController;
        this.userController = userController;
    }

    /**
     * Performs load all events.
     *
     * @param listener the listener
     */
    public void loadAllEvents(OnCompleteListener<List<Event>> listener) {
        userController.isCurrentUserAdmin((isAdmin, success) -> {
            if (!success || !isAdmin) {
                listener.onComplete(null, false);
                return;
            }
            eventRepository.getAllEvents(listener);
        });
    }

    /**
     * Performs delete event.
     *
     * @param eventId the event id
     * @param listener the listener
     */
    public void deleteEvent(String eventId, OnCompleteListener<Boolean> listener) {
        userController.isCurrentUserAdmin((isAdmin, success) -> {
            if (!success || !isAdmin) {
                listener.onComplete(false, false);
                return;
            }

            eventRepository.getEventById(eventId, (event, eventLoaded) -> {
                if (!eventLoaded || event == null) {
                    listener.onComplete(false, false);
                    return;
                }

                eventPosterController.deletePosterFile(event.getPosterUrl(), (posterDeleted, posterSuccess) -> {
                    if (!posterSuccess || !posterDeleted) {
                        listener.onComplete(false, false);
                        return;
                    }

                    eventRepository.deleteEventAsAdmin(eventId, listener);
                });
            });
        });
    }
}
