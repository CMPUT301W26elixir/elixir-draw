package com.example.allot.controller.organizer;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Saves generated entrant CSV files into the device downloads collection.
 */
public class EventEntrantsCsvSaveService {
    private static final String DEFAULT_EVENT_NAME = "event";
    private static final String FILE_PREFIX = "allot_enrolled_";
    private static final String FILE_EXTENSION = ".csv";
    private static final String MIME_TYPE = "text/csv";
    private static final String RELATIVE_PATH = "Download/allot";

    static final class CsvDownloadSpec {
        private final String displayName;
        private final String mimeType;
        private final String relativePath;

        /**
         * Creates a new CsvDownloadSpec instance.
         *
         * @param displayName the display name
         * @param mimeType the mime type
         * @param relativePath the relative path
         */
        CsvDownloadSpec(String displayName, String mimeType, String relativePath) {
            this.displayName = displayName;
            this.mimeType = mimeType;
            this.relativePath = relativePath;
        }

        /**
         * Returns the display name.
         *
         * @return the display name
         */
        String getDisplayName() {
            return displayName;
        }

        /**
         * Returns the mime type.
         *
         * @return the mime type
         */
        String getMimeType() {
            return mimeType;
        }

        /**
         * Returns the relative path.
         *
         * @return the relative path
         */
        String getRelativePath() {
            return relativePath;
        }
    }

    /**
     * Returns the result of save to downloads.
     *
     * @param context the context
     * @param csvContent the csv content
     * @param eventTitle the event title
     * @param eventId the event id
     * @return the result of this call
     */
    public Uri saveToDownloads(Context context,
                               String csvContent,
                               String eventTitle,
                               String eventId) throws IOException, SecurityException {
        validateInputs(context, csvContent);

        Uri savedUri = context.getContentResolver().insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                createDownloadValues(eventTitle, eventId, Build.VERSION.SDK_INT)
        );
        if (savedUri == null) {
            throw new IOException("Unable to create media store entry");
        }

        OutputStream outputStream = context.getContentResolver().openOutputStream(savedUri);
        if (outputStream == null) {
            throw new IOException("Unable to open media output stream");
        }

        try (OutputStream stream = outputStream) {
            stream.write(csvContent.getBytes(StandardCharsets.UTF_8));
            stream.flush();
        }

        return savedUri;
    }

    /**
     * Performs validate inputs.
     *
     * @param context the context
     * @param csvContent the csv content
     */
    void validateInputs(Context context, String csvContent) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        validateCsvContent(csvContent);
    }

    /**
     * Performs validate csv content.
     *
     * @param csvContent the csv content
     */
    void validateCsvContent(String csvContent) {
        if (isBlank(csvContent)) {
            throw new IllegalArgumentException("csvContent must not be blank");
        }
    }

    /**
     * Returns the result of create download values.
     *
     * @param eventTitle the event title
     * @param eventId the event id
     * @param sdkInt the sdk int
     * @return the result of this call
     */
    ContentValues createDownloadValues(String eventTitle, String eventId, int sdkInt) {
        CsvDownloadSpec spec = buildDownloadSpec(eventTitle, eventId, sdkInt);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, spec.getDisplayName());
        values.put(MediaStore.Downloads.MIME_TYPE, spec.getMimeType());
        if (spec.getRelativePath() != null) {
            values.put(MediaStore.Downloads.RELATIVE_PATH, spec.getRelativePath());
        }
        return values;
    }

    /**
     * Returns the result of build file name.
     *
     * @param eventTitle the event title
     * @param eventId the event id
     * @return the result of this call
     */
    String buildFileName(String eventTitle, String eventId) {
        String baseName = !isBlank(eventTitle) ? eventTitle : eventId;
        String normalizedName = baseName == null ? DEFAULT_EVENT_NAME : baseName.trim().toLowerCase(Locale.US);
        normalizedName = normalizedName.replaceAll("[^a-z0-9]+", "_");
        normalizedName = normalizedName.replaceAll("^_+|_+$", "");
        if (normalizedName.isEmpty()) {
            normalizedName = DEFAULT_EVENT_NAME;
        }
        return FILE_PREFIX + normalizedName + FILE_EXTENSION;
    }

    /**
     * Returns the result of build download spec.
     *
     * @param eventTitle the event title
     * @param eventId the event id
     * @param sdkInt the sdk int
     * @return the result of this call
     */
    CsvDownloadSpec buildDownloadSpec(String eventTitle, String eventId, int sdkInt) {
        String relativePath = sdkInt >= Build.VERSION_CODES.Q ? RELATIVE_PATH : null;
        return new CsvDownloadSpec(buildFileName(eventTitle, eventId), MIME_TYPE, relativePath);
    }

    /**
     * Returns whether blank.
     *
     * @param value the value
     * @return whether blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
