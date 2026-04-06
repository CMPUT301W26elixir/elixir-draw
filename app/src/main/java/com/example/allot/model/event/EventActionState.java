package com.example.allot.model.event;
/**
 * Stores the action button state and text for an event detail user.
 */
public class EventActionState {
    public enum ActionType {
        MANAGE,
        OFFER,
        JOIN_WAITLIST,
        LEAVE_WAITLIST,
        INVITED,
        INVITE_ONLY,
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
     * Creates a new EventActionState instance.
     *
     * @param event the event
     * @param actionType the action type
     * @param showWaitlistMessage the show waitlist message
     * @param buttonEnabled the button enabled
     * @param showEntrantCount the show entrant count
     * @param subtext the subtext
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

    /**
     * Returns the event.
     *
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * Returns the action type.
     *
     * @return the action type
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * Returns whether this instance should show waitlist message.
     *
     * @return whether this instance should show waitlist message
     */
    public boolean shouldShowWaitlistMessage() {
        return showWaitlistMessage;
    }

    /**
     * Returns whether button enabled.
     *
     * @return whether button enabled
     */
    public boolean isButtonEnabled() {
        return buttonEnabled;
    }

    /**
     * Returns whether this instance should show entrant count.
     *
     * @return whether this instance should show entrant count
     */
    public boolean shouldShowEntrantCount() {
        return showEntrantCount;
    }

    /**
     * Returns the subtext.
     *
     * @return the subtext
     */
    public String getSubtext() {
        return subtext;
    }
}









