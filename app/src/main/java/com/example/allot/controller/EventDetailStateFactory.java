package com.example.allot.controller;

import com.example.allot.model.Event;
import com.example.allot.model.EventDetailState;

import java.util.List;

/**
 * Builds UI-ready detail state for the event detail screen.
 */
public class EventDetailStateFactory {
    /**
     * Builds detail-screen state for the current event and user.
     *
     * @param event the event to evaluate
     * @param deviceId the current user device ID
     * @return the derived detail-screen state
     */
    public EventDetailState create(Event event, String deviceId) {
        if (isCurrentUserOrganizer(event, deviceId)) {
            return new EventDetailState(event, EventDetailState.ActionType.MANAGE, false, true, true, null);
        }

        if (isCurrentUserEnrolled(event, deviceId)) {
            return new EventDetailState(event, EventDetailState.ActionType.ENROLLED, false, false, true, null);
        }

        if (isCurrentUserSelected(event, deviceId)) {
            return new EventDetailState(event, EventDetailState.ActionType.OFFER, false, true, true, null);
        }

        if (shouldShowReplacementState(event, deviceId)) {
            return new EventDetailState(
                    event,
                    EventDetailState.ActionType.NOT_SELECTED_REPLACEMENT,
                    false,
                    false,
                    false,
                    "You were not selected in the main draw, but you may still receive an offer if spots open up."
            );
        }

        if (shouldShowFinalizedNotSelectedState(event, deviceId)) {
            return new EventDetailState(
                    event,
                    EventDetailState.ActionType.NOT_SELECTED_FINAL,
                    false,
                    false,
                    false,
                    "Registration is finalized and you were not selected for this event."
            );
        }

        boolean isOnWaitingList = isCurrentUserOnWaitingList(event, deviceId);
        return new EventDetailState(
                event,
                isOnWaitingList ? EventDetailState.ActionType.LEAVE_WAITLIST : EventDetailState.ActionType.JOIN_WAITLIST,
                isOnWaitingList,
                true,
                true,
                null
        );
    }

    /**
     * Checks whether the current user is enrolled in the event.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the current user is enrolled, otherwise false
     */
    private boolean isCurrentUserEnrolled(Event event, String deviceId) {
        return containsUser(event == null ? null : event.enrolled, deviceId);
    }

    /**
     * Checks whether the current user has been selected in the event draw.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the current user has been selected, otherwise false
     */
    private boolean isCurrentUserSelected(Event event, String deviceId) {
        return containsUser(event == null ? null : event.chosen, deviceId)
                || containsUser(event != null && event.waitingList != null ? event.waitingList.chosen : null, deviceId);
    }

    /**
     * Checks whether the UI should show the replacement-state message
     * for a user who was not selected in the main draw.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the replacement-state message should be shown
     */
    private boolean shouldShowReplacementState(Event event, String deviceId) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event, deviceId)
                && !isCurrentUserSelected(event, deviceId)
                && !isCurrentUserEnrolled(event, deviceId)
                && !"finalized".equalsIgnoreCase(normalizeNullable(event == null ? null : event.status));
    }

    /**
     * Checks whether the UI should show the finalized not-selected state.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the finalized not-selected state should be shown
     */
    private boolean shouldShowFinalizedNotSelectedState(Event event, String deviceId) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event, deviceId)
                && !isCurrentUserSelected(event, deviceId)
                && !isCurrentUserEnrolled(event, deviceId)
                && "finalized".equalsIgnoreCase(normalizeNullable(event == null ? null : event.status));
    }

    /**
     * Checks whether any selection results have been published for the event.
     *
     * @param event the event to check
     * @return true if selection results exist, otherwise false
     */
    private boolean hasPublishedSelectionResults(Event event) {
        return (event != null && event.chosen != null && !event.chosen.isEmpty())
                || (event != null && event.enrolled != null && !event.enrolled.isEmpty())
                || (event != null && event.cancelled != null && !event.cancelled.isEmpty())
                || (event != null && event.notEnrolled != null && !event.notEnrolled.isEmpty())
                || (event != null && event.waitingList != null && event.waitingList.chosen != null && !event.waitingList.chosen.isEmpty());
    }

    /**
     * Checks whether the current user is on the event waiting list.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the current user is on the waiting list, otherwise false
     */
    private boolean isCurrentUserOnWaitingList(Event event, String deviceId) {
        if (event == null || event.waitingList == null || event.waitingList.list == null) {
            return false;
        }
        return event.waitingList.list.contains(deviceId);
    }

    /**
     * Checks whether the current user is the organizer of the event.
     *
     * @param event the event to check
     * @param deviceId the current user device ID
     * @return true if the current user is the organizer, otherwise false
     */
    private boolean isCurrentUserOrganizer(Event event, String deviceId) {
        if (event == null) {
            return false;
        }
        return !isBlank(deviceId) && deviceId.equals(event.organizerId);
    }

    /**
     * Checks whether a user ID exists in a list of user IDs.
     *
     * @param users the list of user IDs to search
     * @param deviceId the device ID to look for
     * @return true if the user exists in the list, otherwise false
     */
    private boolean containsUser(List<String> users, String deviceId) {
        return users != null && users.contains(deviceId);
    }

    /**
     * Checks whether a string is blank after trimming whitespace.
     *
     * @param value the string to check
     * @return true if the string is blank, otherwise false
     */
    private boolean isBlank(String value) {
        return safeString(value).trim().isEmpty();
    }

    /**
     * Returns a safe string value, replacing null with an empty string.
     *
     * @param value the string to sanitize
     * @return the original string, or an empty string if null
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /**
     * Returns a trimmed string value, or null if the value is null.
     *
     * @param value the string to normalize
     * @return the normalized string
     */
    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}
