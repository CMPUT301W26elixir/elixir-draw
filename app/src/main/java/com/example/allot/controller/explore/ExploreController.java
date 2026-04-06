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
    private List<Event> cachedOpenEvents = new ArrayList<>();
    private boolean hasLoadedOpenEvents;

    /**
     * Creates a new ExploreController instance.
     *
     * @param context the context
     */
    public ExploreController(Context context) {
        this(new EventRepository(), new UserController(context), new ExploreFilterService(), new EventListItemMapper());
    }

    /**
     * Creates a new ExploreController instance.
     *
     * @param eventRepository the event repository
     * @param userController the user controller
     * @param exploreFilterService the explore filter service
     * @param eventListItemMapper the event list item mapper
     */
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
     * Performs load saved event ids.
     *
     * @param listener the listener
     */
    public void loadSavedEventIds(OnCompleteListener<List<String>> listener) {
        userController.loadCurrentUser((User user, boolean success) -> {
            if (!success) {
                listener.onComplete(new ArrayList<>(), false);
                return;
            }

            List<String> savedEvents = user == null || user.getSavedEvents() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(user.getSavedEvents());
            listener.onComplete(savedEvents, true);
        });
    }

    /**
     * Performs refresh open events.
     *
     * @param listener the listener
     */
    public void refreshOpenEvents(OnCompleteListener<List<Event>> listener) {
        eventRepository.getOpenEvents((events, success) -> {
            if (!success || events == null) {
                listener.onComplete(new ArrayList<>(), false);
                return;
            }

            cachedOpenEvents = new ArrayList<>(events);
            hasLoadedOpenEvents = true;
            listener.onComplete(new ArrayList<>(cachedOpenEvents), true);
        });
    }

    /**
     * Performs load browse events.
     *
     * @param searchTerm the search term
     * @param selectedChipFilter the selected chip filter
     * @param keywords the keywords
     * @param startDate the start date
     * @param latitude the latitude
     * @param longitude the longitude
     * @param distanceKm the distance km
     * @param onlyOpenSpots the only open spots
     * @param minimumCapacity the minimum capacity
     * @param savedEventIds the saved event ids
     * @param listener the listener
     */
    public void loadBrowseEvents(String searchTerm,
                                 String selectedChipFilter,
                                 String keywords,
                                 java.util.Date startDate,
                                 Double latitude,
                                 Double longitude,
                                 Double distanceKm,
                                 Boolean onlyOpenSpots,
                                 Integer minimumCapacity,
                                 List<String> savedEventIds,
                                 OnCompleteListener<List<EventListItem>> listener) {
        if (!hasLoadedOpenEvents) {
            refreshOpenEvents((events, success) -> {
                if (success) {
                    filterCachedBrowseEvents(searchTerm, selectedChipFilter, keywords, startDate, latitude,
                            longitude, distanceKm, onlyOpenSpots, minimumCapacity, savedEventIds, listener);
                } else {
                    listener.onComplete(new ArrayList<>(), false);
                }
            });
        } else {
            filterCachedBrowseEvents(searchTerm, selectedChipFilter, keywords, startDate, latitude,
                    longitude, distanceKm, onlyOpenSpots, minimumCapacity, savedEventIds, listener);
        }
    }

    /**
     * Performs filter cached browse events.
     *
     * @param searchTerm the search term
     * @param selectedChipFilter the selected chip filter
     * @param keywords the keywords
     * @param startDate the start date
     * @param latitude the latitude
     * @param longitude the longitude
     * @param distanceKm the distance km
     * @param onlyOpenSpots the only open spots
     * @param minimumCapacity the minimum capacity
     * @param savedEventIds the saved event ids
     * @param listener the listener
     */
    public void filterCachedBrowseEvents(String searchTerm,
                                         String selectedChipFilter,
                                         String keywords,
                                         java.util.Date startDate,
                                         Double latitude,
                                         Double longitude,
                                         Double distanceKm,
                                         Boolean onlyOpenSpots,
                                         Integer minimumCapacity,
                                         List<String> savedEventIds,
                                         OnCompleteListener<List<EventListItem>> listener) {
        if (!hasLoadedOpenEvents) {
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
                distanceKm,
                onlyOpenSpots,
                minimumCapacity
        );
        List<Event> filteredEvents = exploreFilterService.buildBrowsableEventList(
                cachedOpenEvents,
                browseFilter
        );
        listener.onComplete(eventListItemMapper.mapEvents(filteredEvents, savedEventIds), true);
    }

    /**
     * Returns whether this instance has cached open events.
     *
     * @return whether this instance has cached open events
     */
    public boolean hasCachedOpenEvents() {
        return hasLoadedOpenEvents;
    }

    /**
     * Performs toggle saved event.
     *
     * @param currentSavedEventIds the current saved event ids
     * @param eventId the event id
     * @param isSaving whether saving
     * @param listener the listener
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
