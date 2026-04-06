package com.example.allot.controller.event;

import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventActionState;
import java.util.List;
/**
 * Picks the action state the current user should see for an event.
 */
public class EventActionStateFactory {
    /**
     * Returns the result of create.
     *
     * @param event the event
     * @param deviceId the device id
     * @return the result of this call
     */
    public EventActionState create(Event event, String deviceId) {
        if (isCurrentUserOrganizer(event, deviceId)) {
            return new EventActionState(event, EventActionState.ActionType.MANAGE, false, true, true, null);
        }

        if (isCurrentUserEnrolled(event, deviceId)) {
            return new EventActionState(event, EventActionState.ActionType.ENROLLED, false, false, true, null);
        }

        if (isCurrentUserSelected(event, deviceId)) {
            return new EventActionState(event, EventActionState.ActionType.OFFER, false, true, true, null);
        }

        if (shouldShowReplacementState(event, deviceId)) {
            return new EventActionState(
                    event,
                    EventActionState.ActionType.NOT_SELECTED_REPLACEMENT,
                    false,
                    false,
                    false,
                    "You were not selected in the main draw, but you may still receive an offer if spots open up."
            );
        }

        if (shouldShowFinalizedNotSelectedState(event, deviceId)) {
            return new EventActionState(
                    event,
                    EventActionState.ActionType.NOT_SELECTED_FINAL,
                    false,
                    false,
                    false,
                    "Registration is finalized and you were not selected for this event."
            );
        }

        boolean isOnWaitingList = isCurrentUserOnWaitingList(event, deviceId);
        if (event != null && event.isPrivate() && !isOnWaitingList) {
            if (event.isInvited(deviceId)) {
                return new EventActionState(
                        event,
                        EventActionState.ActionType.INVITED,
                        false,
                        true,
                        false,
                        "You have been invited to this private event."
                );
            }

            return new EventActionState(
                    event,
                    EventActionState.ActionType.INVITE_ONLY,
                    false,
                    false,
                    false,
                    "This is a private event. You need an invite to join."
            );
        }

        return new EventActionState(
                event,
                isOnWaitingList ? EventActionState.ActionType.LEAVE_WAITLIST : EventActionState.ActionType.JOIN_WAITLIST,
                isOnWaitingList,
                true,
                true,
                null
        );
    }

    /**
     * Returns whether current user enrolled.
     *
     * @param event the event
     * @param deviceId the device id
     * @return whether current user enrolled
     */
    private boolean isCurrentUserEnrolled(Event event, String deviceId) {
        return containsUser(event == null ? null : event.getEnrolled(), deviceId);
    }

    /**
     * Returns whether current user selected.
     *
     * @param event the event
     * @param deviceId the device id
     * @return whether current user selected
     */
    private boolean isCurrentUserSelected(Event event, String deviceId) {
        return containsUser(event == null ? null : event.getChosen(), deviceId)
                || containsUser(event != null && event.getWaitingList() != null ? event.getWaitingList().chosen : null, deviceId);
    }

    /**
     * Returns whether this instance should show replacement state.
     *
     * @param event the event
     * @param deviceId the device id
     * @return whether this instance should show replacement state
     */
    private boolean shouldShowReplacementState(Event event, String deviceId) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event, deviceId)
                && !isCurrentUserSelected(event, deviceId)
                && !isCurrentUserEnrolled(event, deviceId)
                && !"finalized".equalsIgnoreCase(normalizeNullable(event.getStatus()));
    }

    /**
     * Returns whether this instance should show finalized not selected state.
     *
     * @param event the event
     * @param deviceId the device id
     * @return whether this instance should show finalized not selected state
     */
    private boolean shouldShowFinalizedNotSelectedState(Event event, String deviceId) {
        return hasPublishedSelectionResults(event)
                && isCurrentUserOnWaitingList(event, deviceId)
                && !isCurrentUserSelected(event, deviceId)
                && !isCurrentUserEnrolled(event, deviceId)
                && "finalized".equalsIgnoreCase(normalizeNullable(event.getStatus()));
    }

    /**
     * Returns whether this instance has published selection results.
     *
     * @param event the event
     * @return whether this instance has published selection results
     */
    private boolean hasPublishedSelectionResults(Event event) {
        return (event != null && event.getChosen() != null && !event.getChosen().isEmpty())
                || (event != null && event.getEnrolled() != null && !event.getEnrolled().isEmpty())
                || (event != null && event.getCancelled() != null && !event.getCancelled().isEmpty())
                || (event != null && event.getNotEnrolled() != null && !event.getNotEnrolled().isEmpty())
                || (event != null && event.getWaitingList() != null && event.getWaitingList().chosen != null && !event.getWaitingList().chosen.isEmpty());
    }

    /**
     * Returns whether current user on waiting list.
     *
     * @param event the event
     * @param deviceId the device id
     * @return whether current user on waiting list
     */
    private boolean isCurrentUserOnWaitingList(Event event, String deviceId) {
        if (event == null || event.getWaitingList() == null || event.getWaitingList().list == null) {
            return false;
        }
        return event.getWaitingList().list.contains(deviceId);
    }

    /**
     * Returns whether current user organizer.
     *
     * @param event the event
     * @param deviceId the device id
     * @return whether current user organizer
     */
    private boolean isCurrentUserOrganizer(Event event, String deviceId) {
        if (event == null) {
            return false;
        }
        if (isBlank(deviceId)) {
            return false;
        }
        if (deviceId.equals(event.getOrganizerId())) {
            return true;
        }
        return event.getCoOrganizers() != null && event.getCoOrganizers().contains(deviceId);
    }

    /**
     * Returns the result of contains user.
     *
     * @param users the users
     * @param deviceId the device id
     * @return the result of this call
     */
    private boolean containsUser(List<String> users, String deviceId) {
        return users != null && users.contains(deviceId);
    }

    /**
     * Returns whether blank.
     *
     * @param value the value
     * @return whether blank
     */
    private boolean isBlank(String value) {
        return safeString(value).trim().isEmpty();
    }

    /**
     * Returns the result of safe string.
     *
     * @param value the value
     * @return the result of this call
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /**
     * Returns the result of normalize nullable.
     *
     * @param value the value
     * @return the result of this call
     */
    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }
}









