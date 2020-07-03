package com.rudderstack.sdk.java.http;

import com.google.auto.value.AutoValue;
import com.rudderstack.sdk.java.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class UploadResponse {
  public abstract boolean success();
}
