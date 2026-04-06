package com.example.allot.model.profile;
/**
 * Holds the current profile form values so the UI can spot unsaved changes.
 */
public class ProfileFormSnapshot {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final boolean notificationsEnabled;

    /**
     * Creates a new ProfileFormSnapshot instance.
     *
     * @param firstName the first name
     * @param lastName the last name
     * @param email the email
     * @param phone the phone
     * @param notificationsEnabled the notifications enabled
     */
    public ProfileFormSnapshot(String firstName,
                               String lastName,
                               String email,
                               String phone,
                               boolean notificationsEnabled) {
        this.firstName = normalize(firstName);
        this.lastName = normalize(lastName);
        this.email = normalize(email);
        this.phone = normalize(phone);
        this.notificationsEnabled = notificationsEnabled;
    }

    /**
     * Returns the result of from user.
     *
     * @param user the user
     * @return the result of this call
     */
    public static ProfileFormSnapshot fromUser(User user) {
        if (user == null) {
            return new ProfileFormSnapshot("", "", "", "", false);
        }

        return new ProfileFormSnapshot(
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.isNotiEnabled()
        );
    }

    /**
     * Returns the first name.
     *
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name.
     *
     * @return the last name
     */
    public String getLastName() {
        return lastName;
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

    /**
     * Returns whether notifications enabled.
     *
     * @return whether notifications enabled
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Returns the result of normalize.
     *
     * @param value the value
     * @return the result of this call
     */
    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Compares this profile form snapshot with another object.
     *
     * @param other the other
     * @return whether the supplied object matches this instance
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof ProfileFormSnapshot)) {
            return false;
        }

        ProfileFormSnapshot snapshot = (ProfileFormSnapshot) other;
        return notificationsEnabled == snapshot.notificationsEnabled
                && firstName.equals(snapshot.firstName)
                && lastName.equals(snapshot.lastName)
                && email.equals(snapshot.email)
                && phone.equals(snapshot.phone);
    }

    /**
     * Returns the hash code for this profile form snapshot.
     *
     * @return the hash code for this instance
     */
    @Override
    public int hashCode() {
        int result = firstName.hashCode();
        result = 31 * result + lastName.hashCode();
        result = 31 * result + email.hashCode();
        result = 31 * result + phone.hashCode();
        result = 31 * result + Boolean.hashCode(notificationsEnabled);
        return result;
    }
}









