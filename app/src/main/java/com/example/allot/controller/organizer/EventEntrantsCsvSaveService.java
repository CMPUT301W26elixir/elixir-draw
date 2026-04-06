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

        CsvDownloadSpec(String displayName, String mimeType, String relativePath) {
            this.displayName = displayName;
            this.mimeType = mimeType;
            this.relativePath = relativePath;
        }

        /**
         * Returns whether g.et Display Name
         */
        String getDisplayName() {
            return displayName;
        }

        /**
         * Returns whether g.et Mime Type
         */
        String getMimeType() {
            return mimeType;
        }

        /**
         * Returns whether g.et Relative Path
         */
        String getRelativePath() {
            return relativePath;
        }
    }

    /**
     * Saves CSV text into the device downloads collection and returns the saved URI.
     *
     * @param context the Android context used to access MediaStore
     * @param csvContent the generated CSV text to save
     * @param eventTitle the event title used in the filename when available
     * @param eventId the event ID used as a fallback filename
     * @return the URI of the saved CSV file
     * @throws IOException if the file cannot be created or written
     * @throws SecurityException if the media store cannot be accessed
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
     * Handles validate Inputs.
     */
    void validateInputs(Context context, String csvContent) {
        if (context == null) {
            throw new IllegalArgumentException("context must not be null");
        }
        validateCsvContent(csvContent);
    }

    /**
     * Handles validate Csv Content.
     */
    void validateCsvContent(String csvContent) {
        if (isBlank(csvContent)) {
            throw new IllegalArgumentException("csvContent must not be blank");
        }
    }

    /**
     * Creates download values.
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
     * Builds file name.
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
     * Builds download spec.
     */
    CsvDownloadSpec buildDownloadSpec(String eventTitle, String eventId, int sdkInt) {
        String relativePath = sdkInt >= Build.VERSION_CODES.Q ? RELATIVE_PATH : null;
        return new CsvDownloadSpec(buildFileName(eventTitle, eventId), MIME_TYPE, relativePath);
    }

    /**
     * Returns whether i.s Blank
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
