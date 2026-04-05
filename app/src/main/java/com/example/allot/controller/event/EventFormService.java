package com.example.allot.controller.event;

import com.example.allot.R;
import com.example.allot.common.AppResult;
import com.example.allot.model.event.EventFormData;
import com.example.allot.model.event.EventSubmissionInput;
import com.example.allot.model.event.Event;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
/**
 * Reads and formats event form values for create and edit screens.
 */
public class EventFormService {
    public static final String ERROR_REQUIRED = "required";
    public static final String ERROR_DATE = "date";
    public static final String ERROR_PRICE = "price";
    public static final String ERROR_PARTICIPANTS = "participants";
    public static final String ERROR_ORDER = "order";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public EventFormService() {
        dateFormat.setLenient(false);
    }

    /**
     * Checks create-event form values and turns them into input data.
     *
     * @param formData the raw form values entered by the user
     * @return a good input result or a failed check result
     */
    public AppResult<EventSubmissionInput> buildCreateEventInput(EventFormData formData) {
        ValidationResult validationResult = validateForm(formData);
        if (validationResult.isFailure()) {
            return AppResult.failure(validationResult.getMessage());
        }

        return AppResult.success(new EventSubmissionInput(
                formData.getTitle(),
                formData.getLocation(),
                formData.isGeolocationEnabled(),
                validationResult.getEventDate(),
                validationResult.getPrice(),
                formData.getDescription(),
                validationResult.getParticipants(),
                validationResult.getRegistrationStart(),
                validationResult.getRegistrationEnd(),
                null,
                visibilityFromForm(formData)
        ));
    }

    /**
     * Checks edit-event form values and turns them into input data.
     *
     * @param formData the raw form values entered by the user
     * @return a good input result or a failed check result
     */
    public AppResult<EventSubmissionInput> buildUpdateEventInput(EventFormData formData) {
        ValidationResult validationResult = validateForm(formData);
        if (validationResult.isFailure()) {
            return AppResult.failure(validationResult.getMessage());
        }

        return AppResult.success(new EventSubmissionInput(
                formData.getTitle(),
                formData.getLocation(),
                formData.isGeolocationEnabled(),
                validationResult.getEventDate(),
                validationResult.getPrice(),
                formData.getDescription(),
                validationResult.getParticipants(),
                validationResult.getRegistrationStart(),
                validationResult.getRegistrationEnd(),
                null,
                visibilityFromForm(formData)
        ));
    }

    /**
     * Checks whether all parts of a date input have been filled in.
     *
     * @param month the selected month
     * @param day the entered day
     * @param year the entered year
     * @return true if the date input is complete, otherwise false
     */
    private boolean isDateInputIncomplete(String month, String day, String year) {
        return isBlank(month) || isBlank(day) || isBlank(year);
    }

