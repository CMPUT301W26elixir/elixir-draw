package com.example.allot.model.organizer;
public class EntrantsExportResult {
    private final boolean success;
    private final int messageResId;
    private final String fileName;
    private final String csvContent;

    public EntrantsExportResult(boolean success, int messageResId, String fileName, String csvContent) {
        this.success = success;
        this.messageResId = messageResId;
        this.fileName = fileName;
        this.csvContent = csvContent;
    }

    public boolean isSuccess() { return success; }
    public int getMessageResId() { return messageResId; }
    public String getFileName() { return fileName; }
    public String getCsvContent() { return csvContent; }
}









