package com.example.allot.model.organizer;

/**
 * Holds one enrolled entrant row for CSV export.
 */
public class EntrantExportRow {
    private final String name;
    private final String email;
    private final String phone;

    /**
     * Creates one CSV export row with human-facing entrant details.
     *
     * @param name the entrant display name
     * @param email the entrant email address
     * @param phone the entrant phone number
     */
    public EntrantExportRow(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Returns whether g.et Name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether g.et Email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns whether g.et Phone
     */
    public String getPhone() {
        return phone;
    }
}
