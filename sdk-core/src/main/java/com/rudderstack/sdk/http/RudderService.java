package com.rudderstack.sdk.http;

import com.rudderstack.sdk.messages.Batch;

import retrofit.http.Body;
import retrofit.http.POST;

/** REST interface for the Rudder API. */
public interface RudderService {
  @POST("/v1/batch")
  UploadResponse upload(@Body Batch batch);
}
