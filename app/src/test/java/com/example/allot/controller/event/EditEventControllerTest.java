package com.example.allot.controller.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.allot.data.EventRepository;
import com.example.allot.controller.lottery.LotteryDrawService;
import com.example.allot.model.event.Event;
import com.example.allot.model.event.EventFormData;
import com.example.allot.model.event.EventFormSnapshot;
import org.junit.Before;
import org.junit.Test;
public class EditEventControllerTest {
    private EditEventController controller;

    @Before
    public void setUp() {
        controller = new EditEventController(
                new FakeEventRepository(),
                new EventFormService(),
                new EventInputValidator(),
                new LotteryDrawService()
        );
    }

    @Test
    public void isSaveEnabled_disablesSaveWhenSnapshotMatches() {
        EventFormData formData = buildFormData("Sample Event");
        EventFormSnapshot originalSnapshot = controller.buildSnapshot(formData);

        assertFalse(controller.isSaveEnabled(formData, originalSnapshot, false, false));
    }

    @Test
    public void isSaveEnabled_enablesSaveWhenFormChanges() {
        EventFormData originalFormData = buildFormData("Sample Event");
        EventFormData currentFormData = buildFormData("Updated Event");
        EventFormSnapshot originalSnapshot = controller.buildSnapshot(originalFormData);

        assertTrue(controller.isSaveEnabled(currentFormData, originalSnapshot, false, false));
    }

    private EventFormData buildFormData(String title) {
        return new EventFormData(
                title,
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

    private static class FakeEventRepository extends EventRepository {
        private FakeEventRepository() {
            super((com.google.firebase.firestore.FirebaseFirestore) null);
        }
    }
}









