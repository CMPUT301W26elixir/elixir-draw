package com.example.allot.controller.organizer;

import com.example.allot.R;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventScanResult;

/**
 * Validates scanned payloads and resolves them to events.
 */
public class ScanController {
    private final EventRepository eventRepository;

    /**
     * Creates a new ScanController instance.
     */
    public ScanController() {
        this(new EventRepository());
    }

    /**
     * Creates a new ScanController instance.
     *
     * @param eventRepository the event repository
     */
    ScanController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Returns the result of validate payload.
     *
     * @param rawPayload the raw payload
     * @return the result of this call
     */
    public EventScanResult validatePayload(String rawPayload) {
        String eventId = rawPayload == null ? "" : rawPayload.trim();
        if (eventId.isEmpty()) {
            return EventScanResult.invalidPayload(eventId, R.string.scan_error_invalid_qr);
        }
        return EventScanResult.openEvent(buildEventStub(eventId));
    }

    /**
     * Performs load scanned event.
     *
     * @param rawPayload the raw payload
     * @param listener the listener
     */
    public void loadScannedEvent(String rawPayload, OnCompleteListener<EventScanResult> listener) {
        EventScanResult validation = validatePayload(rawPayload);
        if (!validation.shouldOpenEvent()) {
            listener.onComplete(validation, true);
            return;
        }

        String eventId = validation.getEventId();
        eventRepository.getEventById(eventId, (Event event, boolean success) -> {
            if (!success) {
                listener.onComplete(EventScanResult.loadError(eventId, R.string.scan_error_load), false);
                return;
            }
            if (event == null) {
                listener.onComplete(EventScanResult.eventNotFound(eventId, R.string.scan_error_event_not_found), true);
                return;
            }
            listener.onComplete(EventScanResult.openEvent(event), true);
        });
    }

    /**
     * Returns the result of build event stub.
     *
     * @param eventId the event id
     * @return the result of this call
     */
    private Event buildEventStub(String eventId) {
        Event event = new Event();
        event.setEventId(eventId);
        return event;
    }
}
