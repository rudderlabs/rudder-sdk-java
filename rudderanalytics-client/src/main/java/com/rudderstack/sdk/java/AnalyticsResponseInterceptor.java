package com.rudderstack.sdk.java;

import com.google.gson.Gson;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AnalyticsResponseInterceptor implements Interceptor {
    @Override
    public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        okhttp3.Response response = chain.proceed(request);
        if (response.code() == 200) {
            Map<String, String> jsonResponse = new HashMap<>();
            jsonResponse.put("response", response.body() == null ? "error" : response.body().string());
            MediaType contentType = MediaType.parse("application/json");
            ResponseBody body = ResponseBody.create(contentType, new Gson().toJson(jsonResponse));
            return response.newBuilder().body(body).build();
        } else if (response.code() == 403) {
            Map<String, String> jsonResponse = new HashMap<>();
            jsonResponse.put("response", "error");
            MediaType contentType = MediaType.parse("application/json");
            ResponseBody body = ResponseBody.create(contentType, new Gson().toJson(jsonResponse));
            return response.newBuilder().body(body).build();
        }
        return response;
    }
}