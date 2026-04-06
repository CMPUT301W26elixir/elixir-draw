package com.example.allot.common;

import com.example.allot.model.profile.User;

/**
 * Utility class for user role management.
 * Provides constants and helper methods for checking user roles.
 */
public class UserRole {
    /**
     * Admin role constant.
     */
    public static final String ADMIN = "admin";

    /**
     * Regular user role constant.
     */
    public static final String USER = "user";

    /**
     * Returns whether admin.
     *
     * @param user the user
     * @return whether admin
     */
    public static boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        return ADMIN.equals(user.getRole());
    }

    /**
     * Creates a new UserRole instance.
     */
    private UserRole() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
