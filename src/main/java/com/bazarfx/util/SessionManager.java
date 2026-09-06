package com.bazarfx.util;

import com.bazarfx.model.User;

/** Holds the single currently logged-in user for the running session. */
public class SessionManager {

    private static User currentUser;

    private SessionManager() {
    }

    public static void login(User user) { currentUser = user; }

    public static void logout() { currentUser = null; }

    public static User getCurrentUser() { return currentUser; }

    public static boolean isLoggedIn() { return currentUser != null; }
}
