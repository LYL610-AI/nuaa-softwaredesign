package com.example.myapplication;

import android.content.Context;

public class SessionManager {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean hasLoggedIn() {
        return currentUser != null && TokenManager.hasToken();
    }

    public static void login(Context ctx, User user, String token) {
        currentUser = user;
        TokenManager.saveToken(ctx, token);
    }

    public static void logout(Context ctx) {
        currentUser = null;
        TokenManager.clearToken(ctx);
    }
}
