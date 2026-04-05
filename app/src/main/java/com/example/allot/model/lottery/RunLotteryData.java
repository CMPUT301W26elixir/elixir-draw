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
     * Creates the state object shown by the lottery screen.
     *
     * @param status the overall screen status
     * @param drawDateValue the formatted draw date
     * @param attendeesValue the formatted attendee count
     * @param entrantItems the entrant rows to display
     * @param stateMessageRes the string resource for the empty or status message
     * @param actionEnabled true when the main action button should be enabled
     * @param shouldRedirectToEntrants true when the screen should navigate to the entrants page
     * @param currentEvent the event currently being managed
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
     * @return the overall screen status
     */
    public Status getStatus() { return status; }
    /**
     * @return the formatted draw date
     */
    public String getDrawDateValue() { return drawDateValue; }
    /**
     * @return the formatted attendee count
     */
    public String getAttendeesValue() { return attendeesValue; }
    /**
     * @return a copy of the entrant items shown on screen
     */
    public List<LotteryEntrantItem> getEntrantItems() { return new ArrayList<>(entrantItems); }
    /**
     * @return the state message resource
     */
    public int getStateMessageRes() { return stateMessageRes; }
    /**
     * @return true when the main action is enabled
     */
    public boolean isActionEnabled() { return actionEnabled; }
    /**
     * @return true when the screen should redirect to the entrants page
     */
    public boolean shouldRedirectToEntrants() { return shouldRedirectToEntrants; }
    /**
     * @return the current event model
     */
    public Event getCurrentEvent() { return currentEvent; }
}








