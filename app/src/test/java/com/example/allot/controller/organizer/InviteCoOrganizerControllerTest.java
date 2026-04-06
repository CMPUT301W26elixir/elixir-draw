package com.example.allot.controller.organizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.profile.User;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class InviteCoOrganizerControllerTest {
    private FakeEventRepository eventRepository;
    private FakeUserController userController;
    private InviteCoOrganizerController controller;

    /**
     * Updates up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        userController = new FakeUserController();
        controller = new InviteCoOrganizerController(eventRepository, userController);
    }

    /**
     * Handles methods_delegate To Dependencies.
     */
    @Test
    public void methods_delegateToDependencies() {
        Event event = new Event();
        event.setEventId("event-1");
        eventRepository.event = event;
        List<User> users = Arrays.asList(new User(), new User());
        userController.searchResults = users;

        controller.loadEvent("event-1", (result, success) -> {
            assertTrue(success);
            assertEquals(event, result);
        });

        controller.searchUsers("alex", (result, success) -> {
            assertTrue(success);
            assertEquals(users, result);
        });

        controller.inviteCoOrganizer("event-1", "device-2", (result, success) -> {
            assertTrue(success);
            assertTrue(result);
            assertEquals("event-1", eventRepository.inviteEventId);
            assertEquals("device-2", eventRepository.inviteDeviceId);
        });
    }

    /**
     * Returns whether i.s Organizer Or Co Organizer_checks Organizer And Co Organizer Membership
     */
    @Test
    public void isOrganizerOrCoOrganizer_checksOrganizerAndCoOrganizerMembership() {
        Event organizerEvent = new Event();
        organizerEvent.setOrganizerId("device-1");

        Event coOrganizerEvent = new Event();
        coOrganizerEvent.setOrganizerId("device-9");
        coOrganizerEvent.setCoOrganizers(new java.util.ArrayList<>(Collections.singletonList("device-1")));

        Event unrelatedEvent = new Event();
        unrelatedEvent.setOrganizerId("device-9");
        unrelatedEvent.setCoOrganizers(new java.util.ArrayList<>(Collections.singletonList("device-8")));

        assertTrue(controller.isOrganizerOrCoOrganizer(organizerEvent));
        assertTrue(controller.isOrganizerOrCoOrganizer(coOrganizerEvent));
        assertFalse(controller.isOrganizerOrCoOrganizer(unrelatedEvent));
        assertFalse(controller.isOrganizerOrCoOrganizer(null));
    }

    private static class FakeEventRepository extends EventRepository {
        private Event event;
        private String inviteEventId;
        private String inviteDeviceId;

        /**
         * Handles fake Event Repository.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Returns whether g.et Event By Id
         */
        @Override
        public void getEventById(String eventId, com.example.allot.common.OnCompleteListener<Event> listener) {
            listener.onComplete(event, true);
        }

        /**
         * Handles invite Co Organizer.
         */
        @Override
        public void inviteCoOrganizer(String eventId, String deviceId,
                                      com.example.allot.common.OnCompleteListener<Boolean> listener) {
            inviteEventId = eventId;
            inviteDeviceId = deviceId;
            listener.onComplete(true, true);
        }
    }

    private static class FakeUserController extends UserController {
        private List<User> searchResults;

        /**
         * Handles fake User Controller.
         */
        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        /**
         * Handles search Users.
         */
        @Override
        public void searchUsers(String query, com.example.allot.common.OnCompleteListener<List<User>> listener) {
            listener.onComplete(searchResults, true);
        }
    }

    private static class FakeDeviceSessionStore implements DeviceSessionManager.DeviceSessionStore {
        private final String deviceId;

        /**
         * Handles fake Device Session Store.
         */
        private FakeDeviceSessionStore(String deviceId) {
            this.deviceId = deviceId;
        }

        /**
         * Returns whether g.et Device Id
         */
        @Override
        public String getDeviceId() {
            return deviceId;
        }

        /**
         * Saves device id.
         */
        @Override
        public void saveDeviceId(String deviceId) {
        }
    }
}
