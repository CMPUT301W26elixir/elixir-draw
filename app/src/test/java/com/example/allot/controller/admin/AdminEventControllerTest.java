package com.example.allot.controller.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.controller.event.EventPosterController;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the admin event controller.
 */
public class AdminEventControllerTest {
    private FakeEventRepository eventRepository;
    private FakePosterController posterController;
    private FakeUserController userController;
    private AdminEventController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        posterController = new FakePosterController();
        userController = new FakeUserController();
        controller = new AdminEventController(eventRepository, posterController, userController);
    }

    /**
     * Performs load all events requires admin and delegates when authorized.
     */
    @Test
    public void loadAllEvents_requiresAdminAndDelegatesWhenAuthorized() {
        userController.isAdmin = false;

        controller.loadAllEvents((events, success) -> {
            assertFalse(success);
            assertTrue(events == null);
        });

        userController.isAdmin = true;
        eventRepository.allEvents = Arrays.asList(new Event(), new Event());

        controller.loadAllEvents((events, success) -> {
            assertTrue(success);
            assertEquals(2, events.size());
        });
    }

    /**
     * Performs load all events returns failure when admin lookup fails.
     */
    @Test
    public void loadAllEvents_returnsFailureWhenAdminLookupFails() {
        userController.adminLookupSuccess = false;
        eventRepository.allEvents = Arrays.asList(new Event(), new Event());

        controller.loadAllEvents((events, success) -> {
            assertFalse(success);
            assertTrue(events == null);
        });
    }

    /**
     * Performs load all events propagates repository failure.
     */
    @Test
    public void loadAllEvents_propagatesRepositoryFailure() {
        userController.isAdmin = true;
        eventRepository.getAllEventsSuccess = false;

        controller.loadAllEvents((events, success) -> {
            assertFalse(success);
            assertTrue(events == null);
        });
    }

    /**
     * Performs delete event fails without admin or when poster delete fails.
     */
    @Test
    public void deleteEvent_failsWithoutAdminOrWhenPosterDeleteFails() {
        userController.isAdmin = false;

        controller.deleteEvent("event-1", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
        });

        userController.isAdmin = true;
        Event event = new Event();
        event.setPosterUrl("https://example.com/poster.png");
        eventRepository.event = event;
        posterController.deleteResult = false;
        posterController.deleteSuccess = false;

        controller.deleteEvent("event-1", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
        });
    }

    /**
     * Performs delete event fails when admin lookup fails.
     */
    @Test
    public void deleteEvent_failsWhenAdminLookupFails() {
        userController.adminLookupSuccess = false;

        controller.deleteEvent("event-1", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
        });
    }

    /**
     * Performs delete event fails when event lookup fails.
     */
    @Test
    public void deleteEvent_failsWhenEventLookupFails() {
        userController.isAdmin = true;
        eventRepository.getEventSuccess = false;

        controller.deleteEvent("event-1", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
        });
    }

    /**
     * Performs delete event propagates repository delete failure after poster delete.
     */
    @Test
    public void deleteEvent_propagatesRepositoryDeleteFailureAfterPosterDelete() {
        userController.isAdmin = true;
        Event event = new Event();
        event.setPosterUrl("https://example.com/poster.png");
        eventRepository.event = event;
        posterController.deleteResult = true;
        posterController.deleteSuccess = true;
        eventRepository.deleteEventResult = false;
        eventRepository.deleteEventSuccess = false;

        controller.deleteEvent("event-1", (result, success) -> {
            assertFalse(success);
            assertFalse(result);
            assertEquals("https://example.com/poster.png", posterController.lastPosterUrl);
            assertEquals("event-1", eventRepository.deletedEventId);
        });
    }

    /**
     * Performs delete event uses current poster behavior for blank poster url.
     */
    @Test
    public void deleteEvent_usesCurrentPosterBehaviorForBlankPosterUrl() {
        userController.isAdmin = true;
        Event event = new Event();
        event.setPosterUrl("");
        eventRepository.event = event;
        posterController.deleteResult = true;
        posterController.deleteSuccess = true;
        eventRepository.deleteEventResult = true;
        eventRepository.deleteEventSuccess = true;

        controller.deleteEvent("event-1", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertEquals("", posterController.lastPosterUrl);
            assertEquals("event-1", eventRepository.deletedEventId);
        });
    }

    /**
     * Performs delete event uses current poster behavior for null poster url.
     */
    @Test
    public void deleteEvent_usesCurrentPosterBehaviorForNullPosterUrl() {
        userController.isAdmin = true;
        Event event = new Event();
        eventRepository.event = event;
        posterController.deleteResult = true;
        posterController.deleteSuccess = true;
        eventRepository.deleteEventResult = true;
        eventRepository.deleteEventSuccess = true;

        controller.deleteEvent("event-1", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertTrue(posterController.lastPosterUrl == null);
            assertEquals("event-1", eventRepository.deletedEventId);
        });
    }

    /**
     * Performs delete event deletes poster then event when authorized.
     */
    @Test
    public void deleteEvent_deletesPosterThenEventWhenAuthorized() {
        userController.isAdmin = true;
        Event event = new Event();
        event.setPosterUrl("https://example.com/poster.png");
        eventRepository.event = event;
        posterController.deleteResult = true;
        posterController.deleteSuccess = true;
        eventRepository.deleteEventResult = true;
        eventRepository.deleteEventSuccess = true;

        controller.deleteEvent("event-1", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertEquals("https://example.com/poster.png", posterController.lastPosterUrl);
            assertEquals("event-1", eventRepository.deletedEventId);
        });
    }

    /**
     * Stores and retrieves fake event.
     */
    private static class FakeEventRepository extends EventRepository {
        private List<Event> allEvents;
        private Event event;
        private String deletedEventId;
        private boolean deleteEventResult;
        private boolean getAllEventsSuccess = true;
        private boolean getEventSuccess = true;
        private boolean deleteEventSuccess;

        /**
         * Creates a new FakeEventRepository instance.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Performs get all events.
         *
         * @param listener the listener
         */
        @Override
        public void getAllEvents(com.example.allot.common.OnCompleteListener<List<Event>> listener) {
            listener.onComplete(allEvents, getAllEventsSuccess);
        }

        /**
         * Performs get event by id.
         *
         * @param eventId the event id
         * @param listener the listener
         */
        @Override
        public void getEventById(String eventId, com.example.allot.common.OnCompleteListener<Event> listener) {
            listener.onComplete(event, getEventSuccess && event != null);
        }

        /**
         * Performs delete event as admin.
         *
         * @param eventId the event id
         * @param listener the listener
         */
        @Override
        public void deleteEventAsAdmin(String eventId, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            deletedEventId = eventId;
            listener.onComplete(deleteEventResult, deleteEventSuccess);
        }
    }

    /**
     * Coordinates fake poster.
     */
    private static class FakePosterController extends EventPosterController {
        private String lastPosterUrl;
        private Boolean deleteResult;
        private boolean deleteSuccess;

        /**
         * Creates a new FakePosterController instance.
         */
        private FakePosterController() {
            super(null, null);
        }

        /**
         * Performs delete poster file.
         *
         * @param posterUrl the poster url
         * @param listener the listener
         */
        @Override
        public void deletePosterFile(String posterUrl, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            lastPosterUrl = posterUrl;
            listener.onComplete(deleteResult, deleteSuccess);
        }
    }

    /**
     * Coordinates fake user.
     */
    private static class FakeUserController extends UserController {
        private boolean isAdmin;
        private boolean adminLookupSuccess = true;

        /**
         * Creates a new FakeUserController instance.
         */
        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        /**
         * Performs is current user admin.
         *
         * @param listener the listener
         */
        @Override
        public void isCurrentUserAdmin(com.example.allot.common.OnCompleteListener<Boolean> listener) {
            listener.onComplete(isAdmin, adminLookupSuccess);
        }
    }

    /**
     * Represents the fake device session store.
     */
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
