package com.example.allot.controller.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.allot.common.AppResult;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.model.event.EventSubmissionInput;
import org.junit.Before;
import org.junit.Test;
public class EventFormServiceTest {
    private EventFormService service;

    @Before
    public void setUp() {
        service = new EventFormService();
    }

    @Test
    public void buildCreateEventInput_returnsValidInputForGoodForm() {
        AppResult<EventSubmissionInput> result = service.buildCreateEventInput(buildValidFormData());

        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("Sample Event", result.getData().getTitle());
        assertEquals(Integer.valueOf(25), result.getData().getParticipants());
    }

    @Test
    public void buildUpdateEventInput_failsWhenRequiredFieldsMissing() {
        EventFormData formData = new EventFormData(
                "",
                "Location",
                false,
                false,
                "Jan",
                "5",
                "2027",
                "10",
                "Description",
                "5",
                "Jan",
                "1",
                "2027",
                "Jan",
                "2",
                "2027"
        );

        AppResult<EventSubmissionInput> result = service.buildUpdateEventInput(formData);

        assertEquals(EventFormService.ERROR_REQUIRED, result.getMessage());
    }

    @Test
    public void buildUpdateEventInput_failsForInvalidDate() {
        EventFormData formData = new EventFormData(
                "Sample Event",
                "Location",
                false,
                false,
                "Jan",
                "40",
                "2027",
                "10",
                "Description",
                "5",
                "Jan",
                "1",
                "2027",
                "Jan",
                "2",
                "2027"
        );

        AppResult<EventSubmissionInput> result = service.buildUpdateEventInput(formData);

        assertEquals(EventFormService.ERROR_DATE, result.getMessage());
    }

    @Test
    public void buildUpdateEventInput_failsForInvalidPrice() {
        EventFormData formData = new EventFormData(
                "Sample Event",
                "Location",
                false,
                false,
                "Jan",
                "5",
                "2027",
                "abc",
                "Description",
                "5",
                "Jan",
                "1",
                "2027",
                "Jan",
                "2",
                "2027"
        );

        AppResult<EventSubmissionInput> result = service.buildUpdateEventInput(formData);

        assertEquals(EventFormService.ERROR_PRICE, result.getMessage());
    }

    @Test
    public void buildUpdateEventInput_failsForInvalidParticipants() {
        EventFormData formData = new EventFormData(
                "Sample Event",
                "Location",
                false,
                false,
                "Jan",
                "5",
                "2027",
                "10",
                "Description",
                "0",
                "Jan",
                "1",
                "2027",
                "Jan",
                "2",
                "2027"
        );

        AppResult<EventSubmissionInput> result = service.buildUpdateEventInput(formData);

        assertEquals(EventFormService.ERROR_PARTICIPANTS, result.getMessage());
    }

    @Test
    public void buildUpdateEventInput_failsForInvalidDateOrdering() {
        EventFormData formData = new EventFormData(
                "Sample Event",
                "Location",
                false,
                false,
                "Jan",
                "1",
                "2027",
                "10",
                "Description",
                "5",
                "Jan",
                "5",
                "2027",
                "Jan",
                "6",
                "2027"
        );

        AppResult<EventSubmissionInput> result = service.buildUpdateEventInput(formData);

        assertEquals(EventFormService.ERROR_ORDER, result.getMessage());
    }

    private EventFormData buildValidFormData() {
        return new EventFormData(
                "Sample Event",
                "Location",
                false,
                true,
                "Jan",
                "5",
                "2027",
                "10",
                "Description",
                "25",
                "Jan",
                "1",
                "2027",
                "Jan",
                "2",
                "2027"
        );
    }
}









