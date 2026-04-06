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

        /**
         * Creates a new FooterState instance.
         *
         * @param showWaitlistMessage the show waitlist message
         * @param buttonEnabled the button enabled
         * @param buttonTextRes the button text res
         * @param buttonBackgroundRes the button background res
         * @param buttonTextColorRes the button text color res
         * @param subtext the subtext
         * @param showEntrantCount the show entrant count
         */
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
         * Returns the button text res.
         *
         * @return the button text res
         */
        public int getButtonTextRes() {
            return buttonTextRes;
        }

        /**
         * Returns the button background res.
         *
         * @return the button background res
         */
        public int getButtonBackgroundRes() {
            return buttonBackgroundRes;
        }

        /**
         * Returns the button text color res.
         *
         * @return the button text color res
         */
        public int getButtonTextColorRes() {
            return buttonTextColorRes;
        }

        /**
         * Returns the subtext.
         *
         * @return the subtext
         */
        public String getSubtext() {
            return subtext;
        }

        /**
         * Returns whether this instance should show entrant count.
         *
         * @return whether this instance should show entrant count
         */
        public boolean shouldShowEntrantCount() {
            return showEntrantCount;
        }
    }

    /**
     * Returns the result of build footer state.
     *
     * @param state the state
     * @return the result of this call
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

        if (state.getActionType() == EventActionState.ActionType.INVITED) {
            return new FooterState(
                    false,
                    true,
                    R.string.event_detail_invite_response,
                    R.drawable.bg_waitlist_button,
                    R.color.black,
                    state.getSubtext(),
                    false
            );
        }

        if (state.getActionType() == EventActionState.ActionType.INVITE_ONLY) {
            return new FooterState(
                    false,
                    false,
                    R.string.event_detail_invite_only,
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
     * Returns the entrant count.
     *
     * @param event the event
     * @return the entrant count
     */
    public int getEntrantCount(Event event) {
        if (event != null && event.getWaitingList() != null && event.getWaitingList().list != null) {
            return event.getWaitingList().list.size();
        }
        return 0;
    }

    /**
     * Returns whether manage action.
     *
     * @param state the state
     * @return whether manage action
     */
    public boolean isManageAction(EventActionState state) {
        return state != null && state.getActionType() == EventActionState.ActionType.MANAGE;
    }

    /**
     * Returns whether this instance has active offer.
     *
     * @param state the state
     * @return whether this instance has active offer
     */
    public boolean hasActiveOffer(EventActionState state) {
        return state != null && state.getActionType() == EventActionState.ActionType.OFFER;
    }

    /**
     * Returns whether on waiting list.
     *
     * @param state the state
     * @return whether on waiting list
     */
    public boolean isOnWaitingList(EventActionState state) {
        return state != null && state.getActionType() == EventActionState.ActionType.LEAVE_WAITLIST;
    }

    /**
     * Returns the selection criteria count.
     *
     * @param event the event
     * @return the selection criteria count
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









