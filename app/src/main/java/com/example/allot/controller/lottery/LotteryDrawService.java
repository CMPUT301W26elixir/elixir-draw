package com.example.allot.controller.lottery;

import com.example.allot.model.event.Event;
import com.example.allot.model.event.WaitingList;
import java.util.ArrayList;
import java.util.HashMap;
/**
 * Builds and checks lottery draw data stored on an event.
 */
public class LotteryDrawService {
    /**
     * Returns whether this instance has draw results.
     *
     * @param event the event
     * @return whether this instance has draw results
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
     * Returns the result of build draw result.
     *
     * @param event the event
     * @param attendeesToSelect the attendees to select
     * @return the result of this call
     */
    public Event buildDrawResult(Event event, int attendeesToSelect) {
        if (event == null) {
            return null;
        }

        WaitingList waitingList = event.getWaitingList();
        if (waitingList == null || waitingList.list == null || waitingList.list.isEmpty()) {
            return null;
        }

        event.setCapacity(attendeesToSelect);
        event.setLimit(attendeesToSelect);
        waitingList.limit = attendeesToSelect;
        waitingList.chosen = new ArrayList<>();
        waitingList.status = new HashMap<>();
        event.setChosen(new ArrayList<>());
        event.setEnrolled(new ArrayList<>());
        event.setCancelled(new ArrayList<>());
        event.setNotEnrolled(new ArrayList<>());

        event.lottery();
        return event;
    }
}









