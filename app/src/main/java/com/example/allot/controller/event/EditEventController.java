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

    public EditEventController(android.content.Context context) {
        this(
                new EventRepository(),
                new EventFormService(),
                new EventInputValidator(),
                new LotteryDrawService(),
                new AndroidEventLocationGeocodingService(context)
        );
    }

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
     * Builds the fallback form values shown before Firestore finishes loading.
     *
     * @param title the fallback title
     * @param location the fallback location
     * @param eventDate the fallback event date
     * @param price the fallback price
     * @param description the fallback description
     * @param participants the fallback participant count
     * @param registrationStart the fallback registration start
     * @param registrationEnd the fallback registration end
     * @return the fallback form values
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
     * Loads the latest event from Firestore.
     *
     * @param eventId the event being edited
     * @param listener the listener that receives the loaded event
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
     * Returns whether save should be enabled for the current form.
     *
     * @param currentFormData the form values shown in the view
     * @param originalSnapshot the original loaded snapshot
     * @param isSaving whether a save is in progress
     * @param isLoading whether a load is in progress
     * @return true when save should be enabled
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
     * Builds a snapshot from the current form values.
     *
     * @param formData the current form values
     * @return the form snapshot used for dirty-state tracking
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
     * Validates and saves the current event form.
     *
     * @param eventId the event being edited
     * @param formData the current form values
     * @param listener the listener that receives the save result
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
     * Determines whether the entrants screen should open instead of the draw screen.
     *
     * @param event the current event
     * @return true when draw results already exist
     */
    public boolean shouldOpenEntrantsScreen(Event event) {
        return event != null && lotteryDrawService.hasDrawResults(event);
    }

    /**
     * Turns a saved event into form values for the edit screen.
     *
     * @param event the event being edited
     * @return form data ready to bind into the UI
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
     * Builds the short date text shown while the user edits an event.
     *
     * @param formData the current form values
     * @return a readable summary date, or an empty string when the date is incomplete
     */
    public String buildSummaryDate(EventFormData formData) {
        if (formData == null) {
            return "";
        }

        return formattedDateValue(formData.getEventMonth(), formData.getEventDay(), formData.getEventYear());
    }

    private String formattedDateValue(String month, String day, String year) {
        Date date = eventFormService.parseDate(month, day, year);
        return date == null ? "" : eventFormService.formatDate(date);
    }

    private String monthValue(Date date) {
        return formatDatePart(date, "MMM");
    }

    private String dayValue(Date date) {
        return formatDatePart(date, "d");
    }

    private String yearValue(Date date) {
        return formatDatePart(date, "yyyy");
    }

    private String formatDatePart(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }

    private String monthFromFormattedDate(String value) {
        if (safeValue(value).length() < 3) {
            return "";
        }
        return safeValue(value).substring(0, 3);
    }

    private String dayFromFormattedDate(String value) {
        String[] parts = safeValue(value).split(" ");
        if (parts.length < 2) {
            return "";
        }
        return parts[1].replace(",", "");
    }

    private String yearFromFormattedDate(String value) {
        String[] parts = safeValue(value).split(" ");
        if (parts.length < 3) {
            return "";
        }
        return parts[2];
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

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









