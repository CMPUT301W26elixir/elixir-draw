package com.example.allot.controller;

import com.example.allot.model.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Builds event state transitions related to offer acceptance and decline flows.
 */
public class EventOfferService {
    /**
     * Handles the loaded event snapshot for a declined offer, updates the event state,
     * and optionally assigns a replacement offer.
     *
     * @param event the loaded event state
     * @param declinedDeviceId the device ID of the user declining the offer
     * @return the updated event state, or null if the event cannot be updated
     */
    public Event buildDeclinedOfferState(Event event, String declinedDeviceId) {
        if (event == null) {
            return null;
        }

        if (event.waitingList == null) {
            event.getWaitingList();
        }
        if (event.waitingList == null) {
            return null;
        }

        if (event.waitingList.chosen == null) {
            event.waitingList.chosen = new ArrayList<>();
        }
        if (event.waitingList.status == null) {
            event.waitingList.status = new HashMap<>();
        }
        if (event.chosen == null) {
            event.chosen = new ArrayList<>();
        }
        if (event.enrolled == null) {
            event.enrolled = new ArrayList<>();
        }
        if (event.cancelled == null) {
            event.cancelled = new ArrayList<>();
        }
        if (event.notEnrolled == null) {
            event.notEnrolled = new ArrayList<>();
        }

        event.waitingList.chosen.remove(declinedDeviceId);
        event.waitingList.status.remove(declinedDeviceId);
        event.chosen.remove(declinedDeviceId);
        event.enrolled.remove(declinedDeviceId);
        if (!event.cancelled.contains(declinedDeviceId)) {
            event.cancelled.add(declinedDeviceId);
        }

        if ("open".equalsIgnoreCase(normalizeNullable(event.status))) {
            addReplacementOffer(event, declinedDeviceId);
        }

        event.chosen = new ArrayList<>(event.waitingList.chosen);
        event.enrolled = event.waitingList.enrolled();
        event.notEnrolled = event.waitingList.notEnrolled();
        return event;
    }

    /**
     * Adds a replacement offer to the event by randomly selecting
     * an eligible entrant from the waiting list.
     *
     * @param event the event whose replacement offer should be assigned
     * @param declinedDeviceId the device ID of the user who declined the offer
     */
    private void addReplacementOffer(Event event, String declinedDeviceId) {
        if (event == null || event.waitingList == null || event.waitingList.list == null) {
            return;
        }

        List<String> eligibleEntrants = new ArrayList<>();
        for (String entrantId : event.waitingList.list) {
            if (isBlank(entrantId)) {
                continue;
            }
            if (entrantId.equals(declinedDeviceId)) {
                continue;
            }
            if (event.waitingList.chosen.contains(entrantId)) {
                continue;
            }
            if (event.cancelled.contains(entrantId)) {
                continue;
            }
            eligibleEntrants.add(entrantId);
        }

        if (eligibleEntrants.isEmpty()) {
            return;
        }

        String replacementId = eligibleEntrants.get(new Random().nextInt(eligibleEntrants.size()));
        event.waitingList.chosen.add(replacementId);
        event.waitingList.status.put(replacementId, false);
    }

    /**
     * Checks whether a string is blank after trimming whitespace.
     *
     * @param value the string to check
     * @return true if the string is blank, otherwise false
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Returns a trimmed string value, or null if the value is null.
     *
     * @param value the string to normalize
     * @return the normalized string
     */
    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
