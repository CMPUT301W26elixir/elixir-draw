package com.example.allot.controller.events;

import com.example.allot.model.event.Event;
import java.util.ArrayList;
import java.util.List;
/**
 * Sorts the user's events into the sections shown by the UI.
 */
public class UserEventsSectionService {
    /**
     * Represents the different event status sections shown in the registered tab.
     */
    public enum RegisteredSection {
        INVITED,
        SELECTED,
        WAITING,
        NOT_SELECTED,
        PAST
    }

    /**
     * Groups registered events into their display sections.
     */
    public static class RegisteredSections {
        private final List<Event> invitedEvents = new ArrayList<>();
        private final List<Event> selectedEvents = new ArrayList<>();
        private final List<Event> waitingEvents = new ArrayList<>();
        private final List<Event> notSelectedEvents = new ArrayList<>();
        private final List<Event> pastEvents = new ArrayList<>();
        private final List<Event> coOrganizerInvites = new ArrayList<>();

        /**
         * Returns the invited events.
         *
         * @return the invited events
         */
        public List<Event> getInvitedEvents() {
            return invitedEvents;
        }

        /**
         * Returns the selected events.
         *
         * @return the selected events
         */
        public List<Event> getSelectedEvents() {
            return selectedEvents;
        }

        /**
         * Returns the waiting events.
         *
         * @return the waiting events
         */
        public List<Event> getWaitingEvents() {
            return waitingEvents;
        }

        /**
         * Returns the not selected events.
         *
         * @return the not selected events
         */
        public List<Event> getNotSelectedEvents() {
            return notSelectedEvents;
        }

        /**
         * Returns the past events.
         *
         * @return the past events
         */
        public List<Event> getPastEvents() {
            return pastEvents;
        }

        /**
         * Returns the co organizer invites.
         *
         * @return the co organizer invites
         */
        public List<Event> getCoOrganizerInvites() {
            return coOrganizerInvites;
        }
    }

    /**
     * Groups hosted events into ongoing and completed sections.
     */
    public static class HostedSections {
        private final List<Event> ongoingEvents = new ArrayList<>();
        private final List<Event> completedEvents = new ArrayList<>();

        /**
         * Returns the ongoing events.
         *
         * @return the ongoing events
         */
        public List<Event> getOngoingEvents() {
            return ongoingEvents;
        }

        /**
         * Returns the completed events.
         *
         * @return the completed events
         */
        public List<Event> getCompletedEvents() {
            return completedEvents;
        }
    }

    /**
     * Returns the result of group registered events.
     *
     * @param events the events
     * @param deviceId the device id
     * @return the result of this call
     */
    public RegisteredSections groupRegisteredEvents(List<Event> events, String deviceId) {
        RegisteredSections sections = new RegisteredSections();

        if (events == null) {
            return sections;
        }

        for (Event event : events) {
            if (isInvitedCoOrganizer(event, deviceId)) {
                sections.getCoOrganizerInvites().add(event);
                continue;
            }
            RegisteredSection section = classifyRegisteredEvent(event, deviceId);
            switch (section) {
                case INVITED:
                    sections.getInvitedEvents().add(event);
                    break;
                case SELECTED:
                    sections.getSelectedEvents().add(event);
                    break;
                case WAITING:
                    sections.getWaitingEvents().add(event);
                    break;
                case NOT_SELECTED:
                    sections.getNotSelectedEvents().add(event);
                    break;
                case PAST:
                default:
                    sections.getPastEvents().add(event);
                    break;
            }
        }

        return sections;
    }

    /**
     * Returns the result of group hosted events.
     *
     * @param events the events
     * @return the result of this call
     */
    public HostedSections groupHostedEvents(List<Event> events) {
        HostedSections sections = new HostedSections();

        if (events == null) {
            return sections;
        }

        for (Event event : events) {
            if (isPastEvent(event)) {
                sections.getCompletedEvents().add(event);
            } else {
                sections.getOngoingEvents().add(event);
            }
        }

        return sections;
    }

    /**
     * Returns the result of classify registered event.
     *
     * @param event the event
     * @param deviceId the device id
     * @return the result of this call
     */
    public RegisteredSection classifyRegisteredEvent(Event event, String deviceId) {
        if (isPastEvent(event)) {
            return RegisteredSection.PAST;
        }

        if (isInvited(event, deviceId)) {
            return RegisteredSection.INVITED;
        }

        if (isSelected(event, deviceId)) {
            return RegisteredSection.SELECTED;
        }

        if (isWaiting(event)) {
            return RegisteredSection.WAITING;
        }

        return RegisteredSection.NOT_SELECTED;
    }

    /**
     * Returns whether invited.
     *
     * @param event the event
     * @param deviceId the device id
     * @return whether invited
     */
    private boolean isInvited(Event event, String deviceId) {
        if (event == null || !event.isPrivate()) {
            return false;
        }
        if (deviceId == null || deviceId.trim().isEmpty()) {
            return false;
        }
        return event.isInvited(deviceId)
                && !containsUser(event.getWaitingList() == null ? null : event.getWaitingList().list, deviceId);
    }

    /**
     * Returns whether past event.
     *
     * @param event the event
     * @return whether past event
     */
    public boolean isPastEvent(Event event) {
        return event != null
                && event.getEventDate() != null
                && event.getEventDate().getTime() < System.currentTimeMillis();
    }

    /**
     * Returns whether selected.
     *
     * @param event the event
     * @param deviceId the device id
     * @return whether selected
     */
    public boolean isSelected(Event event, String deviceId) {
        return containsUser(event == null ? null : event.getEnrolled(), deviceId)
                || containsUser(event == null ? null : event.getChosen(), deviceId)
                || containsUser(event != null && event.getWaitingList() != null ? event.getWaitingList().chosen : null, deviceId);
    }

    /**
     * Returns whether invited co organizer.
     *
     * @param event the event
     * @param deviceId the device id
     * @return whether invited co organizer
     */
    public boolean isInvitedCoOrganizer(Event event, String deviceId) {
        return event != null
                && deviceId != null
                && event.getCoOrganizerInvites() != null
                && event.getCoOrganizerInvites().contains(deviceId)
                && (event.getCoOrganizers() == null || !event.getCoOrganizers().contains(deviceId));
    }

    /**
     * Returns whether waiting.
     *
     * @param event the event
     * @return whether waiting
     */
    public boolean isWaiting(Event event) {
        if (event == null) {
            return false;
        }

        if (!isDeadlinePassed(event)) {
            return true;
        }

        return !hasPublishedSelectionResults(event);
    }

    /**
     * Returns whether deadline passed.
     *
     * @param event the event
     * @return whether deadline passed
     */
    public boolean isDeadlinePassed(Event event) {
        return event != null
                && event.getRegistrationDeadline() != null
                && event.getRegistrationDeadline().getTime() <= System.currentTimeMillis();
    }

    /**
     * Returns whether this instance has published selection results.
     *
     * @param event the event
     * @return whether this instance has published selection results
     */
    public boolean hasPublishedSelectionResults(Event event) {
        return (event != null && event.getChosen() != null && !event.getChosen().isEmpty())
                || (event != null && event.getEnrolled() != null && !event.getEnrolled().isEmpty())
                || (event != null && event.getCancelled() != null && !event.getCancelled().isEmpty())
                || (event != null && event.getNotEnrolled() != null && !event.getNotEnrolled().isEmpty())
                || (event != null && event.getWaitingList() != null && event.getWaitingList().chosen != null && !event.getWaitingList().chosen.isEmpty());
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
}









