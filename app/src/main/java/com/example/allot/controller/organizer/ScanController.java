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

    public ScanController() {
        this(new EventRepository());
    }

    ScanController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /**
     * Validates a raw scanned payload and returns the parsed event ID result.
     *
     * @param rawPayload the raw scanned QR contents
     * @return a scan result representing whether the payload is usable
     */
    public EventScanResult validatePayload(String rawPayload) {
        String eventId = rawPayload == null ? "" : rawPayload.trim();
        if (eventId.isEmpty()) {
            return EventScanResult.invalidPayload(eventId, R.string.scan_error_invalid_qr);
        }
        return EventScanResult.openEvent(buildEventStub(eventId));
    }

    /**
     * Loads an event for a scanned payload.
     *
     * @param rawPayload the raw scanned QR contents
     * @param listener callback for the resulting state
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

    private Event buildEventStub(String eventId) {
        Event event = new Event();
        event.setEventId(eventId);
        return event;
    }
}
