package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREFS_NAME = "auth_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static String cachedToken;
    private static Context appContext;

    public static void init(Context ctx) {
        appContext = ctx.getApplicationContext();
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        cachedToken = prefs.getString(KEY_TOKEN, "");
    }

    public static void saveToken(Context ctx, String token) {
        ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_TOKEN, token)
                .apply();
        cachedToken = token;
    }

    public static String getToken() {
        return (cachedToken != null && !cachedToken.isEmpty()) ? cachedToken : "";
    }

    public static void clearToken(Context ctx) {
        ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_TOKEN)
                .apply();
        cachedToken = "";
    }

    public static boolean hasToken() {
        return cachedToken != null && !cachedToken.isEmpty();
    }
}
