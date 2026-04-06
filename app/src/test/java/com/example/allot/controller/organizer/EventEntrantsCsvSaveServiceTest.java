package com.example.allot.controller.organizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

public class EventEntrantsCsvSaveServiceTest {
    private EventEntrantsCsvSaveService service;

    /**
     * Updates up.
     */
    @Before
    public void setUp() {
        service = new EventEntrantsCsvSaveService();
    }

    /**
     * Builds file name_uses event title when present.
     */
    @Test
    public void buildFileName_usesEventTitleWhenPresent() {
        assertEquals(
                "allot_enrolled_spring_gala.csv",
                service.buildFileName("Spring Gala", "event-123")
        );
    }

    /**
     * Builds file name_falls back to event id.
     */
    @Test
    public void buildFileName_fallsBackToEventId() {
        assertEquals(
                "allot_enrolled_event_123.csv",
                service.buildFileName("", "event-123")
        );
    }

    /**
     * Builds file name_falls back to default when inputs are blank.
     */
    @Test
    public void buildFileName_fallsBackToDefaultWhenInputsAreBlank() {
        assertEquals(
                "allot_enrolled_event.csv",
                service.buildFileName("   ", null)
        );
    }

    /**
     * Builds file name_normalizes punctuation and spacing.
     */
    @Test
    public void buildFileName_normalizesPunctuationAndSpacing() {
        assertEquals(
                "allot_enrolled_my_event_2026.csv",
                service.buildFileName(" My Event! 2026 ", null)
        );
    }

    /**
     * Builds file name_ends with csv extension.
     */
    @Test
    public void buildFileName_endsWithCsvExtension() {
        assertFalse(service.buildFileName("Sample Event", null).isEmpty());
        assertEquals(".csv", service.buildFileName("Sample Event", null)
                .substring(service.buildFileName("Sample Event", null).length() - 4));
    }

    /**
     * Saves to downloads_rejects null context.
     */
    @Test
    public void saveToDownloads_rejectsNullContext() {
        try {
            service.saveToDownloads(null, "Name,Email,Phone", "Event", "event-1");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("context must not be null", expected.getMessage());
        } catch (Exception unexpected) {
            fail("Unexpected exception: " + unexpected.getClass().getSimpleName());
        }
    }

    /**
     * Saves to downloads_rejects blank csv content.
     */
    @Test
    public void saveToDownloads_rejectsBlankCsvContent() {
        try {
            service.validateCsvContent("   ");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            assertEquals("csvContent must not be blank", expected.getMessage());
        } catch (Exception unexpected) {
            fail("Unexpected exception: " + unexpected.getClass().getSimpleName());
        }
    }

    /**
     * Builds download spec_sets csv metadata for android qand above.
     */
    @Test
    public void buildDownloadSpec_setsCsvMetadataForAndroidQAndAbove() {
        EventEntrantsCsvSaveService.CsvDownloadSpec spec =
                service.buildDownloadSpec("Spring Gala", "event-1", android.os.Build.VERSION_CODES.Q);

        assertEquals("allot_enrolled_spring_gala.csv", spec.getDisplayName());
        assertEquals("text/csv", spec.getMimeType());
        assertEquals("Download/allot", spec.getRelativePath());
    }

    /**
     * Builds download spec_omits relative path before android q.
     */
    @Test
    public void buildDownloadSpec_omitsRelativePathBeforeAndroidQ() {
        EventEntrantsCsvSaveService.CsvDownloadSpec spec =
                service.buildDownloadSpec("Spring Gala", "event-1", android.os.Build.VERSION_CODES.P);

        assertEquals("allot_enrolled_spring_gala.csv", spec.getDisplayName());
        assertEquals("text/csv", spec.getMimeType());
        org.junit.Assert.assertNull(spec.getRelativePath());
    }
}
