package com.example.allot.model.lottery;

import com.example.allot.model.event.Event;
import java.util.ArrayList;
import java.util.List;
public class RunLotteryData {
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

    public Status getStatus() { return status; }
    public String getDrawDateValue() { return drawDateValue; }
    public String getAttendeesValue() { return attendeesValue; }
    public List<LotteryEntrantItem> getEntrantItems() { return new ArrayList<>(entrantItems); }
    public int getStateMessageRes() { return stateMessageRes; }
    public boolean isActionEnabled() { return actionEnabled; }
    public boolean shouldRedirectToEntrants() { return shouldRedirectToEntrants; }
    public Event getCurrentEvent() { return currentEvent; }
}