    /**
     * Parses a date from the provided month, day, and year fields.
     *
     * @param month the selected month
     * @param day the entered day
     * @param year the entered year
     * @return the parsed date, or null if parsing fails
     */
    public Date parseDate(String month, String day, String year) {
        if (isDateInputIncomplete(month, day, year)) {
            return null;
        }

        try {
            return dateFormat.parse(month + " " + Integer.parseInt(day) + ", " + Integer.parseInt(year));
        } catch (ParseException | NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Parses a price value from text.
     *
     * @param value the price text to parse
     * @return the parsed price, or null if invalid
     */
    public Double parsePrice(String value) {
        try {
            return Double.parseDouble(safeString(value).replace("$", "").trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Parses a positive integer from text.
     *
     * @param value the text to parse
     * @return the parsed integer, or null if invalid
     */
    public Integer parsePositiveInt(String value) {
        try {
            return Integer.parseInt(safeString(value).trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Formats a date using the activity's date format.
     *
     * @param date the date to format
     * @return the formatted date string, or null if the date is null
     */
    public String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return dateFormat.format(date);
    }

    /**
     * Formats a price value for display in the form.
     *
     * @param price the price to format
     * @return the formatted price string
     */
    public String formatPriceValue(Double price) {
        if (price == null) {
            return "";
        }
        if (Math.rint(price) == price) {
            return String.format(Locale.getDefault(), "%.0f", price);
        }
        return String.format(Locale.getDefault(), "%.2f", price);
    }

    /**
     * Maps a validation error code to the message shown to the user.
     *
     * @param errorCode the validation code returned by this service
     * @return the matching string resource
     */
    public int getValidationMessageRes(String errorCode) {
        if (ERROR_DATE.equals(errorCode)) {
            return R.string.create_event_validation_date;
        }
        if (ERROR_PRICE.equals(errorCode)) {
            return R.string.create_event_validation_price;
        }
        if (ERROR_PARTICIPANTS.equals(errorCode)) {
            return R.string.create_event_validation_participants;
        }
        if (ERROR_ORDER.equals(errorCode)) {
            return R.string.create_event_validation_order;
        }
        return R.string.create_event_validation_required;
    }

    private ValidationResult validateForm(EventFormData formData) {
        if (formData == null) {
            return ValidationResult.failure(ERROR_REQUIRED);
        }

        if (isBlank(formData.getTitle())
                || isBlank(formData.getLocation())
                || isBlank(formData.getPrice())
                || isBlank(formData.getDescription())
                || isBlank(formData.getParticipants())
                || isDateInputIncomplete(formData.getEventMonth(), formData.getEventDay(), formData.getEventYear())
                || isDateInputIncomplete(formData.getRegistrationStartMonth(), formData.getRegistrationStartDay(), formData.getRegistrationStartYear())
                || isDateInputIncomplete(formData.getRegistrationEndMonth(), formData.getRegistrationEndDay(), formData.getRegistrationEndYear())) {
            return ValidationResult.failure(ERROR_REQUIRED);
        }

        Date eventDate = parseDate(formData.getEventMonth(), formData.getEventDay(), formData.getEventYear());
        Date registrationStart = parseDate(formData.getRegistrationStartMonth(), formData.getRegistrationStartDay(), formData.getRegistrationStartYear());
        Date registrationEnd = parseDate(formData.getRegistrationEndMonth(), formData.getRegistrationEndDay(), formData.getRegistrationEndYear());
        if (eventDate == null || registrationStart == null || registrationEnd == null) {
            return ValidationResult.failure(ERROR_DATE);
        }

        Double price = parsePrice(formData.getPrice());
        if (price == null || price < 0) {
            return ValidationResult.failure(ERROR_PRICE);
        }

        Integer participants = parsePositiveInt(formData.getParticipants());
        if (participants == null || participants <= 0) {
            return ValidationResult.failure(ERROR_PARTICIPANTS);
        }

        if (registrationEnd.before(registrationStart) || eventDate.before(registrationEnd)) {
            return ValidationResult.failure(ERROR_ORDER);
        }

        return ValidationResult.success(eventDate, registrationStart, registrationEnd, price, participants);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }

    private String visibilityFromForm(EventFormData formData) {
        if (formData != null && formData.isPrivateEvent()) {
            return Event.VISIBILITY_PRIVATE;
        }
        return Event.VISIBILITY_PUBLIC;
    }

    private static class ValidationResult {
        private final boolean success;
        private final String message;
        private final Date eventDate;
        private final Date registrationStart;
        private final Date registrationEnd;
        private final Double price;
        private final Integer participants;

        private ValidationResult(boolean success,
                                 String message,
                                 Date eventDate,
                                 Date registrationStart,
                                 Date registrationEnd,
                                 Double price,
                                 Integer participants) {
            this.success = success;
            this.message = message;
            this.eventDate = eventDate;
            this.registrationStart = registrationStart;
            this.registrationEnd = registrationEnd;
            this.price = price;
            this.participants = participants;
        }

        private static ValidationResult failure(String message) {
            return new ValidationResult(false, message, null, null, null, null, null);
        }

        private static ValidationResult success(Date eventDate,
                                                Date registrationStart,
                                                Date registrationEnd,
                                                Double price,
                                                Integer participants) {
            return new ValidationResult(true, null, eventDate, registrationStart, registrationEnd, price, participants);
        }

        private boolean isFailure() {
            return !success;
        }

        private String getMessage() {
            return message;
        }

        private Date getEventDate() {
            return eventDate;
        }

        private Date getRegistrationStart() {
            return registrationStart;
        }

        private Date getRegistrationEnd() {
            return registrationEnd;
        }

        private Double getPrice() {
            return price;
        }

        private Integer getParticipants() {
            return participants;
        }
    }
}









