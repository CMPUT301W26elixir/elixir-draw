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
     * Creates an AdminEventController with default dependencies.
     *
     * @param context the context used to access shared preferences
     */
    public AdminEventController(Context context) {
        this(new EventRepository(), new EventPosterController(), new UserController(context));
    }

    /**
     * Creates an AdminEventController with provided dependencies.
     *
     * @param eventRepository the event repository
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
     * Loads all events for admin browsing.
     * Only admin users can access this operation.
     *
     * @param listener the listener that receives the events list and success result
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
     * Deletes an event with admin privileges.
     * Removes the event document and all references from user documents.
     * Only admin users can access this operation.
     *
     * @param eventId the event ID to delete
     * @param listener the listener that receives the deletion success result
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
