package com.example.allot.controller.lottery;

import com.example.allot.model.event.Event;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Builds and checks lottery draw data stored on an event.
 */
public class LotteryDrawService {
    /**
     * Checks whether the event already has draw results.
     *
     * @param event the event to check
     * @return true if the event already has selected or processed entrants, false otherwise
     */
    public boolean hasDrawResults(Event event) {
        return event != null
                && ((event.getChosen() != null && !event.getChosen().isEmpty())
                || (event.getEnrolled() != null && !event.getEnrolled().isEmpty())
                || (event.getCancelled() != null && !event.getCancelled().isEmpty())
                || (event.getNotEnrolled() != null && !event.getNotEnrolled().isEmpty())
                || (event.getWaitingList() != null && event.getWaitingList().chosen != null && !event.getWaitingList().chosen.isEmpty()));
    }

    /**
     * Validates the form, runs the draw, and saves the results to Firestore.
     *
     * @param event the current event state
     * @param attendeesToSelect the attendee count for the draw
     * @return the updated event, or null if the draw cannot be created
     */
    public Event buildDrawResult(Event event, int attendeesToSelect) {
        if (event == null) {
            return null;
        }

        if (event.getWaitingList() == null) {
            event.getWaitingList();
        }
        if (event.getWaitingList() == null || event.getWaitingList().list == null || event.getWaitingList().list.isEmpty()) {
            return null;
        }

        event.setCapacity(attendeesToSelect);
        event.setLimit(attendeesToSelect);
        event.getWaitingList().limit = attendeesToSelect;
        event.getWaitingList().chosen = new ArrayList<>();
        event.getWaitingList().status = new HashMap<>();
        event.setChosen(new ArrayList<>());
        event.setEnrolled(new ArrayList<>());
        event.setCancelled(new ArrayList<>());
        event.setNotEnrolled(new ArrayList<>());

        event.lottery();
        return event;
    }
}









