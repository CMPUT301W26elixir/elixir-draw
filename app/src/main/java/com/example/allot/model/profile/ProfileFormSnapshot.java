package com.example.allot.model.profile;
public class ProfileFormSnapshot {
    private final String firstName;
    private final String lastName;
    private final String email;
    private final String phone;
    private final boolean notificationsEnabled;

    /**
     * Creates a snapshot from the provided profile form values.
     *
     * @param firstName the current first name value
     * @param lastName the current last name value
     * @param email the current email value
     * @param phone the current phone value
     * @param notificationsEnabled whether notifications are enabled
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
     * Creates a profile snapshot using the values currently stored on the user model.
     *
     * @param user the user whose profile values should be copied
     * @return a snapshot containing the user's current profile values
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
     * Returns the first name value stored in the snapshot.
     *
     * @return the first name value
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name value stored in the snapshot.
     *
     * @return the last name value
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the email value stored in the snapshot.
     *
     * @return the email value
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the phone value stored in the snapshot.
     *
     * @return the phone value
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns whether notifications are enabled in the snapshot.
     *
     * @return true if notifications are enabled, otherwise false
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Returns a safe, trimmed string value.
     *
     * @param value the string to normalize
     * @return an empty string if the value is null; otherwise the trimmed string
     */
    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Compares the snapshot against another snapshot.
     *
     * @param other the other snapshot to compare
     * @return true if all tracked values match, otherwise false
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
     * Returns the hash code for this snapshot.
     *
     * @return the hash code
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









