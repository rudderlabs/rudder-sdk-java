package com.rudderstack.sdk.http;

import com.google.auto.value.AutoValue;
import com.rudderstack.sdk.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class UploadResponse {
  public abstract boolean success();
}
