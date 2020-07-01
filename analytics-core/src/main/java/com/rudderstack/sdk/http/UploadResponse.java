package com.rudder.analytics.http;

import com.google.auto.value.AutoValue;
import com.rudder.analytics.gson.AutoGson;

@AutoValue
@AutoGson
public abstract class UploadResponse {
  public abstract boolean success();
}
