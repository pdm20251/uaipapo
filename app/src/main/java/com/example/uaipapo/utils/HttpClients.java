package com.example.uaipapo.utils;

import okhttp3.OkHttpClient;

public final class HttpClients {
    public static final OkHttpClient FCM = new OkHttpClient.Builder()
            .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    private HttpClients() {}
}