package com.example.allot.controller.organizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.R;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
import com.example.allot.model.lottery.LotteryEntrantItem;
import com.example.allot.model.organizer.EntrantsExportResult;
import com.example.allot.model.profile.User;
import java.util.Date;
import java.util.List;
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

    @Test
    public void buildExportData_returnsCsvContentForEnrolledEntrants() {
        Event event = buildEvent();
        event.getEnrolled().add("user-1");

        controller.buildExportData(event, (EntrantsExportResult result, boolean success) -> {
            assertTrue(success);
            assertTrue(result.isSuccess());
            assertTrue(result.getCsvContent().contains("Name"));
        });
    }

    @Test
    public void buildExportData_returnsEmptyFailureWhenNoEnrolledEntrantsExist() {
        Event event = buildEvent();

        controller.buildExportData(event, (EntrantsExportResult result, boolean success) -> {
            assertFalse(success);
            assertFalse(result.isSuccess());
            assertEquals(R.string.manage_entrants_export_empty, result.getMessageResId());
        });
    }

    @Test
    public void buildExportData_fallsBackToEntrantIdWhenUserNameMissing() {
        Event event = buildEvent();
        event.getEnrolled().add("fallback-user");
        userController.user = new User();

        controller.buildExportData(event, (EntrantsExportResult result, boolean success) -> {
            assertTrue(success);
            assertTrue(result.getCsvContent().contains("\"fallback-user\""));
        });
    }

    @Test
    public void buildExportData_escapesCsvValuesAndSanitizesFileName() {
        Event event = buildEvent();
        event.setTitle("Spring Gala 2026!");
        event.getEnrolled().add("user-1");

        User user = new User();
        user.setFirstName("Taylor");
        user.setLastName("\"Quoted\"");
        userController.user = user;

        controller.buildExportData(event, (EntrantsExportResult result, boolean success) -> {
            assertTrue(success);
            assertEquals("Spring_Gala_2026_enrolled_entrants.csv", result.getFileName());
            assertTrue(result.getCsvContent().contains("\"Taylor \"\"Quoted\"\"\""));
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
        private User user;

        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        @Override
        public void getUserByDeviceId(String deviceId, com.example.allot.common.OnCompleteListener<com.example.allot.model.profile.User> listener) {
            if (user == null) {
                user = new User();
                user.setFirstName("Test");
                user.setLastName("User");
            }
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









