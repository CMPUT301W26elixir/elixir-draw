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
     * Creates an event detail state object for the screen.
     *
     * @param status the overall screen state
     * @param title the event title to show
     * @param priceText the formatted event price
     * @param locationText the location text shown in the header
     * @param dateText the formatted event date
     * @param heroDeadlineText the deadline text shown in the hero section
     * @param organizerText the organizer name to display
     * @param descriptionText the event description
     * @param registrationOpenText the formatted registration open date
     * @param registrationDeadlineText the formatted registration close date
     * @param heroBackgroundRes the background resource used for the hero image
     * @param entrantCount the number of entrants to display
     * @param showWaitlistMessage true when the waitlist helper message should be shown
     * @param buttonEnabled true when the primary action button should be enabled
     * @param buttonTextRes the string resource used for the primary button label
     * @param buttonBackgroundRes the drawable resource used for the primary button background
     * @param buttonTextColorRes the color resource used for the button text
     * @param subtext the supporting text shown under the primary action
     * @param showEntrantCount true when the entrant count should be visible
     * @param errorMessage the message shown when the screen is in an error state
     * @param currentEvent the backing event model for follow-up actions
     * @param currentDetailState the computed action state for the current user
     * @param nextAction the action the view should take after a button press
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
     * @return the overall screen status
     */
    public Status getStatus() { return status; }
    /**
     * @return the event title
     */
    public String getTitle() { return title; }
    /**
     * @return the formatted price text
     */
    public String getPriceText() { return priceText; }
    /**
     * @return the formatted location text
     */
    public String getLocationText() { return locationText; }
    /**
     * @return the formatted event date text
     */
    public String getDateText() { return dateText; }
    /**
     * @return the hero deadline text
     */
    public String getHeroDeadlineText() { return heroDeadlineText; }
    /**
     * @return the organizer name shown on screen
     */
    public String getOrganizerText() { return organizerText; }
    /**
     * @return the event description text
     */
    public String getDescriptionText() { return descriptionText; }
    /**
     * @return the formatted registration open text
     */
    public String getRegistrationOpenText() { return registrationOpenText; }
    /**
     * @return the formatted registration deadline text
     */
    public String getRegistrationDeadlineText() { return registrationDeadlineText; }
    /**
     * @return the hero background drawable resource
     */
    public int getHeroBackgroundRes() { return heroBackgroundRes; }
    /**
     * @return the entrant count to display
     */
    public int getEntrantCount() { return entrantCount; }
    /**
     * @return true when the waitlist helper message should be shown
     */
    public boolean shouldShowWaitlistMessage() { return showWaitlistMessage; }
    /**
     * @return true when the primary action button is enabled
     */
    public boolean isButtonEnabled() { return buttonEnabled; }
    /**
     * @return the button label resource
     */
    public int getButtonTextRes() { return buttonTextRes; }
    /**
     * @return the button background resource
     */
    public int getButtonBackgroundRes() { return buttonBackgroundRes; }
    /**
     * @return the button text color resource
     */
    public int getButtonTextColorRes() { return buttonTextColorRes; }
    /**
     * @return the supporting subtext under the action button
     */
    public String getSubtext() { return subtext; }
    /**
     * @return true when the entrant count should be visible
     */
    public boolean shouldShowEntrantCount() { return showEntrantCount; }
    /**
     * @return the error message for an error state
     */
    public String getErrorMessage() { return errorMessage; }
    /**
     * @return the current event model
     */
    public Event getCurrentEvent() { return currentEvent; }
    /**
     * @return the action state computed for the current viewer
     */
    public EventActionState getCurrentDetailState() { return currentDetailState; }
    /**
     * @return the next action the view should perform
     */
    public NextAction getNextAction() { return nextAction; }
}









