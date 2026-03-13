package com.example.allot.qr;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

/**
 * Utility class for generating QR code bitmaps from text payloads.
 */
public final class QrCodeGenerator {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private QrCodeGenerator() {
    }

    /**
     * Generates a QR code bitmap for the given payload.
     *
     * @param payload the text content to encode in the QR code
     * @param sizePx the width and height of the QR code in pixels
     * @return a bitmap containing the generated QR code
     * @throws IllegalArgumentException if the payload is blank or the size is not positive
     * @throws IllegalStateException if the QR code cannot be generated
     */
    public static Bitmap generate(String payload, int sizePx) {
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
                    payload,
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
     * Converts a BitMatrix QR code representation into a bitmap image.
     *
     * @param bitMatrix the QR code matrix to convert
     * @return a bitmap version of the QR code
     */
    private static Bitmap toBitmap(BitMatrix bitMatrix) {
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