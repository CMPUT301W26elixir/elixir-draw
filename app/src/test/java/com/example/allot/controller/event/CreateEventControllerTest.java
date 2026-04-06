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

/**
 * Tests the create event controller.
 */
public class CreateEventControllerTest {
    private FakeEventRepository eventRepository;
    private FakeGeocodingService geocodingService;
    private CreateEventController controller;

    /**
     * Updates the up.
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
     * Performs submit event saves coordinates when geocoding succeeds.
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
     * Performs submit event leaves coordinates null when geocoding fails.
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
     * Returns the result of build form data.
     *
     * @return the result of this call
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

    /**
     * Stores and retrieves fake event.
     */
    private static class FakeEventRepository extends EventRepository {
        private Event savedEvent;
        private boolean createSuccess;

        /**
         * Creates a new FakeEventRepository instance.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Performs create new event for user.
         *
         * @param event the event
         * @param organizerId the organizer id
         * @param listener the listener
         */
        @Override
        public void createNewEventForUser(Event event, String organizerId, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            savedEvent = event;
            listener.onComplete(createSuccess, createSuccess);
        }
    }

    /**
     * Provides fake geocoding operations.
     */
    private static class FakeGeocodingService implements EventLocationGeocodingService {
        private EventLocationCoordinates coordinates;

        /**
         * Returns the result of geocode.
         *
         * @param location the location
         * @return the result of this call
         */
        @Override
        public EventLocationCoordinates geocode(String location) {
            return coordinates;
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
