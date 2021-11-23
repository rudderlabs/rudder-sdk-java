package com.rudder.analytics.http;

import com.rudder.analytics.messages.Batch;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/** REST interface for the Rudder API. */
public interface RudderService {
  @POST("v1/batch")
  @Headers(value = {"Content-Type:application/json"})
  Call<ResponseBody> upload(@Body Batch batch);
}
