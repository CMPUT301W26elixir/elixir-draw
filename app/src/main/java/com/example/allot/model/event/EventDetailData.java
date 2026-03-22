package com.example.allot.model.event;
public class EventDetailData {
    public enum Status {
        CONTENT,
        ERROR
    }

    public enum NextAction {
        NONE,
        NAVIGATE_MANAGE,
        NAVIGATE_OFFER,
        SHOW_JOIN_DIALOG,
        LEAVE_WAITLIST
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

    public Status getStatus() { return status; }
    public String getTitle() { return title; }
    public String getPriceText() { return priceText; }
    public String getLocationText() { return locationText; }
    public String getDateText() { return dateText; }
    public String getHeroDeadlineText() { return heroDeadlineText; }
    public String getOrganizerText() { return organizerText; }
    public String getDescriptionText() { return descriptionText; }
    public String getRegistrationOpenText() { return registrationOpenText; }
    public String getRegistrationDeadlineText() { return registrationDeadlineText; }
    public int getHeroBackgroundRes() { return heroBackgroundRes; }
    public int getEntrantCount() { return entrantCount; }
    public boolean shouldShowWaitlistMessage() { return showWaitlistMessage; }
    public boolean isButtonEnabled() { return buttonEnabled; }
    public int getButtonTextRes() { return buttonTextRes; }
    public int getButtonBackgroundRes() { return buttonBackgroundRes; }
    public int getButtonTextColorRes() { return buttonTextColorRes; }
    public String getSubtext() { return subtext; }
    public boolean shouldShowEntrantCount() { return showEntrantCount; }
    public String getErrorMessage() { return errorMessage; }
    public Event getCurrentEvent() { return currentEvent; }
    public EventActionState getCurrentDetailState() { return currentDetailState; }
    public NextAction getNextAction() { return nextAction; }
}









