package com.rudder.analytics.http;

import com.rudder.analytics.messages.Batch;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/** REST interface for the Rudder API. */
public interface RudderService {
  @POST(".")
  Call<UploadResponse> upload(@Body Batch batch);
}
