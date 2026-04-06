package com.example.allot.controller.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.common.AppResult;
import com.example.allot.controller.lottery.LotteryDrawService;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.model.event.EventFormSnapshot;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
public class EditEventControllerTest {
    private FakeEventRepository eventRepository;
    private FakeGeocodingService geocodingService;
    private EditEventController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        geocodingService = new FakeGeocodingService();
        controller = new EditEventController(
                eventRepository,
                new EventFormService(),
                new EventInputValidator(),
                new LotteryDrawService(),
                geocodingService
        );
    }

    /**
     * Performs is save enabled disables save when snapshot matches.
     */
    @Test
    public void isSaveEnabled_disablesSaveWhenSnapshotMatches() {
        EventFormData formData = buildFormData("Sample Event");
        EventFormSnapshot originalSnapshot = controller.buildSnapshot(formData);

        assertFalse(controller.isSaveEnabled(formData, originalSnapshot, false, false));
    }

    /**
     * Performs is save enabled enables save when form changes.
     */
    @Test
    public void isSaveEnabled_enablesSaveWhenFormChanges() {
        EventFormData originalFormData = buildFormData("Sample Event");
        EventFormData currentFormData = buildFormData("Updated Event");
        EventFormSnapshot originalSnapshot = controller.buildSnapshot(originalFormData);

        assertTrue(controller.isSaveEnabled(currentFormData, originalSnapshot, false, false));
    }

    /**
     * Performs save changes updates coordinates when geocoding succeeds.
     */
    @Test
    public void saveChanges_updatesCoordinatesWhenGeocodingSucceeds() {
        geocodingService.coordinates = new EventLocationCoordinates(53.5232, -113.5263);
        eventRepository.updateSuccess = true;
        eventRepository.event = new Event();

        controller.saveChanges("event-1", buildFormData("Sample Event"), (AppResult<Event> result, boolean success) -> {
            assertTrue(success);
            assertEquals(Double.valueOf(53.5232), eventRepository.lastUpdates.get("eventLatitude"));
            assertEquals(Double.valueOf(-113.5263), eventRepository.lastUpdates.get("eventLongitude"));
        });
    }

    /**
     * Performs save changes clears coordinates when geocoding fails.
     */
    @Test
    public void saveChanges_clearsCoordinatesWhenGeocodingFails() {
        geocodingService.coordinates = null;
        eventRepository.updateSuccess = true;
        eventRepository.event = new Event();

        controller.saveChanges("event-1", buildFormData("Sample Event"), (AppResult<Event> result, boolean success) -> {
            assertTrue(success);
            assertNull(eventRepository.lastUpdates.get("eventLatitude"));
            assertNull(eventRepository.lastUpdates.get("eventLongitude"));
        });
    }

    /**
     * Returns the result of build form data.
     *
     * @param title the title
     * @return the result of this call
     */
    private EventFormData buildFormData(String title) {
        return new EventFormData(
                title,
                "Location",
                false,
                true,
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
        private Event event;
        private boolean updateSuccess;
        private Map<String, Object> lastUpdates = new HashMap<>();

        /**
         * Creates a new FakeEventRepository instance.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Performs update event.
         *
         * @param eventId the event id
         * @param updates the updates
         * @param listener the listener
         */
        @Override
        public void updateEvent(String eventId, Map<String, Object> updates, com.example.allot.common.OnCompleteListener<Boolean> listener) {
            lastUpdates = new HashMap<>(updates);
            listener.onComplete(updateSuccess, updateSuccess);
        }

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
}









