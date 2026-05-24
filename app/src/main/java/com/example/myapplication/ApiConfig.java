package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class ApiConfig {
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private static final String BASE_URL = "http://121.36.241.85:8080/api";
    private static volatile OkHttpClient httpClient;

    public static void init(Context ctx) {
        TokenManager.init(ctx);
    }

    private static final String IMAGE_BASE = "http://121.36.241.85:8080";

    public static String getBaseUrl() {
        Log.d("ApiConfig", "Base URL: " + BASE_URL);
        return BASE_URL;
    }

    public static String friendlyMsg(String msg) {
        if ("ok".equals(msg) || "ok".equalsIgnoreCase(msg)) {
            return "成功！";
        }
        return msg;
    }

    public static String getFullImageUrl(String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty()) return null;
        if (relativeUrl.startsWith("http")) return relativeUrl;
        return IMAGE_BASE + (relativeUrl.startsWith("/") ? relativeUrl : "/" + relativeUrl);
    }

    public static OkHttpClient getClient() {
        if (httpClient == null) {
            synchronized (ApiConfig.class) {
                if (httpClient == null) {
                    httpClient = new OkHttpClient.Builder()
                            .addInterceptor(chain -> {
                                String token = TokenManager.getToken();
                                Request original = chain.request();
                                if (token != null && !token.isEmpty()) {
                                    Request authRequest = original.newBuilder()
                                            .header("Authorization", "Bearer " + token)
                                            .build();
                                    return chain.proceed(authRequest);
                                }
                                return chain.proceed(original);
                            })
                            .build();
                }
            }
        }
        return httpClient;
    }
}
