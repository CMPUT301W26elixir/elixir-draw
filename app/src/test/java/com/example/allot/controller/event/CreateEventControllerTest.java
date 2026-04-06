package com.example.allot.controller.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.common.AppResult;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import org.junit.Before;
import org.junit.Test;

public class CreateEventControllerTest {
    private FakeEventRepository eventRepository;
    private FakeGeocodingService geocodingService;
    private CreateEventController controller;

    /**
     * Updates up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        geocodingService = new FakeGeocodingService();
        controller = new CreateEventController(
                eventRepository,
                new EventFormService(),
                new EventInputValidator(),
                new DeviceSessionManager(new FakeDeviceSessionStore("device-1")),
                geocodingService
        );
    }

    /**
     * Handles submit Event_saves Coordinates When Geocoding Succeeds.
     */
    @Test
    public void submitEvent_savesCoordinatesWhenGeocodingSucceeds() {
        geocodingService.coordinates = new EventLocationCoordinates(53.5232, -113.5263);
        eventRepository.createSuccess = true;

        controller.submitEvent(buildFormData(), (AppResult<Event> result, boolean success) -> {
            assertTrue(success);
            assertTrue(result.isSuccess());
            assertEquals(Double.valueOf(53.5232), eventRepository.savedEvent.getEventLatitude());
            assertEquals(Double.valueOf(-113.5263), eventRepository.savedEvent.getEventLongitude());
        });
    }

    /**
     * Handles submit Event_leaves Coordinates Null When Geocoding Fails.
     */
    @Test
    public void submitEvent_leavesCoordinatesNullWhenGeocodingFails() {
        geocodingService.coordinates = null;
        eventRepository.createSuccess = true;

        controller.submitEvent(buildFormData(), (AppResult<Event> result, boolean success) -> {
            assertTrue(success);
            assertTrue(result.isSuccess());
            assertNull(eventRepository.savedEvent.getEventLatitude());
            assertNull(eventRepository.savedEvent.getEventLongitude());
        });
    }

    /**
     * Builds form data.
     */
    private EventFormData buildFormData() {
        return new EventFormData(
                "Sample Event",
                "University of Alberta",
                true,
                false,
                "Jan",
                "5",
                "2027",
                "10",
                "Description",
                "25",
                "Jan",
                "1",
                "2027",
                "Jan",
                "2",
                "2027"
        );
    }

    private static class FakeEventRepository extends EventRepository {
        private Event savedEvent;
        private boolean createSuccess;

        /**
         * Handles fake Event Repository.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Creates new event for user.
         */
        @Override
        public void createNewEventForUser(Event event, String organizerId, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            savedEvent = event;
            listener.onComplete(createSuccess, createSuccess);
        }
    }

    private static class FakeGeocodingService implements EventLocationGeocodingService {
        private EventLocationCoordinates coordinates;

        /**
         * Handles geocode.
         */
        @Override
        public EventLocationCoordinates geocode(String location) {
            return coordinates;
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
