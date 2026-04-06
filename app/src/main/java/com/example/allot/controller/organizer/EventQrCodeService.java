package com.example.allot.controller.organizer;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Handles QR bitmap generation and saving for organizer QR flows.
 */
public class EventQrCodeService {

    /**
     * Returns the result of generate.
     *
     * @param payload the payload
     * @param sizePx the size px
     * @return the result of this call
     */
    public Bitmap generate(String payload, int sizePx) {
        if (payload == null || payload.trim().isEmpty()) {
            throw new IllegalArgumentException("payload must not be blank");
        }
        if (sizePx <= 0) {
            throw new IllegalArgumentException("sizePx must be greater than 0");
        }

        try {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    payload.trim(),
                    BarcodeFormat.QR_CODE,
                    sizePx,
                    sizePx,
                    hints
            );
            return toBitmap(bitMatrix);
        } catch (WriterException exception) {
            throw new IllegalStateException("Unable to generate QR code", exception);
        }
    }

    /**
     * Performs save to gallery.
     *
     * @param context the context
     * @param bitmap the bitmap
     * @param eventTitle the event title
     * @param eventId the event id
     */
    public void saveToGallery(Context context,
                              Bitmap bitmap,
                              String eventTitle,
                              String eventId) throws IOException, SecurityException {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap must not be null");
        }

        Uri savedUri = createImageUri(context, buildFileName(eventTitle, eventId));
        if (savedUri == null) {
            throw new IOException("Unable to create media store entry");
        }

        OutputStream outputStream = context.getContentResolver().openOutputStream(savedUri);
        if (outputStream == null) {
            throw new IOException("Unable to open media output stream");
        }

        try (OutputStream stream = outputStream) {
            if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                throw new IOException("Bitmap compression failed");
            }
        }
    }

    /**
     * Returns the result of create image uri.
     *
     * @param context the context
     * @param fileName the file name
     * @return the result of this call
     */
    private Uri createImageUri(Context context, String fileName) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/allot");
        }
        return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * Returns the result of build file name.
     *
     * @param eventTitle the event title
     * @param eventId the event id
     * @return the result of this call
     */
    private String buildFileName(String eventTitle, String eventId) {
        String baseName = !TextUtils.isEmpty(eventTitle) ? eventTitle : eventId;
        String normalizedName = baseName == null ? "event" : baseName.trim().toLowerCase(Locale.US);
        normalizedName = normalizedName.replaceAll("[^a-z0-9]+", "_");
        normalizedName = normalizedName.replaceAll("^_+|_+$", "");
        if (normalizedName.isEmpty()) {
            normalizedName = "event";
        }
        return "allot_qr_" + normalizedName + ".png";
    }

    /**
     * Returns the result of to bitmap.
     *
     * @param bitMatrix the bit matrix
     * @return the result of this call
     */
    private Bitmap toBitmap(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return bitmap;
    }
}
