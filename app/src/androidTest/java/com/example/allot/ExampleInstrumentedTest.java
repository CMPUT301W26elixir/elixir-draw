package com.example.allot;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.allot.controller.organizer.EventQrCodeService;
import com.example.allot.controller.organizer.ScanDecoderService;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests the example instrumented.
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private final EventQrCodeService qrCodeService = new EventQrCodeService();
    private final ScanDecoderService scanDecoderService = new ScanDecoderService();

    /**
     * Performs use app context.
     */
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.allot", appContext.getPackageName());
    }

    /**
     * Performs generate qr returns square bitmap.
     */
    @Test
    public void generateQr_returnsSquareBitmap() {
        Bitmap bitmap = qrCodeService.generate("event-123", 128);

        assertNotNull(bitmap);
        assertEquals(128, bitmap.getWidth());
        assertEquals(128, bitmap.getHeight());
    }

    /**
     * Performs generate qr rejects blank payload.
     */
    @Test(expected = IllegalArgumentException.class)
    public void generateQr_rejectsBlankPayload() {
        qrCodeService.generate("   ", 128);
    }

    /**
     * Performs generate qr rejects invalid size.
     */
    @Test(expected = IllegalArgumentException.class)
    public void generateQr_rejectsInvalidSize() {
        qrCodeService.generate("event-123", 0);
    }

    /**
     * Performs decode bitmap returns event id from generated qr.
     */
    @Test
    public void decodeBitmap_returnsEventIdFromGeneratedQr() {
        Bitmap bitmap = qrCodeService.generate("event-123", 256);

        assertEquals("event-123", scanDecoderService.decodeBitmap(bitmap));
    }

    /**
     * Performs decode bitmap returns null when no qr is present.
     */
    @Test
    public void decodeBitmap_returnsNullWhenNoQrIsPresent() {
        Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);

        assertNull(scanDecoderService.decodeBitmap(bitmap));
    }
}
