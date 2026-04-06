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
     * Checks if a user has admin role.
     *
     * @param user the user to check
     * @return true if user is an admin, false otherwise
     */
    public static boolean isAdmin(User user) {
        if (user == null) {
            return false;
        }
        return ADMIN.equals(user.getRole());
    }

    /**
     * Private constructor to prevent instantiation.
     */
    private UserRole() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
