package com.example.allot.controller.organizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.controller.event.EventOfferService;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.data.NotificationRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
import com.example.allot.model.organizer.EntrantExportRow;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
public class EventEntrantsControllerTest {
    private FakeEventRepository eventRepository;
    private FakeUserController userController;
    private EventEntrantsController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        userController = new FakeUserController();
        controller = new EventEntrantsController(eventRepository, userController, new NotificationRepository(null), new EventOfferService());
    }

    /**
     * Performs load entrant items returns selected entrants for selected tab.
     */
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

    /**
     * Performs load enrolled export rows returns failure when event is null.
     */
    @Test
    public void loadEnrolledExportRows_returnsFailureWhenEventIsNull() {
        controller.loadEnrolledExportRows(null, (rows, success) -> {
            assertFalse(success);
            assertTrue(rows.isEmpty());
        });
    }

    /**
     * Performs load enrolled export rows returns empty success when no enrolled entrants exist.
     */
    @Test
    public void loadEnrolledExportRows_returnsEmptySuccessWhenNoEnrolledEntrantsExist() {
        Event event = buildEvent();

        controller.loadEnrolledExportRows(event, (rows, success) -> {
            assertTrue(success);
            assertTrue(rows.isEmpty());
        });
    }

    /**
     * Performs load enrolled export rows exports explicit enrolled entrants in order.
     */
    @Test
    public void loadEnrolledExportRows_exportsExplicitEnrolledEntrantsInOrder() {
        Event event = buildEvent();
        event.getEnrolled().add("user-2");
        event.getEnrolled().add("user-1");
        userController.addUser("user-1", buildUser("Alice", "Example", "alice@example.com", "111"));
        userController.addUser("user-2", buildUser("Bob", "Example", "bob@example.com", "222"));

        controller.loadEnrolledExportRows(event, (rows, success) -> {
            assertTrue(success);
            assertEquals(2, rows.size());
            assertEquals("Bob Example", rows.get(0).getName());
            assertEquals("bob@example.com", rows.get(0).getEmail());
            assertEquals("222", rows.get(0).getPhone());
            assertEquals("Alice Example", rows.get(1).getName());
            assertEquals("alice@example.com", rows.get(1).getEmail());
            assertEquals("111", rows.get(1).getPhone());
        });
    }

    /**
     * Performs load enrolled export rows falls back to waiting list chosen status when enrolled list is empty.
     */
    @Test
    public void loadEnrolledExportRows_fallsBackToWaitingListChosenStatusWhenEnrolledListIsEmpty() {
        Event event = buildEvent();
        event.getWaitingList().chosen.add("user-1");
        event.getWaitingList().chosen.add("user-2");
        event.getWaitingList().status.put("user-1", true);
        event.getWaitingList().status.put("user-2", false);
        userController.addUser("user-1", buildUser("Alice", "Example", "alice@example.com", "111"));

        controller.loadEnrolledExportRows(event, (rows, success) -> {
            assertTrue(success);
            assertEquals(1, rows.size());
            assertEquals("Alice Example", rows.get(0).getName());
            assertEquals("alice@example.com", rows.get(0).getEmail());
            assertEquals("111", rows.get(0).getPhone());
        });
    }

    /**
     * Performs load enrolled export rows uses fallback row when user lookup fails.
     */
    @Test
    public void loadEnrolledExportRows_usesFallbackRowWhenUserLookupFails() {
        Event event = buildEvent();
        event.getEnrolled().add("missing-user");

        controller.loadEnrolledExportRows(event, (rows, success) -> {
            assertTrue(success);
            assertEquals(1, rows.size());
            EntrantExportRow row = rows.get(0);
            assertEquals("missing-user", row.getName());
            assertEquals("", row.getEmail());
            assertEquals("", row.getPhone());
        });
    }

    /**
     * Performs load enrolled export rows keeps blank contact fields blank.
     */
    @Test
    public void loadEnrolledExportRows_keepsBlankContactFieldsBlank() {
        Event event = buildEvent();
        event.getEnrolled().add("user-1");
        userController.addUser("user-1", buildUser("Alice", "Example", "", null));

        controller.loadEnrolledExportRows(event, (rows, success) -> {
            assertTrue(success);
            assertEquals(1, rows.size());
            assertEquals("Alice Example", rows.get(0).getName());
            assertEquals("", rows.get(0).getEmail());
            assertEquals("", rows.get(0).getPhone());
        });
    }

    /**
     * Returns the result of build event.
     *
     * @return the result of this call
     */
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

    /**
     * Returns the result of build user.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email
     * @param phone the phone
     * @return the result of this call
     */
    private com.example.allot.model.profile.User buildUser(String firstName,
                                                           String lastName,
                                                           String email,
                                                           String phone) {
        com.example.allot.model.profile.User user = new com.example.allot.model.profile.User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        try {
            java.lang.reflect.Field emailField = com.example.allot.model.profile.User.class.getDeclaredField("email");
            emailField.setAccessible(true);
            emailField.set(user, email);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
        return user;
    }

    private static class FakeEventRepository extends EventRepository {
        /**
         * Creates a new FakeEventRepository instance.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        private Event event;

        /**
         * Performs get event by id.
         *
         * @param eventId the event id
         * @param listener the listener
         */
        @Override
        public void getEventById(String eventId, com.example.allot.common.OnCompleteListener<Event> listener) {
            listener.onComplete(event, event != null);
        }
    }

    private static class FakeUserController extends UserController {
        private final java.util.Map<String, com.example.allot.model.profile.User> users = new java.util.HashMap<>();

        /**
         * Creates a new FakeUserController instance.
         */
        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        /**
         * Performs add user.
         *
         * @param deviceId the device id
         * @param user the user
         */
        private void addUser(String deviceId, com.example.allot.model.profile.User user) {
            users.put(deviceId, user);
        }

        /**
         * Performs get user by device id.
         *
         * @param deviceId the device id
         * @param listener the listener
         */
        @Override
        public void getUserByDeviceId(String deviceId, com.example.allot.common.OnCompleteListener<com.example.allot.model.profile.User> listener) {
            com.example.allot.model.profile.User user = users.get(deviceId);
            listener.onComplete(user, user != null);
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
