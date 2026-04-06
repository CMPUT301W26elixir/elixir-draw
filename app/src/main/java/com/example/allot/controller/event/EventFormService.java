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

    /**
     * Creates a new EventFormService instance.
     */
    public EventFormService() {
        dateFormat.setLenient(false);
    }

    /**
     * Returns the result of build create event input.
     *
     * @param formData the form data
     * @return the result of this call
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
     * Returns the result of build update event input.
     *
     * @param formData the form data
     * @return the result of this call
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
     * Returns whether date input incomplete.
     *
     * @param month the month
     * @param day the day
     * @param year the year
     * @return whether date input incomplete
     */
    private boolean isDateInputIncomplete(String month, String day, String year) {
        return isBlank(month) || isBlank(day) || isBlank(year);
    }

    /**
     * Returns the result of parse date.
     *
     * @param month the month
     * @param day the day
     * @param year the year
     * @return the result of this call
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
     * Returns the result of parse price.
     *
     * @param value the value
     * @return the result of this call
     */
    public Double parsePrice(String value) {
        try {
            return Double.parseDouble(safeString(value).replace("$", "").trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Returns the result of parse positive int.
     *
     * @param value the value
     * @return the result of this call
     */
    public Integer parsePositiveInt(String value) {
        try {
            return Integer.parseInt(safeString(value).trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    /**
     * Returns the result of format date.
     *
     * @param date the date
     * @return the result of this call
     */
    public String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        return dateFormat.format(date);
    }

    /**
     * Returns the result of format price value.
     *
     * @param price the price
     * @return the result of this call
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
     * Returns the validation message res.
     *
     * @param errorCode the error code
     * @return the validation message res
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

    /**
     * Returns the result of validate form.
     *
     * @param formData the form data
     * @return the result of this call
     */
    private ValidationResult validateForm(EventFormData formData) {
        if (formData == null) {
            return ValidationResult.failure(ERROR_REQUIRED);
        }

        /**
         * Returns whether get Registration End Year.
         */
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
     * Returns the result of safe string.
     *
     * @param value the value
     * @return the result of this call
     */
    private String safeString(String value) {
        return value == null ? "" : value;
    }

    /**
     * Returns the result of visibility from form.
     *
     * @param formData the form data
     * @return the result of this call
     */
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

        /**
         * Creates a new ValidationResult instance.
         *
         * @param success the success
         * @param message the message
         * @param eventDate the event date
         * @param registrationStart the registration start
         * @param registrationEnd the registration end
         * @param price the price
         * @param participants the participants
         */
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

        /**
         * Returns the result of failure.
         *
         * @param message the message
         * @return the result of this call
         */
        private static ValidationResult failure(String message) {
            return new ValidationResult(false, message, null, null, null, null, null);
        }

        /**
         * Returns the result of success.
         *
         * @param eventDate the event date
         * @param registrationStart the registration start
         * @param registrationEnd the registration end
         * @param price the price
         * @param participants the participants
         * @return the result of this call
         */
        private static ValidationResult success(Date eventDate,
                                                Date registrationStart,
                                                Date registrationEnd,
                                                Double price,
                                                Integer participants) {
            return new ValidationResult(true, null, eventDate, registrationStart, registrationEnd, price, participants);
        }

        /**
         * Returns whether failure.
         *
         * @return whether failure
         */
        private boolean isFailure() {
            return !success;
        }

        /**
         * Returns the message.
         *
         * @return the message
         */
        private String getMessage() {
            return message;
        }

        /**
         * Returns the event date.
         *
         * @return the event date
         */
        private Date getEventDate() {
            return eventDate;
        }

        /**
         * Returns the registration start.
         *
         * @return the registration start
         */
        private Date getRegistrationStart() {
            return registrationStart;
        }

        /**
         * Returns the registration end.
         *
         * @return the registration end
         */
        private Date getRegistrationEnd() {
            return registrationEnd;
        }

        /**
         * Returns the price.
         *
         * @return the price
         */
        private Double getPrice() {
            return price;
        }

        /**
         * Returns the participants.
         *
         * @return the participants
         */
        private Integer getParticipants() {
            return participants;
        }
    }
}









