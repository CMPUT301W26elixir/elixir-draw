package com.example.allot.controller.lottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.notification.NotificationController;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.data.NotificationRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
import com.example.allot.model.lottery.RunLotteryData;
import java.util.Date;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class LotteryControllerTest {
    private FakeEventRepository eventRepository;
    private FakeUserController userController;
    private LotteryController controller;
    private FakeNotificationController notificationController;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        userController = new FakeUserController();
        notificationController = new FakeNotificationController();
        controller = new LotteryController(
                eventRepository,
                userController,
                new LotteryDrawService(),
                new LotteryInputValidator(),
                notificationController
        );
    }

    /**
     * Performs load lottery state redirects when draw already exists.
     */
    @Test
    public void loadLotteryState_redirectsWhenDrawAlreadyExists() {
        Event event = buildEvent();
        event.getChosen().add("user-1");
        eventRepository.event = event;

        controller.loadLotteryState("event-1", (RunLotteryData state, boolean success) -> {
            assertTrue(success);
            assertTrue(state.shouldRedirectToEntrants());
        });
    }

    /**
     * Performs start lottery draw returns validation date message for invalid date.
     */
    @Test
    public void startLotteryDraw_returnsValidationDateMessageForInvalidDate() {
        controller.startLotteryDraw("event-1", buildEvent(), "bad-date", "5", (AppResult<Event> result, boolean success) -> {
            assertTrue(!success);
            assertEquals(Integer.valueOf(R.string.manage_lottery_validation_date), result.getMessageResId());
        });
    }

    /**
     * Returns the result of build event.
     *
     * @return the result of this call
     */
    private Event buildEvent() {
        Event event = new Event();
        event.setEventId("event-1");
        event.setTitle("Event");
        event.setRegistrationDeadline(new Date(System.currentTimeMillis() + 10_000L));
        event.setWaitingList(new WaitingList());
        event.getWaitingList().list.add("user-1");
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

        private Event event;

        /**
         * Performs get event by id.
         *
         * @param eventId the event id
         * @param listener the listener
         */
        @Override
        public void getEventById(String eventId, OnCompleteListener<Event> listener) {
            listener.onComplete(event, event != null);
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
         * Performs get user by device id.
         *
         * @param deviceId the device id
         * @param listener the listener
         */
        @Override
        public void getUserByDeviceId(String deviceId, OnCompleteListener<com.example.allot.model.profile.User> listener) {
            com.example.allot.model.profile.User user = new com.example.allot.model.profile.User();
            user.setFirstName("Entrant");
            listener.onComplete(user, true);
        }
    }

    /**
     * Documents fake Notification Controller.
     */
    private static class FakeNotificationController extends NotificationController {
        /**
         * Creates a new FakeNotificationController instance.
         */
        FakeNotificationController() {
            super((NotificationRepository) null);
        }

        /**
         * Performs notify selected entrants.
         *
         * @param entrantIds the entrant ids
         * @param eventId the event id
         * @param eventName the event name
         * @param listener the listener
         */
        @Override
        public void notifySelectedEntrants(List<String> entrantIds, String eventId, String eventName, OnCompleteListener<Boolean> listener) {
            listener.onComplete(true, true);
        }

        /**
         * Performs notify not selected entrants.
         *
         * @param entrantIds the entrant ids
         * @param eventId the event id
         * @param eventName the event name
         * @param listener the listener
         */
        @Override
        public void notifyNotSelectedEntrants(List<String> entrantIds, String eventId, String eventName, OnCompleteListener<Boolean> listener) {
            listener.onComplete(true, true);
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
