package com.example.allot.controller.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.controller.shared.UserController;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventDetailData;
import com.example.allot.model.event.WaitingList;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
public class EventDetailControllerTest {
    private FakeEventRepository eventRepository;
    private FakeUserController userController;
    private EventDetailController controller;

    /**
     * Updates up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        userController = new FakeUserController();
        controller = new EventDetailController(
                eventRepository,
                userController,
                new EventActionStateFactory(),
                new EventDetailViewService()
        );
    }

    /**
     * Loads event action state_returns manage action for organizer.
     */
    @Test
    public void loadEventActionState_returnsManageActionForOrganizer() {
        Event event = buildEvent();
        event.setOrganizerId("device-1");
        eventRepository.event = event;

        controller.loadEventActionState("event-1", (state, success) -> {
            assertTrue(success);
            assertEquals(EventDetailData.NextAction.NAVIGATE_MANAGE, state.getNextAction());
            assertEquals("Taylor Organizer", state.getOrganizerText());
        });
    }

    /**
     * Handles join Waiting List_forwards Location And Returns Success Message When Repository Succeeds.
     */
    @Test
    public void joinWaitingList_forwardsLocationAndReturnsSuccessMessageWhenRepositorySucceeds() {
        eventRepository.joinWaitingListSuccess = true;
        eventRepository.event = buildEvent();
        Date joinedAt = new Date();

        controller.joinWaitingList("event-1", 53.5232, -113.5263, joinedAt, (AppResult<Void> result, boolean success) -> {
            assertTrue(success);
            assertTrue(result.isSuccess());
            assertEquals("event-1", eventRepository.joinEventId);
            assertEquals("device-1", eventRepository.joinDeviceId);
            assertEquals(Double.valueOf(53.5232), eventRepository.joinLatitude);
            assertEquals(Double.valueOf(-113.5263), eventRepository.joinLongitude);
            assertEquals(joinedAt, eventRepository.joinedAt);
            assertEquals(Integer.valueOf(R.string.event_detail_join_success), result.getMessageResId());
        });
    }

    /**
     * Handles leave Waiting List_returns Success Message When Repository Succeeds.
     */
    @Test
    public void leaveWaitingList_returnsSuccessMessageWhenRepositorySucceeds() {
        eventRepository.leaveWaitingListSuccess = true;

        controller.leaveWaitingList("event-1", (AppResult<Void> result, boolean success) -> {
            assertTrue(success);
            assertTrue(result.isSuccess());
            assertEquals(Integer.valueOf(R.string.event_detail_leave_success), result.getMessageResId());
        });
    }

    /**
     * Handles decline Offer_returns Success Message When Repository Succeeds.
     */
    @Test
    public void declineOffer_returnsSuccessMessageWhenRepositorySucceeds() {
        eventRepository.declineOfferSuccess = true;

        controller.declineOffer("event-1", (AppResult<Void> result, boolean success) -> {
            assertTrue(success);
            assertTrue(result.isSuccess());
            assertEquals(Integer.valueOf(R.string.event_offer_decline_success), result.getMessageResId());
        });
    }

    /**
     * Handles decline Offer_returns Failure Message When Repository Fails.
     */
    @Test
    public void declineOffer_returnsFailureMessageWhenRepositoryFails() {
        eventRepository.declineOfferSuccess = false;

        controller.declineOffer("event-1", (AppResult<Void> result, boolean success) -> {
            assertTrue(!success);
            assertTrue(!result.isSuccess());
            assertEquals(Integer.valueOf(R.string.event_offer_action_failure), result.getMessageResId());
        });
    }

    /**
     * Builds event.
     */
    private Event buildEvent() {
        Event event = new Event();
        event.setEventId("event-1");
        event.setTitle("Event");
        event.setLocation("Location");
        event.setDescription("Description");
        event.setCategory("Sports");
        event.setEventDate(new Date(System.currentTimeMillis() + 20_000L));
        event.setRegistrationOpen(new Date(System.currentTimeMillis() - 20_000L));
        event.setRegistrationDeadline(new Date(System.currentTimeMillis() + 10_000L));
        event.setWaitingList(new WaitingList());
        event.setChosen(new java.util.ArrayList<>());
        event.setEnrolled(new java.util.ArrayList<>());
        event.setCancelled(new java.util.ArrayList<>());
        event.setNotEnrolled(new java.util.ArrayList<>());
        return event;
    }

    private static class FakeEventRepository extends EventRepository {
        /**
         * Handles fake Event Repository.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        private Event event;
        private boolean joinWaitingListSuccess;
        private boolean leaveWaitingListSuccess;
        private boolean declineOfferSuccess;
        private String joinEventId;
        private String joinDeviceId;
        private Double joinLatitude;
        private Double joinLongitude;
        private Date joinedAt;

        /**
         * Returns whether g.et Event By Id
         */
        @Override
        public void getEventById(String eventId, com.example.allot.common.OnCompleteListener<Event> listener) {
            listener.onComplete(event, event != null);
        }

        /**
         * Handles join Waiting List.
         */
        @Override
        public void joinWaitingList(String eventId,
                                    String deviceId,
                                    Double latitude,
                                    Double longitude,
                                    Date joinedAt,
                                    com.example.allot.common.OnCompleteListener<Boolean> listener) {
            joinEventId = eventId;
            joinDeviceId = deviceId;
            joinLatitude = latitude;
            joinLongitude = longitude;
            this.joinedAt = joinedAt;
            listener.onComplete(joinWaitingListSuccess, joinWaitingListSuccess);
        }

        /**
         * Handles leave Waiting List.
         */
        @Override
        public void leaveWaitingList(String eventId, String deviceId, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            listener.onComplete(leaveWaitingListSuccess, leaveWaitingListSuccess);
        }

        /**
         * Handles decline Offer.
         */
        @Override
        public void declineOffer(String eventId, String deviceId, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            listener.onComplete(declineOfferSuccess, declineOfferSuccess);
        }
    }

    private static class FakeUserController extends UserController {
        /**
         * Handles fake User Controller.
         */
        private FakeUserController() {
            super(null, new DeviceSessionManager(new FakeDeviceSessionStore("device-1")));
        }

        /**
         * Returns whether g.et Current Device Id
         */
        @Override
        public String getCurrentDeviceId() {
            return "device-1";
        }

        /**
         * Returns whether g.et User By Device Id
         */
        @Override
        public void getUserByDeviceId(String deviceId, com.example.allot.common.OnCompleteListener<com.example.allot.model.profile.User> listener) {
            com.example.allot.model.profile.User user = new com.example.allot.model.profile.User();
            user.setFirstName("Taylor");
            user.setLastName("Organizer");
            listener.onComplete(user, true);
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









