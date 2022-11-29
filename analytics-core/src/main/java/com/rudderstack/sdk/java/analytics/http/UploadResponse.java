package com.rudderstack.sdk.java.analytics.http;

import com.google.auto.value.AutoValue;
import com.rudderstack.sdk.java.analytics.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class UploadResponse {
    public abstract boolean success();
}
