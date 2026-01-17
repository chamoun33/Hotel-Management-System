package com.hotel.management.system.security;

import com.hotel.management.system.model.Role;
import com.hotel.management.system.model.User;

public final class CurrentUser {

    private static User user;

    private CurrentUser() {
        // Prevent instantiation
    }

    public static void set(User loggedUser) {
        user = loggedUser;
    }

    public static User get() {
        return user;
    }

    public static boolean isLoggedIn() {
        return user != null;
    }

    public static boolean isAdmin() {
        return user != null && user.getRole() == Role.ADMIN;
    }

    public static boolean isStaff() {
        return user != null && user.getRole() == Role.STAFF;
    }

    public static void clear() {
        user = null;
    }
}
