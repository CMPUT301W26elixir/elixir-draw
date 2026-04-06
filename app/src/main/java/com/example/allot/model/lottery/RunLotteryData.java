package com.example.allot.model.lottery;

import com.example.allot.model.event.Event;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the UI state needed by the run-lottery screen.
 */
public class RunLotteryData {
    /**
     * Describes the loading state of the lottery screen.
     */
    public enum Status {
        LOADING,
        CONTENT,
        ERROR
    }

    private final Status status;
    private final String drawDateValue;
    private final String attendeesValue;
    private final List<LotteryEntrantItem> entrantItems;
    private final int stateMessageRes;
    private final boolean actionEnabled;
    private final boolean shouldRedirectToEntrants;
    private final Event currentEvent;

    /**
     * Creates a new RunLotteryData instance.
     *
     * @param status the status
     * @param drawDateValue the draw date value
     * @param attendeesValue the attendees value
     * @param entrantItems the entrant items
     * @param stateMessageRes the state message res
     * @param actionEnabled the action enabled
     * @param shouldRedirectToEntrants whether redirect to entrants
     * @param currentEvent the current event
     */
    public RunLotteryData(Status status,
                                    String drawDateValue,
                                    String attendeesValue,
                                    List<LotteryEntrantItem> entrantItems,
                                    int stateMessageRes,
                                    boolean actionEnabled,
                                    boolean shouldRedirectToEntrants,
                                    Event currentEvent) {
        this.status = status;
        this.drawDateValue = drawDateValue;
        this.attendeesValue = attendeesValue;
        this.entrantItems = entrantItems == null ? new ArrayList<>() : new ArrayList<>(entrantItems);
        this.stateMessageRes = stateMessageRes;
        this.actionEnabled = actionEnabled;
        this.shouldRedirectToEntrants = shouldRedirectToEntrants;
        this.currentEvent = currentEvent;
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public Status getStatus() { return status; }
    /**
     * Returns the draw date value.
     *
     * @return the draw date value
     */
    public String getDrawDateValue() { return drawDateValue; }
    /**
     * Returns the attendees value.
     *
     * @return the attendees value
     */
    public String getAttendeesValue() { return attendeesValue; }
    /**
     * Returns the entrant items.
     *
     * @return the entrant items
     */
    public List<LotteryEntrantItem> getEntrantItems() { return new ArrayList<>(entrantItems); }
    /**
     * Returns the state message res.
     *
     * @return the state message res
     */
    public int getStateMessageRes() { return stateMessageRes; }
    /**
     * Returns whether action enabled.
     *
     * @return whether action enabled
     */
    public boolean isActionEnabled() { return actionEnabled; }
    /**
     * Returns whether this instance should redirect to entrants.
     *
     * @return whether this instance should redirect to entrants
     */
    public boolean shouldRedirectToEntrants() { return shouldRedirectToEntrants; }
    /**
     * Returns the current event.
     *
     * @return the current event
     */
    public Event getCurrentEvent() { return currentEvent; }
}








