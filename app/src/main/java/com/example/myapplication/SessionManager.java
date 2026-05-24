package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class SessionManager {
    private static final String PREFS_NAME = "session_prefs";
    private static final String KEY_USER = "current_user";
    private static User currentUser;
    private static Gson gson = new Gson();

    public static void init(Context ctx) {
        SharedPreferences prefs = ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String userJson = prefs.getString(KEY_USER, null);
        if (userJson != null) {
            try {
                currentUser = gson.fromJson(userJson, User.class);
            } catch (Exception e) {
                currentUser = null;
            }
        }
    }

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
        // 持久化用户数据
        String userJson = gson.toJson(user);
        ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_USER, userJson)
                .apply();
    }

    public static void logout(Context ctx) {
        currentUser = null;
        TokenManager.clearToken(ctx);
        ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_USER)
                .apply();
    }
}
