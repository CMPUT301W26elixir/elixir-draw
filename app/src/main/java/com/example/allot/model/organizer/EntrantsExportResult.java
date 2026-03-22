package com.example.allot.model.organizer;

/**
 * Stores the result of exporting event entrants to a CSV file.
 */
public class EntrantsExportResult {
    private final boolean success;
    private final int messageResId;
    private final String fileName;
    private final String csvContent;

    /**
     * Creates an export result for the organizer flow.
     *
     * @param success true when the export was built successfully
     * @param messageResId the message resource shown to the user
     * @param fileName the file name suggested for the export
     * @param csvContent the generated CSV text
     */
    public EntrantsExportResult(boolean success, int messageResId, String fileName, String csvContent) {
        this.success = success;
        this.messageResId = messageResId;
        this.fileName = fileName;
        this.csvContent = csvContent;
    }

    /**
     * @return true when the export was generated successfully
     */
    public boolean isSuccess() { return success; }
    /**
     * @return the message resource shown to the user
     */
    public int getMessageResId() { return messageResId; }
    /**
     * @return the generated file name
     */
    public String getFileName() { return fileName; }
    /**
     * @return the generated CSV content
     */
    public String getCsvContent() { return csvContent; }
}









