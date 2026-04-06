package com.example.allot.controller.event;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.controller.lottery.LotteryDrawService;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.model.event.EventFormSnapshot;
import com.example.allot.model.event.EventSubmissionInput;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * Handles loading, checking, and updating an event.
 */
public class EditEventController {
    private final EventRepository eventRepository;
    private final EventFormService eventFormService;
    private final EventInputValidator eventInputValidator;
    private final LotteryDrawService lotteryDrawService;
    private final EventLocationGeocodingService geocodingService;

    /**
     * Creates a new EditEventController instance.
     *
     * @param context the context
     */
    public EditEventController(android.content.Context context) {
        this(
                new EventRepository(),
                new EventFormService(),
                new EventInputValidator(),
                new LotteryDrawService(),
                new AndroidEventLocationGeocodingService(context)
        );
    }

    /**
     * Creates a new EditEventController instance.
     *
     * @param eventRepository the event repository
     * @param eventFormService the event form service
     * @param eventInputValidator the event input validator
     * @param lotteryDrawService the lottery draw service
     * @param geocodingService the geocoding service
     */
    EditEventController(EventRepository eventRepository,
                        EventFormService eventFormService,
                        EventInputValidator eventInputValidator,
                        LotteryDrawService lotteryDrawService,
                        EventLocationGeocodingService geocodingService) {
        this.eventRepository = eventRepository;
        this.eventFormService = eventFormService;
        this.eventInputValidator = eventInputValidator;
        this.lotteryDrawService = lotteryDrawService;
        this.geocodingService = geocodingService;
    }

    /**
     * Returns the result of build fallback view model.
     *
     * @param title the title
     * @param location the location
     * @param eventDate the event date
     * @param price the price
     * @param description the description
     * @param participants the participants
     * @param registrationStart the registration start
     * @param registrationEnd the registration end
     * @return the result of this call
     */
    public EventFormData buildFallbackViewModel(String title,
                                                String location,
                                                String eventDate,
                                                String price,
                                                String description,
                                                String participants,
                                                String registrationStart,
                                                String registrationEnd) {
        return buildViewModel(
                title,
                location,
                eventDate,
                price,
                description,
                participants,
                registrationStart,
                registrationEnd
        );
    }

    /**
     * Performs load event.
     *
     * @param eventId the event id
     * @param listener the listener
     */
    public void loadEvent(String eventId, OnCompleteListener<Event> listener) {
        eventRepository.getEventById(eventId, (event, success) -> {
            if (!success || event == null) {
                listener.onComplete(null, false);
                return;
            }

            listener.onComplete(event, true);
        });
    }

    /**
     * Returns whether save enabled.
     *
     * @param currentFormData the current form data
     * @param originalSnapshot the original snapshot
     * @param isSaving whether saving
     * @param isLoading whether loading
     * @return whether save enabled
     */
    public boolean isSaveEnabled(EventFormData currentFormData,
                                 EventFormSnapshot originalSnapshot,
                                 boolean isSaving,
                                 boolean isLoading) {
        EventFormSnapshot currentSnapshot = buildSnapshot(currentFormData);
        return !isSaving
                && !isLoading
                && originalSnapshot != null
                && !originalSnapshot.equals(currentSnapshot);
    }

    /**
     * Returns the result of build snapshot.
     *
     * @param formData the form data
     * @return the result of this call
     */
    public EventFormSnapshot buildSnapshot(EventFormData formData) {
        if (formData == null) {
            return new EventFormSnapshot("", "", "", "", "", "", "", "", false, false);
        }

        return new EventFormSnapshot(
                safeValue(formData.getTitle()),
                safeValue(formData.getLocation()),
                safeValue(formData.getPrice()),
                safeValue(formData.getDescription()),
                safeValue(formData.getParticipants()),
                formattedDateValue(formData.getEventMonth(), formData.getEventDay(), formData.getEventYear()),
                formattedDateValue(
                        formData.getRegistrationStartMonth(),
                        formData.getRegistrationStartDay(),
                        formData.getRegistrationStartYear()
                ),
                formattedDateValue(
                        formData.getRegistrationEndMonth(),
                        formData.getRegistrationEndDay(),
                        formData.getRegistrationEndYear()
                ),
                formData.isGeolocationEnabled(),
                formData.isPrivateEvent()
        );
    }

