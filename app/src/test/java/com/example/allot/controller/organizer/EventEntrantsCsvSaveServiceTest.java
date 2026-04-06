package com.example.allot.controller.organizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the event entrants csv save service.
 */
public class EventEntrantsCsvSaveServiceTest {
    private EventEntrantsCsvSaveService service;

    /**
     * Updates the up.
     */
    @Before
    public void setUp() {
        service = new EventEntrantsCsvSaveService();
    }

    /**
     * Performs build file name uses event title when present.
     */
    @Test
    public void buildFileName_usesEventTitleWhenPresent() {
        assertEquals(
                "allot_enrolled_spring_gala.csv",
                service.buildFileName("Spring Gala", "event-123")
        );
    }

    /**
     * Performs build file name falls back to event id.
     */
    @Test
    public void buildFileName_fallsBackToEventId() {
        assertEquals(
                "allot_enrolled_event_123.csv",
                service.buildFileName("", "event-123")
        );
    }

    /**
     * Performs build file name falls back to default when inputs are blank.
     */
    @Test
    public void buildFileName_fallsBackToDefaultWhenInputsAreBlank() {
        assertEquals(
                "allot_enrolled_event.csv",
                service.buildFileName("   ", null)
        );
    }

    /**
     * Performs build file name normalizes punctuation and spacing.
     */
    @Test
    public void buildFileName_normalizesPunctuationAndSpacing() {
        assertEquals(
                "allot_enrolled_my_event_2026.csv",
                service.buildFileName(" My Event! 2026 ", null)
        );
    }

    /**
     * Performs build file name ends with csv extension.
     */
    @Test
    public void buildFileName_endsWithCsvExtension() {
        assertFalse(service.buildFileName("Sample Event", null).isEmpty());
        assertEquals(".csv", service.buildFileName("Sample Event", null)
                .substring(service.buildFileName("Sample Event", null).length() - 4));
    }

    /**
     * Performs save to downloads rejects null context.
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
     * Performs save to downloads rejects blank csv content.
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
     * Performs build download spec sets csv metadata for android q and above.
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
     * Performs build download spec omits relative path before android q.
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
