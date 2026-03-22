package com.example.allot.controller.event;

import com.example.allot.R;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventActionState;
/**
 * Turns event detail state into text and UI values.
 */
public class EventDetailViewService {
    /**
     * Represents the footer presentation state for the detail screen.
     */
    public static class FooterState {
        private final boolean showWaitlistMessage;
        private final boolean buttonEnabled;
        private final int buttonTextRes;
        private final int buttonBackgroundRes;
        private final int buttonTextColorRes;
        private final String subtext;
        private final boolean showEntrantCount;

        public FooterState(boolean showWaitlistMessage,
                           boolean buttonEnabled,
                           int buttonTextRes,
                           int buttonBackgroundRes,
                           int buttonTextColorRes,
                           String subtext,
                           boolean showEntrantCount) {
            this.showWaitlistMessage = showWaitlistMessage;
            this.buttonEnabled = buttonEnabled;
            this.buttonTextRes = buttonTextRes;
            this.buttonBackgroundRes = buttonBackgroundRes;
            this.buttonTextColorRes = buttonTextColorRes;
            this.subtext = subtext;
            this.showEntrantCount = showEntrantCount;
        }

        public boolean shouldShowWaitlistMessage() {
            return showWaitlistMessage;
        }

        public boolean isButtonEnabled() {
            return buttonEnabled;
        }

        public int getButtonTextRes() {
            return buttonTextRes;
        }

        public int getButtonBackgroundRes() {
            return buttonBackgroundRes;
        }

        public int getButtonTextColorRes() {
            return buttonTextColorRes;
        }

        public String getSubtext() {
            return subtext;
        }

        public boolean shouldShowEntrantCount() {
            return showEntrantCount;
        }
    }

    /**
     * Updates the footer and action button based on the current user's
     * relationship to the event.
     *
     * @param state the event detail state whose waitlist and offer state should be shown
     * @return the derived footer presentation state
     */
    public FooterState buildFooterState(EventActionState state) {
        if (state == null) {
            return new FooterState(false, false, R.string.event_detail_join_waiting_list,
                    R.drawable.bg_waitlist_button, R.color.black, null, true);
        }

        if (state.getActionType() == EventActionState.ActionType.MANAGE) {
            return new FooterState(
                    false,
                    true,
                    R.string.event_detail_manage_event,
                    R.drawable.bg_waitlist_button,
                    R.color.black,
                    null,
                    true
            );
        }

        if (state.getActionType() == EventActionState.ActionType.ENROLLED) {
            return new FooterState(
                    false,
                    false,
                    R.string.event_detail_enrolled,
                    R.drawable.bg_event_detail_offer_green,
                    R.color.white,
                    null,
                    true
            );
        }

        if (state.getActionType() == EventActionState.ActionType.OFFER) {
            return new FooterState(
                    false,
                    true,
                    R.string.event_detail_offer,
                    R.drawable.bg_event_detail_offer_green,
                    R.color.white,
                    null,
                    true
            );
        }

        if (state.getActionType() == EventActionState.ActionType.NOT_SELECTED_REPLACEMENT) {
            return new FooterState(
                    false,
                    false,
                    R.string.event_detail_not_selected_main_draw,
                    R.drawable.bg_event_detail_offer_yellow,
                    R.color.black,
                    state.getSubtext(),
                    false
            );
        }

        if (state.getActionType() == EventActionState.ActionType.NOT_SELECTED_FINAL) {
            return new FooterState(
                    false,
                    false,
                    R.string.event_detail_not_selected_final,
                    R.drawable.bg_event_detail_offer_grey,
                    R.color.black,
                    state.getSubtext(),
                    false
            );
        }

        return new FooterState(
                state.shouldShowWaitlistMessage(),
                state.isButtonEnabled(),
                state.getActionType() == EventActionState.ActionType.LEAVE_WAITLIST
                        ? R.string.event_detail_leave_waiting_list
                        : R.string.event_detail_join_waiting_list,
                state.getActionType() == EventActionState.ActionType.LEAVE_WAITLIST
                        ? R.drawable.bg_waitlist_button_inactive
                        : R.drawable.bg_waitlist_button,
                R.color.black,
                state.getSubtext(),
                state.shouldShowEntrantCount()
        );
    }

    /**
     * Displays the current number of entrants on the waiting list.
     *
     * @param event the event whose entrant count should be shown
     * @return the current entrant count
     */
    public int getEntrantCount(Event event) {
        if (event != null && event.getWaitingList() != null && event.getWaitingList().list != null) {
            return event.getWaitingList().list.size();
        }
        return 0;
    }

    /**
     * Checks whether the current user is the organizer of the event.
     *
     * @param state the detail state to inspect
     * @return true if the current user is the organizer, otherwise false
     */
    public boolean isManageAction(EventActionState state) {
        return state != null && state.getActionType() == EventActionState.ActionType.MANAGE;
    }

    /**
     * Checks whether the current user has an active offer for the event.
     *
     * @param state the detail state to inspect
     * @return true if the current user is selected but not yet enrolled
     */
    public boolean hasActiveOffer(EventActionState state) {
        return state != null && state.getActionType() == EventActionState.ActionType.OFFER;
    }

    /**
     * Checks whether the current user is on the event waiting list.
     *
     * @param state the detail state to inspect
     * @return true if the current user is on the waiting list, otherwise false
     */
    public boolean isOnWaitingList(EventActionState state) {
        return state != null && state.getActionType() == EventActionState.ActionType.LEAVE_WAITLIST;
    }

    /**
     * Builds the selection criteria text shown in the waiting list dialog.
     *
     * @param event the event being displayed
     * @return the attendee count used by the selection criteria text
     */
    public int getSelectionCriteriaCount(Event event) {
        if (event != null) {
            if (event.getCapacity() > 0) {
                return event.getCapacity();
            } else if (event.getLimit() > 0) {
                return event.getLimit();
            }
        }
        return 0;
    }
}









