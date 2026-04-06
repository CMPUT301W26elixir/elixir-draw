package com.example.allot.model.organizer;

/**
 * Holds one enrolled entrant row for CSV export.
 */
public class EntrantExportRow {
    private final String name;
    private final String email;
    private final String phone;

    /**
     * Creates a new EntrantExportRow instance.
     *
     * @param name the name
     * @param email the email
     * @param phone the phone
     */
    public EntrantExportRow(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    /**
     * Returns the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the phone.
     *
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }
}
