package com.rudder.analytics.http;

import com.rudder.analytics.messages.Batch;

import retrofit.http.Body;
import retrofit.http.POST;

/** REST interface for the Segment API. */
public interface RudderService {
  @POST("/v1/import")
  UploadResponse upload(@Body Batch batch);
}
