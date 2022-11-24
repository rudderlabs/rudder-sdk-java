package com.rudder.analytics.http;

import com.rudder.analytics.messages.Batch;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Url;

/** REST interface for the Rudder API. */
public interface RudderService {
  @POST("v1/batch")
  @Headers(value = {"Content-Type:application/json"})
  Call<UploadResponse> upload(@Url HttpUrl uploadUrl, @Body Batch batch);
}
