package com.example.allot.controller.events;

import com.example.allot.model.event.Event;
import java.util.ArrayList;
import java.util.List;
public class UserEventsSectionService {
    /**
     * Represents the different event status sections shown in the registered tab.
     */
    public enum RegisteredSection {
        SELECTED,
        WAITING,
        NOT_SELECTED,
        PAST
    }

    /**
     * Groups registered events into their display sections.
     */
    public static class RegisteredSections {
        private final List<Event> selectedEvents = new ArrayList<>();
        private final List<Event> waitingEvents = new ArrayList<>();
        private final List<Event> notSelectedEvents = new ArrayList<>();
        private final List<Event> pastEvents = new ArrayList<>();

        public List<Event> getSelectedEvents() {
            return selectedEvents;
        }

        public List<Event> getWaitingEvents() {
            return waitingEvents;
        }

        public List<Event> getNotSelectedEvents() {
            return notSelectedEvents;
        }

        public List<Event> getPastEvents() {
            return pastEvents;
        }
    }

    /**
     * Groups hosted events into ongoing and completed sections.
     */
    public static class HostedSections {
        private final List<Event> ongoingEvents = new ArrayList<>();
        private final List<Event> completedEvents = new ArrayList<>();

        public List<Event> getOngoingEvents() {
            return ongoingEvents;
        }

        public List<Event> getCompletedEvents() {
            return completedEvents;
        }
    }

    /**
     * Classifies and groups registered events into their corresponding status sections:
     * selected, waiting, not selected, and past.
     *
     * @param events the list of registered events to categorize and display
     * @param deviceId the current user's device ID
     * @return the grouped registered sections
     */
    public RegisteredSections groupRegisteredEvents(List<Event> events, String deviceId) {
        RegisteredSections sections = new RegisteredSections();

        if (events == null) {
            return sections;
        }

        for (Event event : events) {
            RegisteredSection section = classifyRegisteredEvent(event, deviceId);
            switch (section) {
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
     * Splits hosted events into ongoing and completed sections and displays them.
     *
     * @param events the list of hosted events to bind
     * @return the grouped hosted sections
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
     * Determines which registered section an event belongs to.
     *
     * @param event the event to classify
     * @param deviceId the current user's device ID
     * @return the matching section for the event
     */
    public RegisteredSection classifyRegisteredEvent(Event event, String deviceId) {
        if (isPastEvent(event)) {
            return RegisteredSection.PAST;
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
     * Determines whether an event has already occurred.
     *
     * @param event the event to evaluate
     * @return true if the event date is in the past; false otherwise
     */
    public boolean isPastEvent(Event event) {
        return event != null
                && event.getEventDate() != null
                && event.getEventDate().getTime() < System.currentTimeMillis();
    }

    /**
     * Determines whether the current user has been selected or enrolled in an event.
     *
     * <p>A user is considered selected if their device ID appears in the enrolled list,
     * chosen list, or waiting list chosen list.
     *
     * @param event the event to check
     * @param deviceId the current user's device ID
     * @return true if the current user is selected; false otherwise
     */
    public boolean isSelected(Event event, String deviceId) {
        return containsUser(event == null ? null : event.getEnrolled(), deviceId)
                || containsUser(event == null ? null : event.getChosen(), deviceId)
                || containsUser(event != null && event.getWaitingList() != null ? event.getWaitingList().chosen : null, deviceId);
    }

    /**
     * Determines whether the event should be shown in the waiting section.
     *
     * <p>An event is considered waiting if the registration deadline has not passed yet,
     * or if the deadline has passed but selection results have not yet been published.
     *
     * @param event the event to evaluate
     * @return true if the event is still waiting for selection results; false otherwise
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
     * Determines whether an event's registration deadline has passed.
     *
     * @param event the event to check
     * @return true if the registration deadline is in the past or exactly now; false otherwise
     */
    public boolean isDeadlinePassed(Event event) {
        return event != null
                && event.getRegistrationDeadline() != null
                && event.getRegistrationDeadline().getTime() <= System.currentTimeMillis();
    }

    /**
     * Determines whether any selection-related result lists have been published for the event.
     *
     * @param event the event to inspect
     * @return true if at least one result list contains data; false otherwise
     */
    public boolean hasPublishedSelectionResults(Event event) {
        return (event != null && event.getChosen() != null && !event.getChosen().isEmpty())
                || (event != null && event.getEnrolled() != null && !event.getEnrolled().isEmpty())
                || (event != null && event.getCancelled() != null && !event.getCancelled().isEmpty())
                || (event != null && event.getNotEnrolled() != null && !event.getNotEnrolled().isEmpty())
                || (event != null && event.getWaitingList() != null && event.getWaitingList().chosen != null && !event.getWaitingList().chosen.isEmpty());
    }

    /**
     * Checks whether a given device ID appears in a list of users.
     *
     * @param users the list of user device IDs
     * @param deviceId the device ID to search for
     * @return true if the device ID is in the list; false otherwise
     */
    private boolean containsUser(List<String> users, String deviceId) {
        return users != null && users.contains(deviceId);
    }
}









