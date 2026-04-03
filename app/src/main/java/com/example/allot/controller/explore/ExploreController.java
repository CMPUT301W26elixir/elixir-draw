package com.example.allot.controller.explore;

import android.content.Context;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.EventRepository;
import com.example.allot.model.BrowseFilter;
import com.example.allot.model.event.Event;
import com.example.allot.model.profile.User;
import com.example.allot.view.shared.EventListItem;
import com.example.allot.view.shared.EventListItemMapper;
import java.util.ArrayList;
import java.util.List;
/**
 * Loads explore screen data and handles save and unsave actions.
 */
public class ExploreController {
    private final EventRepository eventRepository;
    private final UserController userController;
    private final ExploreFilterService exploreFilterService;
    private final EventListItemMapper eventListItemMapper;

    public ExploreController(Context context) {
        this(new EventRepository(), new UserController(context), new ExploreFilterService(), new EventListItemMapper());
    }

    ExploreController(EventRepository eventRepository,
                      UserController userController,
                      ExploreFilterService exploreFilterService,
                      EventListItemMapper eventListItemMapper) {
        this.eventRepository = eventRepository;
        this.userController = userController;
        this.exploreFilterService = exploreFilterService;
        this.eventListItemMapper = eventListItemMapper;
    }

    /**
     * Loads the current user's saved event IDs.
     *
     * @param listener the listener that receives the saved event IDs
     */
    public void loadSavedEventIds(OnCompleteListener<List<String>> listener) {
        userController.loadOrCreateUser((User user, boolean success) -> {
            if (!success || user == null) {
                listener.onComplete(new ArrayList<>(), false);
                return;
            }

            List<String> savedEvents = user.getSavedEvents() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(user.getSavedEvents());
            listener.onComplete(savedEvents, true);
        });
    }

    /**
     * Loads the current browse event items.
     *
     * @param searchTerm the current search term
     * @param selectedChipFilter the currently selected chip filter
     * @param savedEventIds the events currently saved by the user
     * @param listener the listener that receives the mapped event items
     */
    public void loadBrowseEvents(String searchTerm,
                                 String selectedChipFilter,
                                 String keywords,
                                 java.util.Date startDate,
                                 Double latitude,
                                 Double longitude,
                                 Double distanceKm,
                                 List<String> savedEventIds,
                                 OnCompleteListener<List<EventListItem>> listener) {
        eventRepository.getOpenEvents((events, success) -> {
            if (!success || events == null) {
                listener.onComplete(new ArrayList<>(), false);
                return;
            }

            BrowseFilter browseFilter = new BrowseFilter(
                    searchTerm,
                    selectedChipFilter,
                    keywords,
                    startDate,
                    latitude,
                    longitude,
                    distanceKm
            );
            List<Event> filteredEvents = exploreFilterService.buildBrowsableEventList(
                    events,
                    browseFilter
            );
            listener.onComplete(eventListItemMapper.mapEvents(filteredEvents, savedEventIds), true);
        });
    }

    /**
     * Saves or unsaves an event and returns the next saved-event state.
     *
     * @param currentSavedEventIds the current saved event IDs
     * @param eventId the event being toggled
     * @param isSaving true to save, false to unsave
     * @param listener the listener that receives the next saved event IDs
     */
    public void toggleSavedEvent(List<String> currentSavedEventIds,
                                 String eventId,
                                 boolean isSaving,
                                 OnCompleteListener<List<String>> listener) {
        List<String> nextSavedEventIds = new ArrayList<>(currentSavedEventIds == null
                ? new ArrayList<>()
                : currentSavedEventIds);

        if (isSaving && !nextSavedEventIds.contains(eventId)) {
            nextSavedEventIds.add(eventId);
        } else if (!isSaving) {
            nextSavedEventIds.remove(eventId);
        }

        userController.toggleSavedEvent(eventId, isSaving, (result, success) -> {
            if (success && result != null && result) {
                listener.onComplete(nextSavedEventIds, true);
                return;
            }

            listener.onComplete(new ArrayList<>(currentSavedEventIds == null ? new ArrayList<>() : currentSavedEventIds), false);
        });
    }
}









