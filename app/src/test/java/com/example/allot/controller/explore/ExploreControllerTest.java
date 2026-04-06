package com.example.allot.controller.explore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.model.BrowseFilter;
import com.example.allot.model.event.Event;
import com.example.allot.model.profile.User;
import com.example.allot.view.shared.EventListItem;
import com.example.allot.view.shared.EventListItemMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ExploreControllerTest {
    private FakeEventRepository eventRepository;
    private FakeUserController userController;
    private FakeExploreFilterService exploreFilterService;
    private FakeEventListItemMapper mapper;
    private ExploreController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        userController = new FakeUserController();
        exploreFilterService = new FakeExploreFilterService();
        mapper = new FakeEventListItemMapper();
        controller = new ExploreController(eventRepository, userController, exploreFilterService, mapper);
    }

    /**
     * Performs load saved event ids returns user saved events or empty list.
     */
    @Test
    public void loadSavedEventIds_returnsUserSavedEventsOrEmptyList() {
        User user = new User();
        user.setSavedEvents(new ArrayList<>(Arrays.asList("event-1", "event-2")));
        userController.currentUser = user;
        userController.loadCurrentUserSuccess = true;

        controller.loadSavedEventIds((savedIds, success) -> {
            assertTrue(success);
            assertEquals(Arrays.asList("event-1", "event-2"), savedIds);
        });

        userController.currentUser = null;
        userController.loadCurrentUserSuccess = true;

        controller.loadSavedEventIds((savedIds, success) -> {
            assertTrue(success);
            assertTrue(savedIds.isEmpty());
        });
    }

    /**
     * Performs refresh open events caches results.
     */
    @Test
    public void refreshOpenEvents_cachesResults() {
        Event event = buildEvent("event-1");
        eventRepository.openEvents = Collections.singletonList(event);
        eventRepository.openEventsSuccess = true;

        controller.refreshOpenEvents((events, success) -> {
            assertTrue(success);
            assertEquals(1, events.size());
            assertTrue(controller.hasCachedOpenEvents());
        });
    }

    /**
     * Performs filter cached browse events fails when cache missing.
     */
    @Test
    public void filterCachedBrowseEvents_failsWhenCacheMissing() {
        controller.filterCachedBrowseEvents("music", null, null, null, null, null, null,
                null, null, new ArrayList<>(), (items, success) -> {
                    assertFalse(success);
                    assertTrue(items.isEmpty());
                });
    }

    /**
     * Performs load browse events refreshes then filters and maps.
     */
    @Test
    public void loadBrowseEvents_refreshesThenFiltersAndMaps() {
        Event event = buildEvent("event-1");
        eventRepository.openEvents = Collections.singletonList(event);
        eventRepository.openEventsSuccess = true;
        exploreFilterService.filteredEvents = Collections.singletonList(event);
        mapper.itemsToReturn = Collections.singletonList(new EventListItem("event-1", "Title", "Street", "Date", "Price", "Days", "Sports", null));

        controller.loadBrowseEvents("music", "Sports", "live", new Date(1000L), 53.5, -113.5, 25.0,
                true, 50, Collections.singletonList("event-1"), (items, success) -> {
                    assertTrue(success);
                    assertEquals(1, items.size());
                    assertEquals("music", exploreFilterService.lastFilter.getSearchTerm());
                    assertEquals("Sports", exploreFilterService.lastFilter.getSelectedCategory());
                    assertEquals("live", exploreFilterService.lastFilter.getKeywords());
                    assertEquals(Double.valueOf(25.0), exploreFilterService.lastFilter.getDistanceKm());
                    assertEquals(Boolean.TRUE, exploreFilterService.lastFilter.getOnlyOpenSpots());
                    assertEquals(Integer.valueOf(50), exploreFilterService.lastFilter.getMinimumCapacity());
                    assertEquals(Collections.singletonList("event-1"), mapper.lastSavedEventIds);
                });
    }

    /**
     * Performs toggle saved event returns next state on success and original state on failure.
     */
    @Test
    public void toggleSavedEvent_returnsNextStateOnSuccessAndOriginalStateOnFailure() {
        userController.toggleSavedSuccess = true;

        controller.toggleSavedEvent(new ArrayList<>(Collections.singletonList("event-1")), "event-2", true, (savedIds, success) -> {
            assertTrue(success);
            assertEquals(Arrays.asList("event-1", "event-2"), savedIds);
        });

        userController.toggleSavedSuccess = false;

        controller.toggleSavedEvent(new ArrayList<>(Collections.singletonList("event-1")), "event-2", true, (savedIds, success) -> {
            assertFalse(success);
            assertEquals(Collections.singletonList("event-1"), savedIds);
        });
    }

    /**
     * Returns the result of build event.
     *
     * @param eventId the event id
     * @return the result of this call
     */
    private Event buildEvent(String eventId) {
        Event event = new Event();
        event.setEventId(eventId);
        event.setTitle("Title");
        event.setLocation("Street");
        event.setCategory("Sports");
        return event;
    }

    private static class FakeEventRepository extends EventRepository {
        private List<Event> openEvents = new ArrayList<>();
        private boolean openEventsSuccess;

        /**
         * Creates a new FakeEventRepository instance.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Performs get open events.
         *
         * @param listener the listener
         */
        @Override
        public void getOpenEvents(com.example.allot.common.OnCompleteListener<List<Event>> listener) {
            listener.onComplete(openEvents, openEventsSuccess);
        }
    }

    private static class FakeUserController extends UserController {
        private User currentUser;
        private boolean loadCurrentUserSuccess;
        private boolean toggleSavedSuccess;

        /**
         * Creates a new FakeUserController instance.
         */
        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        /**
         * Performs load current user.
         *
         * @param listener the listener
         */
        @Override
        public void loadCurrentUser(com.example.allot.common.OnCompleteListener<User> listener) {
            listener.onComplete(currentUser, loadCurrentUserSuccess);
        }

        /**
         * Performs toggle saved event.
         *
         * @param eventId the event id
         * @param isSaving whether saving
         * @param listener the listener
         */
        @Override
        public void toggleSavedEvent(String eventId, boolean isSaving,
                                     com.example.allot.common.OnCompleteListener<Boolean> listener) {
            listener.onComplete(toggleSavedSuccess, toggleSavedSuccess);
        }
    }

    private static class FakeExploreFilterService extends ExploreFilterService {
        private List<Event> filteredEvents = new ArrayList<>();
        private BrowseFilter lastFilter;

        /**
         * Returns the result of build browsable event list.
         *
         * @param events the events
         * @param filter the filter
         * @return the result of this call
         */
        @Override
        public List<Event> buildBrowsableEventList(List<Event> events, BrowseFilter filter) {
            lastFilter = filter;
            return filteredEvents;
        }
    }

    private static class FakeEventListItemMapper extends EventListItemMapper {
        private List<EventListItem> itemsToReturn = new ArrayList<>();
        private List<String> lastSavedEventIds;

        /**
         * Returns the result of map events.
         *
         * @param events the events
         * @param savedEventIds the saved event ids
         * @return the result of this call
         */
        @Override
        public List<EventListItem> mapEvents(List<Event> events, List<String> savedEventIds) {
            lastSavedEventIds = savedEventIds;
            return itemsToReturn;
        }
    }

    private static class FakeDeviceSessionStore implements DeviceSessionManager.DeviceSessionStore {
        private final String deviceId;

        /**
         * Creates a new FakeDeviceSessionStore instance.
         *
         * @param deviceId the device id
         */
        private FakeDeviceSessionStore(String deviceId) {
            this.deviceId = deviceId;
        }

        /**
         * Returns the device id.
         *
         * @return the device id
         */
        @Override
        public String getDeviceId() {
            return deviceId;
        }

        /**
         * Performs save device id.
         *
         * @param deviceId the device id
         */
        @Override
        public void saveDeviceId(String deviceId) {
        }
    }
}
