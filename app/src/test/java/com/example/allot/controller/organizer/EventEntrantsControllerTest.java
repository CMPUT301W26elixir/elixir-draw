package com.example.allot.controller.organizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
public class EventEntrantsControllerTest {
    private FakeEventRepository eventRepository;
    private FakeUserController userController;
    private EventEntrantsController controller;

    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        userController = new FakeUserController();
        controller = new EventEntrantsController(eventRepository, userController);
    }

    @Test
    public void loadEntrantItems_returnsSelectedEntrantsForSelectedTab() {
        Event event = buildEvent();
        event.getChosen().add("user-1");
        eventRepository.event = event;

        controller.loadEntrantItems(event, EventEntrantsController.Tab.SELECTED, (items, success) -> {
            assertTrue(success);
            assertEquals(1, items.size());
            assertEquals("user-1", items.get(0).getEntrantId());
        });
    }

    private Event buildEvent() {
        Event event = new Event();
        event.setTitle("Event");
        event.setDrawDate(new Date());
        event.setRegistrationDeadline(new Date());
        event.setWaitingList(new WaitingList());
        event.setChosen(new java.util.ArrayList<>());
        event.setEnrolled(new java.util.ArrayList<>());
        event.setCancelled(new java.util.ArrayList<>());
        event.setNotEnrolled(new java.util.ArrayList<>());
        return event;
    }

    private static class FakeEventRepository extends EventRepository {
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        private Event event;

        @Override
        public void getEventById(String eventId, com.example.allot.common.OnCompleteListener<Event> listener) {
            listener.onComplete(event, event != null);
        }
    }

    private static class FakeUserController extends UserController {
        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        @Override
        public void getUserByDeviceId(String deviceId, com.example.allot.common.OnCompleteListener<com.example.allot.model.profile.User> listener) {
            com.example.allot.model.profile.User user = new com.example.allot.model.profile.User();
            user.setFirstName("Test");
            user.setLastName("User");
            listener.onComplete(user, true);
        }
    }

    private static class FakeDeviceSessionStore implements DeviceSessionManager.DeviceSessionStore {
        private final String deviceId;

        private FakeDeviceSessionStore(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public String getDeviceId() { return deviceId; }

        @Override
        public void saveDeviceId(String deviceId) { }
    }

}









