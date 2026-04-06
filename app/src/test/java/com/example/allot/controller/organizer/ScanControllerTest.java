package com.example.allot.controller.organizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.allot.R;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventScanResult;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the scan controller.
 */
public class ScanControllerTest {
    private FakeEventRepository eventRepository;
    private ScanController controller;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        eventRepository = new FakeEventRepository();
        controller = new ScanController(eventRepository);
    }

    /**
     * Performs validate payload accepts trimmed event id.
     */
    @Test
    public void validatePayload_acceptsTrimmedEventId() {
        EventScanResult result = controller.validatePayload("  event-123  ");

        assertEquals(EventScanResult.Status.OPEN_EVENT, result.getStatus());
        assertEquals("event-123", result.getEventId());
    }

    /**
     * Performs validate payload rejects blank payload.
     */
    @Test
    public void validatePayload_rejectsBlankPayload() {
        EventScanResult result = controller.validatePayload("   ");

        assertEquals(EventScanResult.Status.INVALID_PAYLOAD, result.getStatus());
        assertEquals(Integer.valueOf(R.string.scan_error_invalid_qr), result.getMessageResId());
    }

    /**
     * Performs load scanned event returns open event when repository finds match.
     */
    @Test
    public void loadScannedEvent_returnsOpenEventWhenRepositoryFindsMatch() {
        Event event = new Event();
        event.setEventId("event-1");
        event.setTitle("Found Event");
        eventRepository.event = event;

        controller.loadScannedEvent("event-1", (result, success) -> {
            assertTrue(success);
            assertEquals(EventScanResult.Status.OPEN_EVENT, result.getStatus());
            assertEquals("event-1", result.getEvent().getEventId());
        });
    }

    /**
     * Performs load scanned event returns not found when event missing.
     */
    @Test
    public void loadScannedEvent_returnsNotFoundWhenEventMissing() {
        eventRepository.event = null;
        eventRepository.success = true;

        controller.loadScannedEvent("event-404", (result, success) -> {
            assertTrue(success);
            assertEquals(EventScanResult.Status.EVENT_NOT_FOUND, result.getStatus());
            assertEquals(Integer.valueOf(R.string.scan_error_event_not_found), result.getMessageResId());
        });
    }

    /**
     * Performs load scanned event returns load error when repository fails.
     */
    @Test
    public void loadScannedEvent_returnsLoadErrorWhenRepositoryFails() {
        eventRepository.success = false;

        controller.loadScannedEvent("event-500", (result, success) -> {
            assertTrue(!success);
            assertEquals(EventScanResult.Status.LOAD_ERROR, result.getStatus());
            assertEquals(Integer.valueOf(R.string.scan_error_load), result.getMessageResId());
        });
    }

    /**
     * Stores and retrieves fake event.
     */
    private static class FakeEventRepository extends EventRepository {
        private Event event;
        private boolean success = true;

        /**
         * Creates a new FakeEventRepository instance.
         */
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }

        /**
         * Performs get event by id.
         *
         * @param eventId the event id
         * @param listener the listener
         */
        @Override
        public void getEventById(String eventId, com.example.allot.common.OnCompleteListener<Event> listener) {
            listener.onComplete(event, success);
        }
    }
}
