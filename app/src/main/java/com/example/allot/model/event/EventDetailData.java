package com.example.allot.model.event;

/**
 * Holds all UI data needed by the event detail screen.
 */
public class EventDetailData {
    /**
     * Describes the overall loading state of the screen.
     */
    public enum Status {
        CONTENT,
        ERROR
    }

    /**
     * Says what the screen should do when the main button is pressed.
     */
    public enum NextAction {
        NONE,
        NAVIGATE_MANAGE,
        NAVIGATE_OFFER,
        SHOW_JOIN_DIALOG,
        LEAVE_WAITLIST,
        SHOW_INVITE_DIALOG
    }

    private final Status status;
    private final String title;
    private final String priceText;
    private final String locationText;
    private final String dateText;
    private final String heroDeadlineText;
    private final String organizerText;
    private final String descriptionText;
    private final String registrationOpenText;
    private final String registrationDeadlineText;
    private final int heroBackgroundRes;
    private final int entrantCount;
    private final boolean showWaitlistMessage;
    private final boolean buttonEnabled;
    private final int buttonTextRes;
    private final int buttonBackgroundRes;
    private final int buttonTextColorRes;
    private final String subtext;
    private final boolean showEntrantCount;
    private final String errorMessage;
    private final Event currentEvent;
    private final EventActionState currentDetailState;
    private final NextAction nextAction;

    /**
     * Creates a new EventDetailData instance.
     *
     * @param status the status
     * @param title the title
     * @param priceText the price text
     * @param locationText the location text
     * @param dateText the date text
     * @param heroDeadlineText the hero deadline text
     * @param organizerText the organizer text
     * @param descriptionText the description text
     * @param registrationOpenText the registration open text
     * @param registrationDeadlineText the registration deadline text
     * @param heroBackgroundRes the hero background res
     * @param entrantCount the entrant count
     * @param showWaitlistMessage the show waitlist message
     * @param buttonEnabled the button enabled
     * @param buttonTextRes the button text res
     * @param buttonBackgroundRes the button background res
     * @param buttonTextColorRes the button text color res
     * @param subtext the subtext
     * @param showEntrantCount the show entrant count
     * @param errorMessage the error message
     * @param currentEvent the current event
     * @param currentDetailState the current detail state
     * @param nextAction the next action
     */
    public EventDetailData(Status status,
                                  String title,
                                  String priceText,
                                  String locationText,
                                  String dateText,
                                  String heroDeadlineText,
                                  String organizerText,
                                  String descriptionText,
                                  String registrationOpenText,
                                  String registrationDeadlineText,
                                  int heroBackgroundRes,
                                  int entrantCount,
                                  boolean showWaitlistMessage,
                                  boolean buttonEnabled,
                                  int buttonTextRes,
                                  int buttonBackgroundRes,
                                  int buttonTextColorRes,
                                  String subtext,
                                  boolean showEntrantCount,
                                  String errorMessage,
                                  Event currentEvent,
                                  EventActionState currentDetailState,
                                  NextAction nextAction) {
        this.status = status;
        this.title = title;
        this.priceText = priceText;
        this.locationText = locationText;
        this.dateText = dateText;
        this.heroDeadlineText = heroDeadlineText;
        this.organizerText = organizerText;
        this.descriptionText = descriptionText;
        this.registrationOpenText = registrationOpenText;
        this.registrationDeadlineText = registrationDeadlineText;
        this.heroBackgroundRes = heroBackgroundRes;
        this.entrantCount = entrantCount;
        this.showWaitlistMessage = showWaitlistMessage;
        this.buttonEnabled = buttonEnabled;
        this.buttonTextRes = buttonTextRes;
        this.buttonBackgroundRes = buttonBackgroundRes;
        this.buttonTextColorRes = buttonTextColorRes;
        this.subtext = subtext;
        this.showEntrantCount = showEntrantCount;
        this.errorMessage = errorMessage;
        this.currentEvent = currentEvent;
        this.currentDetailState = currentDetailState;
        this.nextAction = nextAction;
    }

    /**
     * Returns the status.
     *
     * @return the status
     */
    public Status getStatus() { return status; }
    /**
     * Returns the title.
     *
     * @return the title
     */
    public String getTitle() { return title; }
    /**
     * Returns the price text.
     *
     * @return the price text
     */
    public String getPriceText() { return priceText; }
    /**
     * Returns the location text.
     *
     * @return the location text
     */
    public String getLocationText() { return locationText; }
    /**
     * Returns the date text.
     *
     * @return the date text
     */
    public String getDateText() { return dateText; }
    /**
     * Returns the hero deadline text.
     *
     * @return the hero deadline text
     */
    public String getHeroDeadlineText() { return heroDeadlineText; }
    /**
     * Returns the organizer text.
     *
     * @return the organizer text
     */
    public String getOrganizerText() { return organizerText; }
    /**
     * Returns the description text.
     *
     * @return the description text
     */
    public String getDescriptionText() { return descriptionText; }
    /**
     * Returns the registration open text.
     *
     * @return the registration open text
     */
    public String getRegistrationOpenText() { return registrationOpenText; }
    /**
     * Returns the registration deadline text.
     *
     * @return the registration deadline text
     */
    public String getRegistrationDeadlineText() { return registrationDeadlineText; }
    /**
     * Returns the hero background res.
     *
     * @return the hero background res
     */
    public int getHeroBackgroundRes() { return heroBackgroundRes; }
    /**
     * Returns the entrant count.
     *
     * @return the entrant count
     */
    public int getEntrantCount() { return entrantCount; }
    /**
     * Returns whether this instance should show waitlist message.
     *
     * @return whether this instance should show waitlist message
     */
    public boolean shouldShowWaitlistMessage() { return showWaitlistMessage; }
    /**
     * Returns whether button enabled.
     *
     * @return whether button enabled
     */
    public boolean isButtonEnabled() { return buttonEnabled; }
    /**
     * Returns the button text res.
     *
     * @return the button text res
     */
    public int getButtonTextRes() { return buttonTextRes; }
    /**
     * Returns the button background res.
     *
     * @return the button background res
     */
    public int getButtonBackgroundRes() { return buttonBackgroundRes; }
    /**
     * Returns the button text color res.
     *
     * @return the button text color res
     */
    public int getButtonTextColorRes() { return buttonTextColorRes; }
    /**
     * Returns the subtext.
     *
     * @return the subtext
     */
    public String getSubtext() { return subtext; }
    /**
     * Returns whether this instance should show entrant count.
     *
     * @return whether this instance should show entrant count
     */
    public boolean shouldShowEntrantCount() { return showEntrantCount; }
    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() { return errorMessage; }
    /**
     * Returns the current event.
     *
     * @return the current event
     */
    public Event getCurrentEvent() { return currentEvent; }
    /**
     * Returns the current detail state.
     *
     * @return the current detail state
     */
    public EventActionState getCurrentDetailState() { return currentDetailState; }
    /**
     * Returns the next action.
     *
     * @return the next action
     */
    public NextAction getNextAction() { return nextAction; }
}









