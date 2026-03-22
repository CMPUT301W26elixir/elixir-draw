package com.example.allot.controller.event;

import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
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

        WaitingList waitingList = event.getWaitingList();
        if (waitingList == null) {
            return null;
        }

        if (waitingList.chosen == null) {
            waitingList.chosen = new ArrayList<>();
        }
        if (waitingList.status == null) {
            waitingList.status = new HashMap<>();
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

        waitingList.chosen.remove(declinedDeviceId);
        waitingList.status.remove(declinedDeviceId);
        event.getChosen().remove(declinedDeviceId);
        event.getEnrolled().remove(declinedDeviceId);
        if (!event.getCancelled().contains(declinedDeviceId)) {
            event.getCancelled().add(declinedDeviceId);
        }

        if ("open".equalsIgnoreCase(normalizeNullable(event.getStatus()))) {
            addReplacementOffer(event, declinedDeviceId);
        }

        event.setChosen(new ArrayList<>(waitingList.chosen));
        event.setEnrolled(waitingList.enrolled());
        event.setNotEnrolled(waitingList.notEnrolled());
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
        if (event == null) {
            return;
        }

        WaitingList waitingList = event.getWaitingList();
        if (waitingList == null || waitingList.list == null) {
            return;
        }

        List<String> eligibleEntrants = new ArrayList<>();
        for (String entrantId : waitingList.list) {
            if (isBlank(entrantId)) {
                continue;
            }
            if (entrantId.equals(declinedDeviceId)) {
                continue;
            }
            if (waitingList.chosen.contains(entrantId)) {
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
        waitingList.chosen.add(replacementId);
        waitingList.status.put(replacementId, false);
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









