package com.example.allot.controller.event;

import android.content.Context;
import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.common.OnCompleteListener;
import com.example.allot.data.DeviceSessionManager;
import com.example.allot.data.EventRepository;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.model.event.EventSubmissionInput;
import java.util.UUID;
/**
 * Handles checking and saving for the create-event flow.
 */
public class CreateEventController {
    private final EventRepository eventRepository;
    private final EventFormService eventFormService;
    private final EventInputValidator eventInputValidator;
    private final DeviceSessionManager deviceSessionManager;
    private final EventLocationGeocodingService geocodingService;

    public CreateEventController(Context context) {
        this(
                new EventRepository(),
                new EventFormService(),
                new EventInputValidator(),
                new DeviceSessionManager(context),
                new AndroidEventLocationGeocodingService(context)
        );
    }

    CreateEventController(EventRepository eventRepository,
                          EventFormService eventFormService,
                          EventInputValidator eventInputValidator,
                          DeviceSessionManager deviceSessionManager,
                          EventLocationGeocodingService geocodingService) {
        this.eventRepository = eventRepository;
        this.eventFormService = eventFormService;
        this.eventInputValidator = eventInputValidator;
        this.deviceSessionManager = deviceSessionManager;
        this.geocodingService = geocodingService;
    }

    /**
     * Validates and submits the current create-event form.
     *
     * @param formData the form values collected by the view
     * @param listener the listener that receives the result
     */
    public void submitEvent(EventFormData formData, OnCompleteListener<AppResult<Event>> listener) {
        AppResult<EventSubmissionInput> result = eventFormService.buildCreateEventInput(formData);
        if (!result.isSuccess() || result.getData() == null) {
            listener.onComplete(AppResult.failure(eventFormService.getValidationMessageRes(result.getMessage())), false);
            return;
        }

        EventSubmissionInput input = result.getData();
        if (!eventInputValidator.isValid(input)) {
            listener.onComplete(AppResult.failure(R.string.create_event_save_failure), false);
            return;
        }

        Event event = new Event(UUID.randomUUID().toString(), deviceSessionManager.getCurrentDeviceId(), input.getTitle(), input.getParticipants());
        event.setTitle(input.getTitle().trim());
        event.setLocation(input.getLocation().trim());
        event.setGeoloc(input.isGeolocationEnabled());
        event.setEventDate(input.getEventDate());
        event.setPrice(input.getPrice());
        event.setDescription(input.getDescription().trim());
        event.setCapacity(input.getParticipants());
        event.setLimit(input.getParticipants());
        event.setRegistrationOpen(input.getRegistrationStart());
        event.setRegistrationDeadline(input.getRegistrationEnd());
        event.setStatus("open");
        event.setCategory(normalizeNullable(input.getCategory()));
        event.setVisibility(input.getVisibility());
        applyResolvedCoordinates(event, input.getLocation());

        eventRepository.createNewEventForUser(event, deviceSessionManager.getCurrentDeviceId(), (resultValue, success) -> {
            if (!success || resultValue == null || !resultValue) {
                listener.onComplete(AppResult.failure(R.string.create_event_save_failure), false);
                return;
            }

            listener.onComplete(AppResult.success(event, R.string.create_event_save_success), true);
        });
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }

    private void applyResolvedCoordinates(Event event, String location) {
        EventLocationCoordinates coordinates = geocodingService == null ? null : geocodingService.geocode(location);
        if (coordinates == null) {
            event.setEventLatitude(null);
            event.setEventLongitude(null);
            return;
        }

        event.setEventLatitude(coordinates.getLatitude());
        event.setEventLongitude(coordinates.getLongitude());
    }
}









