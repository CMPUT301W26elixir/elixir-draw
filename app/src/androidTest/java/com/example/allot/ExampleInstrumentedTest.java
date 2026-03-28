package com.example.allot;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.allot.controller.organizer.EventQrCodeService;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    private final EventQrCodeService qrCodeService = new EventQrCodeService();

    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.allot", appContext.getPackageName());
    }

    @Test
    public void generateQr_returnsSquareBitmap() {
        Bitmap bitmap = qrCodeService.generate("event-123", 128);

        assertNotNull(bitmap);
        assertEquals(128, bitmap.getWidth());
        assertEquals(128, bitmap.getHeight());
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateQr_rejectsBlankPayload() {
        qrCodeService.generate("   ", 128);
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateQr_rejectsInvalidSize() {
        qrCodeService.generate("event-123", 0);
    }
}
