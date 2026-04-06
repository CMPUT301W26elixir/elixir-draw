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
     * Returns the result of build declined offer state.
     *
     * @param event the event
     * @param declinedDeviceId the declined device id
     * @return the result of this call
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
     * Returns the result of build replacement draw state.
     *
     * @param event the event
     * @return the result of this call
     */
    public Event buildReplacementDrawState(Event event) {
        if (event == null) {
            return null;
        }

        WaitingList waitingList = event.getWaitingList();
        if (waitingList == null || waitingList.list == null) {
            return null;
        }

        if (waitingList.chosen == null) {
            waitingList.chosen = new ArrayList<>();
        }
        if (waitingList.status == null) {
            waitingList.status = new HashMap<>();
        }
        if (event.getCancelled() == null) {
            event.setCancelled(new ArrayList<>());
        }

        List<String> eligibleEntrants = new ArrayList<>();
        for (String entrantId : waitingList.list) {
            if (isBlank(entrantId)) {
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
            return null;
        }

        String replacementId = eligibleEntrants.get(new Random().nextInt(eligibleEntrants.size()));
        waitingList.chosen.add(replacementId);
        waitingList.status.put(replacementId, false);

        event.setChosen(new ArrayList<>(waitingList.chosen));
        event.setEnrolled(waitingList.enrolled());
        event.setNotEnrolled(waitingList.notEnrolled());
        return event;
    }

    /**
     * Performs add replacement offer.
     *
     * @param event the event
     * @param declinedDeviceId the declined device id
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
     * Returns the result of build manual cancellation state.
     *
     * @param event the event
     * @param entrantId the entrant id
     * @return the result of this call
     */
    public Event buildManualCancellationState(Event event, String entrantId) {
        if (event == null || isBlank(entrantId)) {
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

        waitingList.chosen.remove(entrantId);
        waitingList.status.remove(entrantId);
        event.getChosen().remove(entrantId);
        event.getEnrolled().remove(entrantId);
        event.getNotEnrolled().remove(entrantId);
        if (!event.getCancelled().contains(entrantId)) {
            event.getCancelled().add(entrantId);
        }

        event.setChosen(new ArrayList<>(waitingList.chosen));
        event.setEnrolled(waitingList.enrolled());
        event.setNotEnrolled(waitingList.notEnrolled());
        return event;
    }

    /**
     * Returns whether blank.
     *
     * @param value the value
     * @return whether blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Returns the result of normalize nullable.
     *
     * @param value the value
     * @return the result of this call
     */
    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
