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

public class AdminEventControllerTest {
    private FakeEventRepository eventRepository;
    private FakePosterController posterController;
    private FakeUserController userController;
    private AdminEventController controller;

    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        posterController = new FakePosterController();
        userController = new FakeUserController();
        controller = new AdminEventController(eventRepository, posterController, userController);
    }

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

    @Test
    public void deleteEvent_deletesPosterThenEventWhenAuthorized() {
        userController.isAdmin = true;
        Event event = new Event();
        event.setPosterUrl("https://example.com/poster.png");
        eventRepository.event = event;
        posterController.deleteResult = true;
        posterController.deleteSuccess = true;
        eventRepository.deleteEventResult = true;

        controller.deleteEvent("event-1", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertEquals("https://example.com/poster.png", posterController.lastPosterUrl);
            assertEquals("event-1", eventRepository.deletedEventId);
        });
    }

    private static class FakeEventRepository extends EventRepository {
        private List<Event> allEvents;
        private Event event;
        private String deletedEventId;
        private boolean deleteEventResult;

        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        @Override
        public void getAllEvents(com.example.allot.common.OnCompleteListener<List<Event>> listener) {
            listener.onComplete(allEvents, true);
        }

        @Override
        public void getEventById(String eventId, com.example.allot.common.OnCompleteListener<Event> listener) {
            listener.onComplete(event, event != null);
        }

        @Override
        public void deleteEventAsAdmin(String eventId, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            deletedEventId = eventId;
            listener.onComplete(deleteEventResult, deleteEventResult);
        }
    }

    private static class FakePosterController extends EventPosterController {
        private String lastPosterUrl;
        private Boolean deleteResult;
        private boolean deleteSuccess;

        private FakePosterController() {
            super(null, null);
        }

        @Override
        public void deletePosterFile(String posterUrl, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            lastPosterUrl = posterUrl;
            listener.onComplete(deleteResult, deleteSuccess);
        }
    }

    private static class FakeUserController extends UserController {
        private boolean isAdmin;

        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        @Override
        public void isCurrentUserAdmin(com.example.allot.common.OnCompleteListener<Boolean> listener) {
            listener.onComplete(isAdmin, true);
        }
    }

    private static class FakeDeviceSessionStore implements DeviceSessionManager.DeviceSessionStore {
        private final String deviceId;

        private FakeDeviceSessionStore(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public String getDeviceId() {
            return deviceId;
        }

        @Override
        public void saveDeviceId(String deviceId) {
        }
    }
}