    /**
     * Performs save changes.
     *
     * @param eventId the event id
     * @param formData the form data
     * @param listener the listener
     */
    public void saveChanges(String eventId,
                            EventFormData formData,
                            OnCompleteListener<AppResult<Event>> listener) {
        if (isBlank(eventId)) {
                listener.onComplete(AppResult.failure(R.string.manage_event_not_found), false);
            return;
        }

        AppResult<EventSubmissionInput> result = eventFormService.buildUpdateEventInput(formData);
        if (!result.isSuccess() || result.getData() == null) {
            listener.onComplete(AppResult.failure(eventFormService.getValidationMessageRes(result.getMessage())), false);
            return;
        }

        EventSubmissionInput input = result.getData();
        if (!eventInputValidator.isValid(input)) {
            listener.onComplete(AppResult.failure(R.string.manage_event_save_failure), false);
            return;
        }

        java.util.Map<String, Object> updates = new java.util.HashMap<>();
        updates.put("title", input.getTitle().trim());
        updates.put("location", input.getLocation().trim());
        updates.put("eventDate", input.getEventDate());
        updates.put("price", input.getPrice());
        updates.put("description", input.getDescription().trim());
        updates.put("capacity", input.getParticipants());
        updates.put("limit", input.getParticipants());
        updates.put("geoloc", input.isGeolocationEnabled());
        updates.put("registrationOpen", input.getRegistrationStart());
        updates.put("registrationDeadline", input.getRegistrationEnd());
        updates.put("visibility", input.getVisibility());
        applyResolvedCoordinates(updates, input.getLocation());

        eventRepository.updateEvent(eventId, updates, (updateResult, success) -> {
            if (!success || updateResult == null || !updateResult) {
                listener.onComplete(AppResult.failure(R.string.manage_event_save_failure), false);
                return;
            }

            eventRepository.getEventById(eventId, (event, loadSuccess) -> {
                if (!loadSuccess || event == null) {
                    listener.onComplete(AppResult.failure(R.string.manage_event_save_failure), false);
                    return;
                }

                listener.onComplete(AppResult.success(event, R.string.manage_event_save_success), true);
            });
        });
    }

    /**
     * Performs delete event.
     *
     * @param eventId the event id
     * @param organizerId the organizer id
     * @param listener the listener
     */
    public void deleteEvent(String eventId,
                            String organizerId,
                            OnCompleteListener<AppResult<Boolean>> listener) {
        if (isBlank(eventId)) {
            listener.onComplete(AppResult.failure(R.string.manage_event_not_found), false);
            return;
        }

        eventRepository.deleteEventAsOrganizer(eventId, organizerId, (result, success) -> {
            if (!success || result == null || !result) {
                listener.onComplete(AppResult.failure(R.string.manage_event_delete_failure), false);
                return;
            }

            listener.onComplete(AppResult.success(true, R.string.manage_event_delete_success), true);
        });
    }

    /**
     * Returns whether this instance should open entrants screen.
     *
     * @param event the event
     * @return whether this instance should open entrants screen
     */
    public boolean shouldOpenEntrantsScreen(Event event) {
        return event != null && lotteryDrawService.hasDrawResults(event);
    }

    /**
     * Returns the result of build view model.
     *
     * @param event the event
     * @return the result of this call
     */
    public EventFormData buildViewModel(Event event) {
        if (event == null) {
            return EventFormData.forBinding("", "", false, false, "", "", "", "", "", "", "", "", "", "", "", "");
        }

        int participantCount = event.getCapacity() > 0 ? event.getCapacity() : event.getLimit();

        return EventFormData.forBinding(
                safeValue(event.getTitle()),
                safeValue(event.getLocation()),
                event.isPrivate(),
                Boolean.TRUE.equals(event.getGeoloc()),
                monthValue(event.getEventDate()),
                dayValue(event.getEventDate()),
                yearValue(event.getEventDate()),
                event.getPrice() == null ? "" : eventFormService.formatPriceValue(event.getPrice()),
                safeValue(event.getDescription()),
                participantCount > 0 ? String.valueOf(participantCount) : "",
                monthValue(event.getRegistrationOpen()),
                dayValue(event.getRegistrationOpen()),
                yearValue(event.getRegistrationOpen()),
                monthValue(event.getRegistrationDeadline()),
                dayValue(event.getRegistrationDeadline()),
                yearValue(event.getRegistrationDeadline())
        );
    }

