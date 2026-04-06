package com.example.allot.controller.organizer;

import android.graphics.Bitmap;
import androidx.camera.core.ImageProxy;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Decodes QR values from camera frames and selected images.
 */
public class ScanDecoderService {

    /**
     * Returns the result of decode image proxy.
     *
     * @param imageProxy the image proxy
     * @return the result of this call
     */
    public String decodeImageProxy(ImageProxy imageProxy) {
        if (imageProxy == null || imageProxy.getPlanes().length == 0) {
            return null;
        }

        int width = imageProxy.getWidth();
        int height = imageProxy.getHeight();
        byte[] luminance = extractLuminancePlane(imageProxy);
        RotatedLuminance rotatedLuminance = rotateLuminance(
                luminance,
                width,
                height,
                imageProxy.getImageInfo().getRotationDegrees()
        );

        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(
                rotatedLuminance.data,
                rotatedLuminance.width,
                rotatedLuminance.height,
                0,
                0,
                rotatedLuminance.width,
                rotatedLuminance.height,
                false
        );
        return decodeBinaryBitmap(new BinaryBitmap(new HybridBinarizer(source)));
    }

    /**
     * Returns the result of decode bitmap.
     *
     * @param bitmap the bitmap
     * @return the result of this call
     */
    public String decodeBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        return decodeBinaryBitmap(new BinaryBitmap(new HybridBinarizer(source)));
    }

    /**
     * Returns the result of decode binary bitmap.
     *
     * @param bitmap the bitmap
     * @return the result of this call
     */
    private String decodeBinaryBitmap(BinaryBitmap bitmap) {
        MultiFormatReader reader = new MultiFormatReader();
        try {
            reader.setHints(buildHints());
            return reader.decode(bitmap).getText();
        } catch (NotFoundException exception) {
            return null;
        } finally {
            reader.reset();
        }
    }

    /**
     * Returns the result of build hints.
     *
     * @return the result of this call
     */
    private Map<DecodeHintType, Object> buildHints() {
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, Collections.singletonList(BarcodeFormat.QR_CODE));
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        return hints;
    }

    /**
     * Returns the result of extract luminance plane.
     *
     * @param imageProxy the image proxy
     * @return the result of this call
     */
    private byte[] extractLuminancePlane(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy plane = imageProxy.getPlanes()[0];
        ByteBuffer buffer = plane.getBuffer();
        buffer.rewind();

        int width = imageProxy.getWidth();
        int height = imageProxy.getHeight();
        int rowStride = plane.getRowStride();
        int pixelStride = plane.getPixelStride();
        byte[] rowBuffer = new byte[rowStride];
        byte[] luminance = new byte[width * height];

        for (int row = 0; row < height; row++) {
            int bytesToRead = Math.min(rowStride, buffer.remaining());
            buffer.get(rowBuffer, 0, bytesToRead);
            if (pixelStride == 1) {
                System.arraycopy(rowBuffer, 0, luminance, row * width, width);
                continue;
            }

            for (int column = 0; column < width; column++) {
                luminance[row * width + column] = rowBuffer[column * pixelStride];
            }
        }

        return luminance;
    }

    /**
     * Returns the result of rotate luminance.
     *
     * @param source the source
     * @param width the width
     * @param height the height
     * @param rotationDegrees the rotation degrees
     * @return the result of this call
     */
    private RotatedLuminance rotateLuminance(byte[] source, int width, int height, int rotationDegrees) {
        if (rotationDegrees == 90) {
            byte[] rotated = new byte[source.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    rotated[x * height + (height - y - 1)] = source[y * width + x];
                }
            }
            return new RotatedLuminance(rotated, height, width);
        }

        if (rotationDegrees == 180) {
            byte[] rotated = new byte[source.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    rotated[(height - y - 1) * width + (width - x - 1)] = source[y * width + x];
                }
            }
            return new RotatedLuminance(rotated, width, height);
        }

        if (rotationDegrees == 270) {
            byte[] rotated = new byte[source.length];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    rotated[(width - x - 1) * height + y] = source[y * width + x];
                }
            }
            return new RotatedLuminance(rotated, height, width);
        }

        return new RotatedLuminance(source, width, height);
    }

    private static class RotatedLuminance {
        private final byte[] data;
        private final int width;
        private final int height;

        /**
         * Creates a new RotatedLuminance instance.
         *
         * @param data the data
         * @param width the width
         * @param height the height
         */
        private RotatedLuminance(byte[] data, int width, int height) {
            this.data = data;
            this.width = width;
            this.height = height;
        }
    }
}
