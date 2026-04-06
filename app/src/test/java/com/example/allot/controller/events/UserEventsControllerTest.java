package com.example.allot.controller.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
import java.util.Arrays;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
public class UserEventsControllerTest {
    private FakeEventRepository eventRepository;
    private FakeUserController userController;
    private UserEventsController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        userController = new FakeUserController();
        controller = new UserEventsController(userController, eventRepository, new UserEventsSectionService(), new com.example.allot.view.shared.EventListItemMapper());
    }

    /**
     * Performs load registered groups groups registered sections.
     */
    @Test
    public void loadRegisteredGroups_groupsRegisteredSections() {
        Event selected = buildEvent(System.currentTimeMillis() + 20_000L, System.currentTimeMillis() + 10_000L);
        selected.getEnrolled().add("device-1");
        eventRepository.allEvents = Arrays.asList(selected);

        controller.loadRegisteredGroups((groups, success) -> {
            assertTrue(success);
            assertEquals(1, groups.getSelectedItems().size());
        });
    }

    /**
     * Performs load hosted groups groups hosted sections.
     */
    @Test
    public void loadHostedGroups_groupsHostedSections() {
        Event ongoing = buildEvent(System.currentTimeMillis() + 20_000L, System.currentTimeMillis() + 10_000L);
        eventRepository.hostedEvents = Arrays.asList(ongoing);

        controller.loadHostedGroups((groups, success) -> {
            assertTrue(success);
            assertEquals(1, groups.getOngoingItems().size());
        });
    }

    /**
     * Returns the result of build event.
     *
     * @param eventTime the event time
     * @param deadlineTime the deadline time
     * @return the result of this call
     */
    private Event buildEvent(long eventTime, long deadlineTime) {
        Event event = new Event();
        event.setEventId("event-1");
        event.setTitle("Event");
        event.setLocation("Location");
        event.setCategory("Sports");
        event.setEventDate(new Date(eventTime));
        event.setRegistrationDeadline(new Date(deadlineTime));
        event.setWaitingList(new WaitingList());
        event.setChosen(new java.util.ArrayList<>());
        event.setEnrolled(new java.util.ArrayList<>());
        event.setCancelled(new java.util.ArrayList<>());
        event.setNotEnrolled(new java.util.ArrayList<>());
        return event;
    }

    private static class FakeEventRepository extends EventRepository {
        /**
         * Creates a new FakeEventRepository instance.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        private java.util.List<Event> allEvents;
        private java.util.List<Event> hostedEvents;

        /**
         * Performs get all events.
         *
         * @param listener the listener
         */
        @Override
        public void getAllEvents(com.example.allot.common.OnCompleteListener<java.util.List<Event>> listener) {
            listener.onComplete(allEvents, allEvents != null);
        }

        /**
         * Performs get hosted events.
         *
         * @param organizerId the organizer id
         * @param listener the listener
         */
        @Override
        public void getHostedEvents(String organizerId, com.example.allot.common.OnCompleteListener<java.util.List<Event>> listener) {
            listener.onComplete(hostedEvents, hostedEvents != null);
        }

        /**
         * Performs get managed events.
         *
         * @param organizerId the organizer id
         * @param listener the listener
         */
        @Override
        public void getManagedEvents(String organizerId, com.example.allot.common.OnCompleteListener<java.util.List<Event>> listener) {
            listener.onComplete(hostedEvents, hostedEvents != null);
        }
    }

    private static class FakeUserController extends UserController {
        /**
         * Creates a new FakeUserController instance.
         */
        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        /**
         * Returns the current device id.
         *
         * @return the current device id
         */
        @Override
        public String getCurrentDeviceId() {
            return "device-1";
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
        public String getDeviceId() { return deviceId; }

        /**
         * Performs save device id.
         *
         * @param deviceId the device id
         */
        @Override
        public void saveDeviceId(String deviceId) { }
    }
}









