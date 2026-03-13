package com.example.allot;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.allot.qr.QrCodeGenerator;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.example.allot", appContext.getPackageName());
    }

    @Test
    public void generateQr_returnsSquareBitmap() {
        Bitmap bitmap = QrCodeGenerator.generate("allot://event/event-123", 128);

        assertNotNull(bitmap);
        assertEquals(128, bitmap.getWidth());
        assertEquals(128, bitmap.getHeight());
    }
}
