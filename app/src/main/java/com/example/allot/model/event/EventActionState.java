package com.example.allot.model.event;
public class EventActionState {
    public enum ActionType {
        MANAGE,
        OFFER,
        JOIN_WAITLIST,
        LEAVE_WAITLIST,
        ENROLLED,
        NOT_SELECTED_REPLACEMENT,
        NOT_SELECTED_FINAL
    }

    private final Event event;
    private final ActionType actionType;
    private final boolean showWaitlistMessage;
    private final boolean buttonEnabled;
    private final boolean showEntrantCount;
    private final String subtext;

    /**
     * Creates a detail-screen state for the current event and user.
     *
     * @param event the loaded event
     * @param actionType the primary action type for the footer
     * @param showWaitlistMessage true to show the waitlist message
     * @param buttonEnabled true if the action button should be enabled
     * @param showEntrantCount true if the entrant count should be shown
     * @param subtext optional descriptive subtext
     */
    public EventActionState(Event event,
                            ActionType actionType,
                            boolean showWaitlistMessage,
                            boolean buttonEnabled,
                            boolean showEntrantCount,
                            String subtext) {
        this.event = event;
        this.actionType = actionType;
        this.showWaitlistMessage = showWaitlistMessage;
        this.buttonEnabled = buttonEnabled;
        this.showEntrantCount = showEntrantCount;
        this.subtext = subtext;
    }

    public Event getEvent() {
        return event;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public boolean shouldShowWaitlistMessage() {
        return showWaitlistMessage;
    }

    public boolean isButtonEnabled() {
        return buttonEnabled;
    }

    public boolean shouldShowEntrantCount() {
        return showEntrantCount;
    }

    public String getSubtext() {
        return subtext;
    }
}