    /**
     * Returns the result of build view model.
     *
     * @param title the title
     * @param location the location
     * @param eventDate the event date
     * @param price the price
     * @param description the description
     * @param participants the participants
     * @param registrationStart the registration start
     * @param registrationEnd the registration end
     * @return the result of this call
     */
    private EventFormData buildViewModel(String title,
                                         String location,
                                         String eventDate,
                                         String price,
                                         String description,
                                         String participants,
                                         String registrationStart,
                                         String registrationEnd) {
        return EventFormData.forBinding(
                safeValue(title),
                safeValue(location),
                false,
                false,
                monthFromFormattedDate(eventDate),
                dayFromFormattedDate(eventDate),
                yearFromFormattedDate(eventDate),
                safeValue(price),
                safeValue(description),
                safeValue(participants),
                monthFromFormattedDate(registrationStart),
                dayFromFormattedDate(registrationStart),
                yearFromFormattedDate(registrationStart),
                monthFromFormattedDate(registrationEnd),
                dayFromFormattedDate(registrationEnd),
                yearFromFormattedDate(registrationEnd)
        );
    }

    /**
     * Returns the result of build summary date.
     *
     * @param formData the form data
     * @return the result of this call
     */
    public String buildSummaryDate(EventFormData formData) {
        if (formData == null) {
            return "";
        }

        return formattedDateValue(formData.getEventMonth(), formData.getEventDay(), formData.getEventYear());
    }

    /**
     * Returns the result of formatted date value.
     *
     * @param month the month
     * @param day the day
     * @param year the year
     * @return the result of this call
     */
    private String formattedDateValue(String month, String day, String year) {
        Date date = eventFormService.parseDate(month, day, year);
        return date == null ? "" : eventFormService.formatDate(date);
    }

    /**
     * Returns the result of month value.
     *
     * @param date the date
     * @return the result of this call
     */
    private String monthValue(Date date) {
        return formatDatePart(date, "MMM");
    }

    /**
     * Returns the result of day value.
     *
     * @param date the date
     * @return the result of this call
     */
    private String dayValue(Date date) {
        return formatDatePart(date, "d");
    }

    /**
     * Returns the result of year value.
     *
     * @param date the date
     * @return the result of this call
     */
    private String yearValue(Date date) {
        return formatDatePart(date, "yyyy");
    }

    /**
     * Returns the result of format date part.
     *
     * @param date the date
     * @param pattern the pattern
     * @return the result of this call
     */
    private String formatDatePart(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }

    /**
     * Returns the result of month from formatted date.
     *
     * @param value the value
     * @return the result of this call
     */
    private String monthFromFormattedDate(String value) {
        if (safeValue(value).length() < 3) {
            return "";
        }
        return safeValue(value).substring(0, 3);
    }

    /**
     * Returns the result of day from formatted date.
     *
     * @param value the value
     * @return the result of this call
     */
    private String dayFromFormattedDate(String value) {
        String[] parts = safeValue(value).split(" ");
        if (parts.length < 2) {
            return "";
        }
        return parts[1].replace(",", "");
    }

    /**
     * Returns the result of year from formatted date.
     *
     * @param value the value
     * @return the result of this call
     */
    private String yearFromFormattedDate(String value) {
        String[] parts = safeValue(value).split(" ");
        if (parts.length < 3) {
            return "";
        }
        return parts[2];
    }

    /**
     * Returns the result of safe value.
     *
     * @param value the value
     * @return the result of this call
     */
    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Returns whether blank.
     *
     * @param value the value
     * @return whether blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Performs apply resolved coordinates.
     *
     * @param updates the updates
     * @param location the location
     */
    private void applyResolvedCoordinates(java.util.Map<String, Object> updates, String location) {
        EventLocationCoordinates coordinates = geocodingService == null ? null : geocodingService.geocode(location);
        if (coordinates == null) {
            updates.put("eventLatitude", null);
            updates.put("eventLongitude", null);
            return;
        }

        updates.put("eventLatitude", coordinates.getLatitude());
        updates.put("eventLongitude", coordinates.getLongitude());
    }
}









