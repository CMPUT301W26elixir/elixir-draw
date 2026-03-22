package com.example.allot.controller.event;

import com.example.allot.model.event.Event;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
/**
 * Handles the rules for event offers and extra draws.
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

        if (event.getWaitingList() == null) {
            event.getWaitingList();
        }
        if (event.getWaitingList() == null) {
            return null;
        }

        if (event.getWaitingList().chosen == null) {
            event.getWaitingList().chosen = new ArrayList<>();
        }
        if (event.getWaitingList().status == null) {
            event.getWaitingList().status = new HashMap<>();
        }
        if (event.getChosen() == null) {
            event.setChosen(new ArrayList<>());
        }
        if (event.getEnrolled() == null) {
            event.setEnrolled(new ArrayList<>());
        }
        if (event.getCancelled() == null) {
            event.setCancelled(new ArrayList<>());
        }
        if (event.getNotEnrolled() == null) {
            event.setNotEnrolled(new ArrayList<>());
        }

        event.getWaitingList().chosen.remove(declinedDeviceId);
        event.getWaitingList().status.remove(declinedDeviceId);
        event.getChosen().remove(declinedDeviceId);
        event.getEnrolled().remove(declinedDeviceId);
        if (!event.getCancelled().contains(declinedDeviceId)) {
            event.getCancelled().add(declinedDeviceId);
        }

        if ("open".equalsIgnoreCase(normalizeNullable(event.getStatus()))) {
            addReplacementOffer(event, declinedDeviceId);
        }

        event.setChosen(new ArrayList<>(event.getWaitingList().chosen));
        event.setEnrolled(event.getWaitingList().enrolled());
        event.setNotEnrolled(event.getWaitingList().notEnrolled());
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
        if (event == null || event.getWaitingList() == null || event.getWaitingList().list == null) {
            return;
        }

        List<String> eligibleEntrants = new ArrayList<>();
        for (String entrantId : event.getWaitingList().list) {
            if (isBlank(entrantId)) {
                continue;
            }
            if (entrantId.equals(declinedDeviceId)) {
                continue;
            }
            if (event.getWaitingList().chosen.contains(entrantId)) {
                continue;
            }
            if (event.getCancelled().contains(entrantId)) {
                continue;
            }
            eligibleEntrants.add(entrantId);
        }

        if (eligibleEntrants.isEmpty()) {
            return;
        }

        String replacementId = eligibleEntrants.get(new Random().nextInt(eligibleEntrants.size()));
        event.getWaitingList().chosen.add(replacementId);
        event.getWaitingList().status.put(replacementId, false);
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









